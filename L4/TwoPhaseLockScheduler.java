import java.util.*;

public class TwoPhaseLockScheduler {

    private Map<Integer, Integer>           database        = new HashMap<>();
    private Map<Integer, List<LockRecord>>  lockTable       = new LinkedHashMap<>();
    private List<LogRecord>                 logTable        = new ArrayList<>();
    private int                             logTimestamp    = 0;
    private Map<Integer, Integer>           lastLogTime     = new HashMap<>();

    // buffer holds the log entry until we confirm the operation succeeded
    // only then is it written to the log table and the timestamp is incremented
    // if the operation fails, the buffer is discarded and the timestamp is NOT incremented
    private LogRecord pendingLog = null;

    private List<Integer>                   transactionIds  = new ArrayList<>();
    private Map<Integer, Deque<Operation>>  operationQueues = new LinkedHashMap<>();
    private Set<Integer>                    committed       = new HashSet<>();
    private List<String>                    finalSchedule   = new ArrayList<>();


    public void addRecord(int recordId, int initialValue) {
        database.put(recordId, initialValue);
    }

    public void addTransaction(int txId, List<Operation> operations) {
        transactionIds.add(txId);
        operationQueues.put(txId, new ArrayDeque<>(operations));
    }


    public void run() {
        int round = 1;

        // round-robin loop: each round every transaction gets one attempt
        // a transaction that is blocked stays at its current operation and retries next round
        while (committed.size() < transactionIds.size()) {
            System.out.println("Round " + round);
            boolean progressMade = false;

            for (int txId : transactionIds) {
                if (committed.contains(txId)) continue;

                Deque<Operation> queue = operationQueues.get(txId);
                if (queue.isEmpty()) continue;

                Operation op = queue.peek();
                boolean success = tryExecute(txId, op);

                if (success) {
                    queue.poll();

                    // flush the buffered log entry to the log table only after the operation succeeded
                    // the timestamp is assigned and incremented here, not when the entry was built
                    if (pendingLog != null) {
                        pendingLog.timestamp = logTimestamp++;
                        logTable.add(pendingLog);
                        lastLogTime.put(txId, pendingLog.timestamp);
                        pendingLog = null;
                    }

                    finalSchedule.add("T" + txId + ":" + op);
                    progressMade = true;
                    printLockTable();

                } else {
                    System.out.println("  T" + txId + " waiting on record " + op.recordId);
                    // discard the pending entry, timestamp is not incremented
                    pendingLog = null;
                }
            }

            if (!progressMade) {
                System.out.println("Deadlock detected, no progress possible.");
                break;
            }
            round++;
        }

        System.out.println("\nFinal schedule: " + String.join(" -> ", finalSchedule));
        printLogTable();
    }


    private boolean tryExecute(int txId, Operation op) {
        switch (op.type) {
            case READ:   return tryRead(txId, op.recordId);
            case WRITE:  return tryWrite(txId, op.recordId, op.writeValue);
            case COMMIT: return doCommit(txId);
            default:     return false;
        }
    }


    // READ: acquire a Shared lock then read
    // a Shared lock is allowed unless another transaction holds an Exclusive lock
    private boolean tryRead(int txId, int recId) {
        LockType myLock    = getMyLockOn(recId, txId);
        LockType otherLock = getOtherLockOn(recId, txId);

        // already have a lock on this record, no need to acquire again
        if (myLock == LockType.SHARED || myLock == LockType.EXCLUSIVE) {
            int value = database.getOrDefault(recId, 0);
            System.out.println("  T" + txId + " already has " + myLock + " lock on Rec " + recId + ", read value = " + value);
            pendingLog = LogRecord.forRead(txId, recId, value, getPrevLogTime(txId));
            return true;
        }

        // another transaction holds an Exclusive lock, must wait
        if (otherLock == LockType.EXCLUSIVE) {
            System.out.println("  T" + txId + " cannot read Rec " + recId + ", another transaction holds X-lock");
            return false;
        }

        // no conflict, acquire Shared lock and read
        addLock(recId, LockType.SHARED, txId);
        int value = database.getOrDefault(recId, 0);
        System.out.println("  T" + txId + " acquired S-lock on Rec " + recId + ", read value = " + value);
        pendingLog = LogRecord.forRead(txId, recId, value, getPrevLogTime(txId));
        return true;
    }


