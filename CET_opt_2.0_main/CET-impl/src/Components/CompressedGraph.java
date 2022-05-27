package src.Components;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CompressedGraph {
    public int[] colIndex;
    public int[] rowIndex;

    private boolean[] isEndPoints;
    private boolean[] isStartPoints;
    private boolean[] isIndependentPoints;

    private int [] inDegrees;

    private int numOfStartPoint;
    private int numOfEndPoint;
    private int numOfIndependentPoint;

    private final ArrayList<Short> startPoints;
    private final ArrayList<Short> endPoints;
    private final ArrayList<Short> independentPoints;

    public CompressedGraph(int colNum, int rowNum) {
        colIndex = new int[colNum];
        rowIndex = new int[rowNum];
        startPoints = new ArrayList<>();
        endPoints = new ArrayList<>();
        independentPoints = new ArrayList<>();

        numOfEndPoint = -1;
        numOfStartPoint = -1;
        numOfIndependentPoint = -1;

        Arrays.fill(colIndex, -1);
        Arrays.fill(rowIndex, -1);
    }

    public int[] getColIndex() {
        return colIndex;
    }

    public int[] getRowIndex() {
        return rowIndex;
    }

    public int getStartPointNum() {
        if(numOfStartPoint == -1) loadStartPoints();
        return numOfStartPoint;
    }

    public int getEndPointNum() {
        if(numOfEndPoint == -1) loadEndPoints();
        return numOfEndPoint;
    }

    public int getIndependentPointNum() {
        if(numOfIndependentPoint == -1) {
            loadIndependentPoints();
        }
        return numOfIndependentPoint;
    }

    public boolean startContains(int i){
        if(numOfStartPoint == -1) loadStartPoints();
        return isStartPoints[i];
    }

    public boolean endContains(int i){
        if(numOfEndPoint == -1) loadEndPoints();
        return isEndPoints[i];
    }

    public int[] getInDegrees(){
        if(inDegrees != null) loadInDegrees();

        return inDegrees;
    }

    private void loadInDegrees(){                                                       // something goes wrong here if this is for getting the start nodes' degree information
        HashMap<Integer, Integer> vertexInDegree = new HashMap<>();
        for (int i = 0; i < getNumVertex(); i++) vertexInDegree.put(i, 0);

        for (int i : colIndex) vertexInDegree.replace(i, vertexInDegree.get(i) + 1);

        inDegrees = new int[getNumVertex()];

        for (int i = 0; i < getNumVertex(); i++) inDegrees[i] = vertexInDegree.get(i);

        vertexInDegree = null;

    }

    public int getIndegree(int i){
        if(inDegrees == null) loadInDegrees();

        assert inDegrees != null;

        return inDegrees[i];
    }

    public List<Short> getStartPoints() {
        if(startPoints.size() == 0) loadStartPoints();
        return startPoints;
    }

    private void loadStartPoints() {
        numOfStartPoint = 0;
        isStartPoints = new boolean[rowIndex.length - 1];
        Arrays.fill(isStartPoints, true);

        for(int i : colIndex) {
            isStartPoints[i] = false;
        }

        for(short i = 0; i < getNumVertex(); i++) {
            if(isStartPoints[i]) {
                startPoints.add(i);
                numOfStartPoint++;
            }
        }
    }

    public List<Short> getEndPoints() {
        if(endPoints.size() == 0) loadEndPoints();
        return endPoints;
    }

    private void loadEndPoints(){
        numOfEndPoint = 0;
        isEndPoints  = new boolean[rowIndex.length - 1];

        for(int i = 0; i < rowIndex.length - 1; ++i) {
            if(rowIndex[i] == rowIndex[i + 1]) {
                isEndPoints[i] = true;
            }
        }

        for(short i = 0; i < getNumVertex(); i++) {
            if(isEndPoints[i]) {
                endPoints.add(i);
                numOfEndPoint++;
            }
        }
    }

    public List<Short> getIndependentPoints() {
        if(numOfIndependentPoint == -1) loadIndependentPoints();
        return independentPoints;
    }

    private void loadIndependentPoints() {
        numOfIndependentPoint = 0;
        isIndependentPoints = new boolean[rowIndex.length - 1];
        for(int i=0; i<rowIndex.length-1; ++i) {
            if(isEndPoints[i] && isStartPoints[i]) {
                isIndependentPoints[i] = true;
            }
        }
        for(short i=0; i<getNumVertex(); ++i) {
            if(isIndependentPoints[i]) {
                independentPoints.add(i);
                numOfIndependentPoint++;
            }
        }
    }

    public int getNumDegree(int i){
        return rowIndex[i + 1] - rowIndex[i];
    }

    public int getTotalNumEdges(){
        return colIndex.length;
    }

    public int getNumVertex() {
        return rowIndex.length - 1;
    }
}
