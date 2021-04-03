package src.util.dagGen;

import java.util.ArrayList;
//import java.util.Iterator;
import java.util.List;
import java.util.Stack;

class DAGFunctions {

    public static boolean isLog = true;

    public static boolean[][] resolveDependencies(boolean[][] matrix) {
        boolean[][] result = new boolean[matrix.length][matrix[0].length];
        for(int i = 0; i < matrix.length; i++) {
            for(int j = 0; j < matrix[0].length; j++) {
                result[i][j] = matrix[i][j];
            }
        }
        boolean cleanPass = false;
        //Iterate through each job
        while (!cleanPass) {
            cleanPass = true;
            for (int i = 0; i < result.length; i++) {
                //iterate through each dependency
                for (int j = 0; j < result[0].length; j++) {
                    if (result[i][j]) {
                        //resolve the dependencies of the dependency
                        for (int k = 0; k < result[0].length; k++) {
                            if (!result[i][k] && result[j][k]) {
                                cleanPass = false;
                                result[i][k] = result[j][k];
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Throws an error if the file does not meet the criteria for being an acyclic
     * digraph.
     *
     * @param matrix Workflow Dependency Matrix
     * @throws DAGSelfDependent If a cloudlet depends on itself
     */
    public static void integrityCheck(boolean[][] matrix) throws DAGSelfDependent {
        if(isLog) {
            System.out.print("Checking Integrity of DAG File...");
        }
        boolean[][] temp = DAGFunctions.resolveDependencies(matrix);
        if(temp.length > 0) {
            for(int i = 0; i < temp.length && i < temp[0].length; i++) {
                //if job depends on self
                if(temp[i][i]) {
                    throw new DAGSelfDependent();
                }
            }
        }
        if(isLog) {
            System.out.println(" File Passed Integrity Check!");
        }
    }

//    //Used for the recursive part of removeSelfDependencies()
//    private static IntegerStack depends = new IntegerStack();

//    /**
//     * Navigates the matrix and removes any self dependencies.
//     *
//     * @deprecated
//     */
//    public static boolean[][] oldRemoveSelfDependencies(boolean[][] matrix) {
//        if(isLog)
//            System.out.println("Removing Dependencies...");
//        for(int i = 0; i < matrix.length; i++) {
//            if(isLog)
//                System.out.println("Checking Row " + i);
//            depends.push(i);
//            buildStack(matrix, i);
//            depends.clear();
//        }
//        return matrix;
//    }


    public static boolean[][] removeSelfDependencies(boolean[][] matrix) {
        TreeNode[] modules = shuffleTree(generateTreeFromGrid(matrix));
        return cleanTree(modules, matrix);
    }


    public static ArrayList<int[]> removeSelfDependencies(ArrayList<int[]> pairs, int jobCount) {
        TreeNode[] modules = shuffleTree(generateTreeFromPairs(pairs, jobCount));
        return cleanTree(modules, pairs);
    }

    public static ArrayList<Integer>[] removeSelfDependencies(ArrayList<Integer>[] lists) {
        TreeNode[] modules = shuffleTree(generateTreeFromLists(lists));
        return cleanTree(modules, lists);
    }

    private static TreeNode[] shuffleTree(TreeNode[] tree) {
        if(isLog)
        {
            System.out.println("Shuffling Tree References...");
        }
        for(TreeNode treeNode : tree) {
            treeNode.shuffle();
        }
        return tree;
    }

    /**
     * Removes any circular references from a dependency adjacency matrix using
     * its tree representation.
     *
     * @param tree the TreeNode Array with shuffled children
     * @param matrix original generated matrix
     * @return a DAG
     */
    private static boolean[][] cleanTree(TreeNode[] tree, boolean[][] matrix) {
        if(isLog) {
            System.out.println("Removing Self Dependencies Using Tree...");
        }
        // Keep track of which nodes have been checked for circular references
        boolean[] checked = new boolean[tree.length];

        // Keep track of the path taken to get to a node
        Stack<TreeNode> path = new Stack<>();
        for(int j = 0; j < checked.length; j++) {
            if(!checked[j]) {
                path.add(tree[j]);
                while(path.size() != 0) {
                    //Get the next step in the path
                    TreeNode next = path.peek().getNext();
                    if(next != null) {
                        //If the node has already been in the path, cut the circular reference
                        //This is/else loop balances the table
                        if(path.contains(next)) {
//                            matrix[next.id][path.peek().id] = false;          // original method
                            matrix[path.peek().id][next.id] = false;
                        }
                        else {
                            // Only do this step if node has not been checked already
                            if(checked[next.id]) {
                                continue;
                            }
                            //Add it to the current path
                            path.push(next);
                        }
                    }
                    else {
                        //If we have reached the end of this branch, traverse back up
                        checked[path.pop().id] = true;
                    }
                }
            }
        }
        return matrix;
    }

    private static ArrayList<int[]> cleanTree(TreeNode[] tree, ArrayList<int[]> pairs) {
        if (isLog)
            System.out.println("Removing Self Dependencies Using Tree...");
        //Keeps track of which nodes have been checked for circular references
        boolean[] checked = new boolean[tree.length];

        //Keeps track of the path taken to get to a node
        Stack<TreeNode> path = new Stack<TreeNode>();
        for (int j = 0; j < checked.length; j++) {
            if (!checked[j]) {
                path.add(tree[j]);
                while (path.size() != 0) {
                    //Get the next step in the path
                    TreeNode next = path.peek().getNext();
                    if (next != null) {
                        //If the node is already in the path, sever the reference
                        //This is/else loop balances the table
                        if (path.contains(next)) {
                            remove(pairs, path.peek().id, next.id);
                        } else {
                            //Only do this step if node has not been checked already
                            if (checked[next.id])
                                continue;
                            //Add it to the current path
                            path.push(next);
                        }
                    } else {
                        //If we have reached the end of this branch, traverse back up
                        checked[path.pop().id] = true;
                    }
                }
            }
        }
        return pairs;
    }

    private static ArrayList<Integer>[] cleanTree(TreeNode[] tree, ArrayList<Integer>[] lists) {
        if (isLog)
            System.out.println("Removing Self Dependencies Using Tree...");
        //Keeps track of which nodes have been checked for circular references
        boolean[] checked = new boolean[tree.length];

        //Keeps track of the path taken to get to a node
        Stack<TreeNode> path = new Stack<TreeNode>();
        for (int j = 0; j < checked.length; j++) {
            if (!checked[j]) {
                path.add(tree[j]);
                while (path.size() != 0) {
                    //Get the next step in the path
                    TreeNode next = path.peek().getNext();
                    if (next != null) {
                        //If the node is already in the path, sever the reference
                        //This is/else loop balances the table
                        if (path.contains(next)) {
                            lists[path.peek().id].remove((Integer)next.id);
                        } else {
                            //Only do this step if node has not been checked already
                            if (checked[next.id])
                                continue;
                            //Add it to the current path
                            path.push(next);
                        }
                    } else {
                        //If we have reached the end of this branch, traverse back up
                        checked[path.pop().id] = true;
                    }
                }
            }
        }
        return lists;
    }

    private static void remove(ArrayList<int[]> matrix, int source, int dest) {
        for (int i = 0; i < matrix.size(); i++) {
            if (matrix.get(i)[0] == source && matrix.get(i)[1] == dest) matrix.remove(i);
        }
    }

    /**
     * Generates a tree data structure that represents the workflow of modules
     * derived from a dependency adjacency matrix.
     *
     * @param matrix a  matrix
     * @return a TreeNode object that contains parent-child relationship
     */
    private static TreeNode[] generateTreeFromGrid(boolean[][] matrix) {
        if(isLog) {
            System.out.println("Building Tree Data Structure...");
        }

        int size = matrix.length;

        //Generate array of nodes
        TreeNode[] result = new TreeNode[size];
        for(int i = 0; i < result.length; i++) {
            result[i] = new TreeNode(i);
        }

        //Go through array of nodes, left-down direction
        for(int j = 0; j < matrix.length; j++) {
            for(int i = 0; i < matrix[0].length; i++) {
                if(matrix[j][i]) {
                    result[j].children.add(result[i]);
                }
            }
        }
        return result;
    }

    private static TreeNode[] generateTreeFromPairs(ArrayList<int[]> pairs, int jobCount) {

        if(isLog) {
            System.out.println("Building Tree Data Structure from compressed pairs...");
        }

        TreeNode[] treeNodes = new TreeNode[jobCount];
        for(int i = 0; i < treeNodes.length; i++) {
            treeNodes[i] = new TreeNode(i);
        }
        for(int i = 0; i < pairs.size(); i++) {
            int id = pairs.get(i)[0];
            int neighbour = pairs.get(i)[1];
            treeNodes[id].children.add(treeNodes[neighbour]);
        }
        return treeNodes;
    }

    private static TreeNode[] generateTreeFromLists(ArrayList<Integer>[] lists) {
        if (isLog) System.out.println("Building Tree Data Structure from compressed lists...");
        TreeNode[] treeNodes = new TreeNode[lists.length];
        for (int i = 0; i < treeNodes.length; i++) {
            treeNodes[i] = new TreeNode(i);
        }
        for (int i = 0; i < lists.length; i++) {
            for (int j = 0; j < lists[i].size(); j++) {
                int neighbour = lists[i].get(j);
                treeNodes[i].children.add(treeNodes[neighbour]);
            }
        }
        return treeNodes;

    }

//    private static void buildStack(boolean[][] matrix, int row) {
//        for (int j = 0; j < matrix[row].length; j++) {
//            if (matrix[row][j]) {
//                if (depends.contains(new Integer(j))) {
//                    matrix[row][j] = false;
//                } else {
//                    depends.push(new Integer(j));
//                    buildStack(matrix, j);
//                }
//            }
//        }
//        depends.pop();
//    }


//    public static void printMatrix(boolean[][] matrix) {
//        if(isLog)
//            System.out.println("Generating String For STD Output...");
//        StringBuilder result = new StringBuilder(matrix.length * (matrix[0].length * 3));
//        for(boolean[] booleans : matrix) {
//            result.append("|");
//            for (int j = 0; j < matrix[0].length; j++) {
//                char c;
//                if (booleans[j])
//                    c = '1';
//                else
//                    c = '0';
//                result.append(c + "|");
//            }
//            result.append("\n");
//        }
//
//        System.out.println(result);
//    }

//    private static class IntegerStack extends Stack<Integer> {
//
//        private static final long serialVersionUID = 8650955703278560778L;
//
//        @Override
//        public boolean contains(Object o) {
//            Integer value = (Integer) o;
//            Iterator<Integer> it = this.iterator();
//            while(it.hasNext()) {
//                if (it.next().intValue() == value.intValue())
//                    return true;
//            }
//            return false;
//        }
//    }

    /**
     * Go through the diagram, if an edge's degree is more than maxDegree, then randomly remove some edges
     * Temporarily, this is just for graphs in matrix form
     *
     * @param matrix graph
     * @param maxDegree A max degree parameter set during the configuration
     * @return a revised graph
     */
    public static boolean[][] maxDegreeCheckAndResolve(boolean[][] matrix, int maxDegree) {
        for(int i=0; i<matrix.length; ++i) {
            List<Integer> neighbours = new ArrayList<>();
            for(int j=0; j<matrix[i].length; ++j) {
                if(matrix[i][j]) {
                    neighbours.add(j);
                }
            }

            if(neighbours.size() > maxDegree) {
                int count = neighbours.size()-maxDegree;
                while(count > 0) {
                    int pickedEdge = (int) Math.floor(Math.random()*neighbours.size());
                    matrix[i][neighbours.get(pickedEdge)] = false;
                    neighbours.remove(pickedEdge);
                    count--;
                }
            }
        }
        return matrix;
    }
}
