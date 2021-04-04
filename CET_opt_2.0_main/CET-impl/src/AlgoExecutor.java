package src;

import src.Components.CompressedGraph;

import src.Traversal.*;
import src.util.AnchorProcessor;
import src.util.AnchorType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

class AlgoExecutor {
    private GraphTraversal algo;
    private long average;               // Average execution time
    private final int numRun;
    private final long[] runTimes;
    private boolean savePathInMem;

    // only when choose sequential anchor algo
    private AnchorType selection = null;
    private int numAnchor = 0;

    /**
     * Constructor
     * @param numRun The number of algo run
     */
    AlgoExecutor(int numRun) {
        this.numRun = numRun;
        this.average = 0;
        this.runTimes = new long[numRun];
    }


    void setSavePathInMem(boolean set) {
        savePathInMem = set;
    }

    /**
     * Selection of algo to initialize
     * 0. Finish choosing (exit program)
     * 1. Normal BFS
     * 2. Normal DFS
     * 3. Anchor (BFS concatenate)
     * 4. Anchor (DFS concatenate)
     * 5. M_CET
     * 6. T_CET
     * 7. Anchor (Double leveling)
     *
     * @param selection selection of algo
     * @param graph graph
     */
    void setAlgo(int selection, CompressedGraph graph) {
        switch (selection) {
            case 1 -> this.algo = new BFSGraphTraversal(graph, savePathInMem);
            case 2 -> this.algo = new DFSGraphTraversal(graph, savePathInMem);
            case 3 -> addHybrid(graph, ConcatenateType.DFS);
            case 4 -> addHybrid(graph, ConcatenateType.BFS);
            case 5 -> this.algo = new M_CETGraphTraversal(graph, savePathInMem);
            case 6 -> this.algo = new T_CETGraphTraversal(graph, savePathInMem);
            case 7 -> addDoubleSeqHybrid(graph);
            default -> System.out.println("Algo unknown");
        }
    }


    private void addHybrid(CompressedGraph graph, ConcatenateType concatenateType) {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n" +
                "Do you want to run it concurrently?(y/n)");
        String input = sc.nextLine();
        if(input.equals("y")) this.algo = new ConcurrentAnchorTraversal(graph, savePathInMem, null, concatenateType);
        else this.algo = new AnchorGraphTraversal(graph, savePathInMem, null,concatenateType );

