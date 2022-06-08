package src.Traversal.ConcurrentRunnables;

import src.Components.CompressedGraph;
import src.util.CustomDS.CustomObjStack;
import src.util.CustomDS.FixedSizeStack;

import java.util.ArrayList;
import java.util.HashMap;

public class ConcurrentDFSConcatenateTask implements Runnable {
    private final int threadId;
    private final short startNode;
    private final short[] anchorNodes;

    private final HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes;
    private final HashMap<Short, HashMap<Short, CustomObjStack<short[]>>> anchorPathsForStartNodes;
    private final CompressedGraph graph;
    private final long[] pathNumArray;
    private final ArrayList<ArrayList<short[]>> validPathsArray;

    /**
     * The constructor
     * @param startAnchor the start anchor idx
     * @param anchorPathsForMidNodes a map storing sub-paths from a anchor node to another anchor/end node
     * @param anchorPathsForStartNodes a map storing sub-paths from a start node to a anchor/end node
     * @param graph a compressed graph
     * @param anchorNodes a array of anchor nodes
     * @param pathNumArray an array whose size is the number of available processors, each thread uses a separate
     *                     counter in this array to count the valid path it meet
     * @param validPathsArray an array whose size is the number of available processors, each thread uses a separate
     *                        ArrayList in this array to store the valid path it meet
     */
    public ConcurrentDFSConcatenateTask(short startAnchor, HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes,
                                        HashMap<Short, HashMap<Short, CustomObjStack<short[]>>> anchorPathsForStartNodes,
                                        CompressedGraph graph, short[] anchorNodes, long[] pathNumArray,
                                        ArrayList<ArrayList<short[]>> validPathsArray, short threadNum) {
        this.threadId = (int) Thread.currentThread().getId()%threadNum + 1;
        this.startNode = startAnchor;
        this.anchorPathsForMidNodes = anchorPathsForMidNodes;
        this.anchorPathsForStartNodes = anchorPathsForStartNodes;
        this.anchorNodes = anchorNodes;
        this.graph = graph;
        this.pathNumArray = pathNumArray;
        this.validPathsArray = validPathsArray;
    }

    /**
     * The start point for a thread, DFS-based implementation
     */
    public void run() {
        System.out.println("Thread " + threadId + " starts concatenation!");

        // For each sub-path under the start node
        HashMap<Short, CustomObjStack<short[]>> map = anchorPathsForStartNodes.get(startNode);

        for(Short endNode : map.keySet()) {
            boolean isFirstConcatenate = true;
            CustomObjStack<short[]> restPaths = new CustomObjStack<>();
            for(Object obj : map.get(endNode).getAllElements()) {
                short[] startPath = (short[]) obj;

                if(isFirstConcatenate) {
                    FixedSizeStack<short[]> stack = new FixedSizeStack<>(anchorNodes.length);
                    stack.push(startPath);
                    recursiveUtil(startPath, stack, restPaths);

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
        }

        System.out.println("Thread " + threadId + " finish concatenate.");
    }

    /**
     * The recursive util function to implementing DFS
     * @param startPath the current sub-path
     * @param curStack the sub-path stack for storing all sub-paths in order
     * @param restPaths storing the valid paths exclude the first start node's sub-path
     */
    public void recursiveUtil(short[] startPath, FixedSizeStack<short[]> curStack, CustomObjStack<short[]> restPaths) {
        // A valid complete path
        if(graph.endContains(startPath[startPath.length - 1])) {
            restPaths.push(getRestPathSeq(curStack));
            return;
        }

        // For each sub-path under the end node of the current sub-path
        for(Object obj : anchorPathsForMidNodes.get(startPath[startPath.length - 1]).getAllElements()) {
            short[] nextAnchorPath = (short[]) obj;
            // Push the sub-path to the stack
            curStack.push(nextAnchorPath);
            // DFS the sub-path
            recursiveUtil(nextAnchorPath, curStack, restPaths);
            // Pop the sub-path
            curStack.pop();
        }
    }

    /**
     * Get the valid full path from the sub-path stack, the first sub-path will be ignored
     * @param stack The sub-path stack
     * @return A valid full path as an short array
     */
    short[] getRestPathSeq(FixedSizeStack<short[]> stack) {
        // ignore the first element in the stack
        int length = 0;
        Object[] subPaths = stack.getAllElements();
        for(int i=1; i<subPaths.length; ++i) {
            short[] s = (short[]) subPaths[i];
            if(i < stack.size() - 1) {
                length += s.length - 1;
            } else {
                length += s.length;
            }
        }

        short[] path = new short[length];

        length = 0;
        for(int i=1; i<subPaths.length; ++i) {
            short[] s = (short[]) subPaths[i];
            if(i < stack.size() - 1) {
                System.arraycopy(s, 0, path, length, s.length - 1);
                length += s.length - 1;
            } else {
                System.arraycopy(s, 0, path, length, s.length);
                length += s.length;
            }
        }

        return path;
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
