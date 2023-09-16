package BruteForceLinearScan;

import Storage.Address;
import Storage.Block;
import Storage.Disk;
import Storage.Record;

import java.util.ArrayList;

public class LinearScan{
    ArrayList<Block> dataBlockList;

    public LinearScan(ArrayList<Block> blockList) {
        this.dataBlockList = blockList;
    }
    
    //can do method overloading here
    public ArrayList<Record> doLinearScanRange(float lowerBound, float upperBound){
        System.out.println("\nBrute-force Range Linear Scan");
        System.out.println("------------------------------------------------------------------");

        int blockAccess = 0;
        ArrayList<Record> recordList = new ArrayList<>();

        for (Block block : dataBlockList) {
            blockAccess++;
            Record[] records = block.getRecords();

            for (Record record : records) {
                if (record != null && record.FG_PCT_home >= lowerBound && record.FG_PCT_home <= upperBound) {
                    recordList.add(record);
                }
            }
        }
        System.out.printf("Total no of data block accesses (brute-force linear scan method): %d\n", blockAccess);

        return recordList;
    }

    public void doLinearScanDeletion(float key, Disk disk) throws Exception{
        System.out.println("\nBrute-force Linear Scan");
        System.out.println("------------------------------------------------------------------");

        int blockAccess = 0;
        ArrayList<Address> addressList = new ArrayList<>();

        int blkid = 0;
        for (Block block : disk.getBlocks()) {
            blockAccess++;
            Record[] records = block.getRecords();

            int count = 0;

            for (Record record : records) {
                if (record != null && record.FG_PCT_home == key) {
                    Address add = new Address(blkid, count);
                    addressList.add(add);
                }
                count++;
            }
            blkid++;
        }

        disk.deleteRecord(addressList);
        System.out.printf("Total no of data block accesses (brute-force linear scan method): %d\n", blockAccess);
        //System.out.printf("Total no of data block accessed to delete a record (brute-force linear scan method): %d\n", disk.getCurrentBlkAccess());
    }
}