    // WRITE: acquire an Exclusive lock then write
    // an Exclusive lock is only allowed if no other transaction holds any lock on this record
    private boolean tryWrite(int txId, int recId, int newValue) {
        LockType myLock    = getMyLockOn(recId, txId);
        LockType otherLock = getOtherLockOn(recId, txId);

        // already hold Exclusive lock, just write
        if (myLock == LockType.EXCLUSIVE) {
            int oldValue = database.getOrDefault(recId, 0);
            database.put(recId, newValue);
            System.out.println("  T" + txId + " already has X-lock on Rec " + recId + ", wrote " + oldValue + " -> " + newValue);
            pendingLog = LogRecord.forWrite(txId, recId, oldValue, newValue, getPrevLogTime(txId));
            return true;
        }

        // hold Shared lock, try to upgrade to Exclusive
        if (myLock == LockType.SHARED) {
            boolean othersSharing = lockTable
                    .getOrDefault(recId, Collections.emptyList())
                    .stream()
                    .anyMatch(lock -> lock.lockType == LockType.SHARED && lock.transactionId != txId);

            // cannot upgrade if another transaction also holds a Shared lock
            if (othersSharing) {
                System.out.println("  T" + txId + " cannot upgrade lock on Rec " + recId + ", another transaction also has S-lock");
                return false;
            }

            // upgrade: remove old Shared lock and replace with Exclusive lock
            lockTable.getOrDefault(recId, new ArrayList<>()).removeIf(lock -> lock.transactionId == txId);
            addLock(recId, LockType.EXCLUSIVE, txId);
            int oldValue = database.getOrDefault(recId, 0);
            database.put(recId, newValue);
            System.out.println("  T" + txId + " upgraded S-lock to X-lock on Rec " + recId + ", wrote " + oldValue + " -> " + newValue);
            pendingLog = LogRecord.forWrite(txId, recId, oldValue, newValue, getPrevLogTime(txId));
            return true;
        }

        // another transaction holds any lock, must wait
        if (otherLock != LockType.NONE) {
            System.out.println("  T" + txId + " cannot write Rec " + recId + ", another transaction holds a lock");
            return false;
        }

        // no locks exist on this record, acquire Exclusive lock and write
        addLock(recId, LockType.EXCLUSIVE, txId);
        int oldValue = database.getOrDefault(recId, 0);
        database.put(recId, newValue);
        System.out.println("  T" + txId + " acquired X-lock on Rec " + recId + ", wrote " + oldValue + " -> " + newValue);
        pendingLog = LogRecord.forWrite(txId, recId, oldValue, newValue, getPrevLogTime(txId));
        return true;
    }


    // COMMIT: release all locks held by this transaction
    // this is the shrinking phase of 2PL, all locks are released at once on commit
    private boolean doCommit(int txId) {
        releaseAllLocks(txId);
        committed.add(txId);
        System.out.println("  T" + txId + " committed, all locks released");
        pendingLog = LogRecord.forCommit(txId, getPrevLogTime(txId));
        return true;
    }


    private void addLock(int recId, LockType type, int txId) {
        lockTable.computeIfAbsent(recId, k -> new ArrayList<>())
                 .add(new LockRecord(recId, type, txId));
    }

    private void releaseAllLocks(int txId) {
        for (List<LockRecord> locks : lockTable.values())
            locks.removeIf(lock -> lock.transactionId == txId);
    }

    private LockType getMyLockOn(int recId, int txId) {
        for (LockRecord lock : lockTable.getOrDefault(recId, Collections.emptyList()))
            if (lock.transactionId == txId) return lock.lockType;
        return LockType.NONE;
    }

    private LockType getOtherLockOn(int recId, int txId) {
        for (LockRecord lock : lockTable.getOrDefault(recId, Collections.emptyList()))
            if (lock.transactionId != txId) return lock.lockType;
        return LockType.NONE;
    }

    private int getPrevLogTime(int txId) {
        return lastLogTime.getOrDefault(txId, -1);
    }


    private void printLockTable() {
        System.out.println("  Lock Table:");
        boolean empty = true;
        for (Map.Entry<Integer, List<LockRecord>> entry : lockTable.entrySet()) {
            for (LockRecord lock : entry.getValue()) {
                System.out.println("    Rec " + lock.recordId + "  " + lock.lockType + "  T" + lock.transactionId);
                empty = false;
            }
        }
        if (empty) System.out.println("    (empty)");
    }

    private void printLogTable() {
        System.out.println("\nLog Table:");
        for (LogRecord log : logTable)
            System.out.println("  " + log);
    }
}
