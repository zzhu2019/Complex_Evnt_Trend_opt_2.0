package src.Traversal;

import src.Components.CompressedGraph;
import src.util.CustomDS.CustomIntStack;

public class DFSGraphTraversal extends GraphTraversal {

    public DFSGraphTraversal(CompressedGraph graph, boolean saveToMem) {
        super(graph, saveToMem);
        traversalType = TraversalType.DFS;

    }

    @Override
    public void traversal(int start) {
        CustomIntStack path = new CustomIntStack();

        path.push(start);

        // Call the recursive helper function to print DFS traversal
        if(graph.getNumDegree(start) != 0) DFSTraversal(start, path);
        else {
            if(isSaveToMem) validPaths.add(path.getAllElements());
            pathNum++;
        }

    }

    private void DFSTraversal(int s, CustomIntStack path) {
        if(graph.endContains(s)) {
            if(isSaveToMem) validPaths.add(path.getAllElements());
            pathNum++;
            return;
        }

        // Recur for all the vertices adjacent to this vertex
        for(int i = graph.rowIndex[s]; i < graph.rowIndex[s + 1]; i++) {
            int edge = graph.colIndex[i];
            path.push(edge);
            DFSTraversal(edge, path);
            path.pop();
        }
    }


}
