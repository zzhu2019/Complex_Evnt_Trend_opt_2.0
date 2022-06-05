package src.Traversal;

import src.Components.CompressedGraph;
import src.util.CustomDS.ArrayQueue;
import src.util.CustomDS.ShortArray;

public class BFSGraphTraversal extends GraphTraversal {
    public BFSGraphTraversal(CompressedGraph graph, boolean saveToMem) {
        super(graph, saveToMem);
        traversalType = TraversalType.BFS;
    }

    public void traversal(short start) {
        ArrayQueue<ShortArray> queue = new ArrayQueue<>(graph.getNumVertex());

        ShortArray path = new ShortArray(10);
        path.add(start);

        queue.offer(path);
        if(graph.endContains(start)) {
            validPaths.add(path.getArray());
            pathNum++;
            return;
        }

        while (!queue.isEmpty()) {
            ShortArray currentPath = queue.poll();
            int cur = currentPath.getLast();
            for(int i = graph.rowIndex[cur]; i < graph.rowIndex[cur + 1]; i++) {
                short neighbour = (short) graph.colIndex[i];

                ShortArray newArray = new ShortArray(currentPath);
                newArray.add(neighbour);
                if(graph.endContains(neighbour)) {
                    if(isSaveToMem) validPaths.add(newArray.getArray());
                    pathNum++;
                } else queue.offer(newArray);
            }
        }
    }
}