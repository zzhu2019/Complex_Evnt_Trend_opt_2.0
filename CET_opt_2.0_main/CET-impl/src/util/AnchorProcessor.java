package src.util;

import src.Components.CompressedGraph;

import javax.sound.midi.SysexMessage;
import java.util.*;
import java.util.stream.IntStream;

public class AnchorProcessor {

    private final CompressedGraph graph;


    public AnchorProcessor(CompressedGraph graph) {
        this.graph = graph;
    }


    public int[] findAnchors(AnchorType type, int anchorNum) {
        System.out.println("- Find anchor points for this graph...");

        if(type.equals(AnchorType.RANDOM)) return findRandomAnchors(anchorNum);
        else if(type.equals(AnchorType.LARGEST_DEGREE)) return findDegreeAnchors(anchorNum, "L");
        else if(type.equals(AnchorType.EQUAL_DISTRIBUTE)) return findEquallyDistributedAnchors(anchorNum);
        else if(type.equals(AnchorType.SMALLEST_DEGREE)) return findDegreeAnchors(anchorNum, "S");
        else System.out.println("Not a valid anchor selection type.");

        return null;
    }


    private int[] findRandomAnchors(int anchorNum) {
        int[] anchorList = new int[graph.getStartPointNum() + anchorNum];

        for(int i = 0; i < graph.getStartPointNum(); i++) {
            anchorList[i] = graph.getStartPoints().get(i);
        }
        Random random = new Random();
        int counter = 0;
        while(counter < anchorNum) {
            int anchor = random.nextInt(graph.getNumVertex());

            if(IntStream.of(anchorList).anyMatch(x -> x == anchor)) continue;

            anchorList[graph.getStartPointNum() + counter++] = anchor;
        }

        return anchorList;
    }

    private int[] findDegreeAnchors(int anchorNum, String anchorType){
        // only put start points into it, not end points
        int[] anchorList = new int[graph.getStartPointNum() + anchorNum];

        for(int i = 0; i < graph.getStartPointNum(); i++) {
            anchorList[i] = graph.getStartPoints().get(i);
        }

        HashMap<Integer, Integer> verticesByDegree = new HashMap<>();
        for(int i = 0; i < graph.getNumVertex(); i++) {
            if(graph.startContains(i) || graph.endContains(i)) {
                continue;
            }
            // put degree with node num
            verticesByDegree.put(i, graph.getNumDegree(i)); // TODO: compare performance later
        }

        TreeMap<Integer, List<Integer>> degreeVertex = sortMap(verticesByDegree);
        anchorList = findAnchorNodesByDegree(anchorNum, degreeVertex, anchorList, anchorType);
//        if(anchorType.equalsIgnoreCase("L")) {
//            anchorList = findLargestDegreeAnchors(anchorNum, degreeVertex, anchorList);
//        }
//        else {
//            anchorList = findSmallestDegreeAnchors(anchorNum, degreeVertex, anchorList);
//        }
        return anchorList;
    }


    /**
     *
     * @param anchorNum         the number of needed anchor nodes
     * @param verticesByDegree  a map that key is degree, value is an array of nodes
     * @param anchorList        the list for anchor nodes, only contains start nodes in the beginning
     * @param anchorType        the anchor nodes slection type
     * @return a full list of anchor nodes
     */
    private int[] findAnchorNodesByDegree(int anchorNum, TreeMap<Integer, List<Integer>> verticesByDegree,
                                          int[] anchorList, String anchorType) {
        int start = graph.getStartPointNum();
        Set<Map.Entry<Integer, List<Integer>>> entrySet;
        if(anchorType.equalsIgnoreCase("L")) {
            entrySet = verticesByDegree.descendingMap().entrySet();
        }
        else {
            entrySet = verticesByDegree.entrySet();
        }

        for(Map.Entry<Integer, List<Integer>> entry : entrySet) {
            if(anchorNum <= 0) break;

            for(int i : entry.getValue()) {
                if(start < anchorList.length) {
                    anchorList[start++] = i;
                }
                else break;
            }

            anchorNum -= entry.getValue().size();
        }

        return anchorList;
    }


//    private int[] findLargestDegreeAnchors(int anchorNum,
//                                           TreeMap<Integer, List<Integer>> verticesByDegree, int[] anchorList) {
//        int start = graph.getStartPointNum();
//
//        for(Map.Entry<Integer, List<Integer>> entry : verticesByDegree.descendingMap().entrySet()) {
//            if(anchorNum <= 0) break;
//
//            for(int i : entry.getValue()) {
//                if(start < anchorList.length) {
//                    anchorList[start++] = i;
//                }
//                else break;
//            }
//
//            anchorNum -= entry.getValue().size();
//        }
//        return anchorList;
//    }

//    private int [] findSmallestDegreeAnchors(int anchorNum,
//                                             TreeMap<Integer, List<Integer>> verticesByDegree, int[] anchorList) {
//
//        int start = graph.getStartPointNum();
//
//        for(Map.Entry<Integer, List<Integer>> entry : verticesByDegree.entrySet()) {
//            if(anchorNum <= 0) break;
//
//            for(int i : entry.getValue()) {
//                if(start < anchorList.length) {
//                    anchorList[start++] = i;
//                }
//                else break;
//            }
//            anchorNum -= entry.getValue().size();
//        }
//        return anchorList;
//    }

