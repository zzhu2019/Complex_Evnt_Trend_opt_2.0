package src;

import src.Components.AlgoExecutor;
import src.Components.CompressedGraph;
import src.util.GraphBuilder;
import src.util.GraphType;
import src.util.DAGFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        System.out.println("Right now it is time: " + new Date().toString());


        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current relative path is: " + s);

        GraphBuilder graphBuilder = new GraphBuilder();
        String input;
        int numNodes;
        int isolatedNodeCount = 0;
        CompressedGraph graph;
        Scanner sc = new Scanner(System.in);

        // Read graph type: either random or a file path
        if(args.length == 1) {
            graph = graphBuilder.generateGraphFile(args[0]);
        }
        // command line read a graph or create a new graph
        else {
            System.out.println("""
                    -------------------------------------------------------------
                    - Do you want to enter an existing config file path? (y/n)  -
                    - If any other char is entered, this program will exit.    -
                    -------------------------------------------------------------""");
            input = sc.nextLine();
            if(input.equalsIgnoreCase("n")) {
                System.out.println("""
                        -------------------------------------------------------------
                        - Do you want to create a direct DAG?(y/n)                  -
                        - The program will exit after creating the DAG.             -
                        -------------------------------------------------------------""");
                input = sc.nextLine();
                if(input.equalsIgnoreCase("y")) {
                    directDAG();
                    return;
                }

                graphBuilder.random = true;

                System.out.println("""
                        -------------------------------------------------------------
                        - Number of nodes for the graph:                            -
                        -------------------------------------------------------------""");
                numNodes = setIntParameter();

                System.out.println("""
                        -------------------------------------------------------------
                        - Choose graph output type:                                 -
                        -   1. Grid                                                 -
                        -   2. Pairs                                                -
                        -   3. Lists                                                -
                        -   4. CSR(Compressed Sparse Row)                           -
                        NOTE: Other selection will go to default -- Grid format     -
                        -------------------------------------------------------------""");

                input = sc.nextLine();

                System.out.println("""
                        -------------------------------------------------------------
                        - Do you want to save the graph to file?(y/n)               -
                        -------------------------------------------------------------""");
                graphBuilder.saveFile = setBooleanParameter();

                switch(input) {
                    case "2" -> graphBuilder.type = GraphType.Pair;
                    case "3" -> graphBuilder.type = GraphType.List;
                    case "4" -> graphBuilder.type = GraphType.CSR;
                    default -> graphBuilder.type = GraphType.Grid;
                }

                System.out.println("""
                    -------------------------------------------------------------
                    - Desired frequency? (the possibility for an edge)          -
                    - 1/(frequency * nodeNum + 1)                               -
                    -------------------------------------------------------------""");
                graphBuilder.frequency = setDoubleParameter();

                System.out.println("""
                    -------------------------------------------------------------
                    - Desired max degree for DAG nodes?                         -
                    -------------------------------------------------------------""");
                graphBuilder.setMaxDegree(setIntParameter());

                graph = graphBuilder.generateRandomGraph(numNodes);
            } else if(input.equalsIgnoreCase("y")) {
                while(true) {
                    System.out.println("""
                            -------------------------------------------------------------
                            - Please specify file path: (enter 'exit' to quit)          -
                            -------------------------------------------------------------""");
                    input = sc.nextLine();

                    if(input.equalsIgnoreCase("exit")) return;

                    if(new File(input).exists()) break;
                    else System.out.println("File doesn't exist!");
                }

                graph = graphBuilder.generateGraphFile(input);
                if(graph == null) {
                    System.out.println("exiting the program...");
                    return;
                }
            } else {
               System.out.println("Please run the program again and input a y/n.");
               return;
            }
        }

        for(int i= graph.getNumVertex() - graph.getEndPointNum(); i<graph.getNumVertex(); ++i) {
            if(graph.getNumDegree(i) != 0) {
                isolatedNodeCount++;

            }
        }
        System.out.println("Isolated Node Count: " + isolatedNodeCount);

        System.out.println("\n\nGraph generated!\n\n");

        System.out.println("Graph has total of " + graph.getTotalNumEdges() + " edges");

        printDegreeNumVSNode(graph);

        // End the program for testing the graph generator
