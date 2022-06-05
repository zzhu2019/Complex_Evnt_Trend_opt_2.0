package src.Traversal.ConcurrentRunnables;

import src.Components.CompressedGraph;
import src.util.CustomDS.ArrayQueue;
import src.util.CustomDS.CustomObjStack;
import src.util.CustomDS.ShortArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;

public class ConcurrentBFSTraversalTask implements Runnable {
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


    public ConcurrentBFSTraversalTask(short anchorIdx, HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes,
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
        ArrayQueue<ShortArray> queue = new ArrayQueue<>();
        ShortArray path = new ShortArray(10);
        path.add(startAnchor);

        // The startAnchor is a valid path itself
        if(graph.endContains(startAnchor)) {
            pathNumArray[threadId]++;
            validPathsArray.get(threadId).add(path.getArray());
            return;
        }

        queue.offer(path);
        while(!queue.isEmpty()) {
            ShortArray currentPath = queue.poll();
            short pathHead = (short) currentPath.getFirst();
            short pathRear = (short) currentPath.getLast();
            if((isAnchor[pathRear] && queue.size() > 1) || graph.endContains(pathRear)) {
                if(graph.startContains(pathHead)) {
                    // for start nodes
                    anchorPathsForStartNodesRLock.lock();
                    boolean isMissingMap = !anchorPathsForStartNodes.get(pathHead).containsKey(pathRear);
                    anchorPathsForStartNodesRLock.unlock();

                    anchorPathsForMidNodesWLock.lock();
                    if(isMissingMap) {
                        anchorPathsForStartNodes.get(pathHead).put(pathRear, new CustomObjStack<>());
                    }
                    anchorPathsForStartNodes.get(pathHead).get(pathRear).push(currentPath.getArray());
                    anchorPathsForMidNodesWLock.unlock();
                } else {
                    // for other anchor nodes
                    anchorPathsForMidNodesWLock.lock();
                    anchorPathsForMidNodes.get(pathHead).push(currentPath.getArray());
                    anchorPathsForMidNodesWLock.unlock();
                }
                return;
            }

            for(int i = graph.rowIndex[startAnchor]; i < graph.rowIndex[startAnchor + 1]; i++) {
                short neighbour = (short) graph.colIndex[i];

                ShortArray newArray = new ShortArray(currentPath);
                newArray.add(neighbour);
                queue.offer(newArray);
            }
        }
    }
}
