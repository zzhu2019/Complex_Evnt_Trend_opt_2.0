package src.util;

import src.util.dagGen.DAGTools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DAGFactory {
    private final int numOfNode;
    private final int numOfStartPoint;
    private final int numOfEndPoint;
    private final int numOfPath;
    private final int stepRange;
    boolean[][] matrix;


    public DAGFactory(int numOfNode, int numOfStartPoint, int numOfEndPoint, int numOfPath, int stepRange) {
        this.numOfNode = numOfNode;
        this.numOfStartPoint = numOfStartPoint;
        this.numOfEndPoint = numOfEndPoint;
        this.numOfPath = numOfPath;
        this.stepRange = stepRange;
        this.matrix = new boolean[numOfNode][numOfNode];
    }


    public void createDAG() {
        int pathPerStartNode = (int) Math.floor((double)numOfPath/numOfStartPoint);
        for(int i=0; i<numOfStartPoint; ++i) {
            for(int j=0; j<pathPerStartNode; ++j) {
                designRandomPath(i);
            }
        }
    }


    private void designRandomPath(int start) {
        // TODO: design a random path
        int currPos = start;
        while(true) {
            // make the step ranges from 1 to stepRange (integer)
            int step = (int) Math.floor((Math.random()*stepRange)) + 1;
            int nextPos = currPos==start ? numOfStartPoint-1+step : currPos+step;
            if(nextPos < numOfNode - numOfEndPoint) {
                matrix[currPos][nextPos] = true;
                currPos = nextPos;
            }
            else if(nextPos < numOfNode) {
                matrix[currPos][nextPos] = true;
                break;
            }
            else {
                int endPos = numOfNode - (int) Math.floor(Math.random()*numOfEndPoint) - 1;
                matrix[currPos][endPos] = true;
                break;
            }
        }
    }


    public void saveToFile() {
        // TODO: save the DAG to file
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
