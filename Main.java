import java.text.DecimalFormat;
import java.util.*;

import BPlusTree.BPTree;
import Storage.Disk;
import Storage.Address;
import Storage.Block;
import Storage.Record;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import Config.Config;

// why use interfaces??? wrong!
public class Main implements Config {
    private Disk disk;
    private BPTree BpTree;

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
        System.out.println("Reading records from " + directory + " ...");

        String line;
        String[] fields = null;
        List<Record> records = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(dataFile));

        reader.readLine();
        while ((line = reader.readLine()) != null) {
            fields = line.split("\\t");
            String GAME_DATE_EST = parseDate(fields[0]);
            int TEAM_ID_home = Integer.parseInt(fields[1]);
            int PTS_home = Integer.parseInt(fields[2].isEmpty() ? "0" : fields[2]);
            float FG_PCT_home = Float.parseFloat(fields[3].isEmpty() ? "0" : fields[3]);
            float FT_PCT_home = Float.parseFloat(fields[4].isEmpty() ? "0" : fields[4]);
            float FG3_PCT_home = Float.parseFloat(fields[5].isEmpty() ? "0" : fields[5]);
            int AST_home = Integer.parseInt(fields[6].isEmpty() ? "0" : fields[6]);
            int REB_home = Integer.parseInt(fields[7].isEmpty() ? "0" : fields[7]);
            boolean HOME_TEAM_WINS = fields[8] != "0";

            Record r = new Record(GAME_DATE_EST, TEAM_ID_home, PTS_home, FG_PCT_home, FT_PCT_home, FG3_PCT_home,
                    AST_home, REB_home, HOME_TEAM_WINS);
            records.add(r);
        }

        reader.close();

        System.out.println("Total number of records loaded: " + records.size());
        return records;
    }

    public void doBlockCreation() throws Exception {
        disk = new Disk();
        BpTree = new BPTree(blkSize);
        List<Record> data = doRecordReading(DATA_FILE_PATH);

        System.out.println();
        System.out.println("Inserting the data into the disk and creating the B+ Tree...");

        Address dataAddr;
        for (Record d : data) {
            dataAddr = disk.insertRecord(d);
            BpTree.doBPTreeInsertion(d.FG_PCT_home, dataAddr);
        }
        System.out.println(
                "Run Successful! The records have been successfully inserted into the disk and the B+ Tree has been created.");
        System.out.println();
    }

    public void runExperiment1() {
        System.out.println("\nRunning Experiment 1...");
        System.out.println("Number of records: " + disk.getRecordCount());
        System.out.println("Size of a record: " + Record.size);
        System.out.println("Number of records stored in a block: " + Block.maxRecordCount);
        System.out.println("Number of blocks for storing the data: " + disk.getBlockCount());
    }

    public void runExperiment2() {
        System.out.println("\nRunning Experiment 2...");
        BpTree.showExperiment2();
    }

    public void runExperiment3() {
        System.out.println("\nRunning Experiment 3...");

        long startTime = System.nanoTime();
        ArrayList<Address> dataAddress = BpTree.showExperiment3(500); // “numVotes” equal to 500 and store them into
                                                                      // ArrayList
        ArrayList<Record> records = disk.doRecordRetrieval(dataAddress); // To store all the records fit the condition
                                                                         // above

        long runtime = System.nanoTime() - startTime;
        System.out.println("The running time of the retrieval process is " + runtime / 1000000 + " ms");

        double averageRate = 0;
        for (Record r : records) {
            averageRate += r.getAverageRating();
        }

        averageRate /= records.size(); // total rating divide by the size of the arraylist to get the average

        System.out.println("The average rating of the records that numVotes = 500 is " + averageRate);

        startTime = System.nanoTime();

        LinearScan ls = new LinearScan(disk.doBlockRetrieval());
        records = ls.doLinearScan(500);

        runtime = System.nanoTime() - startTime;
        System.out.println("The running time of the retrieval process (brute-force linear scan method) is "
                + runtime / 1000000 + " ms");

        averageRate = 0;
        for (Record r : records) {
            averageRate += r.getAverageRating();
        }

        averageRate /= records.size(); // total rating divide by the size of the arraylist to get the average

        System.out.println("The average rating of the records that numVotes = 500 (brute-force linear scan method) is "
                + averageRate);

    }

    public void runExperiment4() throws Exception {
        System.out.println("\nRunning Experiment 4...");

        long startingTime = System.nanoTime();
        ArrayList<Address> addressResult = BpTree.doRangeRecordsRetrieval1(0.6f, 1.0f);
        ArrayList<Record> recordResult = disk.getRecords(addressResult);

        long totalRuntime = System.nanoTime() - startingTime;
        System.out.println("The running time of the retrieval process is " + totalRuntime / 1000000 + " ms");

        float averageVal = 0;
        for (Record r : recordResult) {
            averageVal += r.FG3_PCT_home;
        }

        averageVal /= recordResult.size();

        System.out.println("The average rating of the records where FG3_PCT_home from 0.6 - 1 is " + averageVal);

        startingTime = System.nanoTime();
        recordResult = disk.linearScan(0.6f, 1.0f);
        totalRuntime = System.nanoTime() - startingTime;
        System.out.println("The running time of the retrieval process (brute-forcelinear scan method) is "
                + totalRuntime / 1000000 + " ms");
        averageVal = 0;
        for (Record r : recordResult) {
            averageVal += r.FG3_PCT_home;
        }

        averageVal /= recordResult.size(); // total rating divide by the size of the arraylist to get the average

        System.out.println(
                "The average rating of the records where FG3_PCT_home from 0.6 - 1 using (brute-force linear scan method) is "
                        + averageVal + "\n");

    }

    public void runExperiment5() {
        System.out.println("\nRunning Experiment 5...");
        System.out.println();
        System.out.println("B+ tree");
        System.out.println("------------------------------------------------------------------");
        long startTime = System.nanoTime();
        disk.doRecordDeletion(BpTree.doKeyRemoval(1000));
        long runtime = System.nanoTime() - startTime;
        System.out.println("The running time of the deletion process is " + runtime / 1000000 + " ms");
        BpTree.showExperiment2();

        startTime = System.nanoTime();
        LinearScan ls = new LinearScan();
        ls.doLinearScanDeletion(1000, disk);
        runtime = System.nanoTime() - startTime;
        System.out.println("The running time of the deletion process is (brute-force linear scan method) "
                + runtime / 1000000 + " ms");
    }

    public void displayMenu(int type) throws Exception {
        if (type == 1) {
            System.out
                    .println("======================================================================================");
            System.out.println("            << Welcome to Group 8's DSP Project 1 Implementation >>");
            System.out.println();
            System.out.println("What would you like to do?");
            System.out.println("1) Select an experiment \n2) Exit");
            System.out
                    .println("======================================================================================");
            System.out.print("You have selected: ");
            Scanner in = new Scanner(System.in);
            String input = in.nextLine();

            switch (input) {
                case "1":
                    doBlockCreation();
                    break;
                case "2":
                    System.exit(0);
            }
        } else {
            String input;
            do {
                System.out.println(
                        "======================================================================================");
                System.out.println("Which experiment would you like to run?");
                System.out.println(
                        "Experiment (1): Store the data on the disk & show No. of Records, Size of a Record, No. of Records stored in a Block, and No. of Blocks for storing data.");
                System.out.println(
                        "Experiment (2): Build a B+ tree on the attribute ”numVote” by inserting the records sequentially & show the B+ Tree's parameter n value, No. of Nodes, No. of Levels and Root Node Content.");
                System.out.println(
                        "Experiment (3): Retrieve movies with the “numVotes” equal to 500 and its required statistics.");
                System.out.println(
                        "Experiment (4): Retrieve movies with votes between 30,000 and 40,000 and its required statistics.");
                System.out.println(
                        "Experiment (5): Delete movies with the attribute “numVotes” equal to 1,000 and its required statistics.");
                System.out.println("           (exit): Exit ");
                System.out.println(
                        "======================================================================================");
                System.out.print("Selection: ");
                Scanner in = new Scanner(System.in);
                input = in.nextLine();
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
                }

            } while (!input.equals("exit"));
        }
    }

    // End of Main Functions
    public static void main(String[] args) {
        try {
            Main app = new Main();
            app.displayMenu(1);
            app.displayMenu(2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
