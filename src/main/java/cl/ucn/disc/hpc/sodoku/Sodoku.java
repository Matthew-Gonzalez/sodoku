package cl.ucn.disc.hpc.sodoku;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represents a sodoku.
 */
@Slf4j
public class Sodoku {
    // A matrix with the cells of the sudoku
    private final Cell[][] cells;
    // A matrix with the boxes of the sudoku
    private final Box[][] boxes;

    /**
     * The constructor of the sodoku.
     * @param grid an int matrix with the initial values of the sodoku cells.
     */
    public Sodoku(int[][] grid){
        // Initialize the cells matrix
        this.cells = new Cell[grid.length][grid.length];

        // Initialize the boxes matrix
        int boxesLength = (int)Math.sqrt(cells.length);
        boxes = new Box[boxesLength][boxesLength];

        // First we populate the cells' matrix looking for each cell possible values
        CreateAndCleanCells(grid);

        // Then populates the boxes' matrix cleaning the default values
        CreateAndCleanBoxes();
    }

    /**
     * Auxiliary constructor for clone purposes.
     * @param cells cells.
     * @param boxes boxes.
     */
    private Sodoku(Cell[][] cells, Box[][] boxes){
        // Initialize the matrices
        this.cells = new Cell[cells.length][cells.length];
        this.boxes = new Box[boxes.length][boxes.length];

        // Clone the cells one by one
        for (int i = 0; i < cells.length; i++){
            for (int j = 0; j < cells.length; j++){
                this.cells[i][j] = cells[i][j].GetClone();
            }
        }

        // Clone the boxes one by one
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                this.boxes[i][j] = boxes[i][j].GetClone(this.cells);
            }
        }
    }

    /**
     * Given a grid populate a cells matrix looking for each cell possible values
     * @param grid a matrix with the sodoku default values.
     */
    private void CreateAndCleanCells(int[][] grid){
        // A list for rows and columns default values
        List<List<Integer>> rowDefaultValues = new ArrayList<>(this.cells.length);
        List<List<Integer>> columnDefaultValues = new ArrayList<>(this.cells.length);
        for (int i = 0; i < cells.length; i++){
            rowDefaultValues.add(new ArrayList<>());
            columnDefaultValues.add(new ArrayList<>());
        }

        // Find and save each default value
        for(int i = 0; i < cells.length; i++){
            rowDefaultValues.add(new ArrayList<>());
            for (int j = 0; j < cells.length; j++){
                int value = grid[i][j];
                if (value != 0){
                    rowDefaultValues.get(i).add(value);
                    columnDefaultValues.get(j).add(value);
                }
            }
        }

        // Then we loop through the grid looking for each possible value for each cell
        for(int i = 0; i < cells.length; i++){
            for (int j = 0; j < cells.length; j++){
                int value = grid[i][j];
                // Check if this is a default value
                Cell cell;
                if (value != 0){
                    // Create a cell with a default value
                    cell = new Cell(value);
                }
                // Get the possibles values of the cell
                else{
                    cell = new Cell();
                    for (int k = 1; k <= cells.length; k++){
                        if (!rowDefaultValues.get(i).contains(k) && !columnDefaultValues.get(j).contains(k)){
                            cell.AddPossibleValue(k);
                        }
                    }
                }
                cells[i][j] = cell;
            }
        }
    }

    /**
     * Create the sodoku boxes, then find the default values of each to clean them from the default values
     * of each cell within each box.
     */
    public void CreateAndCleanBoxes(){
        // The box incremental data
        int minX = 0, maxX = boxes.length - 1, minY = 0, maxY = boxes.length - 1;
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                // Create the box
                Box box = new Box(cells,minX, maxX, minY, maxY);
                boxes[i][j] = box;

                // Clean the cells of the box from its default values
                box.CleanDefaultValuesInBox();

                // Determines the range of the next box in the x-axis
                if (maxX == cells.length - 1){
                    minX = 0;
                    maxX = boxes.length - 1;
                }else{
                    minX = maxX + 1;
                    maxX += boxes.length;
                }
            }
            // Determines the range of the next box en int y-axis
            minY = maxY + 1;
            maxY += boxes.length;
        }
    }

    /**
     * Try to solve the sudoku
     * @return true if the sudoku was solved
     */
    public boolean TryToSolve(){
        return ReducePossibleValues();
    }

    /**
     * Try to reduce the possible values of each cell.
     * @return true if the sudoku was solved.
     */
    private boolean ReducePossibleValues(){
        while(true){
            // Is the sudoku solved?
            if (IsTheSudokuSolved()) {
                return true;
            }
            // If we use simple elimination
            if (SimpleElimination()){
                // We go back to the top of the loop
                continue;
            }
            // If we use loner rangers elimination
            if (LonerRangers()){
                // We go back to the top of the loop
                continue;
            }
            // If we use naked twins elimination
            if (NakedTwins()){
                // We go back to the top of the loop
                continue;
            }
            // If we use naked triplets elimination
            if (NakedTriplets()){
                // We go back to the top of the loop
                continue;
            }
            // If we use hidden twins elimination
            if (HiddenTwins()){
                // We go back to the top of the loop
                continue;
            }
            // If we use hidden triplets elimination
            if (HiddenTriplets()){
                // We go back to the top of the loop
                continue;
            }
            // If we cannot make any change we solve the sudoku by brute force
            break;
        }
        return  false;
    }

    /**
     * Check if the sudoku is solved.
     * @return true if the sudoku is solved.
     */
    private boolean IsTheSudokuSolved() {
        // Loop through the boxes
        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes.length; j++) {
                Box box = boxes[i][j];
                if (!box.IsThisBoxValid() || !box.AreTheBoxRowsValid()|| !box.AreTheBoxColumnsValid()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Simple elimination technique for each box in the sudoku.
     * @return true if any change was made.
     */
    private boolean SimpleElimination(){
        boolean anyChange = false;
        // Loop through the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (box.SimpleElimination()){
                    return true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Loner rangers' technique for each box in the sudoku.
     * @return true if any change was made.
     */
    private boolean LonerRangers(){
        boolean anyChange = false;
        // Loop through the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (box.LonerRangers()){
                    return true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Naked Twins technique for each box in the sudoku.
     * @return true if any change was made.
     */
    private boolean NakedTwins(){
        boolean anyChange = false;
        // Loop through the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (box.NakedTwins()){
                    return true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Naked triplets' technique for each box in the sudoku.
     * @return true if any change was made.
     */
    private boolean NakedTriplets(){
        boolean anyChange = false;
        // Loop through the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (box.NakedTriplets()){
                    return true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Hidden twins technique for each box in the sudoku.
     * @return true if any change was made.
     */
    private boolean HiddenTwins(){
        // Loop through the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (box.HiddenTwins()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Hidden triplets' technique for each box in the sudoku.
     * @return true if any change was made.
     */
    private boolean HiddenTriplets(){
        // Loop through the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (box.HiddenTriplets()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Try to solve the sudoku using parallelism.
     * @param cores cores (threads) to use.
     * @return true if the sudoku was solved.
     */
    public boolean ParallelTryToSolve(int cores){
        return ParallelReduceValues(cores);
    }

    /**
     * Try to reduce the possible values of each cell using parallelism.
     * @param cores cores (threads) to use.
     * @return true if the sudoku was solved.
     */
    private boolean ParallelReduceValues(int cores){
        while (true){
            // Is the sudoku solved
            if (ParallelIsTheSudokuSolved(cores)){
                return true;
            }
            // Simple elimination can affect many cell in different rows and columns at a time, so we want to avoid use parallelism
            if (SimpleElimination()){
                // We go back to the top of the loop
                continue;
            }
            // With loner rangers happens the same, use parallelism could provoke to data inconsistencies and threads conflicts
            if (LonerRangers()){
                // We go back to the top of the loop
                continue;
            }
            // If naked twins
            if (ParallelNakedTwins(cores)){
                // We go back to the top of the loop
                continue;
            }
            // If we use naked triplets elimination
            if (ParallelNakedTriplets(cores)){
                // We go back to the top of the loop
                continue;
            }
            // If we use hidden twins elimination
            if (ParallelHiddenTwins(cores)){
                // We go back to the top of the loop
                continue;
            }
            // If we use hidden triplets elimination
            if (ParallelHiddenTriplets(cores)){
                // We go back to the top of the loop
                continue;
            }
            // If we cannot make any change we solve the sudoku by brute force
            break;
        }
        return false;
    }

    /**
     * Use parallelism to check if the sudoku is solved.
     * @param cores cores (threads) to use.
     * @return true if the sudoku is solved.
     */
    private boolean ParallelIsTheSudokuSolved(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any cell is invalid
        AtomicBoolean anyInvalid = new AtomicBoolean(false);

        // Loop through the boxes
        exit :
        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes.length; j++) {
                final Box box = boxes[i][j];
                // Try to safe thread use
                try{
                    if (!executorService.isShutdown()){
                        // Thread to verify the box validity
                        executorService.submit(() -> {
                            // If the box is invalid
                            if (!box.IsThisBoxValid() || !box.AreTheBoxRowsValid() || !box.AreTheBoxColumnsValid()) {
                                anyInvalid.compareAndSet(false, true);
                                // Shutdown all others threads
                                executorService.shutdown();
                            }
                        });
                    }
                } catch (RejectedExecutionException e){
                    break exit;
                }
            }
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel IsTheSudokuSolved method");
            executorService.shutdownNow();
        }

        return !anyInvalid.get();
    }

    /**
     * Naked Twins technique for each box in the sudoku using parallelism.
     * @param cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelNakedTwins(int cores){
        // In order to avoid data inconsistencies
        if (ParallelNakedTwinsBoxes(cores)){
            return true;
        }
        if (ParallelNakedTwinsRows(cores)){
            return true;
        }
        if (ParallelNakedTwinsColumns(cores)){
            return true;
        }
        return false;
    }

    /**
     * Parallel implementation of naked twins for each box.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelNakedTwinsBoxes(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to naked twins for each box
        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes.length; j++) {
                final Box box = boxes[i][j];
                // Thread to clean a box
                executorService.submit(() -> {
                    if (box.NakedTwinsBox()) {
                        anyChange.compareAndSet(false,true);
                    }
                });
            }
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelNakedTwins method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Parallel implementation of naked twins for each row.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelNakedTwinsRows(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to naked twins for each row
        for (int i = 0; i < boxes.length; i++){
            final Box box = boxes[i][0];
            // Thread to clean a row
            executorService.submit(() -> {
                if (box.NakedTwinsRows()) {
                    anyChange.compareAndSet(false,true);
                }
            });
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelNakedTwins method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Parallel implementation of naked twins for each column.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelNakedTwinsColumns(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to naked twins for each column
        for (int j = 0; j < boxes.length; j++){
            final Box box = boxes[0][j];
            // Thread to clean a column
            executorService.submit(() -> {
                if (box.NakedTwinsColumns()) {
                    anyChange.compareAndSet(false,true);
                }
            });
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelNakedTwins method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Naked triplets' technique for each box in the sudoku using parallelism.
     * @param cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelNakedTriplets(int cores){
        // In order to avoid data inconsistencies
        if (ParallelNakedTripletsBoxes(cores)){
            return true;
        }
        if (ParallelNakedTripletsRows(cores)){
            return true;
        }
        if (ParallelNakedTripletsColumns(cores)){
            return true;
        }
        return false;
    }

    /**
     * Parallel implementation of naked triplets for each box.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelNakedTripletsBoxes(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to naked triplets for each box
        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes.length; j++) {
                final Box box = boxes[i][j];
                // Thread to clean a box
                executorService.submit(() -> {
                    if (box.NakedTripletsBox()) {
                        anyChange.compareAndSet(false,true);
                    }
                });
            }
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelNakedTriplets method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Parallel implementation of naked triplets for each row.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelNakedTripletsRows(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to naked triplets for each row
        for (int i = 0; i < boxes.length; i++){
            final Box box = boxes[i][0];
            // Thread to clean a row
            executorService.submit(() -> {
                if (box.NakedTripletsRows()) {
                    anyChange.compareAndSet(false,true);
                }
            });
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelNakedTriplets method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Parallel implementation of naked triplets for each column.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelNakedTripletsColumns(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to naked triplets for each column
        for (int j = 0; j < boxes.length; j++){
            final Box box = boxes[0][j];
            // Thread to clean a column
            executorService.submit(() -> {
                if (box.NakedTripletsColumns()) {
                    anyChange.compareAndSet(false,true);
                }
            });
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelNakedTriplets method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Hidden Twins technique for each box in the sudoku using parallelism.
     * @param cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelHiddenTwins(int cores){
        // In order to avoid data inconsistencies
        if (ParallelHiddenTwinsBoxes(cores)){
            return true;
        }
        if (ParallelHiddenTwinsRows(cores)){
            return true;
        }
        if (ParallelHiddenTwinsColumns(cores)){
            return true;
        }
        return false;
    }

    /**
     * Parallel implementation of hidden twins for each box.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelHiddenTwinsBoxes(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to hidden twins for each box
        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes.length; j++) {
                final Box box = boxes[i][j];
                // Thread to clean a box
                executorService.submit(() -> {
                    if (box.HiddenTwinsBox()) {
                        anyChange.compareAndSet(false,true);
                    }
                });
            }
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelHiddenTwins method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Parallel implementation of hidden twins for each row.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelHiddenTwinsRows(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to hidden twins for each row
        for (int i = 0; i < boxes.length; i++){
            final Box box = boxes[i][0];
            // Thread to clean a row
            executorService.submit(() -> {
                if (box.HiddenTwinsRows()) {
                    anyChange.compareAndSet(false,true);
                }
            });
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelHiddenTwins method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Parallel implementation of hidden twins for each column.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelHiddenTwinsColumns(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to hidden twins for each column
        for (int j = 0; j < boxes.length; j++){
            final Box box = boxes[0][j];
            // Thread to clean a column
            executorService.submit(() -> {
                if (box.HiddenTwinsColumns()) {
                    anyChange.compareAndSet(false,true);
                }
            });
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelHiddenTwins method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Hidden triplets' technique for each box in the sudoku using parallelism.
     * @param cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelHiddenTriplets(int cores){
        // In order to avoid data inconsistencies
        if (ParallelHiddenTripletsBoxes(cores)){
            return true;
        }
        if (ParallelHiddenTripletsRows(cores)){
            return true;
        }
        if (ParallelHiddenTripletsColumns(cores)){
            return true;
        }
        return false;
    }

    /**
     * Parallel implementation of hidden twins for each column.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelHiddenTripletsBoxes(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to hidden triplets for each box
        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes.length; j++) {
                final Box box = boxes[i][j];
                // Thread to clean a box
                executorService.submit(() -> {
                    if (box.HiddenTripletsBox()) {
                        anyChange.compareAndSet(false,true);
                    }
                });
            }
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelHiddenTriplets method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Parallel implementation of hidden twins for each column.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelHiddenTripletsRows(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to hidden triplets for each row
        for (int i = 0; i < boxes.length; i++){
            final Box box = boxes[i][0];
            // Thread to clean a row
            executorService.submit(() -> {
                if (box.HiddenTripletsRows()) {
                    anyChange.compareAndSet(false,true);
                }
            });
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelHiddenTriplets method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Parallel implementation of hidden twins for each column.
     * @param cores cores cores (threads) to use.
     * @return true if any change was made.
     */
    private boolean ParallelHiddenTripletsColumns(int cores){
        // Executor service
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // If any change is made
        AtomicBoolean anyChange = new AtomicBoolean(false);

        // Try to hidden triplets for each column
        for (int j = 0; j < boxes.length; j++){
            final Box box = boxes[0][j];
            // Thread to clean a column
            executorService.submit(() -> {
                if (box.HiddenTripletsColumns()) {
                    anyChange.compareAndSet(false,true);
                }
            });
        }

        // Safe shutdown
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Error with parallel ParallelHiddenTriplets method");
            executorService.shutdownNow();
        }

        return anyChange.get();
    }

    /**
     * Print the cells of the sudoku.
     */
    public void PrintCells(){
        for (int i = 0; i < cells.length; i++){
            List<List<Integer>> row = new ArrayList<>();
            for (int j = 0; j < cells.length; j++){
                row.add(cells[i][j].GetPossibleValues());
            }
            log.debug("{}", row);
        }
        log.debug("");
    }

    /**
     * Get a cell from the sudoku.
     * @param row the cell row.
     * @param column the cell column.
     * @return the cell.
     */
    public Cell GetCell(int row, int column){
        return cells[row][column];
    }

    /**
     * Get the cells of the sudoku.
     * @return a matrix with the cells.
     */
    public Cell[][] GetCells(){
        return this.cells;
    }

    public Sodoku GetClone() {
        return new Sodoku(this.cells, this.boxes);
    }
}
