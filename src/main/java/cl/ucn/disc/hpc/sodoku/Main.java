package cl.ucn.disc.hpc.sodoku;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

/**
 * The Main class
 */
@Slf4j
public class Main {
    // The paths of the text files from which we will get our sudoku
    private final static String NINE_EASY_PATH = "src/main/resources/nine_easy.txt";
    private final static String NINE_NORMAL_PATH = "src/main/resources/nine_normal.txt";
    private final static String NINE_HARD_PATH = "src/main/resources/nine_hard.txt";
    private final static String NINE_VERY_HARD_PATH = "src/main/resources/nine_very_hard.txt";
    private final static String SIXTEEN_NORMAL_PATH = "src/main/resources/sixteen_normal.txt";
    private final static String SIXTEEN_VERY_HARD_PATH = "src/main/resources/sixteen_very_hard.txt";

    // Cores to solve the sudoku using parallelism
    private final static int cores = 4;

    /**
     * Main program method.
     * @param args args.
     */
    public static void main(String[] args) {

        // Try to solve the sudoku
        SolveSudoku(cores, NINE_NORMAL_PATH);
    }

    public static Sodoku SudokuFromTextFile(String path) {
        try {
            File file = new File(path);
            Scanner scanner = new Scanner(file);

            // Get the size
            int size = Integer.parseInt(scanner.nextLine());

            // Initialize an int matrix for the sodoku values
            int [][] grid = new int[size][size];

            // Fill the grid
            int row = 0;
            while (scanner.hasNextLine()){
                String[] line = scanner.nextLine().split(" ");
                for (int i = 0; i < size; i++){
                    grid[row][i] = Integer.parseInt(line[i]);
                }
                row++;
            }

            return new Sodoku(grid);

        }catch (FileNotFoundException e){
            log.error("File not found!");
            return null;
        }
    }

    /**
     * Try to solve a sudoku.
     * @param cores how many cores will be used by brutal force.
     * @param path the path of the .txt file with the sudoku.
     */
    private static void SolveSudoku(final int cores, String path){
        // Build a sudoku from a text file
        Sodoku mainSudoku = SudokuFromTextFile(path);
        // Did it find the text file?
        if (mainSudoku == null){
            return;
        }

        log.debug("Initial state (all possible values for each cell):");
        mainSudoku.PrintCells();
        log.debug("Trying to solve using reductions..." + "\n");

        // Take the time
        StopWatch stopWatch = StopWatch.createStarted();

        // Reduction depends on only one thread
        if (SolveSudokuByReductions(mainSudoku)){
            long time = stopWatch.getTime(TimeUnit.MILLISECONDS);
            mainSudoku.PrintCells();
            log.debug("Sudoku solved using reduction in {} ms!", time);
            return;
        }

        log.debug("Reductions were not enough to solve: " + "\n");
        mainSudoku.PrintCells();
        log.debug("Trying to solve using brutal force..." + "\n");


        // If reduction did not solve the sudoku we have to use brutal force
        if (SolveSudokuByBruteForce(mainSudoku)){
            long time = stopWatch.getTime(TimeUnit.MILLISECONDS);
            log.debug("Sudoku was solved using brutal force in {} ms", time);
            return;
        }

        long time = stopWatch.getTime(TimeUnit.MILLISECONDS);
        log.debug("Brutal force was not enough to solve: " + "\n");
        mainSudoku.PrintCells();
        // Sudoku was not solved :c
        log.debug("Sudoku was not solved | Failed at {} ms", time);
    }

    /**
     * Use reductions techniques to solve the sudoku.
     * @param sudoku the sudoku.
     * @return true if the sudoku was solved.
     */
    private static boolean SolveSudokuByReductions(Sodoku sudoku){
        return sudoku.TryToSolve();
    }

    /**
     * Use brute force to solve the sudoku with parallelism.
     * @param sudoku the sudoku.
     * @return true if the sudoku was solved.
     */
    public static boolean SolveSudokuByBruteForce(Sodoku sudoku){
        // Stack to store all possible combinations for each cell with possible values
        Stack<Sodoku> toTry = new Stack<>();

        // First, for each cell with N >= 2 possible values we are going to store in the stack N possible sodoku
        GetAndStorePossibilities(sudoku, toTry);

        // Then we try to solve each possibility until the stack is empty
        while (!toTry.empty()){
            Sodoku solved = TryToSolveSudoku(toTry.pop(), toTry);
            if (solved != null){
                solved.PrintCells();
                return true;
            }
        }

        return false;
    }

    /**
     * Given a sudoku find the first cell with possible values, then for each value create a clone of the sudoku
     * and try to solve it.
     * @param sudoku the sudoku.
     * @param toTry the stack where we are going to store the possible sudoku solutions.
     */
    public static void GetAndStorePossibilities(Sodoku sudoku, Stack<Sodoku> toTry){
        for (int i = 0; i < sudoku.GetCells().length; i++){
            for (int j = 0; j < sudoku.GetCells().length; j++){
                Cell cell = sudoku.GetCells()[i][j];
                if (!cell.HasOnlyOnePossibleValue()){
                    for (int k = 0; k < cell.GetPossibleValues().size(); k++){
                        CreateCloneAndStoreSudoku(cell.GetPossibleValues().get(k), i, j, sudoku, toTry);
                    }
                    return;
                }
            }
        }
    }

    /**
     * Create a copy of a sudoku but modifying a cell value. Then store the copy int the toTry Stack.
     * @param value the new value for the cell.
     * @param cellToChangeRow the row of the cell.
     * @param cellToChangeColumn the column of the cell.
     * @param sudoku the original sudoku.
     * @param toTry the stack.
     */
    public static void CreateCloneAndStoreSudoku(int value, int cellToChangeRow, int cellToChangeColumn, Sodoku sudoku, Stack<Sodoku> toTry){
        // Creates a clone of the sudoku
        Sodoku clone = (Sodoku) sudoku.GetClone();
        // Get the cell and change it value
        Cell cell = clone.GetCell(cellToChangeRow, cellToChangeColumn);
        cell.RemovePossibleValueExceptOne(value);
        // Stores the cell in the stack
        toTry.push(clone);
    }

    /**
     * Try to use reduction to solve the sudoku. It may origin new possibilities to try and store in the stack.
     * @param sudoku the sudoku to solve.
     * @param toTry the stack.
     * @return the sudoku if it was solved.
     */
    public static Sodoku TryToSolveSudoku(Sodoku sudoku, Stack<Sodoku> toTry){
        // Use reduction to try to solve the sudoku
        if (sudoku.TryToSolve()){
            return sudoku;
        }
        // If not, find a new cell with possible values
        GetAndStorePossibilities(sudoku, toTry);
        return null;
    }
}