//        System.out.println("""
//        -------------------------------------------------------------
//        - Terminate the program? (y/n)                              -
//        -------------------------------------------------------------""");
//        if(setBooleanParameter()) return;

        // Create output directory
        File output = new File("OutputFiles/result/timeResults");
        if (!output.exists()) {
            if(!output.mkdirs()) {
                System.out.println("Cannot create OutputFiles/result/timeResults.");
            }
        }

        System.gc();

        System.out.println("""
                    -------------------------------------------------------------
                    - Please enter number of run you want for the algorithm?    -
                    -------------------------------------------------------------""");
        AlgoExecutor executor = new AlgoExecutor(setIntParameter());

        System.out.println("""
                    -------------------------------------------------------------
                    - Please add the algorithm to process the graph:            -
                    -   99. Finish choosing (exit program)                      -
                    -   1.  Normal BFS                                          -
                    -   2.  Normal DFS                                          -
                    -   3.  Anchor (DFS concatenate)                            -
                    -   4.  Anchor (BFS concatenate)                            -
                    -   5.  Concurrent Anchor (1.DFS 2.DFS)                     -
                    -   6.  Concurrent Anchor (1.BFS 2.DFS)                     -
                    -   7.  Concurrent Anchor (1.DFS 2.BFS)                     -
                    -   8.  Concurrent Anchor (1.BFS 2.BFS)                     -
                    -------------------------------------------------------------""");
        while(true) {
            int algoIndex = setIntParameter();

            if(algoIndex == 99) return;
            else if(algoIndex <= 7) {
                System.out.println("""
                    -------------------------------------------------------------
                    - Do you want to save result to run time memory? (y/n)      -
                    -------------------------------------------------------------""");
                executor.setSavePathInMem(setBooleanParameter());

                executor.setAlgo(algoIndex, graph);
                break;
            }
            else {
                System.out.println("Not a valid option!");
            }
        }


        System.out.println("Start executing...\n\n");

        // execute the algorithm here
        executor.execute();
        executor.cleanGarbage();

        System.out.println("\n\n- Run finished");
        System.out.println("Memeory usage:" + (Runtime.getRuntime().totalMemory()/1000000) +
                "/" +(Runtime.getRuntime().maxMemory()/1000000));

        if(executor.isSavePathInMem()) {
            System.out.println("""
                    -------------------------------------------------------------
                    - Paths are now stored in memory.                           -
                    - Do you want to save result to files? (y/n)                -
                    -------------------------------------------------------------""");

            if(setBooleanParameter()) {
                System.out.println("\n\nWriting results...\n\n");
                executor.savePathsResult();
            }

            System.out.println("""
                    -------------------------------------------------------------
                    - Do you want to print out results? (y/n)                   -
                    -------------------------------------------------------------""");
            if(setBooleanParameter()) {
                executor.printPaths();
            }
        }
        else {
            System.out.println("Warning: No results saved.");
        }
    }


    private static void printDegreeNumVSNode(CompressedGraph graph){
        TreeMap<Integer, Integer> degreeNum = new TreeMap<>(Collections.reverseOrder());

        for(int i = 0; i < graph.getNumVertex(); i++) {
            int degree = graph.getNumDegree(i);
            if(degreeNum.get(degree) == null) {
                degreeNum.put(degree, 1);
            } else {
                degreeNum.replace(degree, degreeNum.get(degree) + 1);
            }
        }
        System.out.println("-------------------------------------------------------------");
        System.out.println("Node Out Degree : The Number of Nodes");
        printDegrees(degreeNum);
        System.out.println("-------------------------------------------------------------");
        degreeNum.clear();
    }

    private static void printDegrees(TreeMap<Integer, Integer> interDegreeNum) {
        Set set;
        Iterator iterator;
        set = interDegreeNum.entrySet();
        iterator = set.iterator();

        while(iterator.hasNext()) {
            Map.Entry me = (Map.Entry)iterator.next();
            System.out.print(me.getKey() + ": ");
            System.out.println(me.getValue());
        }
    }

    /**
     * set a int parameter positive, usually a parameter for graph generator
     * @return a positive int value
     */
    private static int setIntParameter() {
        int value;
        Scanner sc = new Scanner(System.in);

        while(true) {
            try{
                value = Integer.parseInt(sc.nextLine());
                if(value > 0) break;
                else System.out.println("Not a valid number!");
            }
            catch(Exception e) {
                System.out.println("Not a number!");
            }
        }
        return value;
    }

    /**
     * set a double parameter positive, usually a parameter for graph generator
     * @return a positive double value
     */
    private static double setDoubleParameter() {
        double value;
        Scanner sc = new Scanner(System.in);

        while(true) {
            try{
                value = sc.nextDouble();
                if(value > 0) break;
                else System.out.println("Not a valid number!");
            } catch(Exception e) {
                System.out.println("Not a valid number!");
            }
        }
        return value;
    }

    /**
     * set a boolean parameter positive, usually a parameter for graph generator
     * @return a boolean value
     */
    private static boolean setBooleanParameter() {
        boolean value;
        Scanner sc = new Scanner(System.in);

        while(true) {
            String input = sc.nextLine();
            if(input.equalsIgnoreCase("y")) {
                value = true;
                break;
            } else if(input.equalsIgnoreCase("n")) {
                value = false;
                break;
            } else {
                System.out.println("Not a valid option!");
            }
        }

        return value;
    }


    private static void directDAG() {
        int numOfNode, numOfStartPoint, numOfEndPoint, numOfPath, stepRange, maxInDegree, maxOutDegree;
        System.out.println("""
                        -------------------------------------------------------------
                        - Number of nodes for the graph:                            -
                        -------------------------------------------------------------""");
        numOfNode = setIntParameter();

        System.out.println("""
                        -------------------------------------------------------------
                        - Number of start nodes for the graph:                      -
                        -------------------------------------------------------------""");
        numOfStartPoint = setIntParameter();

        System.out.println("""
                        -------------------------------------------------------------
                        - Number of end nodes for the graph:                        -
                        -------------------------------------------------------------""");
        numOfEndPoint = setIntParameter();

        System.out.println("""
                        -------------------------------------------------------------
                        - Number of Paths for the graph:                            -
                        -------------------------------------------------------------""");
        numOfPath = setIntParameter();

        System.out.println("""
                        -------------------------------------------------------------
                        - Max step for the graph:                                   -
                        -------------------------------------------------------------""");
        stepRange = setIntParameter();

        System.out.println("""
                        -------------------------------------------------------------
                        - Max Outdegree for all nodes:                               -
                        -------------------------------------------------------------""");
        maxOutDegree = setIntParameter();

        System.out.println("""
                        -------------------------------------------------------------
                        - Max Indegree for all nodes:                               -
                        -------------------------------------------------------------""");
        maxInDegree = setIntParameter();

        DAGFactory fac = new DAGFactory(numOfNode, numOfStartPoint, numOfEndPoint, numOfPath, stepRange,
                maxInDegree, maxOutDegree);
        fac.createDAG();
        fac.saveToFile();
    }
}
