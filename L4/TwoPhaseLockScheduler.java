import java.sql.*;
import java.util.*;

public class TwoPhaseLockScheduler {

    // path to the local SQLite database file stored on disk (no server needed)
    private static final String DB_PATH = "scheduler.db";
    private Connection conn;    // JDBC connection object used to talk to the SQLite file

    // in-memory write-through cache of the records table in SQLite
    // reads and lock decisions happen against this map; every write also updates the DB
    private Map<Integer, Integer>           database        = new HashMap<>();
    private Map<Integer, List<LockRecord>>  lockTable       = new LinkedHashMap<>();
    private List<LogRecord>                 logTable        = new ArrayList<>();
    private int                             logTimestamp    = 0; // records the number of successive operations performed, also used as the primary key in the DB log_table
    private Map<Integer, Integer>           lastLogTime     = new HashMap<>();
//    example
//    TS=1  T2  READ    Rec=9  Val=99   PrevTS=-1
//    TS=3  T2  READ    Rec=7  Val=42   PrevTS=1
//    TS=5  T2  COMMIT                  PrevTS=3

    // buffer holds the log entry until we confirm the operation succeeded
    // only then is it written to the log table (and the DB) and the timestamp is incremented
    // if the operation fails, the buffer is discarded and the timestamp is NOT incremented
    private LogRecord pendingLog = null;        // buffer: temporary storage of a transaction's log entry before it is confirmed

    private List<Integer>                   transactionIds  = new ArrayList<>();    // ordered list of transaction IDs to keep round-robin order
    private Map<Integer, Deque<Operation>>  operationQueues = new LinkedHashMap<>();    // each transaction's remaining operations; Deque allows peek without removal
    private Set<Integer>                    committed       = new HashSet<>();           // tracks which transactions have finished so we know when all are done
    private List<String>                    finalSchedule   = new ArrayList<>();         // records the order of every successfully executed operation


