package src.Traversal;

import src.Components.CompressedGraph;
import src.util.CustomDS.ArrayQueue;
import src.util.CustomDS.IntArray;

public class BFSGraphTraversal extends GraphTraversal {
    public BFSGraphTraversal(CompressedGraph graph, boolean saveToMem) {
        super(graph, saveToMem);
        traversalType = TraversalType.BFS;
    }

    public void traversal(int start) {
        ArrayQueue<IntArray> queue = new ArrayQueue<>(graph.getNumVertex());

        IntArray path = new IntArray(1);
        path.add(start);

        queue.offer(path);
        if(graph.endContains(start)) {
            validPaths.add((path.getArray()));
            pathNum++;
            return;
        }

        while (!queue.isEmpty()) {
            IntArray currentPath = queue.poll();
            int cur = currentPath.getLast();
            for(int i = graph.rowIndex[cur]; i < graph.rowIndex[cur + 1]; i++) {
                int neighbour = graph.colIndex[i];

                // FIXME: java heap space out of memory error when the computation
                IntArray newArray = new IntArray(currentPath);
                newArray.add(neighbour);
                if(graph.endContains(neighbour)) {
                    if(isSaveToMem) validPaths.add(newArray.getArray());
                    pathNum++;
                }
                else queue.offer(newArray);
            }
        }
    }
}