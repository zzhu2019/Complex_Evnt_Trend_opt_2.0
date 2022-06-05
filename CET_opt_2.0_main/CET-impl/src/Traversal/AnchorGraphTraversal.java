package src.Traversal;

import src.Components.CompressedGraph;

import src.util.CustomDS.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The anchor node algorithm extends the Class GraphTraversal
 */
public class AnchorGraphTraversal extends GraphTraversal {
    short[] anchorNodes;
    boolean[] isAnchor;
    // for in-map anchor nodes, they use it to store sub-paths
    HashMap<Short, CustomObjStack<short[]>> anchorPathsForMidNodes;
    // for start nodes which are also anchor nodes, they have a fine-grained DS to store sub-paths
    HashMap<Short, HashMap<Short, CustomObjStack<short[]>>> anchorPathsForStartNodes;
    public ConcatenateType concatenateType;

    /***
     * The constructor
     * @param graph The CSR format graph
     * @param saveToMem If save the results to a file
     * @param anchorNodes An array of anchor nodes
     * @param type The type of concatenation
     */
    public AnchorGraphTraversal(CompressedGraph graph, boolean saveToMem, short[] anchorNodes, ConcatenateType type) {
        super(graph, saveToMem);
        this.traversalType = TraversalType.Anchor;
        this.concatenateType = type;
        this.anchorNodes = anchorNodes;
        anchorPathsForMidNodes = new HashMap<>();
        anchorPathsForStartNodes = new HashMap<>();
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
        for(int start : anchorNodes) {
            if(graph.getNumVertex() > 5000) {
                System.out.println(new Time(System.currentTimeMillis()).toString() + " - start on: " + start +
                        " with degree " + graph.getNumDegree(start));
            }
            traversal((short) start);
        }

        for(short start : graph.getStartPoints()) {
            concatenate(start);
        }

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
        String fileName = String.format("%s-anchor%d",
                traversalType.toString(),
                anchorNodes.length - graph.getStartPointNum());
        saveResults(fileName);
    }

    /**
     * Use DFS traversal by default
     * @param start the start node
     */
    @Override
    public void traversal(short start) {
        CustomShortStack stack = new CustomShortStack();
        stack.push(start);
        if(graph.getNumDegree(start) != 0) {
            // If this start node's degree > 0
            DFSSubTraversal(start, stack);
        } else {
            // If it is a start point and has no neighbours, then itself is a valid path
            pathNum++;
            if(isSaveToMem) validPaths.add(stack.getAllElements());
        }
    }

    /***
     * The util function for the DFS traversal function
     * @param s The current start node
     * @param curStack The current stack
     */
    private void DFSSubTraversal(short s, CustomShortStack curStack) {
        short bottomElement = curStack.getFirstElement();
        short topElement = curStack.peek();
        // No need to put this path in anchorPaths map for concatenating
        // If the path reaches an end node and its head is a start node, return as a valid path
        if(graph.startContains(bottomElement) && graph.endContains(s)) {
            if(isSaveToMem) {
                validPaths.add(curStack.getAllElements());
            }
            pathNum++;
            return;
        }

        // If the path reaches an end node or an anchor node (size > 1), save this path to anchorPaths
        if((isAnchor[s] && curStack.size()>1) || graph.endContains(s)) {
            // the following section are for only sorting out the sub-paths for start nodes
            if(graph.startContains(bottomElement)) {
                // for start anchor nodes
                if(!anchorPathsForStartNodes.get(bottomElement).containsKey(topElement)) {
                    anchorPathsForStartNodes.get(bottomElement).put(topElement, new CustomObjStack<>());
                }
                anchorPathsForStartNodes.get(bottomElement).get(topElement).push(curStack.getAllElements());
            } else {
                // for other anchor nodes
                anchorPathsForMidNodes.get(bottomElement).push(curStack.getAllElements());
            }
            // reset the stack here
//            curStack = new CustomShortStack();
//            curStack.push(s);
            return;
        }

        // Recursion for all the vertices adjacent to this vertex
        for(int i = graph.rowIndex[s]; i < graph.rowIndex[s + 1]; i++) {
            short edge = (short) graph.colIndex[i];
            curStack.push(edge);
            DFSSubTraversal(edge, curStack);
            curStack.pop();
        }
    }

