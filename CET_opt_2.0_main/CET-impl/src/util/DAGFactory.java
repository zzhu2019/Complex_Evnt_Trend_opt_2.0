package src.util;

import src.util.dagGen.DAGTools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;


public class DAGFactory {
    private final int numOfNode;
    private final int numOfStartPoint;
    private final int numOfEndPoint;
    private final int numOfPath;
    private final int stepRange;
    private final int maxInDegree;
    private final int maxOutDegree;
    boolean[][] matrix;
    int[] outDegree;
    int[] inDegree;


    public DAGFactory(int numOfNode, int numOfStartPoint, int numOfEndPoint, int numOfPath, int stepRange,
                      int maxInDegree, int maxOutDegree) {
        this.numOfNode = numOfNode;
        this.numOfStartPoint = numOfStartPoint;
        this.numOfEndPoint = numOfEndPoint;
        this.numOfPath = numOfPath;
        this.stepRange = stepRange;
        this.matrix = new boolean[numOfNode][numOfNode];
        this.outDegree = new int[numOfNode];
        this.inDegree = new int[numOfNode];
        this.maxInDegree = maxInDegree;
        this.maxOutDegree = maxOutDegree;
    }


    public void createDAG() {
        // from the start nodes
        int failedNodeNum = 0, completedNodeNum = 0;
        int startFromBeginning = maxOutDegree * numOfStartPoint;
        List<Integer> disconnectedNodes = new ArrayList<>(numOfNode);

        for(int i=0; i<numOfStartPoint; ++i) {
            for(int j=0; j<maxOutDegree; ++j) {
                randomPathForStartNode(i);
//                System.out.println(j);
            }
        }

        // from the middle nodes
        int startFromMiddle = numOfPath - startFromBeginning;
        int count = 0;
        do {
            int middleNode = (int) (Math.random()*(numOfNode-numOfStartPoint-numOfEndPoint)) + numOfStartPoint;
            if(outDegree[middleNode] != 0 && outDegree[middleNode] < maxOutDegree) {
                // if the target node's degree is smaller than maxOutDegree and bigger than 0
                randomPathForMiddleNode(middleNode);
                count++;
            }
//            System.out.println(count);
        } while(count < startFromMiddle);

        // find isolated nodes, connect them to the DAG
        for(int i=numOfStartPoint; i<numOfNode-numOfEndPoint; ++i) {
            if(inDegree[i] == 0) {
                disconnectedNodes.add(i);
            }
        }

        for(int node : disconnectedNodes) {
            if(inDegree[node] == 0) {
                // randomly find a node with > 0 degree before it and
                // a node with > 0 degree after it
                // connect
                int retryLimit = 50;
                int prevNode = -1, nextNode = -1;
                do {
                    if(retryLimit <= 0) {
                        failedNodeNum++;
                        break;
                    }

                    prevNode = (int) Math.floor(Math.random()*node);
                    retryLimit--;
                } while(inDegree[prevNode] == 0 || outDegree[prevNode] >= maxOutDegree);

                if(retryLimit <= 0) {
                    System.out.println("fail to connect " + node + " to a previous node");
                    continue;
                }

                retryLimit = 50;

                do {
                    if(retryLimit <= 0) {
                        failedNodeNum++;
                        break;
                    }

                    nextNode = (int) Math.floor(Math.random()*(numOfNode-node-1)) + node + 1;
                    retryLimit--;
                } while(inDegree[nextNode] == 0 || inDegree[nextNode] >= maxOutDegree);

                if(retryLimit <= 0) {
                    System.out.println("fail to connect " + node + " to a next node");
                    continue;
                }

                matrix[prevNode][node] = true;
                outDegree[prevNode]++;
                inDegree[node]++;
                matrix[node][nextNode] = true;
                outDegree[node]++;
                inDegree[nextNode]++;

                completedNodeNum++;
            } else if(outDegree[node] == 0) {
                System.out.println("error");
            } else {
                System.out.println("connected somehow");
            }
        }

        System.out.println("Max in Degree: " + Arrays.stream(inDegree).max().getAsInt());
        System.out.println("Max out Degree: " + Arrays.stream(outDegree).max().getAsInt());
        System.out.println("total " + disconnectedNodes.size() + " completed " + completedNodeNum +
                " failed " + failedNodeNum);

        for(int i=0; i<numOfNode; ++i) {
            for(int j=0; j<numOfNode; ++j) {
                if(i>=j && matrix[i][j]) {
                    System.out.println("row "+i +" col "+j);
                    System.out.println("DAG check failed!");
                    return;
                }
            }
        }

        for(int i = numOfNode - numOfEndPoint; i<numOfNode; ++i) {
            if(outDegree[i] > 0) {
                System.out.println("DAG Check failed!");
                return;
            }
        }

        System.out.println("DAG check passed!");
    }


