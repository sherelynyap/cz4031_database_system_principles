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

    public int insertRecord(Record newRecord) {
        int offset = -1;
        try {
            if (!isFull()) {
                // we can add a record
                for(int i = 0; i < records.length; i++){
                    if(records[i] == null){
                        records[i] = newRecord;
                        recordCount++;
                        offset = i;
                        break;
                    }
                }
            } else {
                //throw error here??
            }
        } catch (Error e) {
            System.out.println("Inserting Record Unsuccessful: " + e.getMessage());
        }
        return offset;
    }

    public boolean deleteRecordAt(int offset){
        // can simplify?
        //try catch
        boolean emptied = false;
        if (records[offset] != null){
            records[offset] = null;
            recordCount--;
            if (recordCount == 0) {
                emptied = true;
            }
        }
        return emptied;
    }
}