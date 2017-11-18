package distributed_system;

import matrix.MatrixMultiple;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Coordinator { 
	
	Connection conn;
	int dim;
	int[][] a;
	int[][] b;
	int[][] c;
	int numNodes;
	DataInputStream[][] disWorkers;
	DataOutputStream[][] dosWorkers;
	Node[][] nodeArray;
	int m;
	int sqrt_m;

	public Coordinator(int n, int numNodes) {
		this.dim = n;
		a = new int[n][n];
		b = new int[n][n];
		c = new int[n][n];
		this.numNodes = numNodes;
		nodeArray = Node.getArray(numNodes, n);
		Node.displayNodeArray(nodeArray);
		sqrt_m = (int) Math.sqrt(numNodes);
		m = (int) (dim / sqrt_m);
	}

 	void configurate(int portNum) {
		try {
			conn = new Connection(portNum);
//			disWorkers = new DataInputStream[sqrt_m][sqrt_m];
//			dosWorkers = new DataOutputStream[sqrt_m][sqrt_m];
//			String[][] ips = new String[sqrt_m][sqrt_m];
//			int[][] ports = new int[sqrt_m][sqrt_m];
			for (int i=0; i<sqrt_m; i++ ) {
				for(int j = 0; j < sqrt_m; j++){
					Node currentNode = nodeArray[i][j];
					DataIO dio = conn.acceptConnect();
					DataInputStream dis = dio.getDis();
					currentNode.nodeNum = dis.readInt(); 			//get worker ID
					currentNode.ip = dis.readUTF(); 			//get worker ip
					currentNode.port = dis.readInt();  		//get worker port #
					currentNode.disWorker = dis;
					currentNode.dosWorker = dio.getDos(); 	//the stream to worker ID

					currentNode.dosWorker.writeInt(m); 		//assign matrix dimension (m), where m = m/ sqrt(num of nodes)
				}
			}
			for (int i=0; i < sqrt_m; i++) {
				for (int j = 0; j < sqrt_m; j++){
					Node currentNode = nodeArray[i][j];
					Node leftNode = Node.getLeftNode(nodeArray, i, j);
					currentNode.dosWorker.writeUTF(leftNode.ip); 	// left worker's ip
					currentNode.dosWorker.writeInt(leftNode.port);

					Node rightNode = Node.getRightNode(nodeArray, i, j);

					currentNode.dosWorker.writeUTF(rightNode.ip); 	// right worker's ip
					currentNode.dosWorker.writeInt(rightNode.port);
				}
			}
		} catch (IOException ioe) { 
			System.out.println("error: Coordinator assigning neighbor infor.");  
			ioe.printStackTrace(); 
		} 
	}
	
	void distribute(int numNodes) { 
		a = MatrixMultiple.createDisplayMatrix(dim); 
		MatrixMultiple.displayMatrix(a);
		for(int i = 0; i < nodeArray.length; i++){
			for (int j = 0; j < nodeArray.length; j++){
				Node currentNode = nodeArray[i][j];

				for (int a_i = currentNode.iStart; a_i <= currentNode.iEnd; a_i++){
					for(int a_j = currentNode.jStart; a_j <= currentNode.jEnd; a_j++){
						try {
							currentNode.dosWorker.writeInt(a[a_i][a_j]);
						} catch (IOException ioe) {
							System.out.println("error in distribute: " + i + ", " + j);
							ioe.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	public static void main(String[] args) { 
		if (args.length != 3) {
			System.out.println("usage: java Coordinator maxtrix-m number-nodes coordinator-port-num");
		}
		int matrixDim = Integer.parseInt(args[0]);
		int numNodes = Integer.parseInt(args[1]);
		int coordinator_port_num = Integer.parseInt(args[2]);
		Coordinator coor = new Coordinator(matrixDim, numNodes);
		coor.configurate(coordinator_port_num);
		coor.distribute(numNodes);
		try {Thread.sleep(12000);} catch (Exception e) {e.printStackTrace();}
		System.out.println("Done.");
	}
}
