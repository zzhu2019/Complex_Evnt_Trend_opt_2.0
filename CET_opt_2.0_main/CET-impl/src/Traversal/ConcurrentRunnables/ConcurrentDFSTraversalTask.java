package src.Traversal.ConcurrentRunnables;

import src.Components.CompressedGraph;
import src.util.CustomDS.ArrayQueue;
import src.util.CustomDS.CustomObjStack;
import src.util.CustomDS.CustomShortStack;
import src.util.CustomDS.ShortArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
public class ConcurrentDFSTraversalTask implements Runnable {
    private int threadId = (int) Thread.currentThread().getId();
    private short startAnchor;
    private boolean[] isAnchor;

    private HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes;
    private HashMap<Short, HashMap<Short, CustomObjStack<short[]>>> anchorPathsForStartNodes;
    private CompressedGraph graph;
    private ArrayList<short[]> validPaths;
    private long[] pathNumArray;
    private ArrayList<ArrayList<short[]>> validPathsArray;
    private Lock anchorPathsForStartNodesWLock;
    private Lock anchorPathsForStartNodesRLock;
    private Lock anchorPathsForMidNodesWLock;


    public ConcurrentDFSTraversalTask(short anchorIdx, HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes,
                                      HashMap<Short, HashMap<Short, CustomObjStack<short[]>>> anchorPathsForStartNodes,
                                      CompressedGraph graph, boolean[] isAnchor, ArrayList<short[]> validPaths,
                                      long[] pathNumArray, ArrayList<ArrayList<short[]>> validPathsArray,
                                      Lock anchorPathsForStartNodesWLock, Lock anchorPathsForStartNodesRLock,
                                      Lock anchorPathsForMidNodesWLock) {
        this.startAnchor = anchorIdx;
        this.anchorPathsForMidNodes = anchorPathsForMidNodes;
        this. anchorPathsForStartNodes = anchorPathsForStartNodes;
        this.graph = graph;
        this.isAnchor = isAnchor;
        this.validPaths = validPaths;
        this.pathNumArray = pathNumArray;
        this.validPathsArray = validPathsArray;
        this.anchorPathsForStartNodesWLock = anchorPathsForStartNodesWLock;
        this.anchorPathsForStartNodesRLock = anchorPathsForStartNodesRLock;
        this.anchorPathsForMidNodesWLock = anchorPathsForMidNodesWLock;
    }

    public void run() {
        CustomShortStack stack = new CustomShortStack();
        stack.push(startAnchor);
        if(graph.getNumDegree(startAnchor) != 0) {
            recursiveUtil(stack);
        } else {
            pathNumArray[threadId]++;
            validPathsArray.get(threadId).add(stack.getAllElements());
        }
    }

    private void recursiveUtil(CustomShortStack stack) {
        short bottomElement = stack.getFirstElement();
        short topElement = stack.peek();

        // No need to put this path in anchorPaths map for concatenating
        // If the path reaches an end node and its head is a start node, return as a valid path
        if(graph.startContains(bottomElement) && graph.endContains(topElement)) {
            validPaths.add(stack.getAllElements());
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

                anchorPathsForMidNodesWLock.lock();
                if(!anchorPathsForStartNodes.get(bottomElement).containsKey(topElement)) {
                    anchorPathsForStartNodes.get(bottomElement).put(topElement, new CustomObjStack<>());
                }
                anchorPathsForStartNodes.get(bottomElement).get(topElement).push(stack.getAllElements());
                anchorPathsForMidNodesWLock.unlock();
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
            recursiveUtil(stack);
            stack.pop();
        }
    }
}
