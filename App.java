// Munira Momim

package com.mycompany.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Random;
import java.util.function.IntBinaryOperator;

public class App {

    // data structure that describes a region in a potentially larger matrix. This allows algorithms that operate on matrix to also support
    // operating on a region of a larger matrix to avoid making copies of that subsection, they can read/write in-place.
    public class MatrixSlice
    {
        public int[][] matrix;
        public int rowStart;
        public int columnStart;
        public int rowLength;
        public int columnLength;

        public MatrixSlice(int[][] matrix, int rowStart, int columnStart, int rowLength, int columnLength)
        {
            this.matrix = matrix;
            this.rowStart = rowStart;
            this.columnStart = columnStart;
            this.rowLength = rowLength;
            this.columnLength = columnLength;
        }

        public MatrixSlice(MatrixSlice matrix, int rowStart, int columnStart, int rowLength, int columnLength){
            this(matrix.matrix, rowStart + matrix.rowStart, columnStart + matrix.columnStart, rowLength, columnLength);
        }

        private int get(int row, int column){
            return this.matrix[rowStart + row][columnStart + column];
        }

        private void set(int row, int column, int value){
            this.matrix[rowStart + row][columnStart + column] = value;
        }
    }

    // MatrixSlize can refer to any subsection of a bigger matrix, but sometimes we want to refer to the entire matrix, so this
    // helper function makes it easy to describe the entire thing.
    private MatrixSlice fullMatrixSlize(int[][] matrix)
    {
        return new MatrixSlice(matrix, 0, 0, matrix.length, matrix[0].length);
    }

    // Represents the result of a binary matrix arithmetic operation with count of operations performed. 
    public class MatrixOperationResult
    {
        public String operationName;
        public MatrixSlice matrixA;
        public MatrixSlice matrixB;
        public MatrixSlice result;

        public long countMultiplications;
        public long countAdditions;

        // public int millisecondsTaken;

        public MatrixOperationResult(String operationName, MatrixSlice matrixA, MatrixSlice matrixB, MatrixSlice result, long countMultiplications, long countAdditions)
        {
            this.operationName = operationName;
            this.matrixA = matrixA;
            this.matrixB = matrixB;
            this.result = result;
            this.countMultiplications = countMultiplications;
            this.countAdditions = countAdditions;
        }
    }

    public static void main(String[] args) throws NumberFormatException, IOException, Exception {
        var app = new App();

        // app.generateBenchmarkingExample("manysizes.txt", 10);

        String filename = "manysizes.txt"; // "LabStrassenInput.txt"
        if (args.length == 1){
            filename = args[0].trim();
        }
        else {
            throw new Exception("Expecting one cmd line argument with filename");
        }

        app.execute(filename);
    }
    
    public void execute(String filename) throws NumberFormatException, IOException, Exception {
        File sourceFile = new File(filename);

        System.out.println("Full output in 'output.txt'");
        
        try (Scanner fileScanner = new Scanner(sourceFile))
        {
            try (var outputFile = new PrintWriter("output.txt", "UTF-8")) {
                
                while (fileScanner.hasNextLine()) {
                    String nextLine = fileScanner.nextLine().trim();
                    if(nextLine.isBlank()){
                        continue;
        
                    }
                    Integer matrixOrder = Integer.parseInt(nextLine);
                    
                    var matrixA = readMatrixFromScanner(fileScanner, matrixOrder);
                    var matrixB = readMatrixFromScanner(fileScanner, matrixOrder);
        
                    MatrixOperationResult squareMatrixMultiplyResult = squareMatrixMultiply(matrixA, matrixB);
    
                    MatrixOperationResult strassenResult = strassenMultiply(matrixA, matrixB);

                    writeResult(outputFile, squareMatrixMultiplyResult, strassenResult);
                }
            }
        }

        System.out.println("Full output in 'output.txt'");
    }

