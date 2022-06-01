package src.Traversal;

import src.Components.CompressedGraph;
import src.util.CustomDS.CustomObjStack;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ConcurrentAnchorGraphTraversal extends GraphTraversal {
    short[] anchorNodes;
    boolean[] isAnchor;
    HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes;
    HashMap<Short, HashMap<Short, CustomObjStack<short[]>>> anchorPathsForStartNodes;
    public ConcatenateType concatenateType;
    public TraversalType traversalType;
    private static final int STACK_NUM = 10;

    /***
     * The constructor
     * @param graph The CSR format graph
     * @param saveToMem If save the results to a file
     * @param anchorNodes An array of anchor nodes
     * @param ttype The type of traversal
     * @param ctype the type of concatenation
     */
    public ConcurrentAnchorGraphTraversal(CompressedGraph graph, boolean saveToMem, short[] anchorNodes,
                                          TraversalType ttype,ConcatenateType ctype) {
        super(graph, saveToMem);
        this.traversalType = ttype;
        this.concatenateType = ctype;
        this.anchorNodes = anchorNodes;
        anchorPathsForStartNodes = new HashMap<>();
        anchorPathsForMidNodes = new HashMap<>();
        isAnchor = new boolean[graph.getNumVertex()];
    }

    /***
     * Init maps used in this algorithm in case of multiple runs
     */
    private void initMap() {
        for(Short anchorNode : anchorNodes) {
            if(graph.startContains(anchorNode)) {
                anchorPathsForStartNodes.put(anchorNode, new HashMap<Short, CustomObjStack<short[]>>());
            } else {
                anchorPathsForMidNodes.put(anchorNode, new CustomObjStack<short[]>());
            }
        }
    }

    /**
     * Initiate a boolean list, true for anchor node.
     */
    private void initAnchorBool() {
        Arrays.fill(isAnchor, false);
        for(int i : anchorNodes) {
            isAnchor[i] = true;
        }
    }

    /***
     * Set the anchorNodes variable
     * @param anchorNodes An array of anchor nodes
     */
    public void setAnchorNodes(short[] anchorNodes) {
        this.anchorNodes = anchorNodes;
        initAnchorBool();
    }

    /***
     * Return an array of anchor nodes
     * @return An array of anchor nodes
     */
    short[] getAnchorNodes() {
        return anchorNodes;
    }

    /***
     * Reset the data structures by removing all references
     */
    @Override
    void clearAll() {
        validPaths = new ArrayList<>();
        anchorPathsForMidNodes = new HashMap<>();
        anchorPathsForStartNodes = new HashMap<>();
        initMap();
        pathNum = 0;
    }

    /***
     * Execute the algorithm
     */
    @Override
    public void execute() {
        clearAll();

        System.out.println("Number of start points: " + graph.getStartPointNum());
        System.out.println("Number of anchor points: " + anchorNodes.length);
        long startTime = System.nanoTime();
        // TODO: traversal

        // TODO: concatenate

        long endTime = System.nanoTime();
        timeElapsed = endTime - startTime;
        System.out.println(new Time(System.currentTimeMillis()).toString() + " - finished sub concatenate!");
        System.out.println("path num: " + pathNum);
    }

    /***
     * Save the results into a file
     */
    @Override
    public void saveResults() {
        String fileName = String.format("%s-%s-concurrent-anchor%d",
                traversalType.toString(),
                concatenateType.toString(),
                anchorNodes.length - graph.getStartPointNum());
        saveResults(fileName);
    }

    /**
     * Use DFS traversal by default
     * @param start the start node
     */
    @Override
    public void traversal(short start) {
        //TODO: concurrent traversal
    }

    /***
     * The concatenation section
     * @param start The start node
     */
    void concatenate(short start) {}
}
