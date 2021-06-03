package src.Traversal;

import src.Components.CompressedGraph;
import src.util.CustomDS.ArrayQueue;
import src.util.CustomDS.CustomObjStack;

import java.sql.Time;
import java.util.Stack;


public class DoubleAnchorTraversal extends AnchorGraphTraversal {

    public ConcatenateType firstLevel;
    public ConcatenateType secondLevel;
    String reduceAnchorType;
    public DoubleAnchorTraversal(CompressedGraph graph, boolean saveToMem, int[] anchorNodes,
                                 ConcatenateType firstLevel, ConcatenateType secondLevel, String doubleType) {
        super(graph, saveToMem, anchorNodes, firstLevel);
        this.firstLevel = firstLevel;
        this.secondLevel = secondLevel;
        traversalType = TraversalType.DoubleAnchor;
        reduceAnchorType = doubleType;
    }


    @Override
    public void execute() {
        clearAll();
        System.gc();
        long startTime = System.nanoTime();
        for(int start : getAnchorNodes()) {
//            if (graph.getNumVertex() > 5000)
//                System.out.println(new Time(System.currentTimeMillis()).toString() + " - start on: " + start +
//                        " with degree " + graph.getNumDegree(start));
            traversal(start);
        }

        concatenate();
        long endTime = System.nanoTime();
        timeElapsed = endTime - startTime;
        System.out.println(new Time(System.currentTimeMillis()).toString() + " - finished double leveling - -!");
        System.out.println("path num: " + pathNum);
    }

    void reduceHalfAnchorNodes(){
        // set the smallest half to be non-anchor
        for(int i = anchorNodes.length - 1;
            i >anchorNodes.length - (anchorNodes.length - graph.getStartPointNum())/2; i --) {
            if(!graph.startContains(anchorNodes[i])){
                isAnchor[anchorNodes[i]] = false;
            }
        }

    }


    void reduceMostAnchorNodes(){
        // set it to remain only the largest one
        int largestDegree = 0;
        for(int i : anchorNodes){
            if(graph.startContains(i)) continue;
            if(largestDegree < graph.getNumDegree(i))
                largestDegree = graph.getNumDegree(i);
            if (graph.getNumDegree(i) < largestDegree)
                isAnchor[i] = false;
        }

    }

    void reduceAnchorNodes(){
        if(reduceAnchorType.contains("largest")) reduceMostAnchorNodes();
        else reduceHalfAnchorNodes();
    }

     void concatenate(){
        reduceAnchorNodes();

        for(int i : anchorNodes){
            Stack<int[]> newAnchorPaths = firstConcatenate(i);
            anchorPaths.replace(i, newAnchorPaths);
        }

        System.out.println("first concatenate finished");

        for(int i: anchorNodes){
            if(!isAnchor[i]) anchorPaths.remove(i);
        }

        for(int i : graph.getStartPoints()){
            secondConcatenate(i);
        }
    }

    Stack<int[]> firstConcatenate(int start) {

        Stack<int[]>newAnchorPaths = new Stack<>();

        if(firstLevel.equals(ConcatenateType.DFS)) {
            for(int[] startPath : anchorPaths.get(start)) {
                Stack<int[]> stack = new Stack<>();
                stack.push(startPath);
                firstConcatenateDFS(startPath, stack, newAnchorPaths);
            }
        }
        else firstConcatenateBFS(start, newAnchorPaths);

        return newAnchorPaths;
    }



    private void firstConcatenateDFS(int[] s, Stack<int[]> curStack, Stack<int[]> newAnchorPaths) {

        if(graph.startContains((curStack.firstElement())[0]) && graph.endContains(s[s.length - 1])) {
            if(isSaveToMem) validPaths.add(getPathSeq(curStack));
            return;
        }

        if(isAnchor[s[s.length - 1]] || graph.endContains(s[s.length - 1])) {
            newAnchorPaths.push(getPathSeq(curStack));
            return;
        }

        for(int[] nextAnchorPath : anchorPaths.get(s[s.length - 1])) {
            curStack.push(nextAnchorPath);
            firstConcatenateDFS(nextAnchorPath, curStack, newAnchorPaths);
            curStack.pop();
        }
    }

    private void firstConcatenateBFS(int start, Stack<int[]> stack) {
        ArrayQueue<Stack<int[]>> queue = new ArrayQueue<>(graph.getStartPointNum());

        queue.offer(anchorPaths.get(start));

        while(!queue.isEmpty()) {
            Stack<int[]> currentPaths = queue.poll();
            for(int[] subPath : currentPaths) {

                if(graph.startContains(subPath[0]) && graph.endContains(subPath[subPath.length - 1])) {
                    if(isSaveToMem) validPaths.add(subPath);
                    pathNum ++;
                    continue;
                }
                if(isAnchor[subPath[subPath.length - 1]]
                        || graph.endContains(subPath[subPath.length - 1])) {
                    stack.push(subPath);
                    continue;
                }

                Stack<int[]> combo = new Stack<>();
                for(int[] nextList : anchorPaths.get(subPath[subPath.length - 1])) {
                    int[] newPath = new int[subPath.length - 1 + nextList.length];
                    System.arraycopy(subPath, 0, newPath, 0, subPath.length - 1);
                    System.arraycopy(nextList, 0, newPath, subPath.length - 1, nextList.length);

                    combo.push(newPath);
                }
                queue.offer(combo);
            }
            currentPaths = null; // let garbage collection deal with it
        }
    }


    void secondConcatenate(int start) {
        // TODO: remove CustomObjStack in the future
        // second level concatenate
        if(secondLevel.equals(ConcatenateType.DFS) ){
            for(int[] startPath : anchorPaths.get(start)) {
                CustomObjStack<int[]> stack = new CustomObjStack<>();
                stack.push(startPath);
                DFSSubConcatenate(startPath, stack);
            }
        }
        else {
            BFSSubConcatenate(start);
        }

    }
}
