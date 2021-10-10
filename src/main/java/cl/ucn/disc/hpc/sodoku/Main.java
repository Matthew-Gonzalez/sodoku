package cl.ucn.disc.hpc.sodoku;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /**
     * Main program method.
     * @param args args.
     */
    public static void main(String[] args) {
        // Number of cores
        int cores = 5;
        int attempts = 40;

        log.debug("Start program...");
        log.debug("Cores: {}", cores);
        log.debug("");

        Sodoku sodoku = SudokuFromTextFile(SIXTEEN_NORMAL_PATH);
        if (sodoku == null){
            return;
        }
        SolveSudokuSpeedTest(sodoku, cores, attempts);

        log.debug("");
        log.debug("Program finished...");
    }

    /**
     * Solve a sudoku multiple times to test the speed of the algorithm using N number of cores.
     * @param sudoku the sudoku.
     * @param cores the number of cores (threads) to use.
     * @param attempts the number of tests.
     */
    private static void SolveSudokuSpeedTest(Sodoku sudoku, int cores, int attempts){
        // A list to store the times
        List<Long> times = new ArrayList<>();

        log.debug("Solving sudoku with {} cores in {} attempts...", cores, attempts);

        // Solving sudoku N times
        for (int i = 0; i < attempts; i++){
            long timeToComplete = SolveSudoku(sudoku, cores);
            times.add(timeToComplete);
            // Time measurement is unstable
            /*
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }*/
        }

        // Removes min extreme value
        long minTime = Collections.min(times);
        log.debug("Min time: {} ms", minTime);
        times.remove(minTime);

        // Removes max extreme value
        long maxTime = Collections.max(times);
        log.debug("Max time: {} ms", maxTime);
        times.remove(maxTime);

        // There are some times when the time is 0, and it does not have any sense
        while(times.contains(Integer.toUnsignedLong(0))){
            int index = times.indexOf(Integer.toUnsignedLong(0));
            if (index >= 0){
                times.remove(index);
            }
        }

        log.debug("Times: {}", times);

        // Calculates the average time
        double averageTime = times.stream().mapToLong(n -> n).average().getAsDouble();
        log.debug("Average time: {} ms", averageTime);
        log.debug("");
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
     * @param sudoku the sudoku.
     * @param cores how many cores will be used by brutal force.
     * @return the time it took to solve the sudoku.
     */
    private static long SolveSudoku(Sodoku sudoku, int cores){

        //log.debug("Initial state (all possible values for each cell):");
        //sudoku.PrintCells();
        //log.debug("Trying to solve using reductions..." + "\n");

        // Take the timeToComplete
        StopWatch stopWatch = StopWatch.createStarted();

        // Reduction depends on only one thread
        if (SolveSudokuByBruteForce(sudoku)){
            stopWatch.stop();
            long timeToComplete = stopWatch.getTime(TimeUnit.MILLISECONDS);
            //sudoku.PrintCells();
            //log.debug("Sudoku solved using reduction in {} ms!", timeToComplete);
            return timeToComplete;
        }

        //log.debug("Reductions were not enough to solve: " + "\n");
        //sudoku.PrintCells();
        //log.debug("Trying to solve using brutal force..." + "\n");

        // If reduction did not solve the sudoku we have to use brutal force
        if (cores == 1){
            if (SolveSudokuByBruteForce(sudoku)){
                stopWatch.stop();
                long timeToComplete = stopWatch.getTime(TimeUnit.MILLISECONDS);
                //sudoku.PrintCells();
                //log.debug("Sudoku was solved using brutal force in {} ms", timeToComplete);
                return timeToComplete;
            }
        }else{
            if (ParallelSolveSudokuByBruteForce(sudoku, cores)){
                stopWatch.stop();
                long timeToComplete = stopWatch.getTime(TimeUnit.MILLISECONDS);
                //sudoku.PrintCells();
                //log.debug("Sudoku was solved using brutal force in {} ms", timeToComplete);
                return timeToComplete;
            }
        }

        stopWatch.stop();
        long timeToComplete = stopWatch.getTime(TimeUnit.MILLISECONDS);
        //log.debug("Brutal force was not enough to solve: " + "\n");
        //sudoku.PrintCells();
        // Sudoku was not solved :c
        //log.debug("Sudoku was not solved | Failed at {} ms", timeToComplete);

        return timeToComplete;
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
            // A solution was found
            if (solved != null){
                //solved.PrintCells();
                return true;
            }
        }

        return false;
    }

    /**
     * Use brute force and parallelism to solve the sudoku with parallelism.
     * @param sudoku the sudoku.
     * @return true if the sudoku was solved.
     */
    private static boolean ParallelSolveSudokuByBruteForce(Sodoku sudoku, int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // Stack to store all possible combinations for each cell with possible values
        final Stack<Sodoku> toTry = new Stack<>();
        // A solution was found ?
        AtomicBoolean solutionWasFound = new AtomicBoolean(false);

        // First, for each cell with N >= 2 possible values we are going to store in the stack N possible sodoku
        GetAndStorePossibilities(sudoku, toTry);

        // Then we try to solve each possibility until the stack is empty
        while (!toTry.empty()){

            //final Sodoku toSolve = toTry.pop();

            // Try to safe thread use
            try{
                if (!executorService.isShutdown()){
                    // Thread to try to solve the sudoku
                    executorService.submit(() -> {
                       Sodoku solved = TryToSolveSudoku(toTry.pop(), toTry);
                       // A solution was found
                       if (solved != null){
                           solutionWasFound.compareAndSet(false, true);
                           //solved.PrintCells();
                           // Shutdown other threads
                           executorService.shutdownNow();
                       }
                    });
                }else{
                    break;
                }
            }catch (RejectedExecutionException e){
                // Another thread finds the solution already
                break;
            }
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel SolveSudokuByBruteForce method");
            executorService.shutdownNow();
        }

        return solutionWasFound.get();
    }

    /**
     * Given a sudoku find the first cell with possible values, then for each value create a clone of the sudoku
     * and try to solve it.
     * @param sudoku the sudoku.
     * @param toTry the stack where we are going to store the possible sudoku solutions.
     */
    private static void GetAndStorePossibilities(Sodoku sudoku, Stack<Sodoku> toTry){
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
    private static void CreateCloneAndStoreSudoku(int value, int cellToChangeRow, int cellToChangeColumn, Sodoku sudoku, Stack<Sodoku> toTry){
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
    private static Sodoku TryToSolveSudoku(Sodoku sudoku, Stack<Sodoku> toTry){
        // Use reduction to try to solve the sudoku
        if (sudoku.TryToSolve()){
            return sudoku;
        }
        // If not, find a new cell with possible values
        GetAndStorePossibilities(sudoku, toTry);
        return null;
    }
}