    /**
     * Source: https://www.geeksforgeeks.org/sorting-a-hashmap-according-to-values/
     *
     * @param   map to sort by value
     * @return map sorted by value
     */
    private TreeMap<Integer, List<Integer>> sortMap(HashMap<Integer, Integer> map) {

        TreeMap<Integer, List<Integer>> temp = new TreeMap<>();

        for(Map.Entry<Integer, Integer> element : map.entrySet()) {
            temp.computeIfAbsent(element.getValue(), k -> new ArrayList<>());
            temp.get(element.getValue()).add(element.getKey());
        }
        return temp;
    }

    // TODO: possible optimization as a DFS takes place here to topological sort
    // TODO: overlapping of start nodes and later added anchor nodes
    private int[] findEquallyDistributedAnchors(int anchorNum) {
        int[] anchorList = new int[graph.getStartPointNum() + anchorNum];
        Stack<Integer> topStack = new Stack<>();
        boolean[] visited = new boolean[graph.getNumVertex()];
        Arrays.fill(visited, false);

        for(int i : graph.getStartPoints()) {
            if(!visited[i]) {
                topologicalSort(i, visited, topStack);
            }
        }

        // Reverse the order of elements in the stack
//        ArrayList<Integer> results = new ArrayList<>(graph.getStartPoints());
        ArrayList<Integer> results = new ArrayList<>();

        // Add non-end nodes secondly
        while(!topStack.empty()) {
            int r = topStack.pop();
            if(!graph.getStartPoints().contains(r) && !graph.getEndPoints().contains(r)) {
                results.add(r);
            }
        }

        // TODO: fine-tuned for topological ordering anchor node selection
        // Reduce the number of anchor nodes to around HALF if more than HALF of available nodes will be anchor nodes
        if((graph.getNumVertex() - graph.getStartPointNum() - graph.getEndPointNum())/anchorNum < 2) {
            anchorNum = (graph.getNumVertex() - graph.getStartPointNum() - graph.getEndPointNum()) / 3;
            System.out.println("The number of anchor nodes is too large! Reduced to " + anchorNum);
            anchorList = new int[graph.getStartPointNum() + anchorNum];
        }

        // Add all start nodes in the anchor node lists firstly
        for(int i = 0; i < graph.getStartPointNum(); i++) {
            anchorList[i] = graph.getStartPoints().get(i);
        }

        // The gap between every two anchor nodes
        int spacing = results.size() / anchorNum - 1;

        for(int i = 0; i < anchorNum; i++) {
            anchorList[i + graph.getStartPointNum()] = results.get((i + 1) * spacing);
        }

        return anchorList;
    }

    /**
     * reference from https://www.geeksforgeeks.org/topological-sorting/
     * this algo is based on a modified DFS that push a node into the stack once all of its neighbours are called
     * @param s         a start node
     * @param visited   a boolean array marks a node true if this is fully accessed
     * @param stack     the result stack
     */
    private void topologicalSort(int s, boolean[] visited, Stack<Integer> stack) {
        visited[s] = true;
        // For node s, call all its neighbours
        for(int i = graph.rowIndex[s]; i < graph.rowIndex[s+1]; i++) {
            int neighbour = graph.colIndex[i];
            if(!visited[neighbour]) {
                topologicalSort(neighbour, visited, stack);
            }
        }
        // Push node s into the stack once the FOR loop finishes
        stack.push(s);
    }
}