        selectAnchorType(graph);
    }


    private void addDoubleSeqHybrid(CompressedGraph graph){
        Scanner sc = new Scanner(System.in);
        System.out.println("\n" +
                "First level concatenation: 1. BFS   2. DFS");
        ConcatenateType firstConcatenate = sc.nextLine().equals("1") ? ConcatenateType.BFS : ConcatenateType.DFS;

        System.out.println("\n" +
                "Second level concatenation: 1. BFS   2. DFS");
        ConcatenateType secondConcatenate = sc.nextLine().equals("1") ? ConcatenateType.BFS : ConcatenateType.DFS;

        System.out.println("\n" +
                "Reduce Anchor type: 1. Half   2. Keep largest degree");
        String reduceType = sc.nextLine().equals("1") ? "Half" : "largest";

        System.out.println("\n" +
                "Do you want to run it concurrently?(y/n)");
        String input = sc.nextLine();
        if(input.equalsIgnoreCase("y"))
            this.algo = new ConcurrentDoubleAnchorTraversal(graph, savePathInMem, null, firstConcatenate, secondConcatenate, reduceType);
        else
            this.algo = new DoubleAnchorTraversal(graph, savePathInMem, null, firstConcatenate, secondConcatenate, reduceType);

        selectAnchorType(graph);
    }


    // Add anchor nodes to algorithm
    private void selectAnchorType(CompressedGraph graph){
        Scanner sc = new Scanner(System.in);

        System.out.println("""
                -----
                - As you have selected hybrid type,
                - please specify the anchor nodes selection strategy:
                -   1. Random selection
                -   2. Largest degree nodes
                -   3. Equally distributed nodes
                -   4. Smallest degree nodes""");
        String input = sc.nextLine();
        selection = input.equals("1") ? AnchorType.RANDOM : input.equals("2") ? AnchorType.LARGEST_DEGREE : AnchorType.EQUAL_DISTRIBUTE;
        if(input.equals("4")) selection = AnchorType.SMALLEST_DEGREE;


        while(true) {
            System.out.println("\n- Please enter the number of anchors:");
            numAnchor = Integer.parseInt(sc.nextLine());
            if (numAnchor + graph.getStartPointNum() <= graph.getNumVertex()) break;
            System.out.println("WARNING: The number of anchor nodes is larger than the number of nodes in graph, try again.\n\n");
        }

        ((AnchorGraphTraversal) this.algo).setAnchorNodes(
                findAnchor(this.algo.getGraph(), selection));
    }


    private int[] findAnchor(CompressedGraph graph, AnchorType selection) {
        AnchorProcessor anchorProcessor = new AnchorProcessor(graph);
        int[] anchor = anchorProcessor.findAnchors(selection, numAnchor);

        System.out.println("\nSource nodes:");

        for (int i = 0; i < graph.getStartPointNum(); i++) {
            System.out.print(String.format("[%d, %d] ",
                    graph.getStartPoints().get(i),
                    graph.getNumDegree(graph.getStartPoints().get(i))));
        }
        System.out.println("\nSelected anchor nodes: ");

        for (int i = graph.getStartPointNum(); i < anchor.length; i++) {
            System.out.print(String.format("[%d, %d] ", anchor[i], graph.getNumDegree(anchor[i])));
        }
        System.out.println("\n");
        return anchor;

    }

    void execute() {
        System.out.println("Algorithm to execute: " + this.algo.getClass().getName());
        boolean isDoubleConcatenate = this.algo.getClass().getName().contains("Double");

        String concurrentPrefix = (this.algo.getClass().getName().contains("Concurrent") ? "Concurrent-" : "");

        String doublePrefix = (isDoubleConcatenate ?
            ((DoubleAnchorTraversal)this.algo).firstLevel +
                "-"+((DoubleAnchorTraversal)this.algo).secondLevel+ "-":"")
                .replaceAll("AnchorType.", "");

        String concatenatePrefix = "";
        if(selection != null){
            if(isDoubleConcatenate) {
                concatenatePrefix ="-" +  ((DoubleAnchorTraversal)this.algo).firstLevel + ""
                        + ((DoubleAnchorTraversal)this.algo).secondLevel;
            }
            else {
                concatenatePrefix = "-" + ((AnchorGraphTraversal)this.algo).concatenateType;
            }
            concatenatePrefix.replace("ConcatenateType.", "");
        }


        String fileName = "OutputFiles/result/timeResults/" + "graph-" +
                this.algo.getGraph().getNumVertex() + "-" +
                this.algo.traversalType + "-" +
                doublePrefix + concurrentPrefix +
                selection + concatenatePrefix +  "-" + new Date().toString() +  ".txt";


        // for anchor node algorithms
        if(selection!= null && this.algo.getGraph().getNumVertex() > 100) {
            System.out.println("Do you want to run range of anchor node num?(y/n)");
            int upper;

            if (new Scanner(System.in).nextLine().equals("y")) {
                while (true) {
                    System.out.println("\nDesired upper bound:");
                    try {
                        upper = Integer.parseInt(new Scanner(System.in).nextLine());
                        break;
                    } catch (Exception e) {
                        System.out.println("Not a number!");
                    }
                }
                // get the closest int of numAnchor as start point
                numAnchor = numAnchor / 5 * 5;
                if (upper < numAnchor) upper = this.algo.getGraph().getNumVertex() / 10 + 10;

                for (int i = numAnchor; i <= upper; i += 5) {
                    // set new Anchor num
                    numAnchor = i;
                    ((AnchorGraphTraversal) this.algo).setAnchorNodes(
                            findAnchor(this.algo.getGraph(), selection));
                    runAlgo();
                    writeTimeResult(fileName);

                    System.out.println("\n-- Anchor nodes " + i + " finished!\n\n" +
                            "--------------------------------------------------------------------------------\n\n\n\n");
                }
                return;
            }
        }
        runAlgo();
        writeTimeResult(fileName);
    }

    // TODO: possible concurrent optimization
    public void cleanGarbage(){
        System.gc();
        if(this.algo.getClass().getName().contains("ConcurrentDouble"))
        {
            ((ConcurrentDoubleAnchorTraversal)this.algo).pool.shutdownNow();
        }
        else if(this.algo.getClass().getName().contains("ConcurrentAnchor"))
        {
            ((ConcurrentAnchorTraversal)this.algo).pool.shutdownNow();
        }
    }

    private void runAlgo() {
        average = 0;
        for (int i = 0; i < numRun; i++) {
            this.algo.execute();

            average += this.algo.timeElapsed;
            runTimes[i] = this.algo.timeElapsed;
            System.out.println("run: " + runTimes[i]);
            System.gc();
        }

        System.out.println("\n\nAverage execution time in nanoseconds: " + average / numRun);
        System.out.println("Average execution time in seconds: " + average / numRun / Math.pow(10, 9) + "\n");

    }

    private void writeTimeResult(String fileName) {
        File file = new File(fileName.replace(":", "-"));

        try{
            if(!file.exists()) {
                if(!file.createNewFile()) {
                    return;
                }
            }

            FileWriter fw = new FileWriter(file, true);

            if(numAnchor != 0) {
                fw.write("\n" + numAnchor + "," + average / numRun / Math.pow(10, 9));
            }
            else {
                fw.write("Average time(s) running " + numRun + " times: " + average / numRun / Math.pow(10, 9));
            }

            fw.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    boolean isSavePathInMem() {
        return this.savePathInMem;
    }

    void savePathsResult() {
        this.algo.saveResults();
    }

    void printPaths() {
        this.algo.printPaths();
    }
}
