package src.Traversal.ConcurrentRunnables;

import src.Components.CompressedGraph;
import src.util.CustomDS.ArrayQueue;
import src.util.CustomDS.CustomObjStack;

import java.util.ArrayList;
import java.util.HashMap;

public class ConcurrentBFSConcatenateTask implements Runnable {
    private final int threadNum;
    private final short startPathStartNode;
    private final short startPathEndNode;
    private final short[] anchorNodes;

    private final HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes;
    private final HashMap<Short, HashMap<Short, CustomObjStack<short[]>>> anchorPathsForStartNodes;
    private final CompressedGraph graph;
    private final long[] pathNumArray;
    private final ArrayList<ArrayList<short[]>> validPathsArray;

    /**
     * The constructor
     * @param startPathStartNode the start node of the task target path
     * @param startPathEndNode the end node of the task target path
     * @param anchorPathsForMidNodes a map storing sub-paths from a anchor node to another anchor/end node
     * @param anchorPathsForStartNodes a map storing sub-paths from a start node to a anchor/end node
     * @param graph a compressed graph
     * @param anchorNodes a array of anchor nodes
     * @param pathNumArray an array whose size is the number of available processors, each thread uses a separate
     *                     counter in this array to count the valid path it meet
     * @param validPathsArray an array whose size is the number of available processors, each thread uses a separate
     *                        ArrayList in this array to store the valid path it meet
     */
    public ConcurrentBFSConcatenateTask(short startPathStartNode, short startPathEndNode, HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes,
                                        HashMap<Short, HashMap<Short, CustomObjStack<short[]>>> anchorPathsForStartNodes,
                                        CompressedGraph graph, short[] anchorNodes, long[] pathNumArray,
                                        ArrayList<ArrayList<short[]>> validPathsArray, short threadNum) {
        this.threadNum = threadNum;
        this.startPathStartNode = startPathStartNode;
        this.startPathEndNode = startPathEndNode;
        this.anchorPathsForMidNodes = anchorPathsForMidNodes;
        this.anchorPathsForStartNodes = anchorPathsForStartNodes;
        this.anchorNodes = anchorNodes;
        this.graph = graph;
        this.pathNumArray = pathNumArray;
        this.validPathsArray = validPathsArray;
    }

    /**
     * The start point for a thread, BFS-based implementation
     */
    public void run() {
        int threadId = (int) Thread.currentThread().getId()%threadNum + 1;

//        System.out.println("Thread " + threadId + " starts concatenation!");

        // each thread get a start node
        HashMap<Short, CustomObjStack<short[]>> map = anchorPathsForStartNodes.get(startPathStartNode);

        boolean isFirstConcatenate = true;
        CustomObjStack<short[]> restPaths = new CustomObjStack<>();

        for(Object obj : map.get(startPathEndNode).getAllElements()) {
            short[] startPath = (short[]) obj;

            if(isFirstConcatenate) {
                ArrayQueue<short[]> queue = new ArrayQueue<>(anchorNodes.length);
                queue.offer(startPath);

                while(!queue.isEmpty()) {
                    // Get the first set of sub-paths from the queue
                    short[] currentPath = queue.poll();
                    short lastNode = currentPath[currentPath.length - 1];
                    short firstNode = currentPath[0];

                    if(graph.endContains(lastNode)) {
                        restPaths.push(currentPath);
                        continue;
                    }

                    // Get all these sub-paths under the end node of the current sub-path
                    for(Object object : anchorPathsForMidNodes.get(lastNode).getAllElements()) {
                        if(graph.startContains(firstNode)) {
                            // Ignore the first sub-path
                            queue.offer((short[]) object);
                        } else {
                            // Create the new sub-path by merging
                            short[] nextPath = (short[]) object;
                            short[] newPath = new short[currentPath.length - 1 + nextPath.length];
                            System.arraycopy(currentPath, 0, newPath, 0, currentPath.length - 1);
                            System.arraycopy(nextPath, 0, newPath, currentPath.length - 1, nextPath.length);

                            queue.offer(newPath);
                        }
                    }
                }

                isFirstConcatenate = false;
            }

            for(Object object : restPaths.getAllElements()) {
                short[] restPath = (short[]) object;
                // merge startPath and restPath
                // push this result to validPaths
                validPathsArray.get(threadId).add(mergePaths(startPath, restPath));
                pathNumArray[threadId]++;
            }
        }

//        System.out.println("Thread " + threadId + " finish concatenate.");
    }

    /**
     * The function to merge two sub-paths
     * @param pathA A sub-path
     * @param pathB A sub-path
     * @return A valid full path
     */
    short[] mergePaths(short[] pathA, short[] pathB) {
        // pathA is placed before pathB
        // will remove the duplicated node at the end of pathA and at the start of pathB
        int len1 = pathA.length, len2 = pathB.length;
        short[] res = new short[len1 + len2 - 1];
        System.arraycopy(pathA, 0, res, 0, len1-1);
        System.arraycopy(pathB, 0, res, len1-1, len2);
        return res;
    }
}
