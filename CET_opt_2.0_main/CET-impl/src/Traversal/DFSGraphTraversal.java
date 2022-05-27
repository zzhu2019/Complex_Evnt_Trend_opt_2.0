package src.Traversal;qq:q


import src.Components.CompressedGraph;
import src.util.CustomDS.CustomShortStack;

public class DFSGraphTraversal extends GraphTraversal {

    public DFSGraphTraversal(CompressedGraph graph, boolean saveToMem) {
        super(graph, saveToMem);
        traversalType = TraversalType.DFS;

    }

    @Override
    public void traversal(short start) {
        CustomShortStack path = new CustomShortStack();

        path.push(start);

        // Call the recursive helper function to print DFS traversal
        if(graph.getNumDegree(start) != 0)
            DFSTraversal(start, path);
        else {
            if(isSaveToMem) validPaths.add(path.getAllElements());
            pathNum++;
        }

    }

    /**
     * DFS recursively traversal
     * @param s: the current node
     * @param path: the path currents at the node 's'
     */
    private void DFSTraversal(int s, CustomShortStack path) {
        if(graph.endContains(s)) {
            if(isSaveToMem) validPaths.add(path.getAllElements());
            pathNum++;
            if(pathNum%1000000 == 0) {
                System.out.println((Runtime.getRuntime().totalMemory()/1000000) + "/" +(Runtime.getRuntime().maxMemory()/1000000));
                System.out.println("Path count reaches " + pathNum);
            }
            return;
        }

        // Recur for all the vertices adjacent to this vertex
        for(int i = graph.rowIndex[s]; i < graph.rowIndex[s + 1]; i++) {
            int edge = graph.colIndex[i];
            path.push((short) edge);
            DFSTraversal(edge, path);
            path.pop();
        }
    }
}
