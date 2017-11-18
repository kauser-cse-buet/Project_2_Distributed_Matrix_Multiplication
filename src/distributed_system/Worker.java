package distributed_system;

import matrix.MatrixMultiple;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

public class Worker {

	int nodeNum;
	int localPort;
	Connection conn;
	int m;
	int[][] a;
	int[][] b;
	int[][] c;
	DataInputStream disCoor;
	DataOutputStream dosCoor;
	DataOutputStream dosLeft;
	DataInputStream disRight;

	DataOutputStream dosUp;
	DataInputStream disDown;

	public Worker(int nodeNum, int localPort) {
		this.nodeNum = nodeNum;
		this.localPort = localPort;
	}

	void configurate(String coorIP, int coorPort) {
		try {
			conn = new Connection(localPort); 
			DataIO dio = conn.connectIO(coorIP, coorPort);
			dosCoor = dio.getDos();  
			dosCoor.writeInt(nodeNum);
			dosCoor.writeUTF(InetAddress.getLocalHost().getHostAddress());
			dosCoor.writeInt(localPort);
			disCoor = dio.getDis();
			m = disCoor.readInt(); 				//get matrix dimension from coordinator
			a = new int[m][m];
			b = new int[m][m];
			c = new int[m][m];
			String ipLeft = disCoor.readUTF();		//left block connection info
			int portLeft = disCoor.readInt();
			System.out.println("portLeft" + portLeft);

			String ipRight = disCoor.readUTF();		//right block connection info 
			int portRight = disCoor.readInt();
			System.out.println("portRight" + portRight);

			String ipUp = disCoor.readUTF();		//up block connection info
			int portUp = disCoor.readInt();

			System.out.println("port up" + portUp);

			String ipDown = disCoor.readUTF();		//down block connection info
			int portDown = disCoor.readInt();

			System.out.println("port down" + portDown);

//			if (nodeNum%2==0) {		// Even # worker connecting manner
//				dosLeft = conn.connect2write(ipLeft, portLeft);
//				disRight = conn.accept2read();
//				dosUp = conn.connect2write(ipUp, portUp);
//				disDown = conn.accept2read();
//			} else {				// Odd # worker connecting manner
//				disRight = conn.accept2read();
//				dosLeft = conn.connect2write(ipRight, portRight);
//				disDown = conn.accept2read();
//				dosUp = conn.connect2write(ipDown, portDown);
//			}

			dosLeft = conn.connect2write(ipLeft, portLeft);
			disRight = conn.accept2read();
			dosUp = conn.connect2write(ipUp, portUp);
			disDown = conn.accept2read();

//			if(nodeNum % 2 == 0){
//				dosUp = conn.connect2write(ipUp, portUp);
//				disDown = conn.accept2read();
//			}
//			else {				// Odd # worker connecting manner
//				disDown = conn.accept2read();
//				dosUp = conn.connect2write(ipDown, portDown);
//			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} 
		System.out.println("Configuration done."); 
	}

	void compute() {
		// get the block from coordinator
		// recieve matrix A
		for (int i = 0; i < m; i++) {
			for (int j = 0; j <m; j++) {
				try {
					a[i][j] = disCoor.readInt();
				} catch (IOException ioe) {
					System.out.println("error: " + i + ", " + j);
					ioe.printStackTrace();
				}
			}
		}
		System.out.println("Matrix A: ");
		MatrixMultiple.displayMatrix(a);
		System.out.println("-----------------");

		// recieve matrix B
		for (int i = 0; i < m; i++) {
			for (int j = 0; j <m; j++) {
				try {
					b[i][j] = disCoor.readInt();
				} catch (IOException ioe) {
					System.out.println("error: " + i + ", " + j);
					ioe.printStackTrace();
				}
			}
		}
		System.out.println("Matrix b: ");
		MatrixMultiple.displayMatrix(b);
		System.out.println("-----------------");

		c = MatrixMultiple.multiplyMatrixCellWise(a, b);
		System.out.println("Matrix c: ");
		MatrixMultiple.displayMatrix(c, 6);
		System.out.println("-----------------");


//		send matrix A to left
		for(int i = 0; i < m; i++){
			for(int j = 0; j < m; j++){
				try {
					dosLeft.writeInt(a[i][j]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Matrix A sent to left: Left shift");

		for(int i = 0; i < m; i++){
			for(int j = 0; j < m; j++){
				try {
					a[i][j] = disRight.readInt();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Matrix A recieved from right.");
		MatrixMultiple.displayMatrix(a);
		System.out.println("------------------------------");


//		send message to up
		for(int i = 0; i < m; i++){
			for(int j = 0; j < m; j++){
				try {
					dosUp.writeInt(b[i][j]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Matrix B sent to Up: shift Up");

		for(int i = 0; i < m; i++){
			for(int j = 0; j < m; j++){
				try {
					b[i][j] = disDown.readInt();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("Matrix B recieved from down.");
		MatrixMultiple.displayMatrix(b);
		System.out.println("------------------------------");

		int[][] temp = MatrixMultiple.multiplyMatrixCellWise(a, b);
		c = MatrixMultiple.addMatrixCellWise(c, temp);
		System.out.println("Updated c, by c =  c + a * b .");
		MatrixMultiple.displayMatrix(c, 5);
		System.out.println("------------------------------");



//		// shift matrix a toward left
//		int[] tempIn = new int[m];
//		int[] tempOut = new int[m];
//		if (nodeNum%2==0) { 		// Even # worker shifting procedure
//			for (int i = 0; i < m; i++) {
//				try {
//					dosLeft.writeInt(a[i][0]);
//				} catch (IOException ioe) {
//					System.out.println("error in sending to left, row=" + i);
//					ioe.printStackTrace();
//				}
//			}
//			// local shift
//			for (int i = 0; i < m; i++) {
//				for (int j = 1; j < m; j++) {
//					a[i][j-1] = a[i][j];
//				}
//			}
//			// receive the rightmost column
//			for (int i = 0; i < m; i++) {
//				try {
// 					a[i][m-1] = disRight.readInt();
//				} catch (IOException ioe) {
//					System.out.println("error in receiving from right, row=" + i);
//					ioe.printStackTrace();
//				}
//			}
//		} else { 					// Odd # worker shifting procedure
//			for (int i = 0; i < m; i++) {		// receive a column from right
//				try {
//					tempIn[i] = disRight.readInt();
//				} catch (IOException ioe) {
//					System.out.println("error in receiving from right, row=" + i);
//					ioe.printStackTrace();
//				}
//			}
//			for (int i = 0; i < m; i++) {		// local shift
//				tempOut[i] = a[i][0];
//			}
//			for (int i = 0; i < m; i++) {
//				for (int j = 1; j < m; j++) {
//					a[i][j-1] = a[i][j];
//				}
//			}
//			for (int i = 0; i < m; i++) {
//				a[i][m-1] = tempIn[i];
//			}
//			for (int i = 0; i < m; i++) {		// send leftmost column to left node
//				try {
//					dosLeft.writeInt(tempOut[i]);
//				} catch (IOException ioe) {
//					System.out.println("error in sending left, row=" + i);
//					ioe.printStackTrace();
//				}
//			}
//		}
//		System.out.println("Shifted matrix");
//		MatrixMultiple.displayMatrix(a);
		// shift b up omitted ...
	}

	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("usage: java Worker workerID worker-port-num coordinator-ip coordinator-port-num"); 
		} 
		int workerID = Integer.parseInt(args[0]); 
		int portNum = Integer.parseInt(args[1]);
		Worker worker = new Worker(workerID, portNum);
		worker.configurate(args[2], Integer.parseInt(args[3]));
		worker.compute();
		try {Thread.sleep(12000);} catch (Exception e) {e.printStackTrace();}
		System.out.println("Done.");
	}
}
