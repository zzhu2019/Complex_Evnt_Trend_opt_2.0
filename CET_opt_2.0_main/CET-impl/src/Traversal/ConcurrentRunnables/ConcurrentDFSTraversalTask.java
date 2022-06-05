package src.Traversal.ConcurrentRunnables;

import src.Components.CompressedGraph;
import src.util.CustomDS.ArrayQueue;
import src.util.CustomDS.CustomObjStack;
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

    }
}
