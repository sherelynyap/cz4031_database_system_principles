package Storage;

import Config.Config;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Disk {
    public static final int maxBlockSize = (int) Math.floor(Config.DISK_CAPACITY / Config.BLOCK_SIZE);

    int blockCount;
    int recordCount;

    ArrayList<Block> blocks;
    Set<Integer> candidateBlocks; //Block with free slots after deleting records

    public Disk() {
        this.blockCount = 0;
        this.recordCount = 0;

        this.blocks = new ArrayList<>();
        this.candidateBlocks = new HashSet<>();
    }

    public int getBlockCount(){
        return this.blockCount;
    }

    public int getRecordCount(){
        return this.recordCount;
    }

    public ArrayList<Block> getBlocks(){
        return this.blocks;
    }

    public Record getRecord(Address address) {
        return blocks.get(address.blockID).getRecordAt(address.offset);
    }

    public ArrayList<Record> getRecords(ArrayList<Address> addressList) {
        ArrayList<Record> recordList = new ArrayList<>();
        
        for (Address address : addressList) {
            recordList.add(getRecord(address));
        }
        return recordList;
    }

    public Address insertRecord(Record record) throws Exception{
        int candidateBlockID;
        if (!candidateBlocks.isEmpty()){
            //If some records deleted from blocks previously
            candidateBlockID = candidateBlocks.iterator().next();
        } else {
            //Else check the ArrayList of blocks
            candidateBlockID = blocks.size() - 1;
            if (candidateBlockID == -1 || blocks.get(candidateBlockID).isFull()){
                if (blocks.size() == maxBlockSize){
                    throw new Exception("Maximum capacity of disk reached");
                }
                Block newBlock = new Block();
                blocks.add(newBlock);
                blockCount++;
                candidateBlockID = blocks.size() - 1;
            }
        }

        Block candidateBlock = blocks.get(candidateBlockID);
        int offset = candidateBlock.insertRecord(record);
        recordCount++;

        //Update candidateBlocks
        if (candidateBlock.isFull()){
            candidateBlocks.remove(candidateBlockID);
        }

        return new Address(candidateBlockID, offset);
    }
    
    public void deleteRecord(ArrayList<Address> addressList) throws Exception{
        for (Address address : addressList){
            int blockID = address.blockID;
            int offset = address.offset;

            boolean emptiedAfterDeletion = blocks.get(blockID).deleteRecordAt(offset);
            recordCount--;
            candidateBlocks.add(blockID);

            if (emptiedAfterDeletion){
                blockCount--;
            }
        }
    }
}