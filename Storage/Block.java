package Storage;

import Config.Config;

public class Block {
    public static int maxRecordCount = (int) Math.floor(Config.BLOCK_SIZE / Record.size);
    int recordCount;
    Record[] records;

    public Block(){
        this.recordCount = 0;
        this.records = new Record[maxRecordCount];
    }

    public boolean isFull() {
        return recordCount >= maxRecordCount;
    }

    public Record getRecordAt(int offset){
        return records[offset];
    }

    public Record[] getRecords() {
        return this.records;
    }

    //Visibility: Package
    int insertRecord(Record newRecord) throws Exception{
        int offset = -1;
        
        for (int i = 0; i < records.length; i++){
            if (records[i] == null){
                records[i] = newRecord;
                recordCount++;
                offset = i;
                break;
            }
        }

        if (offset == -1){
            throw new Exception("Insertion failed");
        }

        return offset;
    }

    //Visibility: Package
    //Return True if the block is emptied
    boolean deleteRecordAt(int offset) throws Exception{
        if (records[offset] != null){
            records[offset] = null;
            recordCount--;
            return recordCount == 0;
        } else {
            throw new Exception("Deletion failed");
        }
    }
}