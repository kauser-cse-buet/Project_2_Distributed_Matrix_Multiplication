package distributed_system;

import matrix.MatrixMultiple;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Worker {

	int nodeNum;
	int localPort;
	Connection conn;
	int m;
	int dm;
	int[][] a;
	int[][] b;
	int[][] c;
	DataInputStream disCoor;
	DataOutputStream dosCoor;
	DataOutputStream dosLeft;
	DataInputStream disRight;

	DataOutputStream dosUp;
	DataInputStream disDown;
    private int numNodes;
    private int matrixRowGap = 6;
	private int workerArrayDim;


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
			dosCoor.writeUTF(getIp());
			dosCoor.writeInt(localPort);
			disCoor = dio.getDis();
			m = disCoor.readInt(); 				//get matrix dimension from coordinator
			dm = disCoor.readInt(); 				//get the whole matrix dimension from coordinator
            numNodes = disCoor.readInt();

			workerArrayDim = (int) (Math.sqrt(numNodes));
			System.out.println("# workerArrayDim: " + workerArrayDim);

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

            if (((nodeNum % workerArrayDim) % 2) == 0){
                dosLeft = conn.connect2write(ipLeft, portLeft);
                disRight = conn.accept2read();
            }
            else{
				disRight = conn.accept2read();
				dosLeft = conn.connect2write(ipLeft, portLeft);
            }

            int nodeRowNumber = (nodeNum / workerArrayDim);
            System.out.println("# nodeRowNumber: " + nodeRowNumber);
            if (nodeRowNumber % 2 == 0){
				dosUp = conn.connect2write(ipUp, portUp);
				disDown = conn.accept2read();
            }
            else{
				disDown = conn.accept2read();
				dosUp = conn.connect2write(ipUp, portUp);
            }

            System.out.println("dosLeft: " + dosLeft);
            System.out.println("disRight: " + disRight);
            System.out.println("dosUp: " + dosUp);
            System.out.println("disDown: " + disDown);

		} catch (IOException ioe) {
			ioe.printStackTrace();
		} 
		System.out.println("Configuration done."); 
	}


    void compute() throws Exception {
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
        System.out.println("A: ");
        MatrixMultiple.displayMatrix(a, matrixRowGap);
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
        System.out.println("B: ");
        MatrixMultiple.displayMatrix(b, matrixRowGap);
        System.out.println("-----------------");

        c = MatrixMultiple.multiplyMatrixCellWise(a, b);
        System.out.println("Matrix c: ");
        MatrixMultiple.displayMatrix(c, matrixRowGap);
        System.out.println("-----------------");

        for(int k = 1; k <= dm - 1; k++){

            // A:
            System.out.println("# Iteration number: " + k);

            int []columnToSend = new int[m];

            for(int i = 0; i < m; i++){
                columnToSend[i] = a[i][0];
            }

            // B:
            int[] rowToSend = new int[m];

            for(int i = 0; i < m; i++){
                rowToSend[i] = b[0][i];
            }

            int[] columnRecieved = new int[m];
            int[] rowRecieved = new int[m];

            sendIntArray(columnToSend, dosLeft, m, MatrixName.A);
            System.out.print("# Column send to left node.");
            displayColumn(columnToSend);
            sendIntArray(rowToSend, dosUp, m, MatrixName.B);
            System.out.println("#B: Send 1st row to up.");
            displayRow(rowToSend);

            WorkerMessage message1 = recieveMessage(disDown);
            if (message1.matrixName == MatrixName.A.asChar()){
                columnRecieved = message1.intArray;
                System.out.println("A: recieve last column from right node.");
                displayColumn(columnRecieved);
            }
            else if(message1.matrixName == MatrixName.B.asChar()){
                rowRecieved = message1.intArray;
                System.out.println("#B: Recieved last row from down: ");
                displayRow(rowRecieved);
            }
            else {
                throw new Exception("Nothing recieved");
            }

            WorkerMessage message2 = recieveMessage(disRight);

            if (message2.matrixName == MatrixName.A.asChar()){
                columnRecieved = message2.intArray;
                System.out.println("A: recieve last column from right node.");
                displayColumn(columnRecieved);
            }
            else if(message2.matrixName == MatrixName.B.asChar()){
                rowRecieved = message2.intArray;
                System.out.println("#B: Recieved last row from down: ");
                displayRow(rowRecieved);
            }
            else {
                throw new Exception("Nothing recieved");
            }

            a = MatrixMultiple.shiftLeftAllRowsBy1(a);

            System.out.println("A: Shifted left.");
            MatrixMultiple.displayMatrix(a, matrixRowGap);
            System.out.println("--------------------------");

//			recieve last column from right node.
            for(int i = 0; i < m; i++){
                a[i][m - 1] = columnRecieved[i];
            }

            System.out.println("# A: ");
            MatrixMultiple.displayMatrix(a, matrixRowGap);
            System.out.println("------------------------------");

//			B Matrix

//		send first row to up
            System.out.println("#B: Sending 1st row to up: ");


            System.out.println("B:");
            MatrixMultiple.displayMatrix(b, matrixRowGap);
            System.out.println("-------------------------------------");

            b = MatrixMultiple.shiftUpAllColumnBy1(b);
            System.out.println("B: Shift up all column by 1");
            MatrixMultiple.displayMatrix(b, matrixRowGap);
            System.out.println("-------------------------------------");



            for(int i = 0; i < m; i++){
                b[m - 1][i] = rowRecieved[i];
            }


            System.out.println("B: Last row recieved from down.");
            MatrixMultiple.displayMatrix(b, matrixRowGap);
            System.out.println("------------------------------");

            int[][] temp = MatrixMultiple.multiplyMatrixCellWise(a, b);
            System.out.println("# A * B");
            MatrixMultiple.displayMatrix(temp, matrixRowGap);
            System.out.println("------------------------------");

            c = MatrixMultiple.addMatrixCellWise(c, temp);
            System.out.println("Updated c, by c =  c + a * b .");
            MatrixMultiple.displayMatrix(c, matrixRowGap);
            System.out.println("------------------------------");

            System.out.println("-------------------------# Iteration end.--------------------: " + k);
        }

        //			send result to coordinator
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < m; i++){
            for(int j = 0; j < m; j++){
                try {
                    dosCoor.writeInt(c[i][j]);
                    dosCoor.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private WorkerMessage recieveMessage(DataInputStream dis) {
        int messageLength = 0;
        WorkerMessage messageRecieved = null;
        try {
            messageLength = dis.readInt();
            byte[] bytes = new byte[messageLength];
            dis.read(bytes);
            messageRecieved = new WorkerMessage(new String(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return messageRecieved;
    }

    private void displayRow(int[] rowRecieved) {
        System.out.println("Row: ");
        for (int i = 0; i < rowRecieved.length; i++){
            System.out.print(rowRecieved[i]);
            System.out.print(" ");
        }
        System.out.println();
    }

    private void displayColumn(int[] rowRecieved) {
        System.out.println("Column: ");
        for (int i = 0; i < rowRecieved.length; i++){
            System.out.println(rowRecieved[i]);
        }
    }

    private int[] recieveIntArray(DataInputStream dis, MatrixName m) {
        WorkerMessage messageRecieved = null;
        try {
            do{
                int messageLength = dis.readInt();
                byte[] bytes = new byte[messageLength];
                dis.read(bytes);
                messageRecieved = new WorkerMessage(new String(bytes));
            }while (messageRecieved.matrixName != m.asChar());

//            return messageRecieved.intArray;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return messageRecieved.intArray;
    }

    private void sendIntArray(int[] ints, DataOutputStream dos, int m, MatrixName matrixName) {
        WorkerMessage workerMessage = new WorkerMessage(matrixName, ints);
        String workerMessageStr = workerMessage.toString();

        try {
            int workerMessageLength = workerMessageStr.length();
            dos.writeInt(workerMessageLength);
            dos.flush();
            dos.write(workerMessageStr.getBytes(Charset.forName("UTF-8")));
            dos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("usage: java Worker workerID worker-port-num coordinator-ip coordinator-port-num");
		} 
		int workerID = Integer.parseInt(args[0]); 
		int portNum = Integer.parseInt(args[1]);
		Worker worker = new Worker(workerID, portNum);
		worker.configurate(args[2], Integer.parseInt(args[3]));
        try {
            worker.compute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {Thread.sleep(12000);} catch (Exception e) {e.printStackTrace();}
		System.out.println("Done.");
	}

	String getIp(){
		URL whatismyip = null;
		try {
			whatismyip = new URL("http://checkip.amazonaws.com");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		BufferedReader in = null;
		try {
			try {
				in = new BufferedReader(new InputStreamReader(
                        whatismyip.openStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			String ip = null;
			try {
				ip = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ip;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}


class WorkerMessage {
    char matrixName;
    int[] intArray;

    public WorkerMessage(MatrixName arrayName, int[] intArray){
        this.matrixName = arrayName.asChar();
        this.intArray = intArray;
    }

    public WorkerMessage(String message){
        String[] messageSplittedArray = message.split(":");

        if(messageSplittedArray.length == 2){
            matrixName = messageSplittedArray[0].charAt(0);
            intArray = parseStringToIntArray(messageSplittedArray[1]);
        }
    }

    public int[] parseStringToIntArray(String intArrayStr){
        String[] stringSplittedArray = intArrayStr.replace("[", "").replace("]", "").split(",");
        int[] convertedIntArray = new int[stringSplittedArray.length];
        for (int i = 0; i < stringSplittedArray.length; i++){
            convertedIntArray[i] = Integer.parseInt(stringSplittedArray[i].trim());
        }

        return convertedIntArray;
    }

    @Override
    public String toString() {
        return matrixName + ":" + Arrays.toString(intArray);
    }
}

enum MatrixName{
    A('A'), B('B');

    public char asChar() {
        return asChar;
    }

    private final char asChar;

    private MatrixName(char asChar) {
        this.asChar = asChar;
    }
}