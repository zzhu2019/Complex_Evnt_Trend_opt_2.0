package src.Traversal.ConcurrentRunnables;

import src.Components.CompressedGraph;
import src.util.CustomDS.CustomObjStack;
import src.util.CustomDS.CustomShortStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
public class ConcurrentDFSTraversalTask implements Runnable {
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
    public ConcurrentDFSTraversalTask(short anchorIdx, HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes,
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
     * The start point for a thread, DFS-based implementation
     */
    public void run() {
        int threadId = (int) Thread.currentThread().getId()%threadNum + 1;
        System.out.println("Thread " + threadId + " starts traversal!");

        CustomShortStack stack = new CustomShortStack();
        stack.push(startAnchor);
        if(graph.getNumDegree(startAnchor) != 0) {
            recursiveUtil(stack, threadId);
        } else {
            pathNumArray[threadId]++;
            validPathsArray.get(threadId).add(stack.getAllElements());
        }

        System.out.println("Thread " + threadId + " finish traversal.");
    }

    /**
     * The recursive util function to implementing DFS
     * @param stack the current path
     */
    private void recursiveUtil(CustomShortStack stack, int threadId) {
        short bottomElement = stack.getFirstElement();
        short topElement = stack.peek();

        // No need to put this path in anchorPaths map for concatenating
        // If the path reaches an end node and its head is a start node, return as a valid path
        if(graph.startContains(bottomElement) && graph.endContains(topElement)) {
            validPathsArray.get(threadId).add(stack.getAllElements());
            pathNumArray[threadId]++;
            return;
        }

        // If the path reaches an end node or an anchor node (size > 1), save this path to anchorPaths
        if((isAnchor[topElement] && stack.size()>1) || graph.endContains(topElement)) {
            if(graph.startContains(bottomElement)) {
                // for start anchor nodes
                anchorPathsForStartNodesRLock.lock();
                boolean isMissingMap = !anchorPathsForStartNodes.get(bottomElement).containsKey(topElement);
                anchorPathsForStartNodesRLock.unlock();

                anchorPathsForStartNodesWLock.lock();
                if(!anchorPathsForStartNodes.get(bottomElement).containsKey(topElement)) {
                    anchorPathsForStartNodes.get(bottomElement).put(topElement, new CustomObjStack<>());
                }
                anchorPathsForStartNodes.get(bottomElement).get(topElement).push(stack.getAllElements());
                anchorPathsForStartNodesWLock.unlock();
            } else {
                // for other anchor nodes
                anchorPathsForMidNodesWLock.lock();
                anchorPathsForMidNodes.get(bottomElement).push(stack.getAllElements());
                anchorPathsForMidNodesWLock.unlock();
            }
            return;
        }

        // Recursion for all the vertices adjacent to this vertex
        for(int i = graph.rowIndex[topElement]; i < graph.rowIndex[topElement + 1]; i++) {
            short edge = (short) graph.colIndex[i];
            stack.push(edge);
            recursiveUtil(stack, threadId);
            stack.pop();
        }
    }
}
