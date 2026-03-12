// Lock Table: tracks which transaction holds what lock on which record
public class LockRecord {

    int recordId;
    LockType lockType;  //(NONE,SHARED,EXCLUSIVE)
    int transactionId;

    LockRecord(int recordId, LockType lockType, int transactionId) {
        this.recordId      = recordId;
        this.lockType      = lockType;
        this.transactionId = transactionId;
    }
}