    // Reads a single matrix from an already opened file Scanner. Fails it the number of rows or number of columns is different than expectedOrder
    private MatrixSlice readMatrixFromScanner(Scanner fileScanner, Integer expectedOrder) throws NumberFormatException, IOException, Exception {
        int[][] result = new int[expectedOrder][expectedOrder];

        for (int currentRowOrder = 0; currentRowOrder< expectedOrder; currentRowOrder++) {
            if(!fileScanner.hasNextLine())
            {
                throw new Exception("file ended too soon");
            }

            String line = fileScanner.nextLine();
            String[] lineItems = line.trim().split(" ");
            lineItems = Arrays.stream(lineItems).filter(s -> !s.trim().isBlank()).toArray(String[]::new);

            if (lineItems.length != expectedOrder) {
                throw new Exception(String.format("Row had different length than expected. Expected: %s, Got: %s, Line: %s", expectedOrder, lineItems.length, line));
            }

            for (int currentColumnOrder = 0; currentColumnOrder < lineItems.length; currentColumnOrder++) {
                Integer parsedCell = Integer.parseInt(lineItems[currentColumnOrder]);
                result[currentRowOrder][currentColumnOrder] = parsedCell;
            }
        }

        return fullMatrixSlize(result);
    }

    private void generateBenchmarkingExample(String filename, int maxMatrixOrderPower) throws IOException {
        Random random = new Random(1234567);

        try (var writer = new PrintWriter(filename, "UTF-8")) {
            for (int matrixOrderPower = 0; matrixOrderPower <= maxMatrixOrderPower; matrixOrderPower++){
                var matrixOrder = (int)Math.pow(2, matrixOrderPower);
                writer.println(matrixOrder); //print matrix order into its own line

                for (int matrixNumber = 0; matrixNumber < 2; matrixNumber++) { //two matrices per size
                    for (int row = 0; row < matrixOrder; row++) {
                        for (int column = 0; column < matrixOrder; column++) {
                            writer.print(String.format("%s ", random.nextInt(21) - 10));
                        }
                        writer.println(); //newlne at the end of the row
                    }
                }
                writer.println(); //empty line between each order
            }
        }
    }

     // Naive n cubed matrix multiplication algorithm
     private MatrixOperationResult squareMatrixMultiply(MatrixSlice matrixA, MatrixSlice matrixB)
     {
         int[][] result = new int[matrixA.rowLength][matrixB.columnLength];
         int countMultiplicationsPerformed = 0;
 
         for (int resultRow = 0; resultRow < matrixA.rowLength; resultRow++)
         {
             for (int resultColumn = 0; resultColumn < matrixB.columnLength; resultColumn++)
             {
                 result[resultRow][resultColumn] = 0;
                 for (int i = 0; i < matrixA.columnLength; i++)
                 {
                     result[resultRow][resultColumn] += matrixA.get(resultRow,i) * matrixB.get(i, resultColumn);
                     countMultiplicationsPerformed++;
                 }
             }
         }
 
         return this.new MatrixOperationResult("Square Matrix Multiply", matrixA, matrixB, fullMatrixSlize(result), countMultiplicationsPerformed, 0);
     }

