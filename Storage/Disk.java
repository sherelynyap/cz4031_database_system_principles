package storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import config.Config;

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

    public Record getRecord(Address address) throws Exception{
        return blocks.get(address.blockID).getRecordAt(address.offset);
    }

    public ArrayList<Record> getRecords(ArrayList<Address> addressList) throws Exception{
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

    public ArrayList<Record> linearScan(float key) {
        System.out.println("\nBrute-force Linear Scan");
        System.out.println("------------------------------------------------------------------");

        int blockAccess = 0;
        ArrayList<Record> recordList = new ArrayList<>();

        for (Block block : blocks) {
            //Ignore empty blocks
            if (block.isEmpty()){
                continue;
            }

            blockAccess++;
            Record[] records = block.getRecords();

            for (Record record : records) {
                if (record != null && record.FG_PCT_home == key) {
                    recordList.add(record);
                }
            }
        }

        System.out.printf("The number of data blocks accessed: %d\n", blockAccess);

        return recordList;
    }

    public ArrayList<Record> linearScan(float lowerBound, float upperBound) {
        System.out.println("\nBrute-force Linear Scan (Range)");
        System.out.println("------------------------------------------------------------------");

        int blockAccess = 0;
        ArrayList<Record> recordList = new ArrayList<>();

        for (Block block : blocks) {
            //Ignore empty blocks
            if (block.isEmpty()){
                continue;
            }

            blockAccess++;
            Record[] records = block.getRecords();

            for (Record record : records) {
                if (record != null && record.FG_PCT_home >= lowerBound && record.FG_PCT_home <= upperBound) {
                    recordList.add(record);
                }
            }
        }

        System.out.printf("The number of data blocks accessed: %d\n", blockAccess);

        return recordList;
    }

    public void linearScanDeletion(float upperBound) throws Exception{
        System.out.println("\nBrute-force Linear Scan (Delete)");
        System.out.println("------------------------------------------------------------------");

        int blockAccess = 0;
        ArrayList<Address> addressList = new ArrayList<>();

        for (int blockID = 0; blockID < blocks.size(); blockID++) {
            Block block = blocks.get(blockID);
            //Ignore empty blocks
            if (block.isEmpty()){
                continue;
            }

            blockAccess++;
            Record[] records = block.getRecords();

            int count = 0;

            for (Record record : records) {
                if (record != null && record.FG_PCT_home <= upperBound) {
                    Address address = new Address(blockID, count);
                    addressList.add(address);
                }
                count++;
            }
        }

        deleteRecord(addressList);
        System.out.printf("The number of data blocks accessed: %d\n", blockAccess);
    }
}