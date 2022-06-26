package src.Traversal;

import src.Components.CompressedGraph;
import src.util.CustomDS.CustomObjStack;
import src.Traversal.ConcurrentRunnables.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.*;
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
    private final ReadWriteLock anchorPathsForStartNodesLock = new ReentrantReadWriteLock();
    private final ReadWriteLock anchorPathsForMidNodesLock = new ReentrantReadWriteLock();
    Lock anchorPathsForStartNodesWLock = anchorPathsForStartNodesLock.writeLock();
    Lock anchorPathsForStartNodesRLock = anchorPathsForStartNodesLock.readLock();
    Lock anchorPathsForMidNodesWLock = anchorPathsForMidNodesLock.writeLock();
    ThreadPoolExecutor executor;

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
//        pathNumArray = new long[optimalThreadNum+1];
//        validPathsArray = new ArrayList<ArrayList<short[]>>(optimalThreadNum+1);
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(optimalThreadNum);
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

        for(int i = 0; i < optimalThreadNum+1; ++i) {
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
        pathNumArray = new long[optimalThreadNum+1];
        validPathsArray = new ArrayList<ArrayList<short[]>>(optimalThreadNum+1);

        initMap();
        pathNum = 0;
    }

    /**
     * Execute the algorithm
     */
    @Override
    public void execute() {
        clearAll();

        System.out.println("Number of thread: " + optimalThreadNum);
        System.out.println("Number of start points: " + graph.getStartPointNum());
        System.out.println("Number of anchor points: " + anchorNodes.length);
        long startTime = System.nanoTime();

        // the traversal
        traversal(optimalThreadNum);
        executor.shutdown();

        while(!executor.isTerminated());
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(optimalThreadNum);

        // the concatenation
        concatenate(optimalThreadNum);

        executor.shutdown();
        while(!executor.isTerminated());

//        for(int i = 0 ; i < optimalThreadNum+1; ++i) {
//            pathNum += pathNumArray[i];
//            System.out.println("Thread " + i + " with " + pathNumArray[i]);
//        }
        long endTime = System.nanoTime();
        timeElapsed = endTime - startTime;
        System.out.println(new Time(System.currentTimeMillis()).toString() + " - finished concatenate!");

        // sum up path count


        // sum up paths
        for(int i = 0 ; i < optimalThreadNum+1; ++i) {
            validPaths.addAll(validPathsArray.get(i));
        }

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
     * @param threadNum the number of threads
     */
    public void traversal(short threadNum) {
        if(traversalType.equals(TraversalType.DFS)) {
            DFSTraversal(threadNum);
        } else {
            BFSTraversal(threadNum);
        }
    }

    /**
     * The concurrent BFS traversal
     */
    void BFSTraversal(short threadNum) {
        for(short start : anchorNodes) {
            Runnable worker = new ConcurrentBFSTraversalTask(start, anchorPathsForMidNodes,
                    anchorPathsForStartNodes, graph, isAnchor, pathNumArray, validPathsArray,
                    anchorPathsForStartNodesWLock, anchorPathsForStartNodesRLock, anchorPathsForMidNodesWLock,
                    threadNum);
            executor.execute(worker);
        }
    }

    /**
     * The concurrent DFS traversal
     */
    void DFSTraversal(short threadNum) {
        for(short start : anchorNodes) {
            Runnable worker = new ConcurrentDFSTraversalTask(start, anchorPathsForMidNodes,
                    anchorPathsForStartNodes, graph, isAnchor, pathNumArray, validPathsArray,
                    anchorPathsForStartNodesWLock, anchorPathsForStartNodesRLock, anchorPathsForMidNodesWLock,
                    threadNum);
            executor.execute(worker);
        }
    }

    /**
     * The concurrent concatenation section
     */
    void concatenate(short threadNum) {
        if(traversalType.equals(TraversalType.DFS)) {
            DFSConcatenate(threadNum);
        } else {
            BFSConcatenate(threadNum);
        }
    }

    /**
     * The concurrent BFS concatenate
     */
    void BFSConcatenate(short threadNum) {
        // TODO: run sequential first and then each thread takes a start path
        for(short start : graph.getStartPoints()) {
            for(Short endNode : anchorPathsForStartNodes.get(start).keySet()) {
                Runnable worker = new ConcurrentBFSConcatenateTask(start, endNode, anchorPathsForMidNodes,
                        anchorPathsForStartNodes, graph, anchorNodes, pathNumArray, validPathsArray, threadNum);
                executor.execute(worker);
            }
        }
    }

    /**
     * The concurrent DFS concatenate
     */
    void DFSConcatenate(short threadNum) {
        for(short start : graph.getStartPoints()) {
//            System.out.println("start " + start);
            for(Short endNode : anchorPathsForStartNodes.get(start).keySet()) {
//                System.out.println("start " + start + " endNode " + endNode);
                Runnable worker = new ConcurrentDFSConcatenateTask(start, endNode, anchorPathsForMidNodes,
                        anchorPathsForStartNodes, graph, anchorNodes, pathNumArray, validPathsArray, threadNum);
                executor.execute(worker);
            }
        }
    }
}
