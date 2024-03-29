package src.util;

import src.Components.CompressedGraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

class FileGraphParser {
    // graph are read from a text file, with the format of adjacency matrix representation


    CompressedGraph readGraph(String fileName) {
        File myObj = new File(fileName);
        Scanner myReader = null;
        try {
            myReader = new Scanner(myObj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assert myReader != null;

        // The first line in the graph file is the graph type
        String type = myReader.nextLine();

        myReader.close();

        switch(type) {
            case "Pair":
                return readCompressedPairGraph(fileName);
            case "Grid":
                return readGridGraph(fileName);
            case "List":
                return readCompressedListGraph(fileName);
            case "CSR":
                return readCSRGraph(fileName);
            default:
                System.out.println("Unrecognized type!");
                return null;
        }
    }

    private CompressedGraph readCSRGraph(String fileName) {
        File file = new File(fileName);
        Scanner myReader;
        CompressedGraph graph;
        String[] colNums = null;
        String[] rowNums = null;
        try {
            myReader = new Scanner(file);
            myReader.nextLine();// read type
            int nodeNum = Integer.parseInt(myReader.nextLine());
            int colNum = 0;
            while (myReader.hasNextLine()) {
                String str = myReader.nextLine();
                if (str.contains("col")) {
                    str = str.replace("col: ", "");
                    colNums = str.split(" ");
                    colNum = colNums.length;
                }
                if (str.contains("row")) {
                    str = str.replace("row: ", "");
                    rowNums = str.split(" ");
                }
            }
            graph = new CompressedGraph(colNum, nodeNum + 1);

            for (int i = 0; i < colNum; i++) graph.getColIndex()[i] = Integer.parseInt(colNums[i]);

            assert rowNums != null;
            for (int i = 0; i < nodeNum + 1; i++) graph.getRowIndex()[i] = Integer.parseInt(rowNums[i]);

            myReader.close();

            return graph;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Example:
     * <p>
     * Pair
     * 4
     * 0,2
     * 1,2
     * 1,3
     * 2,3
     */
    private CompressedGraph readCompressedPairGraph(String fileName) {
        CompressedGraph graph;
        int nodeNum = 0;
        ArrayList<int[]> matrix = new ArrayList<>();
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            String type = myReader.nextLine();
            if(type.contains("Random") || !type.contains("Pair")) {
                System.out.println("ERROR: Read wrong type of graph! Should be Compressed Pair!");
            }

            nodeNum = Integer.parseInt(myReader.nextLine());
            while(myReader.hasNext()) {
                String[] data = myReader.nextLine().split(",");
                if(data.length != 2) break;
                matrix.add(new int[]{Integer.parseInt(data[0]), Integer.parseInt(data[1])});
            }
            myReader.close();
        }
        catch(Exception e) {
            System.out.println("File parsing error for reading sparse matrix.");
            e.printStackTrace();
        }
        GraphGenerator graphGenerator = new GraphGenerator();
        graph = graphGenerator.buildGraph(matrix, nodeNum);

        return graph;
    }

    /**
     * Example:
     *  Grid
     *  3
     *  1,0,0
     *  0,0,0
     *  0,1,0
     */
    private CompressedGraph readGridGraph(String fileName) {
        CompressedGraph graph;
        boolean[][] grid = null;
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            String type = myReader.nextLine();
//            if(type.contains("Random") || type.contains("Sparse")) {
//                System.out.println("ERROR: Read wrong type of graph! Should be grid!");
//            }
            int nodeNum = Integer.parseInt(myReader.nextLine());

            grid = new boolean[nodeNum][nodeNum];

            for(int i = 0; i < nodeNum; i++) {
                String data = myReader.nextLine();
                String[] neighbours = data.split(",");
                for(int j = 0; j < nodeNum; j++) {
                    grid[i][j] = neighbours[j].equals("1");
                }
            }
            myReader.close();

            for(int i=0; i<nodeNum; ++i) {
                for(int j=0; j<nodeNum; ++j) {
                    if(i>=j && grid[i][j]) {
                        System.out.println("row "+i +" col "+j);
                        System.out.println("DAG check failed!");
                    }
                }
            }

            System.out.println("DAG check passed!");

        }
        catch(Exception e) {
            System.out.println("File parsing error.");
            e.printStackTrace();
        }
        GraphGenerator graphGenerator = new GraphGenerator();

        assert grid != null;
        graph = graphGenerator.buildGraph(grid);

        return graph;
    }

    /**
     * Example:
     * <p>
     * List
     * 5
     * 1,2
     * 3
     * 3,4
     * 4
     * NaN
     */
    private CompressedGraph readCompressedListGraph(String fileName) {
        ArrayList<Integer>[] lists = null;
        CompressedGraph graph;
        int counter = 0;
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            String type = myReader.nextLine();

            lists = new ArrayList[Integer.parseInt(myReader.nextLine())];

            while (myReader.hasNext()) {
                String data = myReader.nextLine();
                lists[counter] = new ArrayList<>();

                if (data.equalsIgnoreCase("NaN")) {
                    counter++;
                    continue;
                }
                String[] neighbours = data.split(",");
                for (String neighbour : neighbours) lists[counter].add(Integer.parseInt(neighbour));
                counter++;
            }
            myReader.close();
        } catch (Exception e) {
            System.out.println("File parsing error for reading sparse matrix.");
            e.printStackTrace();
        }
        GraphGenerator graphGenerator = new GraphGenerator();

        assert lists != null;
        graph = graphGenerator.buildGraph(lists);

        return graph;
    }
}
