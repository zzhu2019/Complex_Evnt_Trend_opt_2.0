package src.Traversal;

import src.Components.CompressedGraph;

import src.util.CustomDS.ArrayQueue;
import src.util.CustomDS.CustomIntStack;
import src.util.CustomDS.CustomObjStack;
import src.util.CustomDS.FixedSizeStack;

import java.sql.Time;
import java.util.*;

public class AnchorGraphTraversal extends GraphTraversal {
    int[] anchorNodes;
    boolean[] isAnchor;
    HashMap<Integer, Stack<int[]>> anchorPaths;
    public ConcatenateType concatenateType;

    public AnchorGraphTraversal(CompressedGraph graph, boolean saveToMem, int[] anchorNodes, ConcatenateType type) {
        super(graph, saveToMem);
        this.traversalType = TraversalType.Anchor;
        this.concatenateType = type;
        this.anchorNodes = anchorNodes;
        anchorPaths = new HashMap<>();
        isAnchor = new boolean[graph.getNumVertex()];
    }

    private void initMap() {
        for(Integer anchorNode : anchorNodes) {
            anchorPaths.put(anchorNode, new Stack<>());
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


    int[] getAnchorNodes(){
        return anchorNodes;
    }

    @Override
    void clearAll(){
        validPaths = new ArrayList<>();
        anchorPaths = new HashMap<>();
        initMap();
        pathNum = 0;
    }


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
        if(graph.startContains(curStack.firstElement()) && graph.endContains(s)) {
            if(isSaveToMem) {
                validPaths.add(curStack.getAllElements());
            }
            pathNum++;
            return;
        }

        // If the path reaches an end node or an anchor node, save this path to anchorPaths
        if((isAnchor[s] && curStack.size()>1) || graph.endContains(s)) {
            // FIXME: Java heap space overflows when run anchor algo in a range
            anchorPaths.get(curStack.firstElement()).push(curStack.getAllElements());
            return;
        }

        // unexpected case
        if(graph.endContains(s)) {
            System.out.println("test");
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


    void concatenate(int start){
        if(concatenateType.equals(ConcatenateType.BFS)) {
            BFSSubConcatenate(start);
        }
        else if(concatenateType.equals(ConcatenateType.DFS)) {
            // For each sub-path under the start node
            // Note that the order is NOT LIFO
            for(int[] startPath : anchorPaths.get(start)) {
                FixedSizeStack stack = new FixedSizeStack(anchorNodes.length);
                stack.push(startPath);
                DFSSubConcatenate(startPath, stack);
            }



//            for(Object obj : anchorPaths.get(start).getAllElements()) {
//                // TODO: remove cast
//                int[] startPath = (int[]) obj;
//                // max size is the num of anchor nodes
//                FixedSizeStack stack = new FixedSizeStack(anchorNodes.length);
//                stack.push(startPath);
//                DFSSubConcatenate(startPath, stack);
//            }



        }
        else {
            System.out.println("Unrecognized concatenate type!");
        }
    }


    void BFSSubConcatenate(int start) {
        ArrayQueue<Stack<int[]>> queue = new ArrayQueue<>(graph.getStartPointNum());

        // Add all these sub-paths under 'start' node as a start
        queue.offer(anchorPaths.get(start));

        while(!queue.isEmpty()) {
            // Get the first set of sub-paths from the queue
            Stack<int[]> currentPaths = queue.poll();
            // For each sub-path
            for(int[] subPath : currentPaths) {
                if(graph.endContains(subPath[subPath.length - 1])) {
                    if(isSaveToMem) validPaths.add(subPath);
                    pathNum++;
                    continue;
                }

                // New sub-paths by joining two sub-paths
                Stack<int[]> combo = new Stack<>();

                // Get all these sub-paths under the end node of the current sub-path
                for(int[] nextList : anchorPaths.get(subPath[subPath.length - 1])) {
                    // FIXME: Java heap space overflows because memory use increases exponentially
                    int[] newPath = new int[subPath.length - 1 + nextList.length];

                    System.arraycopy(subPath, 0, newPath, 0, subPath.length - 1);
                    System.arraycopy(nextList, 0, newPath, subPath.length - 1, nextList.length);

                    combo.push(newPath);
                }
                queue.offer(combo);
            }
        }
    }


    void DFSSubConcatenate(int[] s, FixedSizeStack curStack) {
        // A valid complete path
        if(graph.endContains(s[s.length - 1])) {
            if(isSaveToMem) validPaths.add(getPathSeq(curStack));
            pathNum++;
            return;
        }

        // For each sub-path under the end node of the current sub-path
        for(int[] nextAnchorPath : anchorPaths.get(s[s.length - 1])) {
            // Push the sub-path to the stack
            curStack.push(nextAnchorPath);
            // DFS the sub-path
            DFSSubConcatenate(nextAnchorPath, curStack);
            // Pop the sub-path
            curStack.pop();
        }
    }

    // TODO: need to be removed
    void DFSSubConcatenate(int[] s, CustomObjStack<int[]> curStack) {
        // A valid complete path
        if(graph.endContains(s[s.length - 1])) {
            if(isSaveToMem) validPaths.add(getPathSeq(curStack));
            pathNum++;
            return;
        }

        // For each sub-path under the end node of the current sub-path
        for(int[] nextAnchorPath : anchorPaths.get(s[s.length - 1])) {
            // Push the sub-path to the stack
            curStack.push(nextAnchorPath);
            // DFS the sub-path
            DFSSubConcatenate(nextAnchorPath, curStack);
            // Pop the sub-path
            curStack.pop();
        }
    }


    int[] getPathSeq(FixedSizeStack stack) {
        // TODO: possible optimization here
        int length = 1;
//        int counter = 0;

        Iterator<int[]> itr = stack.getIterator();
        while(itr.hasNext()) {
            int[] s = itr.next();
            length += s.length - 1;
        }


//        for(Object object : stack.getAllElements()) {
//            int[] s = (int[]) object;
//            if(counter < stack.size() - 1){
//                length += s.length - 1;
//                counter ++;
//            }
//            else {
//                length += s.length;
//            }
//        }

        int[] pathArray = new int[length];


        length = 0;
        int counter = 0;
        itr = stack.getIterator();
        while(itr.hasNext()) {
            int[] s = itr.next();
            if(counter < stack.size() - 1) {
                System.arraycopy(s, 0, pathArray, length, s.length - 1);
                length += s.length - 1;
            }
            else {
                System.arraycopy(s, 0, pathArray, length, s.length);
                length += s.length;
            }
            counter++;
        }

//        counter = 0;
//        for(Object obj : stack.getAllElements()) {
//            int[] s = (int[]) obj;
//            if(counter < stack.size() - 1) {
//                System.arraycopy(s, 0, pathArray, length, s.length - 1);
//                length += s.length - 1;
//            }
//            else {
//                System.arraycopy(s, 0, pathArray, length, s.length);
//                length += s.length;
//            }
//            counter++;
//        }

        return pathArray;
    }

    int[] getPathSeq(Stack<int[]> stack) {
        int length = 1;
//        int counter = 0;

        Iterator<int[]> itr = stack.iterator();
        while(itr.hasNext()) {
            int[] s = itr.next();
            length += s.length - 1;
        }


//        for(Object object : stack.getAllElements()) {
//            int[] s = (int[]) object;
//            if(counter < stack.size() - 1){
//                length += s.length - 1;
//                counter ++;
//            }
//            else {
//                length += s.length;
//            }
//        }

        int[] pathArray = new int[length];


        length = 0;
        int counter = 0;
        itr = stack.iterator();
        while(itr.hasNext()) {
            int[] s = itr.next();
            if(counter < stack.size() - 1) {
                System.arraycopy(s, 0, pathArray, length, s.length - 1);
                length += s.length - 1;
            }
            else {
                System.arraycopy(s, 0, pathArray, length, s.length);
                length += s.length;
            }
            counter++;
        }

//        counter = 0;
//        for(Object obj : stack.getAllElements()) {
//            int[] s = (int[]) obj;
//            if(counter < stack.size() - 1) {
//                System.arraycopy(s, 0, pathArray, length, s.length - 1);
//                length += s.length - 1;
//            }
//            else {
//                System.arraycopy(s, 0, pathArray, length, s.length);
//                length += s.length;
//            }
//            counter++;
//        }

        return pathArray;
    }

    // TODO: need to be removed
    int[] getPathSeq(CustomObjStack<int[]> stack) {
        // TODO: possible optimization here
        int length = 0;
        int counter = 0;

        for(Object object : stack.getAllElements()) {
            int[] s = (int[]) object;
            if(counter < stack.size() - 1){
                length += s.length - 1;
                counter ++;
            }
            else {
                length += s.length;
            }
        }

        int[] pathArray = new int[length];

        length = 0;
        counter = 0;
        for(Object obj : stack.getAllElements()) {
            int[] s = (int[]) obj;
            if(counter < stack.size() - 1) {
                System.arraycopy(s, 0, pathArray, length, s.length - 1);
                length += s.length - 1;
            }
            else {
                System.arraycopy(s, 0, pathArray, length, s.length);
                length += s.length;
            }
            counter++;
        }

        return pathArray;
    }
}