    // recursive matrix multiplication algorithm with divide & conquer
    private MatrixOperationResult strassenMultiply(MatrixSlice matrixA, MatrixSlice matrixB) {
        int[][] result = new int[matrixA.rowLength][matrixB.columnLength];
        long countMultiplications = 0;
        long countAdditions = 0;

        // recursive escape case, multiply two scalars
        if (matrixA.rowLength == 1) {
            result[0][0] = matrixA.get(0, 0) * matrixB.get(0, 0);
            return new MatrixOperationResult("Base case multiplication", matrixA, matrixB, fullMatrixSlize(result), 1, 0);
        }

        int halfN = matrixA.rowLength / 2;

        // Step 1
        //  Each of A and B get described in four evenly divided quadrants
        //  Using MatrixSlice to "point at" sections of a larger matrix to avoid copies
        MatrixSlice a11 = this.new MatrixSlice(matrixA, 0, 0, halfN, halfN);
        MatrixSlice a12 = this.new MatrixSlice(matrixA, 0, halfN, halfN, halfN);
        MatrixSlice a21 = this.new MatrixSlice(matrixA, halfN, 0, halfN, halfN);
        MatrixSlice a22 = this.new MatrixSlice(matrixA, halfN, halfN, halfN, halfN);

        MatrixSlice b11 = this.new MatrixSlice(matrixB, 0, 0, halfN, halfN);
        MatrixSlice b12 = this.new MatrixSlice(matrixB, 0, halfN, halfN, halfN);
        MatrixSlice b21 = this.new MatrixSlice(matrixB, halfN, 0, halfN, halfN);
        MatrixSlice b22 = this.new MatrixSlice(matrixB, halfN, halfN, halfN, halfN);

        // Step 2
        var s1 = matrixSubtraction(b12, b22);
        var s2 = matrixAddition(a11, a12);
        var s3 = matrixAddition(a21, a22);
        var s4 = matrixSubtraction(b21, b11);
        var s5 = matrixAddition(a11, a22);
        var s6 = matrixAddition(b11, b22);
        var s7 = matrixSubtraction(a12, a22);
        var s8 = matrixAddition(b21, b22);
        var s9 = matrixSubtraction(a11, a21);
        var s10 = matrixAddition(b11, b12);

        countAdditions += s1.countAdditions + s2.countAdditions + s3.countAdditions + s4.countAdditions + s5.countAdditions
             + s6.countAdditions + s7.countAdditions + s8.countAdditions + s9.countAdditions + s10.countAdditions;

        // Step 3
        var p1 = strassenMultiply(a11, s1);
        var p2 = strassenMultiply(s2, b22);
        var p3 = strassenMultiply(s3, b11);       
        var p4 = strassenMultiply(a22, s4);
        var p5 = strassenMultiply(s5, s6);
        var p6 = strassenMultiply(s7, s8);
        var p7 = strassenMultiply(s9, s10);

        countMultiplications += p1.countMultiplications + p2.countMultiplications + p3.countMultiplications 
        + p4.countMultiplications + p5.countMultiplications + p6.countMultiplications + p7.countMultiplications;

        countAdditions += p1.countAdditions + p2.countAdditions + p3.countAdditions 
        + p4.countAdditions + p5.countAdditions + p6.countAdditions + p7.countAdditions;
        
        // Step 4
        //  Describe the four quadrants of the result as C sections
        MatrixSlice c11 = this.new MatrixSlice(result, 0, 0, halfN, halfN);
        MatrixSlice c12 = this.new MatrixSlice(result, 0, halfN, halfN, halfN);
        MatrixSlice c21 = this.new MatrixSlice(result, halfN, 0, halfN, halfN);
        MatrixSlice c22 = this.new MatrixSlice(result, halfN, halfN, halfN, halfN);

        MatrixOperationResult resultC; // temp store operation result to accumulate operation count

        // C11
        resultC = matrixAddition(p5.result, p4.result, c11);
        countAdditions += resultC.countAdditions;
        resultC = matrixSubtraction(c11, p2.result, c11);
        countAdditions += resultC.countAdditions;
        resultC = matrixAddition(c11, p6.result, c11);
        countAdditions += resultC.countAdditions;

        // C12
        resultC = matrixAddition(p1.result, p2.result, c12);
        countAdditions += resultC.countAdditions;

        // C21
        resultC = matrixAddition(p3.result, p4.result, c21);
        countAdditions += resultC.countAdditions;

        // C22
        resultC = matrixAddition(p5.result, p1.result, c22);
        countAdditions += resultC.countAdditions;
        resultC = matrixSubtraction(c22, p3.result, c22);
        countAdditions += resultC.countAdditions;
        resultC = matrixSubtraction(c22, p7.result, c22);
        countAdditions += resultC.countAdditions;

        return this.new MatrixOperationResult("Strassen", matrixA, matrixB, fullMatrixSlize(result), countMultiplications, countAdditions);
    }

    // Override of Strassen that makes computation of S matrices more readable
    private MatrixOperationResult strassenMultiply(MatrixOperationResult matrixA, MatrixOperationResult matrixB) {
        return strassenMultiply(matrixA.result, matrixB.result);
    }

    // Override of Strassen that just to make computation of p1 & p4 more readable.
    private MatrixOperationResult strassenMultiply(MatrixSlice matrixA, MatrixOperationResult matrixB) {
        return strassenMultiply(matrixA, matrixB.result);
    }

