package socket_example_1row;

import java.io.*;  

import matrix.*; 

public class Coordinator { 
	
	Connection conn; 
	int dim; 
	int[][] a; 
	int[][] b; 
	int[][] c; 
	int numNodes; 
	DataInputStream[] disWorkers;
	DataOutputStream[] dosWorkers; 
	
	public Coordinator(int n, int numNodes) { 
		this.dim = n; 
		a = new int[n][n]; 
		b = new int[n][n]; 
		c = new int[n][n]; 
		this.numNodes = numNodes; 
	}
	
 	void configurate(int portNum) { 
		try { 
			conn = new Connection(portNum); 
			disWorkers = new DataInputStream[numNodes]; 
			dosWorkers = new DataOutputStream[numNodes];
			String[] ips = new String[numNodes]; 
			int[] ports = new int[numNodes]; 
			for (int i=0; i<numNodes; i++ ) { 
				DataIO dio = conn.acceptConnect(); 
				DataInputStream dis = dio.getDis(); 
				int nodeNum = dis.readInt(); 			//get worker ID
				ips[nodeNum] = dis.readUTF(); 			//get worker ip
				ports[nodeNum] = dis.readInt();  		//get worker port #
				disWorkers[nodeNum] = dis; 
				dosWorkers[nodeNum] = dio.getDos(); 	//the stream to worker ID
				dosWorkers[nodeNum].writeInt(dim); 		//assign matrix dimension (height)
				int width = (nodeNum<numNodes-1) ? dim/numNodes : dim/numNodes+dim%numNodes;
				dosWorkers[nodeNum].writeInt(width); 	//assign matrix width 
			} 
			for (int w=0; w<numNodes; w++) { 
				int LEFT1 = (w+numNodes-1)%numNodes; 	//shift to (send)
				dosWorkers[w].writeUTF(ips[LEFT1]); 	// left worker's ip 
				dosWorkers[w].writeInt(ports[LEFT1]); 
				int RIGHT1 = (w+1)%numNodes; 			//receive from 
				dosWorkers[w].writeUTF(ips[RIGHT1]); 	// right worker's ip 
				dosWorkers[w].writeInt(ports[RIGHT1]); 
			}
		} catch (IOException ioe) { 
			System.out.println("error: Coordinator assigning neighbor infor.");  
			ioe.printStackTrace(); 
		} 
	}
	
	void distribute(int numNodes) { 
		a = MatrixMultiple.createDisplayMatrix(dim); 
		MatrixMultiple.displayMatrix(a); 
		for (int w = 0; w < numNodes; w++) {			// send blocks 
			int width = (w<numNodes-1) ? (w+1)*dim/numNodes : dim; 
			for (int i = 0; i < dim; i++) { 
				for (int j = w*dim/numNodes; j < width; j++) { 
					try { 
						dosWorkers[w].writeInt(a[i][j]); 
					} catch (IOException ioe) { 
						System.out.println("error in distribute: " + i + ", " + j);  
						ioe.printStackTrace(); 
					}
				} 
			} 
			System.out.println("Sent to Worker" + w); 
		} 
	}
	
	public static void main(String[] args) { 
		if (args.length != 3) {
			System.out.println("usage: java Coordinator maxtrix-dim number-nodes coordinator-port-num"); 
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