    /***
     * The concatenation section
     * @param start The start node
     */
    void concatenate(short start) {
        if(concatenateType.equals(ConcatenateType.BFS)) {
            // No pre-process for the BFS concatenation
            BFSSubConcatenate(start);
        } else if(concatenateType.equals(ConcatenateType.DFS)) {
            // For each sub-path under the start node
            HashMap<Short, CustomObjStack<short[]>> map = anchorPathsForStartNodes.get(start);

            for(Short endNode : map.keySet()) {
                boolean isFirstConcatenate = true;
                CustomObjStack<short[]> restPaths = new CustomObjStack<>();
                for(Object obj : map.get(endNode).getAllElements()) {
                    short[] startPath = (short[]) obj;

                    if(isFirstConcatenate) {
                        FixedSizeStack<short[]> stack = new FixedSizeStack<>(anchorNodes.length);
                        stack.push(startPath);
                        DFSSubConcatenate(startPath, stack, restPaths);

                        isFirstConcatenate = false;
                    }

                    for(Object object : restPaths.getAllElements()) {
                        short[] restPath = (short[]) object;
                        // merge startPath and restPath
                        // push this result to validPaths
                        if(isSaveToMem) {
                            validPaths.add(mergePaths(startPath, restPath));
                        }
                        pathNum++;
                    }
                }
            }
        }
    }

    /***
     * The util function for BFS concatenation
     * @param start The start node
     */
    void BFSSubConcatenate(short start) {
//        ArrayQueue<CustomObjStack<short[]>> queue = new ArrayQueue<>(graph.getStartPointNum());

//        queue.offer(anchorPaths.get(start));

//        while(!queue.isEmpty()) {
//            CustomObjStack<short[]> currentPath = queue.poll();
//            short[] rearElement = currentPath.lastElement();
//            if(graph.endContains(rearElement[rearElement.length - 1])) {
//                if(isSaveToMem) validPaths.add(getFullPath(currentPath));
//                pathNum++;
//            } else {
//                // New sub-paths by joining two sub-paths
//                CustomObjStack<short[]> combo = new CustomObjStack<short[]>(currentPath);
//
//                for(Object object : anchorPaths.get(rearElement[rearElement.length - 1]).getAllElements()) {
//                    combo.push((short[]) object);
//                    queue.offer(combo);
//                }
//            }
//        }
        HashMap<Short, CustomObjStack<short[]>> map = anchorPathsForStartNodes.get(start);

        for(Short endNode : map.keySet()) {
            boolean isFirstConcatenate = true;
            CustomObjStack<short[]> restPaths = new CustomObjStack<>();

            for(Object obj : map.get(endNode).getAllElements()) {
                // TODO
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
                    if(isSaveToMem) {
                        validPaths.add(mergePaths(startPath, restPath));
                    }
                    pathNum++;
                }
            }
        }

        // Add all these sub-paths under 'start' node as a path
//        for(Object path : anchorPathsForStartNodes.get(start).getAllElements()) {
//            queue.offer((short[]) path);
//        }
//
//        while(!queue.isEmpty()) {
//            // Get the first set of sub-paths from the queue
//            short[] currentPaths = queue.poll();
//            // For each sub-path
//            if(graph.endContains(currentPaths[currentPaths.length - 1])) {
//                if(isSaveToMem) validPaths.add(currentPaths);
//                pathNum++;
//                continue;
//            }
//
//            // Get all these sub-paths under the end node of the current sub-path
//            for(Object object : anchorPathsForMidNodes.get(currentPaths[currentPaths.length - 1]).getAllElements()) {
//                short[] nextPath = (short[]) object;
//                short[] newPath = new short[currentPaths.length - 1 + nextPath.length];
//
//                System.arraycopy(currentPaths, 0, newPath, 0, currentPaths.length - 1);
//                System.arraycopy(nextPath, 0, newPath, currentPaths.length - 1, nextPath.length);
//
//                queue.offer(newPath);
//            }
//        }
    }

    /***
     * The util function for BFS concatenation

     * @param s The start sub-path
     * @param curStack The current stack
     * @param restPaths The stack for storing the rest sub-paths
     */
    void DFSSubConcatenate(short[] s, FixedSizeStack<short[]> curStack, CustomObjStack<short[]> restPaths) {
        // A valid complete path
        if(graph.endContains(s[s.length - 1])) {
            restPaths.push(getRestPathSeq(curStack));
            return;
        }

        // For each sub-path under the end node of the current sub-path
        for(Object obj : anchorPathsForMidNodes.get(s[s.length - 1]).getAllElements()) {
            short[] nextAnchorPath = (short[]) obj;
            // Push the sub-path to the stack
            curStack.push(nextAnchorPath);
            // DFS the sub-path
            DFSSubConcatenate(nextAnchorPath, curStack, restPaths);
            // Pop the sub-path
            curStack.pop();
        }
    }

    /***
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

    /***
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