    // Override of Strassen just to make computation of p2 & p3 more readable.
    private MatrixOperationResult strassenMultiply(MatrixOperationResult matrixA, MatrixSlice matrixB) {
        return strassenMultiply(matrixA.result, matrixB);
    }

    // General implementation of some 2d matrix binary function application from two source subsections of matrices into some third section
    private MatrixOperationResult matrixOperation(MatrixSlice matrixSliceA, MatrixSlice matrixSliceB, MatrixSlice result, IntBinaryOperator operator) {
        // TODO assert that matrixSlizeA and matrixSlizeB are the same dimensions
        long countAdditions = 0;

        for (int row = 0; row < matrixSliceA.rowLength; row++) {
            for (int column = 0; column < matrixSliceA.columnLength; column++) {

                var elementResult = operator.applyAsInt(
                    matrixSliceA.get(row, column),
                    matrixSliceB.get(row, column));

                result.set(row, column, elementResult);

                countAdditions++;
            }
        }
        return new MatrixOperationResult("Addition/Subtraction", matrixSliceA, matrixSliceB, result, 0, countAdditions);
    }

    // Overload of matrix addition that creates a new array to store the result, used in computation of s1-10
    private MatrixOperationResult matrixAddition(MatrixSlice matrixSliceA, MatrixSlice matrixSliceB) {
        return matrixOperation(matrixSliceA, matrixSliceB, fullMatrixSlize(new int[matrixSliceA.rowLength][matrixSliceA.columnLength]), (val1, val2) -> val1 + val2);
    }

    // Overload of matrix subtraction that creates a new array to store the result, used in computation of s1-10
    private MatrixOperationResult matrixSubtraction(MatrixSlice matrixSliceA, MatrixSlice matrixSliceB) {
        return matrixOperation(matrixSliceA, matrixSliceB, fullMatrixSlize(new int[matrixSliceA.rowLength][matrixSliceA.columnLength]), (val1, val2) -> val1 - val2);
    }

    // Overload of matrix addition that stores the result in an existing array subsection, used in computation of C sections
    private MatrixOperationResult matrixAddition(MatrixSlice matrixSliceA, MatrixSlice matrixSliceB, MatrixSlice result) {
        return matrixOperation(matrixSliceA, matrixSliceB, result, (val1, val2) -> val1 + val2);
    }

    // Overload of matrix subtraction that stores the result in an existing array subsection, used in computation of C sections
    private MatrixOperationResult matrixSubtraction(MatrixSlice matrixSliceA, MatrixSlice matrixSliceB, MatrixSlice result) {
        return matrixOperation(matrixSliceA, matrixSliceB, result, (val1, val2) -> val1 - val2);
    }

    private void writeResult(
        PrintWriter outputFile,
        MatrixOperationResult squareMatrixMultiplyResult,
        MatrixOperationResult strassenResult) {

            String resultSummary = String.format(
                "MatrixOrder=%s\t\tSquareCountMultiplications=%s\t\tStrassenCountMultiplications=%s\t\tStrassenCountAddtions=%s",
                squareMatrixMultiplyResult.matrixA.rowLength,
                squareMatrixMultiplyResult.countMultiplications,
                strassenResult.countMultiplications,
                strassenResult.countAdditions);

            System.out.println(resultSummary);
            outputFile.println(resultSummary);

            outputFile.println("Input A");
            writeMatrix(outputFile, squareMatrixMultiplyResult.matrixA);
            
            outputFile.println("Input B");
            writeMatrix(outputFile, squareMatrixMultiplyResult.matrixB);
            
            outputFile.println("Square Multiply result");
            writeMatrix(outputFile, squareMatrixMultiplyResult.result);

            outputFile.println("Strassen result");
            writeMatrix(outputFile, strassenResult.result);
            
            outputFile.println();
    }

    public static void writeMatrix(PrintWriter outputFile, MatrixSlice matrix) {
        for (int row = 0; row < matrix.rowLength; row++) {
            for (int column = 0; column < matrix.columnLength; column++) {
                outputFile.print(matrix.get(row, column) + "\t");
            }
            outputFile.println();
        }
        outputFile.println();
    }
}