package Dither;

public class BayerCalculator {
    /**
     * Recursively computes the Bayer matrix (index matrix) for ordered dithering.
     * The input dimension 'n' must be a power of 2 and at least 2.
     *
     * Recurrence relation:
     *   M(2n) = 4 * M(n) + D
     *
     * where D is determined by the quadrant:
     *   top-left:      add 0
     *   top-right:     add 2
     *   bottom-left:   add 3
     *   bottom-right:  add 1
     *
     * Base case: for n == 2, the 2x2 Bayer matrix is:
     *   [ [0, 2],
     *     [3, 1] ]
     *
     * @param n desired matrix dimension (n must be power of 2, n >= 2)
     * @return the computed Bayer matrix as an int[][]
     */
    public int[][] computeBayerMatrix(int n) {
        // Check if n is valid (power of two and at least 2)
        if (n < 2 || (n & (n - 1)) != 0) {
            throw new IllegalArgumentException("n must be a power of 2 and at least 2.");
        }
        
        // Base case: 2x2 matrix
        if (n == 2) {
            return new int[][] {
                {0, 2},
                {3, 1}
            };
        }
        
        // Recursively compute the smaller Bayer matrix of size (n/2 x n/2)
        int half = n / 2;
        int[][] smallerMatrix = computeBayerMatrix(half);
        int[][] result = new int[n][n];
        
        // For each element in the smaller matrix, create a 2x2 block in the result.
        for (int i = 0; i < half; i++) {
            for (int j = 0; j < half; j++) {
                int base = 4 * smallerMatrix[i][j];
                
                // Top-left quadrant: add 0
                result[i][j] = base;
                // Top-right quadrant: add 2
                result[i][j + half] = base + 2;
                // Bottom-left quadrant: add 3
                result[i + half][j] = base + 3;
                // Bottom-right quadrant: add 1
                result[i + half][j + half] = base + 1;
            }
        }
        
        return result;
    }
}