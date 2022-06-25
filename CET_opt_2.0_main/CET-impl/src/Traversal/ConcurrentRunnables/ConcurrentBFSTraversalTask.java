package src.Traversal.ConcurrentRunnables;

import src.Components.CompressedGraph;
import src.util.CustomDS.ArrayQueue;
import src.util.CustomDS.CustomObjStack;
import src.util.CustomDS.ShortArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;

public class ConcurrentBFSTraversalTask implements Runnable {
    private final int threadNum;
    private final short startAnchor;
    private final boolean[] isAnchor;

    private final HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes;
    private final HashMap<Short, HashMap<Short, CustomObjStack<short[]>>> anchorPathsForStartNodes;
    private final CompressedGraph graph;
    private final long[] pathNumArray;
    private final ArrayList<ArrayList<short[]>> validPathsArray;
    private final Lock anchorPathsForStartNodesWLock;
    private final Lock anchorPathsForStartNodesRLock;
    private final Lock anchorPathsForMidNodesWLock;

    /**
     * The constructor
     * @param anchorIdx the start anchor idx
     * @param anchorPathsForMidNodes a map storing sub-paths from a anchor node to another anchor/end node
     * @param anchorPathsForStartNodes a map storing sub-paths from a start node to a anchor/end node
     * @param graph a compressed graph
     * @param isAnchor a boolean array storing a flag for each node determine if a node is a anchor node
     * @param pathNumArray an array whose size is the number of available processors, each thread uses a separate
     *                     counter in this array to count the valid path it meet
     * @param validPathsArray an array whose size is the number of available processors, each thread uses a separate
     *      *                     ArrayList in this array to store the valid path it meet
     * @param anchorPathsForStartNodesWLock A ReentrantWriteLock for accessing the hashmap "anchorPathsForStartNodes"
     * @param anchorPathsForStartNodesRLock A ReentrantReadLock for accessing the hashmap "anchorPathsForStartNodes"
     * @param anchorPathsForMidNodesWLock A ReentrantWriteLock for accessing the hashmap "anchorPathsForMidNodes"
     */
    public ConcurrentBFSTraversalTask(short anchorIdx, HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes,
                                      HashMap<Short, HashMap<Short, CustomObjStack<short[]>>> anchorPathsForStartNodes,
                                      CompressedGraph graph, boolean[] isAnchor, long[] pathNumArray,
                                      ArrayList<ArrayList<short[]>> validPathsArray, Lock anchorPathsForStartNodesWLock,
                                      Lock anchorPathsForStartNodesRLock, Lock anchorPathsForMidNodesWLock, short threadNum) {
        this.threadNum = threadNum;
        this.startAnchor = anchorIdx;
        this.anchorPathsForMidNodes = anchorPathsForMidNodes;
        this.anchorPathsForStartNodes = anchorPathsForStartNodes;
        this.graph = graph;
        this.isAnchor = isAnchor;
        this.pathNumArray = pathNumArray;
        this.validPathsArray = validPathsArray;
        this.anchorPathsForStartNodesWLock = anchorPathsForStartNodesWLock;
        this.anchorPathsForStartNodesRLock = anchorPathsForStartNodesRLock;
        this.anchorPathsForMidNodesWLock = anchorPathsForMidNodesWLock;
    }

    /**
     * The start point for a thread, BFS-based implementation
     */
    public void run() {
        int threadId = (int) Thread.currentThread().getId()%threadNum + 1;
        System.out.println("Thread " + threadId + " starts traversal!");

        ShortArray path = new ShortArray(10);
        path.add(startAnchor);

        // The startAnchor is a valid path itself
        if(graph.startContains(startAnchor) && graph.endContains(startAnchor)) {
            pathNumArray[threadId]++;
            validPathsArray.get(threadId).add(path.getArray());
            System.out.println("Thread " + threadId + " finish traversal.");
            return;
        }

        ArrayQueue<ShortArray> queue = new ArrayQueue<>();
        queue.offer(path);
        while(!queue.isEmpty()) {
            ShortArray currentPath = queue.poll();
            short pathHead = (short) currentPath.getFirst();
            short pathRear = (short) currentPath.getLast();

            if(graph.startContains(pathHead) && graph.endContains(pathRear)) {
                validPathsArray.get(threadId).add(currentPath.getArray());
                pathNumArray[threadId]++;
                continue;
            } else if((isAnchor[pathRear] && currentPath.size() > 1) || graph.endContains(pathRear)) {
                if(graph.startContains(pathHead)) {
                    // for start nodes
                    anchorPathsForStartNodesRLock.lock();
                    boolean isMissingMap = !anchorPathsForStartNodes.get(pathHead).containsKey(pathRear);
                    anchorPathsForStartNodesRLock.unlock();

                    anchorPathsForStartNodesWLock.lock();
                    if(isMissingMap) {
                        anchorPathsForStartNodes.get(pathHead).put(pathRear, new CustomObjStack<>());
                    }
                    anchorPathsForStartNodes.get(pathHead).get(pathRear).push(currentPath.getArray());
                    anchorPathsForStartNodesWLock.unlock();
                } else {
                    // for other anchor nodes
                    anchorPathsForMidNodesWLock.lock();
                    anchorPathsForMidNodes.get(pathHead).push(currentPath.getArray());
                    anchorPathsForMidNodesWLock.unlock();
                }
                continue;
            }

            for(int i = graph.rowIndex[pathRear]; i < graph.rowIndex[pathRear + 1]; i++) {
                short neighbour = (short) graph.colIndex[i];

                ShortArray newArray = new ShortArray(currentPath);
                newArray.add(neighbour);
                queue.offer(newArray);
            }
        }

        System.out.println("Thread " + threadId + " finish traversal.");
    }
}
