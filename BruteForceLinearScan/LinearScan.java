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

    public ArrayList<Record> doLinearScan(int key) {
        System.out.println("\nBrute-force Linear Scan");
        System.out.println("------------------------------------------------------------------");

        int blockAccess = 0;
        ArrayList<Record> recordList = new ArrayList<>();

        for (Block b: dataBlockList) {
            blockAccess++;
            Record[] records = b.doAllRecordRetrieval();

            for (Record r: records) {
                if (r != null && r.getNumVotes() == key) {
                    recordList.add(r);
                }

            }
        }
        System.out.printf("Total no of data block accesses (brute-force linear scan method): %d\n", blockAccess);

        return recordList;
    }
    
    //can do method overloading here
    public ArrayList<Record> doLinearScanRange(int lowerBound, int upperBound){
        System.out.println("\nBrute-force Range Linear Scan");
        System.out.println("------------------------------------------------------------------");

        int blockAccess = 0;
        ArrayList<Record> recordList = new ArrayList<>();

        for (Block b: dataBlockList) {
            blockAccess++;
            Record[] records = b.doAllRecordRetrieval();

            for (Record r: records) {
                if (r != null && r.getNumVotes() >= lowerBound && r.getNumVotes() <= upperBound) {
                    recordList.add(r);
                }

            }
        }
        System.out.printf("Total no of data block accesses (brute-force linear scan method): %d\n", blockAccess);

        return recordList;
    }

    public void doLinearScanDeletion(int key, Disk disk) {
        System.out.println("\nBrute-force Linear Scan");
        System.out.println("------------------------------------------------------------------");

        int blockAccess = 0;
        ArrayList<Address> addressList = new ArrayList<>();

        int flag = 0;
        int blkid = 0;
        for (Block b: disk.doBlockRetrieval()) {
            blockAccess++;
            Record[] records = b.doAllRecordRetrieval();

            int count = 0;

            for (Record r: records) {
                if (r != null && r.getNumVotes() == key) {
                    Address add = new Address(blkid,count);
                    addressList.add(add);
                }
                count++;
            }
            blkid++;
        }

        disk.doRecordDeletion(addressList);
        System.out.printf("Total no of data block accesses (brute-force linear scan method): %d\n", blockAccess);
        //System.out.printf("Total no of data block accessed to delete a record (brute-force linear scan method): %d\n", disk.getCurrentBlkAccess());
    }
}
