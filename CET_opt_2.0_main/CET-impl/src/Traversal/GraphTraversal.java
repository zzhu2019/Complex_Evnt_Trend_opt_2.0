package src.Traversal;

import src.Components.CompressedGraph;

import java.io.File;
import java.io.FileWriter;
import java.sql.Time;
import java.util.*;

public abstract class GraphTraversal {
    // TODO： add get method for class attributes
    CompressedGraph graph;
    ArrayList<short[]> validPaths;
    public TraversalType traversalType;
    public long timeElapsed;

    long pathNum;
    boolean isSaveToMem;


    GraphTraversal(CompressedGraph graph, boolean isSaveToMem) {
        this.graph = graph;
        this.isSaveToMem = isSaveToMem;
        this.validPaths = new ArrayList<>();
        this.timeElapsed = 0;
        this.pathNum = 0;
    }

    /**
     * This method is for multiple runs of the same algorithm
     */
    void clearAll() {
        if(validPaths.size() > 0) validPaths.clear();
        pathNum = 0;
    }

    /**
     * Get the graph
     * @return the graph
     */
    public CompressedGraph getGraph() {
        return graph;
    }

    /**
     * The abstract method
     * @param i the start node index
     */
    public abstract void traversal(short i);


    /**
     * Execute the algorithm
     */
    public void execute() {
        clearAll();
        System.out.println("Number of start points: " + graph.getStartPointNum());
        int i = 1;

        long startTime = System.nanoTime();
        for(int start : graph.getStartPoints()) {
            System.out.println("Execute start node: " + (i++) + "/" + graph.getStartPointNum());
            System.out.println(new Time(System.currentTimeMillis()).toString() + " - start on: " + start);

            traversal((short) start);
        }
        long endTime = System.nanoTime();
        timeElapsed = endTime - startTime;
        System.out.println("path num: " + pathNum);
    }

    /**
     * Save the results without entering the algo type
     */
    public void saveResults() {
        saveResults(traversalType.toString());
    }

    /**
     * Save the results
     * @param algo: the algo type
     */
    void saveResults(String algo) {
        System.out.println("Write to file...");
        System.out.println(validPaths.size() + " paths to be written.");

        File outputFolder = new File("OutputFiles/");
        outputFolder.mkdirs();

        File outputFile = new File("OutputFiles/" + algo + "-" + "V-" + graph.getNumVertex()
                + new Date().toString().replace(':', '-') + ".txt");
        int maxLength = 0;
        try {
            outputFile.createNewFile();
            FileWriter fileWriter = new FileWriter(outputFile);
            for(short[] path : validPaths) {
                if(maxLength < path.length) maxLength = path.length;
                fileWriter.write("(" + path.length + ")");
                fileWriter.write(Arrays.toString(path) + "\n");
            }
            fileWriter.write("Longest path has " + maxLength + " nodes.");
            fileWriter.close();
        } catch (Exception e) {
            System.out.println(algo + " has error: " + e.toString());
        }
    }

    /**
     * ?
     * @param stack: ?
     * @return ?
     */
    int[] getPath(Stack<Integer> stack) {
        Enumeration enumeration = stack.elements();
        int[] path = new int[stack.size()];
        int counter = 0;
        while (enumeration.hasMoreElements()) {
            path[counter++] = (int) enumeration.nextElement();
        }
        return path;
    }

    /**
     * Print all paths
     */
    public void printPaths() {
        for(short[] singlePath : validPaths) {
            System.out.println(traversalType.toString() + ": " + Arrays.toString(singlePath));
        }
    }

    /**
     * ?
     * @param pathList: ?
     * @return ?
     */
    int[] getPath(ArrayList<Integer> pathList) {
        int[] path = new int[pathList.size()];

        for(int i = 0; i < pathList.size(); i++) {
            path[i] = pathList.get(i);
        }
        return path;
    }
}
