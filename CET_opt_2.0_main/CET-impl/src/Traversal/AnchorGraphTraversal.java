package src.Traversal;

import src.Components.CompressedGraph;

import src.util.CustomDS.ArrayQueue;
import src.util.CustomDS.CustomIntStack;
import src.util.CustomDS.CustomObjStack;
import src.util.CustomDS.FixedSizeStack;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AnchorGraphTraversal extends GraphTraversal {
    int[] anchorNodes;
    boolean[] isAnchor;
    // for in-map anchor nodes, they use it to store sub-paths
    HashMap<Integer, CustomObjStack<int[]>> anchorPaths;
    // for start nodes which are also anchor nodes, they have a fine-grained DS to store sub-paths
    HashMap<Integer, HashMap<Integer, CustomObjStack<int[]>>> anchorPathsForStartNodes;
    public ConcatenateType concatenateType;

    public AnchorGraphTraversal(CompressedGraph graph, boolean saveToMem, int[] anchorNodes, ConcatenateType type) {
        super(graph, saveToMem);
        this.traversalType = TraversalType.Anchor;
        this.concatenateType = type;
        this.anchorNodes = anchorNodes;
        anchorPaths = new HashMap<>();
        anchorPathsForStartNodes = new HashMap<>();
        isAnchor = new boolean[graph.getNumVertex()];
    }

    private void initMap() {
        for(Integer anchorNode : anchorNodes) {
            if(graph.startContains(anchorNode)) {
                anchorPathsForStartNodes.put(anchorNode, new HashMap<Integer, CustomObjStack<int[]>>());
            }
            else {
                anchorPaths.put(anchorNode, new CustomObjStack<int[]>());
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


    public void setAnchorNodes(int[] anchorNodes) {
        this.anchorNodes = anchorNodes;
        initAnchorBool();
    }


    int[] getAnchorNodes() {
        return anchorNodes;
    }

    @Override
    void clearAll() {
        validPaths = new ArrayList<>();
        anchorPaths = new HashMap<>();
        anchorPathsForStartNodes = new HashMap<>();
        initMap();
        pathNum = 0;
    }


    @Override
    public void execute() {
        // TODO: possible optimization here to remove clearAll()
        clearAll();

        System.out.println("Number of start points: " + graph.getStartPointNum());
        System.out.println("Number of anchor points: " + anchorNodes.length);
        long startTime = System.nanoTime();
        for(int start : anchorNodes) {
            if(graph.getNumVertex() > 5000) {
                System.out.println(new Time(System.currentTimeMillis()).toString() + " - start on: " + start +
                        " with degree " + graph.getNumDegree(start));
            }
            traversal(start);
        }

        for(int start : graph.getStartPoints()) {
            concatenate(start);
        }

        long endTime = System.nanoTime();
        timeElapsed = endTime - startTime;
        System.out.println(new Time(System.currentTimeMillis()).toString() + " - finished sub concatenate!");
        System.out.println("path num: " + pathNum);
    }

    @Override
    public void saveResults() {
        String fileName = String.format("%s-anchor%d",
                traversalType.toString(),
                anchorNodes.length - graph.getStartPointNum());
        saveResults(fileName);
    }

    /**
     * Use DFS traversal by default
     * @param start: the start node
     */
    @Override
    public void traversal(int start) {
//        System.out.println("start node "+start);
        CustomIntStack stack = new CustomIntStack();
        stack.push(start);
        // If this start node's degree > 0
        if(graph.getNumDegree(start) != 0) {
            DFSSubTraversal(start, stack);
        }
        // If it is a start point and has no neighbours, then it is a path itself
        else if(graph.startContains(start)) {
            if(isSaveToMem) {
                validPaths.add(stack.getAllElements());
            }
            pathNum++;
        }
        // else end nodes
    }

    private void DFSSubTraversal(int s, CustomIntStack curStack) {
        // No need to put this path in anchorPaths map for concatenating
        // If the path reaches an end node and its head is a start node, return as a valid path
        if(graph.startContains(curStack.getFirstElement()) && graph.endContains(s)) {
            if(isSaveToMem) {
                validPaths.add(curStack.getAllElements());
            }
            pathNum++;
            return;
        }

        // If the path reaches an end node or an anchor node, save this path to anchorPaths
        if((isAnchor[s] && curStack.size()>1) || graph.endContains(s)) {
            // FIXME: Java heap space overflows when run anchor algo in a range

            if(graph.startContains(curStack.getFirstElement())) {
                // for start anchor nodes
                anchorPathsForStartNodes.get(curStack.getFirstElement()).
                        putIfAbsent(curStack.peek(), new CustomObjStack<int[]>());
                anchorPathsForStartNodes.get(curStack.getFirstElement()).
                        get(curStack.peek()).push(curStack.getAllElements());
            }
            else {
                // for other anchor nodes
                anchorPaths.get(curStack.getFirstElement()).push(curStack.getAllElements());
            }
            return;
        }

        // unexpected case
        if(graph.endContains(s)) {
            System.out.println("A unexpected case here");
            return;
        }

        // Recursion for all the vertices adjacent to this vertex
        for(int i = graph.rowIndex[s]; i < graph.rowIndex[s + 1]; i++) {
            int edge = graph.colIndex[i];
            curStack.push(edge);
            DFSSubTraversal(edge, curStack);
            curStack.pop();
        }
    }


    void concatenate(int start) {
        if(concatenateType.equals(ConcatenateType.BFS)) {
            BFSSubConcatenate(start);
        }
        else if(concatenateType.equals(ConcatenateType.DFS)) {
            // For each sub-path under the start node
            HashMap<Integer, CustomObjStack<int[]>> map = anchorPathsForStartNodes.get(start);

            for(Integer endNode : map.keySet()) {
                boolean isFirstConcatenate = true;
                CustomObjStack<int[]> restPaths = new CustomObjStack<>();
                for(Object obj : map.get(endNode).getAllElements()) {
                    int[] startPath = (int[]) obj;

                    if(isFirstConcatenate) {
                        FixedSizeStack<int[]> stack = new FixedSizeStack<>(anchorNodes.length);
                        stack.push(startPath);
                        DFSSubConcatenate(startPath, stack, restPaths);

                        isFirstConcatenate = false;
                    }
                    for(Object object : restPaths.getAllElements()) {
                        int[] restPath = (int[]) object;
                        // merge startPath and restPath
                        // push this result to validPaths
                        validPaths.add(mergePaths(startPath, restPath));
                        pathNum++;
                    }
                }
            }
        }
        else {
            System.out.println("Unrecognized concatenate type!");
        }
    }


    void BFSSubConcatenate(int start) {
        ArrayQueue<CustomObjStack<int[]>> queue = new ArrayQueue<>(graph.getStartPointNum());

        // Add all these sub-paths under 'start' node as a start
        queue.offer(anchorPaths.get(start));

        while(!queue.isEmpty()) {
            // Get the first set of sub-paths from the queue
            CustomObjStack<int[]> currentPaths = queue.poll();
            // For each sub-path
            for(Object obj : currentPaths.getAllElements()) {
                int[] subPath = (int[]) obj;
                if(graph.endContains(subPath[subPath.length - 1])) {
                    if(isSaveToMem) validPaths.add(subPath);
                    pathNum++;
                    continue;
                }

                // New sub-paths by joining two sub-paths
                CustomObjStack<int[]> combo = new CustomObjStack<>();

                // Get all these sub-paths under the end node of the current sub-path
                for(Object object : anchorPaths.get(subPath[subPath.length - 1]).getAllElements()) {
                    int[] nextList = (int[]) object;

                    // FIXME: Java heap space overflows because memory usage increases exponentially
                    int[] newPath = new int[subPath.length - 1 + nextList.length];

                    System.arraycopy(subPath, 0, newPath, 0, subPath.length - 1);
                    System.arraycopy(nextList, 0, newPath, subPath.length - 1, nextList.length);

                    combo.push(newPath);
                }
                queue.offer(combo);
            }
        }
    }


    void DFSSubConcatenate(int[] s, FixedSizeStack<int[]> curStack, CustomObjStack<int[]> restPaths) {
        // A valid complete path
        if(graph.endContains(s[s.length - 1])) {
            if(isSaveToMem) {
                restPaths.push(getRestPathSeq(curStack));
            }
            return;
        }

        // For each sub-path under the end node of the current sub-path
        for(Object obj : anchorPaths.get(s[s.length - 1]).getAllElements()) {
            int[] nextAnchorPath = (int[]) obj;
            // Push the sub-path to the stack
            curStack.push(nextAnchorPath);
            // DFS the sub-path
            DFSSubConcatenate(nextAnchorPath, curStack, restPaths);
            // Pop the sub-path
            curStack.pop();
        }
    }


    int[] getRestPathSeq(FixedSizeStack<int[]> stack) {
        // ignore the first element in the stack
        int length = 0;
        Object[] subPaths = stack.getAllElements();
        for(int i=1; i<subPaths.length; ++i) {
            int[] s = (int[]) subPaths[i];
            if(i < stack.size() - 1){
                length += s.length - 1;
            }
            else {
                length += s.length;
            }
        }

        int[] path = new int[length];

        length = 0;
        for(int i=1; i<subPaths.length; ++i) {
            int[] s = (int[]) subPaths[i];
            if(i < stack.size() - 1) {
                System.arraycopy(s, 0, path, length, s.length - 1);
                length += s.length - 1;
            }
            else {
                System.arraycopy(s, 0, path, length, s.length);
                length += s.length;
            }
        }

        return path;
    }

    int[] mergePaths(int[] pathA, int[] pathB) {
        // pathA is placed before pathB
        // will remove the duplicated node at the end of pathA and at the start of pathB
        int len1 = pathA.length, len2 = pathB.length;
        int[] res = new int[len1 + len2 - 1];
        System.arraycopy(pathA, 0, res, 0, len1-1);
        System.arraycopy(pathB, 0, res, len1-1, len2);
        return res;
    }
}
