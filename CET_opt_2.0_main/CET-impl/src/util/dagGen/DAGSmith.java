package src.util.dagGen;

import src.Components.CompressedGraph;

import java.util.ArrayList;

/**
 * DAGSmith is a class used for generating NxN Directed Acyclic Graphs (DAGs).
 *
 * @author crackerz
 */
public class DAGSmith {

    /**
     * The default constructor for DAGSmith. This behaves as if DAGSmith(false) was called as a constructor (logging will be disabled).
     */
    public DAGSmith() {
        DAGFunctions.isLog = false;
        DAGTools.log = false;
    }

    /**
     * Create a DAGSmith.
     *
     * @param isLog If set to true, debugging information will be printed to stdout during the creation of DAGs.
     */
    public DAGSmith(boolean isLog) {
        DAGFunctions.isLog = isLog;
        DAGTools.log = isLog;
    }

    /**
     * Save the graph to file and return it
     *
     * @param jobCount  The number of nodes to put in the graph (i.e. a value of 8 would result in an 8x8 matrix).
     * @param frequency The likelihood two nodes will have an edge between them. This value represents a percentage such that probability = 1 / frequency.
     *                  (i.e. a value of 2 would yield a 50% chance. 3 would yield 33%. 4: 25%, 5: 20%, etc.)
     * @param maxDegree the max degree in the DAG
     * @param FileName  The file to save the generated matrix too.
     * @return The generated graph.
     */
    public boolean[][] generateRandomFile(int jobCount, int frequency, int maxDegree,String FileName) {
        boolean[][] matrix = this.generateDAGMatrix(jobCount, frequency, maxDegree);
        DAGTools.saveToFile(matrix, FileName);
        return matrix;
    }

    /**
     * Generates a random NxN directed acyclic graph with jobcount nodes.
     * Temporarily, the maxValue is just for graphs in matrix form
     *
     * @param jobCount  The number of nodes to put in the graph (i.e. a value of 8 would result in an 8x8 matrix).
     * @param frequency The likelihood two nodes will have an edge between them. This value represents a percentage such that probability = 1 / frequency.
     *                  (i.e. a value of 2 would yield a 50% chance. 3 would yield 33%. 4: 25%, 5: 20%, etc.)
     * @param maxValue The max degree for nodes in the DAG
     * @return The generated graph.
     */
    public boolean[][] generateDAGMatrix(int jobCount, int frequency, int maxValue) {
        boolean[][] matrix = generateRandomMatrix(jobCount, frequency);
        boolean[][] matrix1 = DAGFunctions.maxDegreeCheckAndResolve(matrix, maxValue);       // reduce degree if applicable
        return DAGFunctions.removeSelfDependencies(matrix1);                     // remove circular dependence
    }


    public CompressedGraph generateDAGCSR(int jobCount, int frequency, int maxDegree){
        boolean[][] dag = generateDAGMatrix(jobCount, frequency, maxDegree);
        int edgeNum = 0;
        for(boolean[] col : dag)
            for(boolean b: col)
                if(b) edgeNum ++;

        CompressedGraph dagGraph = new CompressedGraph(edgeNum, jobCount+1);

        int [] colIndex = dagGraph.getColIndex();
        int [] rowIndex = dagGraph.getRowIndex();
        int colCounter = 0;
        int rowCounter = 0;
        rowIndex[rowCounter++] = 0;
        for(int i = 0; i < jobCount; i ++){
            for(int j = 0; j < jobCount; j ++){
                if(dag[i][j]) {
                    colIndex[colCounter++] = j;
                }
            }
            rowIndex[rowCounter++] = colCounter;
        }


        return dagGraph;
    }

    // TODO: check if all nodes' degree are below the max degree
    public ArrayList<int[]> generateDAGPairs(int jobCount, int frequency) {
        ArrayList<int[]> sparseMatrix = this.generateRandomCompressedPair(jobCount, frequency);
        sparseMatrix = DAGFunctions.removeSelfDependencies(sparseMatrix, jobCount);
        return sparseMatrix;
    }


    public ArrayList<Integer>[] generateDAGLists(int jobCount, int frequency) {
        ArrayList<Integer>[] lists = this.generateRandomCompressedLists(jobCount, frequency);
        lists = DAGFunctions.removeSelfDependencies(lists);
        return lists;
    }


    private boolean[][] generateRandomMatrix(int jobCount, int frequency) {
        if(getIsLog()) {
            System.out.println("Generating Matrix...");
        }

        boolean[][] result = new boolean[jobCount][jobCount];

        for(int i = 0; i < jobCount; i++) {
            // try to see if it's connected to the graph at all
            for(int j = 0; j < jobCount; j++) {
                if (i != j) result[i][j] = random(frequency);
            }
        }

        return result;
    }

    private ArrayList<int[]> generateRandomCompressedPair(int jobCount, int frequency) {
        if(getIsLog()) System.out.println("Generating Compressed Pair...");
        ArrayList<int[]> results = new ArrayList<>();

        for(int i = 0; i < jobCount; i++) {
            boolean[] edges = new boolean[jobCount];
            for(int j = 0; j < jobCount; j++) {
                if(i != j) edges[j] = random(frequency);
            }
            results.addAll(generateSparsePairs(i, edges));
        }
        return results;

    }

    private ArrayList<int[]> generateSparsePairs(int node, boolean[] edges) {

        ArrayList<int[]> results = new ArrayList<>();
        for (int i = 0; i < edges.length; i++) {
            if (edges[i]) results.add(new int[]{node, i});
        }
        return results;
    }

    private ArrayList<Integer>[] generateRandomCompressedLists(int jobCount, int frequency) {
        if(getIsLog())
            System.out.println("Generating Compressed Lists...");

        ArrayList<Integer>[] lists = new ArrayList[jobCount];
        for(int i = 0; i < jobCount; i++) {
            lists[i] = new ArrayList<>();
            // try to see if it's connected to the graph at all
            for(int j = 0; j < jobCount; j++) {
                if(i != j) {
                    if(random(frequency)) {
                        lists[i].add(j);
                    }
                }
            }

        }
        return lists;
    }

    private boolean random(int frequency) {
        return ((int) (Math.random() * (frequency + 1)) % frequency == 0);
    }


    /**
     * Commandline output
     */
    private boolean getIsLog() {
        return DAGFunctions.isLog;
    }
}
