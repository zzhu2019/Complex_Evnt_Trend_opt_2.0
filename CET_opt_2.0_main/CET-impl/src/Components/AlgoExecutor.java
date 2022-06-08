package src.Components;

import src.Traversal.*;
import src.util.AnchorProcessor;
import src.util.AnchorType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Scanner;

public class AlgoExecutor {
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
    public AlgoExecutor(int numRun) {
        this.numRun = numRun;
        this.average = 0;
        this.runTimes = new long[numRun];
    }


    public void setSavePathInMem(boolean set) {
        savePathInMem = set;
    }

    /**
     * Selection of algo to initialize
     * 0. Finish choosing (exit program)
     * 1. Normal BFS
     * 2. Normal DFS
     * 3. Anchor (BFS concatenate)
     * 4. Anchor (DFS concatenate)
     * 5. Concurrent Anchor (1.DFS 2.DFS)
     * 6. Concurrent Anchor (1.BFS 2.DFS)
     * 7. Concurrent Anchor (1.DFS 2.BFS)
     * 8. Concurrent Anchor (1.BFS 2.BFS)
     *
     * @param selection selection of algo
     * @param graph graph
     */
    public void setAlgo(int selection, CompressedGraph graph) {
        switch (selection) {
            case 1 -> this.algo = new BFSGraphTraversal(graph, savePathInMem);
            case 2 -> this.algo = new DFSGraphTraversal(graph, savePathInMem);
            case 3 -> addHybrid(graph, ConcatenateType.DFS);
            case 4 -> addHybrid(graph, ConcatenateType.BFS);
            case 5 -> composeConcurrent(graph, TraversalType.DFS, ConcatenateType.DFS);
            case 6 -> composeConcurrent(graph, TraversalType.BFS, ConcatenateType.DFS);
            case 7 -> composeConcurrent(graph, TraversalType.DFS, ConcatenateType.BFS);
            case 8 -> composeConcurrent(graph, TraversalType.BFS, ConcatenateType.BFS);
            default -> System.out.println("Algo unknown");
        }
    }


    private void addHybrid(CompressedGraph graph, ConcatenateType concatenateType) {
        algo = new AnchorGraphTraversal(graph, savePathInMem, null, concatenateType);
        selectAnchorType(graph);
    }

    private void composeConcurrent(CompressedGraph graph, TraversalType ttype, ConcatenateType ctype) {
        algo = new ConcurrentAnchorGraphTraversal(graph, savePathInMem, null, ttype, ctype);
        selectAnchorType(graph);
    }


    // Add anchor nodes to algorithm
    private void selectAnchorType(CompressedGraph graph){
        Scanner sc = new Scanner(System.in);

        System.out.println("""
                -------------------------------------------------------------
                - As you have selected hybrid type,                         -
                - please specify the anchor nodes selection strategy:       -
                -   1. Random selection                                     -
                -   2. Largest degree nodes                                 -
                -   3. Equally distributed nodes                            -
                -   4. Smallest degree nodes                                -
                - If an invalid option is entered, the default is 1.        -
                -------------------------------------------------------------""");
        String input = sc.nextLine();
        switch(input) {
            case "2" -> selection = AnchorType.LARGEST_DEGREE;
            case "3" -> selection = AnchorType.EQUAL_DISTRIBUTE;
            case "4" -> selection = AnchorType.SMALLEST_DEGREE;
            default -> selection = AnchorType.RANDOM;
        }

        while(true) {
            System.out.println("\n- Please enter the number of anchors:");
            numAnchor = Integer.parseInt(sc.nextLine());
            if(numAnchor + graph.getStartPointNum() <= graph.getNumVertex()) {
                break;
            } else {
                System.out.println("""
                        WARNING: The number of anchor nodes is larger than the number of nodes in graph, try again.""");
            }
        }

        if(algo.getClass().getSimpleName().equals("AnchorGraphTraversal")) {
            ((AnchorGraphTraversal) algo).setAnchorNodes(findAnchor(algo.getGraph(), selection));
        } else {
            ((ConcurrentAnchorGraphTraversal) algo).setAnchorNodes(findAnchor(algo.getGraph(), selection));
        }
    }


