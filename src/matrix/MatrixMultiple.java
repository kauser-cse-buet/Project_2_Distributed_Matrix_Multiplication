package matrix;

public class MatrixMultiple {
	// create an n by n matrix for displaying
	public static int[][] createDisplayMatrix(int n) {
		int[][] matrix = new int[n][n];
		int up = (int)Math.pow(10, (int)Math.log10(n)+1); 
		for (int row = 1; row <= n; row++) {
			for (int col = 1; col <= n; col++) {
				matrix[row - 1][col - 1] = row * up + col;
			}
		}
		return matrix; 
	}

	public static int[][] getOddMatrix(int n){
		int[][] matrix = new int[n][n];

		int oddNumber = 1;
		for (int i = 0; i < n; i++){
			for (int j = 0; j < n; j++){
				matrix[i][j] = oddNumber;
				oddNumber += 2;
			}
		}

		return matrix;
	}

	public static int[][] getEvenMatrix(int n){
		int[][] matrix = new int[n][n];

		int evenNumber = 2;
		for (int i = 0; i < n; i++){
			for (int j = 0; j < n; j++){
				matrix[i][j] = evenNumber;
				evenNumber += 2;
			}
		}

		return matrix;
	}
	
	// create an n by n unit matrix  
	public static int[][] createUnitMatrix(int n) {
		int[][] matrix = new int[n][n];
		for (int row = 0; row < n; row++) {
			for (int col = 0; col < n; col++) {
				matrix[row][col] = 0;
			}
			matrix[row][row] = 1; 
		}
		return matrix; 
	}
	
	// create an n by n unit matrix  
	public static int[][] createRandomMatrix(int n) {
		int[][] matrix = new int[n][n];
		for (int row = 0; row < n; row++) {
			for (int col = 0; col < n; col++) {
				matrix[row][col] = (int)(Math.random()*1000);
			}
		}
		return matrix; 
	}
	
	// display n by n "display matrix"; n is limited to 660.
	public static void displayMatrix(int[][] mat) {
		int n = mat.length; 
		int m = mat[0].length; 
		if (n <= 660) {
			int digit = (int) Math.log10(n)*2+3;
			for (int row = 0; row < n; row++) {
				for (int col = 0; col < m; col++) {
					String numStr = String.format("%"+digit+"d", mat[row][col]);
					System.out.print(numStr);
				}
				System.out.println();
			}
		} else {
			System.out.println("The matrix is too big to display on screen.");
		}
	}

	public static int[][] shiftLeftAllRowsBy1(int[][] matrix){
		int[][] result = new int[matrix.length][matrix[0].length];

		for (int i = 0; i < matrix.length; i++){
			result[i] = shiftLeftSingleRow(matrix[i]);
		}

		return result;
	}
	
	// display n by n matrix with maximum value of d digits.
	public static void displayMatrix(int[][] mat, int d) {
		int n = mat.length; 
		int m = mat[0].length; 
		for (int row = 0; row < n; row++) {
			for (int col = 0; col < m; col++) {
				String numStr = String.format("%"+(d+2)+"d", mat[row][col]);
				System.out.print(numStr);
			}
			System.out.println();
		}
	}

	// a X b
	public static int[][] multiplyMatrix(int[][] a, int[][] b) {
		int n = a.length;
		int[][] c = new int[n][n]; 
		for (int row = 0; row < n; row++) { 
			for (int col = 0; col < n; col++) {
				c[row][col] = 0; 
				for (int i = 0; i < n; i++) {
					c[row][col] = c[row][col] + a[row][i] * b[i][col];
				}
			}
		} 
		return c; 
	}
	
	// compare a to b
	public static boolean compareMatrix(int[][] a, int[][] b) {
		int n = a.length;
		boolean result = true; 
		for (int row = 0; row < n; row++) { 
			for (int col = 0; col < n; col++) {
				if (a[row][col] != b[row][col]) {
					result = false; 
					System.out.println("row="+row+" col="+col + ":"+a[row][col]+"<-->"+b[row][col]); 
				}
			}
		} 
		return result; 
	}

//	public static void shiftLeft(int[][] a, int noOfColumnToShift){
//		for (int i = 0; i < a.length; i++){
//			for (int j = 0; j < a[0].length; j++){
//				if(i == 0){
//
//				}
//
//			}
//		}
//
//	}

	public static int[] shiftLeftSingleRow(int[] row){
		int[] rowCopy = row.clone();

		for(int i = 0; i < row.length; i++){
			if(i == row.length - 1){
				rowCopy[i] = row[0];
			}
			else{
				rowCopy[i] = row[i + 1];
			}
		}

		return rowCopy;
	}

	public static int[] shiftLeftSingleRowByN(int[] row, int n){
		int[] rowCopy = row.clone();

		for(int i = 0; i < n; i++){
			rowCopy = shiftLeftSingleRow(rowCopy);
		}

		return rowCopy;
	}
	

