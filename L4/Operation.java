public class Operation {

    OperationType type;
    int recordId;
    int writeValue;

    // COMMIT operation
    Operation() {
        this.type = OperationType.COMMIT;
        this.recordId = -1;
    }

    // READ operation
    Operation(int recordId) {
        this.type = OperationType.READ;
        this.recordId = recordId;
    }

    // WRITE operation
    Operation(int recordId, int value) {
        this.type = OperationType.WRITE;
        this.recordId = recordId;
        this.writeValue = value;
    }

    @Override
    public String toString() {
        if (type == OperationType.READ)  return "R(" + recordId + ")";
        if (type == OperationType.WRITE) return "W(" + recordId + "," + writeValue + ")";
        return "C";
    }
}
