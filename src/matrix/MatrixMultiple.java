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
	
	// tester
	public static void main(String[] args) {
		int n = Integer.parseInt(args[0]);
		int[][] matrix = createDisplayMatrix(n); 
		System.out.println("display matrix"); 
		displayMatrix(matrix); 
		int[][] unitM = createUnitMatrix(n); 
		int[][] product = multiplyMatrix(matrix, unitM); 
		System.out.println("display matrix X unit matrix"); 
		displayMatrix(product); 
		if (compareMatrix(matrix, product)) System.out.println("Identical."); 
		int [][] randomM = createRandomMatrix(n); 
		System.out.println("random matrix"); 
		displayMatrix(randomM, 5);
		product = multiplyMatrix(matrix, randomM); 
		System.out.println("display matrix X random matrix"); 
		displayMatrix(product, 7); 
		int [][] product2 = multiplyMatrix(randomM, matrix); 
		System.out.println("random matrix X display matrix"); 
		displayMatrix(product2, 7); 
		if (compareMatrix(product, product2)) System.out.println("Identical."); 
	}
}
