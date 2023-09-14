package Storage;

public class Block {
    int maxRecords;
    int currRecords;
    Record[] data;

    public Block(){

    }
    public Block(int size){
        this.currRecords = 0;
        this.maxRecords = (int) Math.floor(size / Record.size());
        this.data = new Record[maxRecords];
    }

    public boolean isBlockFull() {
        return currRecords >= maxRecords;
    }

    // return all the records in the block
    public Record[] doAllRecordRetrieval() {
        return this.data;
    }

    /**
     doRecordInsertion(Record newRecord): Insert a new record into a block of data.
     If the block is not full:
        Iterates through the data array to find the first null element and adds the newRecord to that index.
        It then increments the currRecords counter and returns the offset of the inserted record.
     If the block is full:
        Does not add the new record and returns -1 to indicate that the insertion was unsuccessful.
     */
    public int doRecordInsertion(Record newRecord) {
        int offset = -1;
        try {
            if (!isBlockFull()) {
                // we can add a record
                for(int i = 0; i < data.length; i++){
                    if(data[i] == null){
                        data[i] = newRecord;
                        currRecords++;
                        offset = i;
                        break;
                    }
                }
            }
        } catch (Error e) {
            System.out.println("Inserting Record Unsuccessful: " + e.getMessage());
        }
        return offset;
    }

    /**
     doRecordDeletionAt(int offset): takes offset and attempts to delete the record at that position in the data array of the block.

     If a record exists at the specified offset:
        Deletion happens by setting the element in the data array to null.
        Decrements the currRecords count to reflect the deletion.

     Returns a boolean value to indicate whether the record was successfully deleted.
     */
    public boolean doRecordDeletionAt(int offset){
        boolean success = false;
        if (data[offset] != null){
            data[offset] = null;
            // if currRecords becomes 0, indicates block is empty, return
            currRecords--;
            if (currRecords == 0) {
                success = true;
            }
        }
        return success;
    }

    /**
     doRecordRetrievalAt(int offset): given the offset, do record retrival in the data array of records
     */
    public Record doRecordRetrievalAt(int offset){
        return data[offset];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < data.length; i++){
            if (i > 0){
                sb.append(", ");
            }
            sb.append(String.format("%d:{%s}", i, data[i].tconst));
        }
        sb.append("]");
        return sb.toString();
    }
}
