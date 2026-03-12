// Log Table: records every successful operation for rollback purposes
public class LogRecord {

    int timestamp;       // assigned when flushed to the log table, not when created
    int transactionId;
    OperationType operationType;
    int recordId;
    Integer oldValue;    // only for WRITE
    Integer newValue;    // only for WRITE
    Integer readValue;   // only for READ
    int prevTimestamp;   // points to previous log entry for this transaction (used for rollback)

    static LogRecord forWrite(int txId, int recId, int oldVal, int newVal, int prevTs) {
        LogRecord log      = new LogRecord();
        log.transactionId  = txId;
        log.operationType  = OperationType.WRITE;
        log.recordId       = recId;
        log.oldValue       = oldVal;
        log.newValue       = newVal;
        log.prevTimestamp  = prevTs;
        return log;
    }

    static LogRecord forRead(int txId, int recId, int val, int prevTs) {
        LogRecord log      = new LogRecord();
        log.transactionId  = txId;
        log.operationType  = OperationType.READ;
        log.recordId       = recId;
        log.readValue      = val;
        log.prevTimestamp  = prevTs;
        return log;
    }

    static LogRecord forCommit(int txId, int prevTs) {
        LogRecord log      = new LogRecord();
        log.transactionId  = txId;
        log.operationType  = OperationType.COMMIT;
        log.recordId       = -1;
        log.prevTimestamp  = prevTs;
        return log;
    }

    @Override
    public String toString() {
        if (operationType == OperationType.WRITE)
            return String.format("TS=%d  T%d  WRITE   Rec=%d  Old=%d  New=%d  PrevTS=%d",
                    timestamp, transactionId, recordId, oldValue, newValue, prevTimestamp);
        if (operationType == OperationType.READ)
            return String.format("TS=%d  T%d  READ    Rec=%d  Val=%d  PrevTS=%d",
                    timestamp, transactionId, recordId, readValue, prevTimestamp);
        return String.format("TS=%d  T%d  COMMIT  PrevTS=%d",
                timestamp, transactionId, prevTimestamp);
    }
}
