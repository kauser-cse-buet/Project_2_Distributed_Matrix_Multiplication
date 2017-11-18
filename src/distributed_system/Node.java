package distributed_system;

import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Created by mahmmed on 11/18/2017.
 */
public class Node {
    int iStart;
    int iEnd;
    int jStart;
    int jEnd;
    Point coordinate;
    DataInputStream disWorker;
    DataOutputStream dosWorker;
    String ip;
    int port;
    int nodeNum;

    public Node( int iStart, int iEnd, int jStart, int jEnd, int x, int y) {
        this.iStart = iStart;
        this.iEnd = iEnd;
        this.jStart = jStart;
        this.jEnd = jEnd;
        coordinate = new Point(x, y);
    }

    public static Node[][] getArray(int numNode, int actualMatrixDim){
        int nodeArraySize = (int) Math.sqrt(numNode);
        int m = actualMatrixDim / nodeArraySize;

        Node[][] nodeArray = new Node[nodeArraySize][nodeArraySize];

        for(int x = 0; x < nodeArraySize; x++){
            for(int y = 0; y < nodeArraySize; y++){
                int iStart = x * m;
                int iEnd = x * m + m - 1;
                int jStart = y * m;
                int jEnd = y * m + m - 1;
                nodeArray[x][y] = new Node(iStart, iEnd, jStart, jEnd, x, y);
            }
        }
        return nodeArray;
    }

    public static void displayNodeArray(Node[][] nodeArray){
        for(int i = 0; i < nodeArray.length; i++){
            for (int j = 0; j < nodeArray[0].length; j++){
                nodeArray[i][j].display();
                System.out.print("-----");
            }
            System.out.println();
        }
    }

    public void display(){
        System.out.print("i: [" + this.iStart + ", " + this.iEnd + "], j:[" + this.jStart + ", " + this.jEnd + "], (x, y): " + this.coordinate);
    }

    public static Node getUpNode(Node[][] nodeArray, int i, int j){
        if(i < nodeArray.length && j < nodeArray[0].length){
            int upI = -1;
            int upJ = j;
            if(i == 0){
                upI = nodeArray.length - 1;
            }
            else{
                upI = i - 1;
            }

            return nodeArray[upI][upJ];
        }
        else{
            System.out.println("i > nodeArray.length or j > nodeArray[0].length");
            return null;
        }
    }

    public static Node getLeftNode(Node[][] nodeArray, int i, int j){
        if(i < nodeArray.length && j < nodeArray[0].length){
            int leftI = i;
            int leftJ = -1;

            if(j == 0){
                leftJ = nodeArray[0].length - 1;
            }
            else {
                leftJ = j - 1;
            }

            return nodeArray[leftI][leftJ];
        }
        else{
            System.out.println("i > nodeArray.length or j > nodeArray[0].length");
            return null;
        }
    }

    public static Node getRightNode(Node[][] nodeArray, int i, int j){
        if(i < nodeArray.length && j < nodeArray[0].length){
            int rightI = i;
            int rightJ = -1;

            if(j == nodeArray.length - 1){
                rightJ = 0;
            }
            else {
                rightJ = j + 1;
            }

            return nodeArray[rightI][rightJ];
        }
        else{
            System.out.println("i > nodeArray.length or j > nodeArray[0].length");
            return null;
        }
    }



    public static void main(String[] args){
        Node[][] nodeArray = getArray(4, 8);
        displayNodeArray(nodeArray);

//        Node.getUpNode(nodeArray, 0, 0).display();
//        Node.getUpNode(nodeArray, 0, 1).display();
//        Node.getUpNode(nodeArray, 1, 0).display();
//        Node.getUpNode(nodeArray, 1, 1).display();

//        Node.getLeftNode(nodeArray, 0, 0).display();
//        System.out.println();
//        Node.getLeftNode(nodeArray, 0, 1).display();
//        System.out.println();
//        Node.getLeftNode(nodeArray, 1, 0).display();
//        System.out.println();
//        Node.getLeftNode(nodeArray, 1, 1).display();

//        Node.getRightNode(nodeArray, 0, 0).display();
//        System.out.println();
//        Node.getRightNode(nodeArray, 0, 1).display();
//        System.out.println();
//        Node.getRightNode(nodeArray, 1, 0).display();
//        System.out.println();
//        Node.getRightNode(nodeArray, 1, 1).display();

        Node.getDownNode(nodeArray, 0, 0).display();
        System.out.println();
        Node.getDownNode(nodeArray, 0, 1).display();
        System.out.println();
        Node.getDownNode(nodeArray, 1, 0).display();
        System.out.println();
        Node.getDownNode(nodeArray, 1, 1).display();
    }

    public static Node getDownNode(Node[][] nodeArray, int i, int j) {
        if(i < nodeArray.length && j < nodeArray[0].length){
            int downI = -1;
            int downJ = j;
            if(i == nodeArray.length - 1){
                downI = 0;
            }
            else{
                downI = i + 1;
            }

            return nodeArray[downI][downJ];
        }
        else{
            System.out.println("i > nodeArray.length or j > nodeArray[0].length");
            return null;
        }
    }
}
