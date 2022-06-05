package src.Traversal;

import src.Components.CompressedGraph;
import src.util.CustomDS.CustomObjStack;
import src.Traversal.ConcurrentRunnables.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConcurrentAnchorGraphTraversal extends GraphTraversal {
    short[] anchorNodes;
    boolean[] isAnchor;
    HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes;
    HashMap<Short, HashMap<Short, CustomObjStack<short[]>>> anchorPathsForStartNodes;
    public ConcatenateType concatenateType;
    public TraversalType traversalType;
    private static final int STACK_NUM = 10;
    private long[] pathNumArray;
    private ArrayList<ArrayList<short[]>> validPathsArray;
    final short optimalThreadNum = (short) Runtime.getRuntime().availableProcessors();
    // default is using the non-fair mode
    private ReadWriteLock anchorPathsForStartNodesLock = new ReentrantReadWriteLock();
    private ReadWriteLock anchorPathsForMidNodesLock = new ReentrantReadWriteLock();
    Lock anchorPathsForStartNodesWLock = anchorPathsForStartNodesLock.writeLock();
    Lock anchorPathsForStartNodesRLock = anchorPathsForStartNodesLock.readLock();
    Lock anchorPathsForMidNodesWLock = anchorPathsForMidNodesLock.writeLock();

    /**
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
//        anchorPathsForStartNodes = new HashMap<>();
//        anchorPathsForMidNodes = new HashMap<>();
        isAnchor = new boolean[graph.getNumVertex()];
//        pathNumArray = new long[optimalThreadNum];
        validPathsArray = new ArrayList<ArrayList<short[]>>(optimalThreadNum);
    }

    /**
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

        for(int i = 0; i < validPathsArray.size(); ++i) {
            validPathsArray.add(new ArrayList<short[]>());
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

    /**
     * Set the anchorNodes variable
     * @param anchorNodes An array of anchor nodes
     */
    public void setAnchorNodes(short[] anchorNodes) {
        this.anchorNodes = anchorNodes;
        initAnchorBool();
    }

    /**
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
        pathNumArray = new long[optimalThreadNum];
        validPathsArray = new ArrayList<ArrayList<short[]>>(optimalThreadNum);

        initMap();
        pathNum = 0;
    }

    /**
     * Execute the algorithm
     */
    @Override
    public void execute() {
        clearAll();

        System.out.println("Number of start points: " + graph.getStartPointNum());
        System.out.println("Number of anchor points: " + anchorNodes.length);
        long startTime = System.nanoTime();
        short initialThreadNum = (short) Runtime.getRuntime().availableProcessors();
        // the traversal
        traversal(initialThreadNum);
        // the concatenation
        concatenate(initialThreadNum);
        long endTime = System.nanoTime();
        timeElapsed = endTime - startTime;
        System.out.println(new Time(System.currentTimeMillis()).toString() + " - finished sub concatenate!");
        System.out.println("path num: " + pathNum);
    }

    /**
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
     * The concurrent traversal function
     * @param initialThreadNum the number of threads
     */
    public void traversal(short initialThreadNum) {
        if(traversalType.equals(TraversalType.DFS)) {
            DFSTraversal(initialThreadNum);
        } else if(traversalType.equals(TraversalType.BFS)) {
            BFSTraversal(initialThreadNum);
        } else {
            DFSTraversal(initialThreadNum);
        }
    }

    /**
     * The concurrent BFS traversal
     * @param initialThreadNum the number of thread for the thread pool
     */
    void BFSTraversal(short initialThreadNum) {
        ExecutorService executor = Executors.newFixedThreadPool(initialThreadNum);
        for(int start : anchorNodes) {
            Runnable worker = new ConcurrentBFSTraversalTask((short) start, anchorPathsForMidNodes,
                    anchorPathsForStartNodes, graph, isAnchor, validPaths, pathNumArray, validPathsArray,
                    anchorPathsForStartNodesWLock, anchorPathsForStartNodesRLock, anchorPathsForMidNodesWLock);
            executor.execute(worker);
        }
        executor.shutdown();
    }

    /**
     * The concurrent DFS traversal
     * @param initialThreadNum the number of thread for the thread pool
     */
    void DFSTraversal(short initialThreadNum) {
        ExecutorService executor = Executors.newFixedThreadPool(initialThreadNum);
        for(int start : anchorNodes) {
            Runnable worker = new ConcurrentDFSTraversalTask((short) start, anchorPathsForMidNodes,
                    anchorPathsForStartNodes, graph, isAnchor, validPaths, pathNumArray, validPathsArray,
                    anchorPathsForStartNodesWLock, anchorPathsForStartNodesRLock, anchorPathsForMidNodesWLock);
            executor.execute(worker);
        }
        executor.shutdown();
    }

    /**
     * The concurrent concatenation section
     * @param initialThreadNum the number of threads
     */
    void concatenate(short initialThreadNum) {
        if(traversalType.equals(TraversalType.DFS)) {
            DFSConcatenate(initialThreadNum);
        } else if(traversalType.equals(TraversalType.BFS)) {
            BFSConcatenate(initialThreadNum);
        } else {
            DFSConcatenate(initialThreadNum);
        }
    }

    /**
     * The concurrent BFS concatenate
     * @param initialThreadNum the number of thread for the thread pool
     */
    void BFSConcatenate(short initialThreadNum) {
        ExecutorService executor = Executors.newFixedThreadPool(initialThreadNum);
        executor.shutdown();
    }

    /**
     * The concurrent DFS concatenate
     * @param initialThreadNum the number of thread for the thread pool
     */
    void DFSConcatenate(short initialThreadNum) {
        ExecutorService executor = Executors.newFixedThreadPool(initialThreadNum);
        executor.shutdown();
    }
}