    private void randomPathForStartNode(int start) {
        int retryLimit = 10;
        int curPos = start;
        while(true) {
            // make the step ranges from 1 to stepRange (integer)
            int step = (int) Math.floor((Math.random()*stepRange)) + 1;
            int nextPos = curPos==start ? numOfStartPoint-1+step : curPos+step;
            if(nextPos < numOfNode - numOfEndPoint) {
                // fall on middle nodes
                if(outDegree[nextPos] >= maxOutDegree || inDegree[nextPos] >= maxInDegree) {
                    if(retryLimit > 0) {
                        retryLimit--;
                        continue;
                    }
                    else break;
                }

                // reset the limit count
                retryLimit = 10;
                matrix[curPos][nextPos] = true;
                inDegree[nextPos]++;
                outDegree[curPos]++;
                curPos = nextPos;
            } else if(nextPos < numOfNode) {
                // fall on end nodes
                matrix[curPos][nextPos] = true;
                inDegree[nextPos]++;
                outDegree[curPos]++;
                break;
            } else {
                // outside, randomly choose an end node
                int endPos = numOfNode - (int) Math.floor(Math.random() * numOfEndPoint) - 1;
                matrix[curPos][endPos] = true;
                inDegree[endPos]++;
                outDegree[curPos]++;
                break;
            }
        }
    }


    private void randomPathForMiddleNode(int start) {
        // in case there is a dead-loop of the re-choosing
        int retryLimit = 10;
        int currPos = start;
        while(retryLimit > 0) {
            int step = (int) Math.floor((Math.random()*stepRange)) + 1;
            int nextPos = currPos+step;
            if(nextPos < numOfNode - numOfEndPoint) {
                // fall on a middle node
                if(outDegree[nextPos] >= maxOutDegree || inDegree[nextPos] < maxInDegree) {
                    // re-choose the nextPos if nextPos is not visited and currPos' degree has reached the maximum
//                    System.out.println("Error: re-choose");
                        retryLimit--;
                        continue;
                }

                retryLimit = 10;
                matrix[currPos][nextPos] = true;
                outDegree[currPos]++;
                inDegree[nextPos]++;
                currPos = nextPos;
            } else if(nextPos < numOfNode) {
                // fall on end nodes
                if(inDegree[nextPos] >= maxInDegree) continue;

                matrix[currPos][nextPos] = true;
                outDegree[currPos]++;
                inDegree[nextPos]++;
                break;
            } else {
                // fall outside
                while(retryLimit > 0) {
                    int endPos = numOfNode - (int) Math.floor(Math.random() * numOfEndPoint) - 1;
                    if(inDegree[endPos] >= maxInDegree) {
                        retryLimit--;
                        continue;
                    }

                    matrix[currPos][endPos] = true;
                    outDegree[currPos]++;
                    inDegree[endPos]++;
                    break;
                }
                break;
            }
        }
    }


    public void saveToFile() {
        String fileName = "Grid" + numOfNode+"DAGFactory.txt";
        File file = new File(fileName);

        String content = ("Grid\n" + numOfNode + "\n" + DAGTools.printDAG(matrix)).trim();

        try {
            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file, true);
            fw.write(content.substring(0, content.length() / 2));
            fw.write(content.substring(content.length() / 2));
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