	public static void display1DArray(int[] n){
		for (int i = 0; i < n.length; i++){
			System.out.print(n[i] + " ");
		}
		System.out.println();

	}

	public static int[][] shiftLeftIncreasingly(int[][] matrix){
		int[][] matrixCopy = matrixClone(matrix);

		for(int i = 0; i < matrix.length; i++){
			matrixCopy[i] = shiftLeftSingleRowByN(matrix[i], i + 1);
		}

		return matrixCopy;
	}

	public static int[][] matrixClone(int[][] matrix){
		int[][] matrixCopy = new int[matrix.length][matrix[0].length];

		for(int i = 0; i < matrix.length; i++){
			for(int j = 0; j < matrix[0].length; j++){
				matrixCopy[i][j] = matrix[i][j];
			}
		}

		return matrixCopy;

	}

	public static int[][] shiftUpColumnNBy1(int[][] matrix, int columnIndex){
		int[][] matrixCopy = matrixClone(matrix);
		for(int i = 0; i < matrix.length; i++){
			if (i == matrix.length - 1){
				matrixCopy[i][columnIndex] = matrix[0][columnIndex];
			}
			else{
				matrixCopy[i][columnIndex] = matrix[i + 1][columnIndex];
			}
		}

		return matrixCopy;
	}

	public static int[][] shiftUpColumnNByJ(int[][] matrix, int columnIndex, int noOfShift){
		int[][] matrixCopy = matrixClone(matrix);

		for(int i = 0; i < noOfShift; i++){
			matrixCopy = shiftUpColumnNBy1(matrixCopy, columnIndex);
		}

		return matrixCopy;
	}

	public static int[][] shiftUpColumnIncreasingly(int[][] matrix){
		int[][] matrixCopy = matrixClone(matrix);

		for (int i = 0; i < matrix[0].length; i++){
			matrixCopy = shiftUpColumnNByJ(matrixCopy, i, i + 1);
		}

		return matrixCopy;
	}

	public static int[][] multiplyMatrixCellWise(int[][] a, int[][] b){
		int noOfRows = a.length;
		int noOfColumns = a[0].length;
		int[][] c = new int[noOfRows][noOfColumns];

		for (int i = 0; i < noOfRows; i++){
			for(int j = 0; j < noOfColumns; j++){
				c[i][j] = a[i][j] * b[i][j];
			}
		}

		return c;
	}

	public static int[][] addMatrixCellWise(int[][] a, int[][] b){
		int noOfRows = a.length;
		int noOfColumns = a[0].length;
		int[][] c = new int[noOfRows][noOfColumns];

		for (int i = 0; i < noOfRows; i++){
			for(int j = 0; j < noOfColumns; j++){
				c[i][j] = a[i][j] + b[i][j];
			}
		}

		return c;
	}


	public static int[][] shiftUpAllColumnBy1(int[][] matrix){
		int[][] result = matrixClone(matrix);

		for(int i = 0; i < matrix[0].length; i++){
			result =  shiftUpColumnNBy1(result, i);
		}

		return result;
	}


	// tester
	public static void main(String[] args) {
//		int n = Integer.parseInt(args[0]);
		int n = 8;
		int[][] matrixA = createDisplayMatrix(n);
		int[][] matrixB = createDisplayMatrix(n);
		displayMatrix(matrixA);
		System.out.println("-------------------------");
		displayMatrix(matrixB);
		System.out.println("-------------------------");
		displayMatrix(multiplyMatrix(matrixA, matrixB), 8);
//		System.out.println("display matrix");
//		displayMatrix(matrix);
//		int[][] unitM = createUnitMatrix(n);
//		int[][] product = multiplyMatrix(matrix, unitM);
//		System.out.println("display matrix X unit matrix");
//		displayMatrix(product);
//		if (compareMatrix(matrix, product)) System.out.println("Identical.");
//		int [][] randomM = createRandomMatrix(n);
//		System.out.println("random matrix");
//		displayMatrix(randomM, 5);
//		product = multiplyMatrix(matrix, randomM);
//		System.out.println("display matrix X random matrix");
//		displayMatrix(product, 7);
//		int [][] product2 = multiplyMatrix(randomM, matrix);
//		System.out.println("random matrix X display matrix");
//		displayMatrix(product2, 7);
//		if (compareMatrix(product, product2)) System.out.println("Identical.");

//		displayMatrix(matrix);
//		System.out.println("----------------");
////		int[][] matrixLeftShiftedIncreasingly = shiftLeftIncreasingly(matrix);
////		displayMatrix(matrixLeftShiftedIncreasingly);
//
////		int[][] matrixShiftColumnIncreasingly = shiftUpColumnIncreasingly(matrix);
////		displayMatrix(matrixShiftColumnIncreasingly);
//
////		displayMatrix(shiftLeftAllRowsBy1(matrix));
//		displayMatrix(shiftUpAllColumnBy1(matrix));
	}
}