    private short[] findAnchor(CompressedGraph graph, AnchorType selection) {
        AnchorProcessor anchorProcessor = new AnchorProcessor(graph);
        // Anchor nodes contain start nodes
        short[] anchors = anchorProcessor.findAnchors(selection, numAnchor);

        System.out.println("\n[source node, node's degree]: ");
        for(int i = 0; i < graph.getStartPointNum(); i++) {
            System.out.print(String.format("[%d, %d] ",
                    graph.getStartPoints().get(i),
                    graph.getNumDegree(graph.getStartPoints().get(i))));
        }

        System.out.println("\n[anchor node, node's degree]: ");
        for(int i = graph.getStartPointNum(); i < anchors.length; i++) {
            System.out.print(String.format("[%d, %d] ",
                    anchors[i],
                    graph.getNumDegree(anchors[i])));
        }
        System.out.println("\n");
        return anchors;
    }

    public void execute() {
        System.out.println("Algorithm to execute: " + this.algo.getClass().getSimpleName());

        String concurrentPrefix = (this.algo.getClass().getName().contains("Concurrent") ? "Concurrent-" : "");

        String concatenatePrefix = "";
        if(selection != null && algo.getClass().getSimpleName().equals("AnchorGraphTraversal")) {
            concatenatePrefix = "-" + ((AnchorGraphTraversal)this.algo).concatenateType;
            concatenatePrefix = concatenatePrefix.replace("ConcatenateType.", "");
        } else if(algo.getClass().getSimpleName().equals("ConcurrentAnchorGraphTraversal")) {
            concatenatePrefix = "-" + ((ConcurrentAnchorGraphTraversal)this.algo).traversalType +
                    "-" + ((ConcurrentAnchorGraphTraversal)this.algo).concatenateType;
            concatenatePrefix = concatenatePrefix.replace("traversalType.", "");
            concatenatePrefix = concatenatePrefix.replace("ConcatenateType.", "");
        }


        String fileName = "OutputFiles/result/timeResults/" + "graph-" +
                this.algo.getGraph().getNumVertex() + "-" +
                this.algo.traversalType + "-" + concurrentPrefix +
                selection + concatenatePrefix + "-" + new Date().toString() + ".txt";


        // for anchor node algorithms
//        if(selection != null && this.algo.getGraph().getNumVertex() > 100) {
//            System.out.println("""
//                    -------------------------------------------------------------
//                    - Do you want to run range of anchor node num?(y/n)         -
//                    - If y is not entered, the algo will only run once.         -
//                    -------------------------------------------------------------""");
//            int upper;
//
//            if(new Scanner(System.in).nextLine().equalsIgnoreCase("y")) {
//                while(true) {
//                    System.out.println("\nDesired upper bound:");
//                    try {
//                        upper = Integer.parseInt(new Scanner(System.in).nextLine());
//                        break;
//                    }
//                    catch(Exception e) {
//                        System.out.println("Not a valid number!");
//                    }
//                }
//                // get a smaller numAnchor which is multiple of 5 as start point
//                if(numAnchor >= 5) numAnchor = numAnchor / 5 * 5;
//                if(upper < numAnchor) upper = this.algo.getGraph().getNumVertex() / 10 + 10;
//
//                for(int i = numAnchor; i <= upper; i += 5) {
//                    // set new Anchor num with an increment of evey 5 until hitting the upper
//                    numAnchor = i;
//                    ((AnchorGraphTraversal) algo).setAnchorNodes(
//                            findAnchor(algo.getGraph(), selection));
//                    runAlgo();
//                    writeTimeResult(fileName);
//
//                    System.out.println("\n- Anchor nodes " + i + " finished!\n\n" +
//                            "-------------------------------------------------------------\n\n");
//                }
//                return;
//            }
//        }
        runAlgo();
        writeTimeResult(fileName);
    }

    // TODO: possible concurrent optimization
    public void cleanGarbage(){
        System.gc();
//        if(this.algo.getClass().getName().contains("ConcurrentDouble")) {
//            ((ConcurrentDoubleAnchorTraversal)this.algo).pool.shutdownNow();
//        }
//        if(this.algo.getClass().getName().contains("ConcurrentAnchor")) {
//            ((ConcurrentAnchorTraversal)this.algo).pool.shutdownNow();
//        }
    }

    private void runAlgo() {
        average = 0;
        for(int i = 0; i < numRun; i++) {
            algo.execute();

            average += algo.timeElapsed;
            runTimes[i] = algo.timeElapsed;
            System.out.println("run: " + runTimes[i]);
        }

        System.out.println("\n\nAverage execution time in nanoseconds: " + average/numRun);
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
            } else {
                fw.write("Average time(s) running " + numRun + " times: " + average / numRun / Math.pow(10, 9));
            }

            fw.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isSavePathInMem() {
        return this.savePathInMem;
    }

    public void savePathsResult() {
        this.algo.saveResults();
    }

    public void printPaths() {
        this.algo.printPaths();
    }
}