    public TwoPhaseLockScheduler() {
        // open (or create) the local SQLite file, set up the tables, and load any existing data
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_PATH);
            initDB();           // create tables if this is the first run
            loadRecordsFromDB();// pull existing records into the in-memory cache
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open database: " + e.getMessage(), e);
        }
    }

    // drops and recreates both tables on every run so the DB always starts clean
    //   records   — stores each data item (record_id, value), the actual database being transacted on
    //   log_table — stores every committed operation for rollback purposes (mirrors the in-memory logTable)
    private void initDB() throws SQLException {
        try (Statement st = conn.createStatement()) {
            // drop first so every run starts with a fresh empty database
            st.executeUpdate("DROP TABLE IF EXISTS records");
            st.executeUpdate("DROP TABLE IF EXISTS log_table");
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS records (" +
                "  record_id INTEGER PRIMARY KEY," +
                "  value     INTEGER NOT NULL" +
                ")"
            );
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS log_table (" +
                "  timestamp      INTEGER PRIMARY KEY," +   // same counter as logTimestamp
                "  tx_id          INTEGER," +
                "  op_type        TEXT," +                  // READ, WRITE, or COMMIT
                "  record_id      INTEGER," +
                "  old_value      INTEGER," +               // only populated for WRITE
                "  new_value      INTEGER," +               // only populated for WRITE
                "  read_value     INTEGER," +               // only populated for READ
                "  prev_timestamp INTEGER" +                // points to previous log entry for this transaction (used for rollback)
                ")"
            );
        }
    }

    // loads any records that already exist in the DB into the in-memory cache
    // since initDB() always drops and recreates the tables, this will always be empty on startup
    // kept here in case the reset behaviour is ever changed back to persistent mode
    private void loadRecordsFromDB() throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT record_id, value FROM records")) {
            while (rs.next()) {
                database.put(rs.getInt("record_id"), rs.getInt("value"));
            }
        }
    }

    public void addRecord(int recordId, int initialValue) {
        // INSERT OR IGNORE: if this record already exists in the DB from a prior run, leave its value alone
        // this preserves the committed state across runs
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO records(record_id, value) VALUES(?,?)")) {
            ps.setInt(1, recordId);
            ps.setInt(2, initialValue);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // sync the in-memory cache: if the record already existed in DB, getOrDefault returns that existing value
        database.put(recordId, database.getOrDefault(recordId, initialValue));
    }

    public void addTransaction(int txId, List<Operation> operations) {
        transactionIds.add(txId);
        operationQueues.put(txId, new ArrayDeque<>(operations));
    }

    // called every time a write succeeds — keeps the SQLite records table in sync with the in-memory cache
    // this is the "write-through" part of the cache: every change is immediately persisted to disk
    private void updateRecordInDB(int recId, int newValue) {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE records SET value=? WHERE record_id=?")) {
            ps.setInt(1, newValue);
            ps.setInt(2, recId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // called when a log entry is flushed from the pending buffer to the in-memory logTable
    // inserts the same entry into the SQLite log_table so the log is durable on disk
    // this mirrors exactly when the in-memory log is written, so both are always in sync
    private void insertLogToDB(LogRecord log) {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO log_table(timestamp,tx_id,op_type,record_id,old_value,new_value,read_value,prev_timestamp)" +
                " VALUES(?,?,?,?,?,?,?,?)")) {
            ps.setInt(1, log.timestamp);
            ps.setInt(2, log.transactionId);
            ps.setString(3, log.operationType.name());
            ps.setObject(4, log.recordId >= 0 ? log.recordId : null);  // COMMIT has no record, stored as NULL
            ps.setObject(5, log.oldValue);      // null for READ and COMMIT
            ps.setObject(6, log.newValue);      // null for READ and COMMIT
            ps.setObject(7, log.readValue);     // null for WRITE and COMMIT
            ps.setInt(8, log.prevTimestamp);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void run() {
        int round = 1;

        // round-robin loop: each round every transaction gets one attempt
        // a transaction that is blocked stays at its current operation and retries next round
        while (committed.size() < transactionIds.size()) {
            System.out.println("Round " + round);
            boolean progressMade = false;

            for (int txId : transactionIds) {
                if (committed.contains(txId)) continue;       // skip transactions that have already committed

                Deque<Operation> queue = operationQueues.get(txId);
                if (queue.isEmpty()) continue;

                Operation op = queue.peek();    // peek at the next operation without removing it (round-robin: try it, only advance if it succeeds)
                boolean success = tryExecute(txId, op);

                if (success) {
                    queue.poll();   // operation succeeded, remove it from the queue

                    // flush the buffered log entry to both the in-memory log table and the SQLite DB
                    // the timestamp is assigned and incremented here, not when the entry was built
                    if (pendingLog != null) {
                        pendingLog.timestamp = logTimestamp++;          // assign the next available timestamp e.g. TS=1
                        logTable.add(pendingLog);                       // add to in-memory log e.g. T2 Read rec=9 val=99 prevTS=-1
                        lastLogTime.put(txId, pendingLog.timestamp);    // remember this transaction's most recent log time e.g. T2's last log was at TS=1
                        insertLogToDB(pendingLog);                      // persist the same entry to the SQLite log_table on disk
                        pendingLog = null;
                    }
                    // record this operation in the final schedule string
                    finalSchedule.add("T" + txId + ":" + op);
                    progressMade = true; // at least one operation succeeded this round, so no deadlock yet

                } else { // operation failed — another transaction holds a conflicting lock, this transaction waits
                    System.out.println("  T" + txId + " waiting on Rec " + op.recordId);
                    // discard the pending buffer entry; timestamp is not incremented since nothing was committed
                    pendingLog = null;
                }
            }

            printLockTable();   // show the lock table once per round after all transactions have had their turn

            // deadlock: no transaction made any progress this round, meaning everyone is waiting on everyone else
            if (!progressMade) {
                System.out.println("\nDeadlock detected, no progress possible."); // waiting on something and something waiting on something else :)
                break;
            }
            System.out.println();
            round++;
        }
        // when committed count equals transaction count, all transactions finished successfully
        System.out.println("Final schedule: " + String.join(" -> ", finalSchedule));
        printLogTable();        // print the log by querying SQLite directly, confirming the data is actually on disk
        printFinalDBState();    // print the final value of every record by querying SQLite directly
    }


    private boolean tryExecute(int txId, Operation op) {
        switch (op.type) {
            case READ:   return tryRead(txId, op.recordId);
            case WRITE:  return tryWrite(txId, op.recordId, op.writeValue);
            case COMMIT: return doCommit(txId);
            default:     return false;
        }
    }


    // READ: acquire a Shared lock then read the value from the in-memory cache (which mirrors the DB)
    // a Shared lock is allowed unless another transaction holds an Exclusive lock
    private boolean tryRead(int txId, int recId) {
        LockType myLock    = getMyLockOn(recId, txId);      // check if this transaction already has any lock on this record
        LockType otherLock = getOtherLockOn(recId, txId);   // check if any other transaction has a lock on this record

        // already have a lock on this record (from an earlier operation in this transaction), no need to acquire again
        if (myLock == LockType.SHARED || myLock == LockType.EXCLUSIVE) {
            int value = database.getOrDefault(recId, 0);   // read from the in-memory cache (always in sync with DB)
            System.out.println("  T" + txId + " READ  Rec " + recId + " = " + value + "  (has " + myLock + ")");
            pendingLog = LogRecord.forRead(txId, recId, value, getPrevLogTime(txId));   // stage the log entry in the buffer
            return true;
        }

        // another transaction holds an Exclusive lock — must wait, cannot read until that lock is released
        if (otherLock == LockType.EXCLUSIVE) {
            return false;
        }

        // no conflicting lock exists, acquire a Shared lock and read from the in-memory cache
        addLock(recId, LockType.SHARED, txId);
        int value = database.getOrDefault(recId, 0);
        System.out.println("  T" + txId + " READ  Rec " + recId + " = " + value + "  [S-lock acquired]");
        pendingLog = LogRecord.forRead(txId, recId, value, getPrevLogTime(txId));   // stage the log entry in the buffer
        return true;
    }


    // WRITE: acquire an Exclusive lock, write to the in-memory cache, and immediately persist to SQLite
    // an Exclusive lock is only allowed if no other transaction holds any lock on this record
    private boolean tryWrite(int txId, int recId, int newValue) {
        LockType myLock    = getMyLockOn(recId, txId);      // check if this transaction already has any lock on this record
        LockType otherLock = getOtherLockOn(recId, txId);   // check if any other transaction has a lock on this record

        // already hold an Exclusive lock — write directly, no new lock needed
        if (myLock == LockType.EXCLUSIVE) {
            int oldValue = database.getOrDefault(recId, 0);
            database.put(recId, newValue);          // update the in-memory cache
            updateRecordInDB(recId, newValue);      // persist the new value to the SQLite records table immediately
            System.out.println("  T" + txId + " WRITE Rec " + recId + ": " + oldValue + " -> " + newValue + "  (has X-lock)");
            pendingLog = LogRecord.forWrite(txId, recId, oldValue, newValue, getPrevLogTime(txId));
            return true;
        }

        // hold a Shared lock, try to upgrade it to Exclusive so we can write
        if (myLock == LockType.SHARED) {
            boolean othersSharing = lockTable
                    .getOrDefault(recId, Collections.emptyList())
                    .stream()
                    .anyMatch(lock -> lock.lockType == LockType.SHARED && lock.transactionId != txId);

            // cannot upgrade if another transaction also holds a Shared lock — must wait
            if (othersSharing) {
                return false;
            }

            // safe to upgrade: remove the old Shared lock entry and add an Exclusive lock in its place
            lockTable.getOrDefault(recId, new ArrayList<>()).removeIf(lock -> lock.transactionId == txId);
            addLock(recId, LockType.EXCLUSIVE, txId);
            int oldValue = database.getOrDefault(recId, 0);
            database.put(recId, newValue);          // update the in-memory cache
            updateRecordInDB(recId, newValue);      // persist the new value to the SQLite records table immediately
            System.out.println("  T" + txId + " WRITE Rec " + recId + ": " + oldValue + " -> " + newValue + "  [S->X upgrade]");
            pendingLog = LogRecord.forWrite(txId, recId, oldValue, newValue, getPrevLogTime(txId));
            return true;
        }

        // another transaction holds any kind of lock — must wait
        if (otherLock != LockType.NONE) {
            return false;
        }

        // no locks exist on this record at all — acquire an Exclusive lock and write
        addLock(recId, LockType.EXCLUSIVE, txId);
        int oldValue = database.getOrDefault(recId, 0);
        database.put(recId, newValue);          // update the in-memory cache
        updateRecordInDB(recId, newValue);      // persist the new value to the SQLite records table immediately
        System.out.println("  T" + txId + " WRITE Rec " + recId + ": " + oldValue + " -> " + newValue + "  [X-lock acquired]");
        pendingLog = LogRecord.forWrite(txId, recId, oldValue, newValue, getPrevLogTime(txId));
        return true;
    }


    // COMMIT: release all locks held by this transaction
    // this is the shrinking phase of 2PL — all locks are released at once on commit
    // the released locks allow other waiting transactions to proceed in the next round
    private boolean doCommit(int txId) {
        releaseAllLocks(txId);      // remove every lock this transaction holds from the lock table
        committed.add(txId);        // mark it as done so the round-robin loop skips it going forward
        System.out.println("  T" + txId + " committed");
        pendingLog = LogRecord.forCommit(txId, getPrevLogTime(txId));   // stage the commit log entry in the buffer
        return true;
    }


    // adds a new lock entry to the in-memory lock table for the given record
    private void addLock(int recId, LockType type, int txId) {
        lockTable.computeIfAbsent(recId, k -> new ArrayList<>())
                 .add(new LockRecord(recId, type, txId));
    }

    // removes all lock entries owned by this transaction from every record in the lock table
    private void releaseAllLocks(int txId) {
        for (List<LockRecord> locks : lockTable.values())
            locks.removeIf(lock -> lock.transactionId == txId);
    }

    // looks in the lock table for a lock on this record that belongs to this transaction
    private LockType getMyLockOn(int recId, int txId) {
        for (LockRecord lock : lockTable.getOrDefault(recId, Collections.emptyList()))
            if (lock.transactionId == txId) return lock.lockType;
        return LockType.NONE;
    }

    // looks in the lock table for a lock on this record held by any OTHER transaction
    private LockType getOtherLockOn(int recId, int txId) {
        for (LockRecord lock : lockTable.getOrDefault(recId, Collections.emptyList()))
            if (lock.transactionId != txId) return lock.lockType;
        return LockType.NONE;
    }

    // looks up the most recent log timestamp for this transaction (used as prevTimestamp in new log entries)
    // returns -1 if this transaction has no prior log entries yet
    private int getPrevLogTime(int txId) {
        return lastLogTime.getOrDefault(txId, -1);
    }


    private void printLockTable() {
        System.out.println("  Lock Table:");
        boolean empty = true;
        for (Map.Entry<Integer, List<LockRecord>> entry : lockTable.entrySet()) {
            for (LockRecord lock : entry.getValue()) {
                System.out.printf("    Rec %-3d  %-9s  T%d%n", lock.recordId, lock.lockType, lock.transactionId);
                empty = false;
            }
        }
        if (empty) System.out.println("    (empty)");
    }

    // queries the log_table directly from SQLite and prints it
    // this confirms the log was actually written to disk, not just kept in memory
    private void printLogTable() {
        System.out.println("Log Table:");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM log_table ORDER BY timestamp")) {
            while (rs.next()) {
                String opType = rs.getString("op_type");
                int ts     = rs.getInt("timestamp");
                int txId   = rs.getInt("tx_id");
                int prevTs = rs.getInt("prev_timestamp");
                if ("WRITE".equals(opType)) {
                    System.out.printf("  TS=%-3d T%d  WRITE   Rec=%d  Old=%d  New=%d  PrevTS=%d%n",
                        ts, txId, rs.getInt("record_id"),
                        rs.getInt("old_value"), rs.getInt("new_value"), prevTs);
                } else if ("READ".equals(opType)) {
                    System.out.printf("  TS=%-3d T%d  READ    Rec=%d  Val=%d  PrevTS=%d%n",
                        ts, txId, rs.getInt("record_id"), rs.getInt("read_value"), prevTs);
                } else {
                    System.out.printf("  TS=%-3d T%d  COMMIT  PrevTS=%d%n", ts, txId, prevTs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // queries the records table directly from SQLite and prints the final committed value of every record
    // this is the authoritative state since every write was persisted to SQLite immediately
    private void printFinalDBState() {
        System.out.println("\nFinal DB state:");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT record_id, value FROM records ORDER BY record_id")) {
            while (rs.next())
                System.out.println("  Rec " + rs.getInt("record_id") + "  =  " + rs.getInt("value"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
