import java.util.*;

import BPlusTree.BPTree;
import Storage.Disk;
import Storage.Address;
import Storage.Block;
import Storage.Record;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import Config.Config;

public class Main {
    private Disk disk;
    private BPTree index;

    // Date parser to return a date with fixed format DDMMYYYY
    public static String parseDate(String date) {
        String[] dateParts = date.split("/");

        int day = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]);
        int year = Integer.parseInt(dateParts[2]);

        String formattedDay = String.format("%02d", day);
        String formattedMonth = String.format("%02d", month);
        String formattedYear = String.format("%04d", year);

        String result = formattedDay + formattedMonth + formattedYear;
        return result;
    }

    public static List<Record> doRecordReading(String directory) throws Exception {
        File dataFile = new File(directory);
        System.out.println("Reading records from " + directory + "...");

        String line;
        String[] fields = null;
        List<Record> records = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(dataFile));

        reader.readLine();
        while ((line = reader.readLine()) != null) {
            fields = line.split("\\t");

            // Ignore empty fields
            if (fields[2].isEmpty()) {
                continue;
            }

            String GAME_DATE_EST = parseDate(fields[0]);
            int TEAM_ID_home = Integer.parseInt(fields[1]);
            int PTS_home = Integer.parseInt(fields[2]);
            float FG_PCT_home = Float.parseFloat(fields[3]);
            float FT_PCT_home = Float.parseFloat(fields[4]);
            float FG3_PCT_home = Float.parseFloat(fields[5]);
            int AST_home = Integer.parseInt(fields[6]);
            int REB_home = Integer.parseInt(fields[7]);
            boolean HOME_TEAM_WINS = fields[8] != "0";

            Record record = new Record(GAME_DATE_EST, TEAM_ID_home, PTS_home, FG_PCT_home, FT_PCT_home, FG3_PCT_home,
                    AST_home, REB_home, HOME_TEAM_WINS);
            records.add(record);
        }
        reader.close();

        System.out.println("Inserted records from " + directory + " successfully.");
        return records;
    }

    public void init() throws Exception {
        System.out.println();
        System.out.println("Initializing the database...");

        // Initialization
        disk = new Disk();
        index = new BPTree(Config.BLOCK_SIZE);

        // Insertion
        List<Record> rows = doRecordReading(Config.DATA_FILE_PATH);
        for (Record row : rows) {
            Address address = disk.insertRecord(row);
            index.insert(row.FG_PCT_home, address);
        }

        System.out.println(
                "Database and B+ tree index created successfully.");
        System.out.println();
    }

    public void runExperiment1() {
        System.out.println("Number of records: " + disk.getRecordCount());
        System.out.println("Size of a record: " + Record.size);
        System.out.println("Number of records stored in a block: " + Block.maxRecordCount);
        System.out.println("Number of blocks for storing the data: " + disk.getBlockCount());
    }

    public void runExperiment2() {
        index.printDetail();
    }

    public void runExperiment3() throws Exception {
        long startTime = System.nanoTime();
        ArrayList<Address> dataAddress = index.showExperiment3((float) 0.5);

        ArrayList<Record> records = disk.getRecords(dataAddress); // To store all the records fit the condition above

        long runtime = System.nanoTime() - startTime;
        System.out.println("The running time of the retrieval process: " + runtime / 1000000 + " ms");

        double total_FG3 = 0;
        for (Record r : records) {
            total_FG3 += r.FG3_PCT_home;
        }

        System.out.println("For records with FG_PCT_home = 0.5, average FG3_PCT_home: " + total_FG3 / records.size());

        startTime = System.nanoTime();

        records = disk.linearScan((float) 0.5);

        runtime = System.nanoTime() - startTime;
        System.out.println("The running time of the retrieval process (brute-force linear scan method): "
                + runtime / 1000000 + " ms");

        total_FG3 = 0;
        for (Record r : records) {
            total_FG3 += r.FG3_PCT_home;
        }

        System.out.println("For records with FG_PCT_home = 0.5, average FG_PCT_home (brute-force linear scan method): "
                + total_FG3 / records.size());

    }

    public void runExperiment4() throws Exception {
        // Normal query
        long startingTime = System.nanoTime();
        ArrayList<Address> addressList = index.doRangeRecordsRetrieval(0.6f, 1.0f);
        ArrayList<Record> records = disk.getRecords(addressList);
        long totalRuntime = System.nanoTime() - startingTime;

        System.out.println("The running time of the retrieval process is " + totalRuntime / 1000000 + " ms");

        float averageVal = 0;
        for (Record record : records) {
            System.out.println(record.FG_PCT_home);
            averageVal += record.FG3_PCT_home;
        }

        averageVal /= records.size();

        System.out.println("The average FG3_PCT_home of the records where FG_PCT_home from 0.6 - 1 is " + averageVal);

        // Brute Force Linear Scan
        startingTime = System.nanoTime();
        records = disk.linearScan(0.6f, 1.0f);
        totalRuntime = System.nanoTime() - startingTime;
        System.out.println("The running time of the retrieval process (brute-forcelinear scan method) is "
                + totalRuntime / 1000000 + " ms");

        averageVal = 0;
        for (Record record : records) {
            averageVal += record.FG3_PCT_home;
        }

        averageVal /= records.size(); // total rating divide by the size of the arraylist to get the average

        System.out.println(
                "The average FG3_PCT_home of the records where FG_PCT_home from 0.6 - 1 using (brute-force linear scan method) is "
                        + averageVal + "\n");
    }

    public void runExperiment5() throws Exception {
        // Create a copy of disk to perform linear scan
        Disk tempDisk = new Disk();
        List<Record> rows = doRecordReading(Config.DATA_FILE_PATH);

        for (Record row : rows) {
            tempDisk.insertRecord(row);
        }
        
        // Normal deletion
        ArrayList<Address> addressResult = index.doRangeRecordsRetrievalnoDuplicate(0f, 0.35f);
        ArrayList<Record> recordResult = disk.getRecords(addressResult);
        
        long startTime = System.nanoTime();
        for (Record r : recordResult) {
            disk.deleteRecord(index.KeyRemoval(r.FG_PCT_home));
        }
        long runtime = System.nanoTime() - startTime;

        System.out.println("The running time of the deletion process is " + runtime / 1000000 + " ms");
        index.printDetail();

        // Brute force deletion
        startTime = System.nanoTime();
        tempDisk.linearScanDeletion(0.35f);
        runtime = System.nanoTime() - startTime;
        System.out.println("The running time of the deletion process is (brute-force linear scan method) "
                + runtime / 1000000 + " ms");
    }

    public void printBPTree() {
        index.printTree();
    }

    public void displayMenu() throws Exception {
        String input;
        Scanner sc = new Scanner(System.in);

        do {
            System.out.println(
                    "======================================================================================");
            System.out.println("Which option would you like to select?");
            System.out.println("(1): Experiment 1");
            System.out.println("(2): Experiment 2");
            System.out.println("(3): Experiment 3");
            System.out.println("(4): Experiment 4");
            System.out.println("(5): Experiment 5");
            System.out.println("(6): Print B+ Tree");
            System.out.println("(7): Exit");
            System.out.println(
                    "======================================================================================");
            System.out.print("Your option (Type the number of the option): ");

            input = sc.nextLine();
            switch (input) {
                case "1":
                    runExperiment1();
                    break;
                case "2":
                    runExperiment2();
                    break;
                case "3":
                    runExperiment3();
                    break;
                case "4":
                    runExperiment4();
                    break;
                case "5":
                    runExperiment5();
                    break;
                case "6":
                    printBPTree();
                    break;
                default:
                    break;
            }
        } while (input != "7");

        sc.close();
    }

    public static void main(String[] args) {
        try {
            Main db = new Main();
            db.init();
            db.displayMenu();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}