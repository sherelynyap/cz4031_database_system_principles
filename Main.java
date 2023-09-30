import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import Index.BPTree;

import Storage.Disk;
import Storage.Address;
import Storage.Block;
import Storage.Record;

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

    public static List<Record> readDataFile(String directory) throws Exception {
        File dataFile = new File(directory);
        System.out.println("Reading records from " + directory + "...");

        String line;
        String[] fields = null;
        List<Record> records = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(dataFile));

        reader.readLine();
        while ((line = reader.readLine()) != null) {
            fields = line.split("\\t");

            // Ignore row if some fields are empty
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
        List<Record> rows = readDataFile(Config.DATA_FILE_PATH);
        for (Record row : rows) {
            Address address = disk.insertRecord(row);
            index.insert(row.FG_PCT_home, address);
        }

        System.out.println("Database and B+ tree index created successfully.");
        System.out.println();
    }

    public void start() throws Exception {
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
                    experiment1();
                    pressEnterToContinue();
                    break;
                case "2":
                    experiment2();
                    pressEnterToContinue();
                    break;
                case "3":
                    experiment3();
                    pressEnterToContinue();
                    break;
                case "4":
                    experiment4();
                    pressEnterToContinue();
                    break;
                case "5":
                    experiment5();
                    pressEnterToContinue();
                    break;
                case "6":
                    printBPTree();
                    pressEnterToContinue();
                    break;
                default:
                    break;
            }
        } while (input != "7");

        sc.close();
    }

    public void experiment1() {
        System.out.println("The number of records: " + disk.getRecordCount());
        System.out.println("The size of a record: " + Record.size);
        System.out.println("The number of records stored in a block: " + Block.maxRecordCount);
        System.out.println("The number of blocks for storing the data: " + disk.getBlockCount());
    }

    public void experiment2() {
        index.printInfo();
    }

    public void experiment3() throws Exception {
        //Normal Query
        long startTime = System.nanoTime();
        ArrayList<Address> dataAddress = index.doRecordsWithKeysRetrieval(0.5f);
        ArrayList<Record> records = disk.getRecords(dataAddress);
        long runtime = System.nanoTime() - startTime;

        System.out.println("The running time of the retrieval process: " + runtime / 1000000 + " ms");

        float total_FG3 = 0;
        for (Record record : records) {
            total_FG3 += record.FG3_PCT_home;
        }

        System.out.println("For records with FG_PCT_home = 0.5, average FG3_PCT_home: " + total_FG3 / records.size());

        // Brute Force Linear Scan
        startTime = System.nanoTime();
        records = disk.linearScan(0.5f);
        runtime = System.nanoTime() - startTime;
        System.out.println("The running time of the retrieval process (brute-force linear scan method): "
                + runtime / 1000000 + " ms");

        total_FG3 = 0;
        for (Record record : records) {
            total_FG3 += record.FG3_PCT_home;
        }

        System.out.println("For records with FG_PCT_home = 0.5, average FG_PCT_home (brute-force linear scan method): "
                + total_FG3 / records.size());
    }

    public void experiment4() throws Exception {
        // Normal query
        long startingTime = System.nanoTime();
        ArrayList<Address> addressList = index.doRangeRecordsRetrieval(0.6f, 1.0f);
        ArrayList<Record> records = disk.getRecords(addressList);
        long totalRuntime = System.nanoTime() - startingTime;

        System.out.println("The running time of the retrieval process is " + totalRuntime / 1000000 + " ms");

        float total_FG3 = 0;
        for (Record record : records) {
            total_FG3 += record.FG3_PCT_home;
        }

        total_FG3 /= records.size();

        System.out.println("The average FG3_PCT_home of the records where FG_PCT_home from 0.6 - 1 is " + total_FG3);

        // Brute Force Linear Scan
        startingTime = System.nanoTime();
        records = disk.linearScan(0.6f, 1.0f);
        totalRuntime = System.nanoTime() - startingTime;
        System.out.println("The running time of the retrieval process (brute-forcelinear scan method) is "
                + totalRuntime / 1000000 + " ms");

        total_FG3 = 0;
        for (Record record : records) {
            total_FG3 += record.FG3_PCT_home;
        }

        total_FG3 /= records.size();

        System.out.println(
                "The average FG3_PCT_home of the records where FG_PCT_home from 0.6 - 1 using (brute-force linear scan method) is "
                        + total_FG3 + "\n");
    }

    public void experiment5() throws Exception {
        // Create a deep copy of disk to perform linear scan for comparison
        Disk tempDisk = new Disk();
        List<Record> rows = readDataFile(Config.DATA_FILE_PATH);

        for (Record row : rows) {
            tempDisk.insertRecord(row);
        }
        
        // Normal deletion
        long startTime = System.nanoTime();
        disk.deleteRecord(index.removeKey(0f, 0.35f));
        long runtime = System.nanoTime() - startTime;

        System.out.println("The running time of the deletion process is " + runtime / 1000000 + " ms");
        index.printInfo();

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

    public static void pressEnterToContinue() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Press enter key to continue");
        sc.nextLine();
    }

    public static void main(String[] args) {
        try {
            Main db = new Main();
            db.init();
            pressEnterToContinue();
            db.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}