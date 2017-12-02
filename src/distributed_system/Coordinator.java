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

					System.out.println(currentNode.ip);
					System.out.println(currentNode.port);

					currentNode.port = dis.readInt();  		//get worker port #
					currentNode.disWorker = dis;
					currentNode.dosWorker = dio.getDos(); 	//the stream to worker ID

					currentNode.dosWorker.writeInt(m); 		//assign matrix dimension (m), where m = m/ sqrt(num of nodes)
                    currentNode.dosWorker.flush();
					currentNode.dosWorker.writeInt(dim); 		//assign actual matrix dimension (dm).
                    currentNode.dosWorker.flush();
                    currentNode.dosWorker.writeInt(numNodes); 		//assign actual matrix dimension (dm).
                    currentNode.dosWorker.flush();
				}
			}
			for (int i=0; i < sqrt_m; i++) {
				for (int j = 0; j < sqrt_m; j++){
					Node currentNode = nodeArray[i][j];

					Node leftNode = Node.getLeftNode(nodeArray, i, j);
					currentNode.dosWorker.writeUTF(leftNode.ip); 	// left worker's ip
                    currentNode.dosWorker.flush();
					currentNode.dosWorker.writeInt(leftNode.port);
                    currentNode.dosWorker.flush();

					Node rightNode = Node.getRightNode(nodeArray, i, j);

					currentNode.dosWorker.writeUTF(rightNode.ip); 	// right worker's ip
                    currentNode.dosWorker.flush();
					currentNode.dosWorker.writeInt(rightNode.port);
                    currentNode.dosWorker.flush();

					Node upNode = Node.getUpNode(nodeArray, i, j);
					currentNode.dosWorker.writeUTF(upNode.ip); 	// up worker's ip
                    currentNode.dosWorker.flush();
					currentNode.dosWorker.writeInt(upNode.port);
                    currentNode.dosWorker.flush();

					Node downNode = Node.getDownNode(nodeArray, i, j);
					System.out.println("currentNode.coordinate" + currentNode.coordinate);
					System.out.println("downnode.coordinate" + downNode.coordinate);

					currentNode.dosWorker.writeUTF(downNode.ip); 	// down worker's ip
                    currentNode.dosWorker.flush();
					currentNode.dosWorker.writeInt(downNode.port);
                    currentNode.dosWorker.flush();
				}
			}
		} catch (IOException ioe) {
			System.out.println("error: Coordinator assigning neighbor infor.");  
			ioe.printStackTrace(); 
		} 
	}
	
	void distribute(int numNodes) { 
		a = MatrixMultiple.getOddMatrix(dim);
		b = MatrixMultiple.getEvenMatrix(dim);

		System.out.println("Matrix A: Before shift left");
		MatrixMultiple.displayMatrix(a);
		System.out.println("-------------------------");
		int[][] aShiftedLeftIncreasingly = MatrixMultiple.shiftLeftIncreasingly(a);
		MatrixMultiple.displayMatrix(aShiftedLeftIncreasingly);

		System.out.println("Matrix B: Before shift left");
		MatrixMultiple.displayMatrix(b);
		System.out.println("-------------------------");
		int[][] bShiftedUpColumnIncreasingly = MatrixMultiple.shiftUpColumnIncreasingly(b);

		MatrixMultiple.displayMatrix(bShiftedUpColumnIncreasingly);
		System.out.println("-------------------------");

		for(int i = 0; i < nodeArray.length; i++){
			for (int j = 0; j < nodeArray.length; j++){
				Node currentNode = nodeArray[i][j];

//				Send matrix A
				for (int a_i = currentNode.iStart; a_i <= currentNode.iEnd; a_i++){
					for(int a_j = currentNode.jStart; a_j <= currentNode.jEnd; a_j++){
						try {
							currentNode.dosWorker.writeInt(aShiftedLeftIncreasingly[a_i][a_j]);
                            currentNode.dosWorker.flush();
						} catch (IOException ioe) {
							System.out.println("error in distribute: " + i + ", " + j);
							ioe.printStackTrace();
						}
					}
				}

				//				Send matrix B
				for (int a_i = currentNode.iStart; a_i <= currentNode.iEnd; a_i++){
					for(int a_j = currentNode.jStart; a_j <= currentNode.jEnd; a_j++){
						try {
							currentNode.dosWorker.writeInt(bShiftedUpColumnIncreasingly[a_i][a_j]);
                            currentNode.dosWorker.flush();
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
		coor.collect();
		System.out.println("Done.");
	}

	private void collect() {
		//		recieve result
		for (int i=sqrt_m - 1; i >= 0; i--) {
			for (int j = sqrt_m - 1; j >= 0; j--){
				Node currentNode = nodeArray[i][j];
				for(int c_i = currentNode.iStart; c_i <= currentNode.iEnd; c_i++)
				{
					for (int c_j = currentNode.jStart; c_j <= currentNode.jEnd; c_j++){
						try {
							c[c_i][c_j] = currentNode.disWorker.readInt();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		System.out.println("# Matrix c, the multiplcation of matrix A and B is collected by distributed system.");
		MatrixMultiple.displayMatrix(c, 6);

		int[][] nonDistributedC = MatrixMultiple.multiplyMatrix(a, b);
		System.out.println("# Matrix c, the multiplcation of matrix A and B is calculated normally.");
		MatrixMultiple.displayMatrix(nonDistributedC, 6);
		System.out.println("-----------------------------------------");
		System.out.println("Compare result between distributed multiplication and normal multiplication");
		MatrixMultiple.compareMatrix(c, nonDistributedC);
		System.out.println("-----------------------------------------");
	}
}
