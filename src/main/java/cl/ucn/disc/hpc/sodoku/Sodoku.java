package cl.ucn.disc.hpc.sodoku;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represents a sodoku.
 */
@Slf4j
public class Sodoku {
    // Number of cores that are going to be used to solve the sudoku
    private final int cores = 4;
    // Max time in minutes to wait for the threads
    private final int maxTime = 5;
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
                // Determines if the box is going to evaluate row, columns or neither
                Box.ToEvaluate toEvaluate = Box.ToEvaluate.None;
                if (i == 0){
                    if (j > 0){
                        toEvaluate = Box.ToEvaluate.Column;
                    }
                    else {
                        toEvaluate = Box.ToEvaluate.Row;
                    }
                } else if (i == 1 && j == 0){
                    toEvaluate = Box.ToEvaluate.Column;
                }
                else {
                    if (j == 1){
                        toEvaluate = Box.ToEvaluate.Row;
                    }else if (j == 0){
                        toEvaluate = Box.ToEvaluate.Column;
                    }
                }
                // Create the box
                Box box = new Box(cells, toEvaluate,minX, maxX, minY, maxY);
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
    public boolean Solve(){
        while(true){
            PrintCells();
            // Is the sudoku solved?
            if (IsTheSodokuSolved()){
                return true;
            }
            // If we use simple elimination technique int the rows and a change occurs
            if (SimpleEliminationByRows()){
                // We back to the top of the loop
                log.debug("A change was made by simple elimination by rows:");
                continue;
            }
            // If we use simple elimination technique int the columns and a change occurs
            if (SimpleEliminationByColumns()){
                // We back to the top of the loop
                log.debug("A change was made by simple elimination by columns:");
                continue;
            }
            // If we use simple elimination technique in the boxes and a change occurs
            if (SimpleEliminationByBox()){
                // We back to the top of the loop
                log.debug("A change was made by simple elimination by box:");
                continue;
            }
            // If we cannot make any change we solve the sudoku by brute force
            break;
        }
        return false;
    }

    /**
     * Parallel check if the sodoku is solved.
     * @return true if the sodoku is solved.
     */
    public boolean IsTheSodokuSolved(){
        AtomicBoolean isSolved = new AtomicBoolean(true);
        ExecutorService executorService = Executors.newFixedThreadPool(cores);

        log.debug("Checking if the sodoku is solved");
        StopWatch sw = StopWatch.createStarted();

        // Loop through the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                final int row = i;
                final int column = j;
                // Use a thread to check if this box is valid
                executorService.submit(() -> {
                    Box box = boxes[row][column];
                    if (!box.IsThisBoxValid()){
                        isSolved.compareAndSet(true, false);
                    }
                });
            }
        }
        // wait for threads to end
        executorService.shutdown();
        try{
            if (executorService.awaitTermination(maxTime, TimeUnit.MINUTES)){
                executorService.shutdown();
            }
        }catch (InterruptedException e){
            executorService.shutdown();
            log.warn("Error in check valid box threads!");
        }

        log.debug("Finished in {} ms!" + "\n", sw.getTime(TimeUnit.MILLISECONDS));

        return isSolved.get();
    }

    /**
     * Parallel simple elimination for each box in the sudoku.
     * @return true if any change was made.
     */
    private boolean SimpleEliminationByBox(){
        AtomicBoolean anyChange = new AtomicBoolean(false);
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // Loop through the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                final int row = i;
                final int column = j;
                // Use a thread to use simple elimination technique in the box
                executorService.submit(() -> {
                    Box box = boxes[row][column];
                    if (box.SimpleEliminationBox()){
                        anyChange.compareAndSet(false, true);
                    }
                });
            }
        }
        // wait for threads to end
        executorService.shutdown();
        try{
            if (executorService.awaitTermination(maxTime, TimeUnit.MINUTES)){
                executorService.shutdown();
            }
        }catch (InterruptedException e){
            executorService.shutdown();
            log.warn("Error in simple elimination box threads!");
        }

        return anyChange.get();
    }

    /**
     * Parallel simple elimination for each row in the sudoku
     * @return true if any change was made
     */
    private boolean SimpleEliminationByRows(){
        AtomicBoolean anyChange = new AtomicBoolean(false);
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // We only need to loop through N boxes, where N is the length of boxes matrix
        for (int i = 0; i < boxes.length; i++){
            final int row = i;
            executorService.submit(() ->{
                Box box = boxes[row][0];
                if (box.SimpleEliminationRow()){
                    anyChange.compareAndSet(false, true);
                }
            });
        }
        // wait for threads to end
        executorService.shutdown();
        try{
            if (executorService.awaitTermination(maxTime, TimeUnit.MINUTES)){
                executorService.shutdown();
            }
        }catch (InterruptedException e){
            executorService.shutdown();
            log.warn("Error in simple elimination box threads!");
        }

        return anyChange.get();
    }

    /**
     * Parallel simple elimination for each column in the sudoku
     * @return true if any change was made
     */
    private boolean SimpleEliminationByColumns(){
        AtomicBoolean anyChange = new AtomicBoolean(false);
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        // We only need to loop through N boxes, where N is the length of boxes matrix
        for (int j = 0; j < boxes.length; j++){
            final int column = j;
            executorService.submit(() ->{
                Box box = boxes[0][column];
                if (box.SimpleEliminationColumn()){
                    anyChange.compareAndSet(false, true);
                }
            });
        }
        // wait for threads to end
        executorService.shutdown();
        try{
            if (executorService.awaitTermination(maxTime, TimeUnit.MINUTES)){
                executorService.shutdown();
            }
        }catch (InterruptedException e){
            executorService.shutdown();
            log.warn("Error in simple elimination box threads!");
        }

        return anyChange.get();
    }


    /**
     * Elimination technique.
     * @return true if any change was made.
     */
    public boolean SimpleElimination(){
        int changes = 0;
        // First we need to clean the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (box.SimpleEliminationBox()){
                    changes++;
                }
            }
        }
        if (changes > 0){
            return true;
        }
        // Then, we clean the rows and columns
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if(box.SimpleEliminationRowsColumns()){
                    changes++;
                }
            }
        }
        return changes > 0;
    }

    /**
     * Lone Rangers elimination technique.
     * @return true if a change was made.
     */
    public boolean LoneRangerElimination(){
        int changes = 0;
        // First we need to clean the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (box.LoneRangersBox()){
                    changes++;
                }
            }
        }
        if (changes > 0){
            return true;
        }
        // Then, we clean the rows and columns
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if(box.LoneRangersRowsColumns()){
                    changes++;
                }
            }
        }
        return changes > 0;
    }

    /**
     * Twins elimination technique.
     * @return true if a change was made.
     */
    public boolean TwinsElimination(){
        int changes = 0;
        // Firs we need to clean the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (box.TwinsBox()){
                    changes++;
                }
            }
        }/*
        if (changes > 0){
            return true;
        }

        // Then we clean the rows and columns
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (box.Twins()){
                    changes++;
                }
            }
        }*/
        return  changes > 0;
    }

    /**
     * Triplets elimination technique.
     * @return true if a change was made.
     */
    public boolean TripletsElimination(){
        int changes = 0;
        // First we need to clean the boxes
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (box.TripletsBox()){
                    changes++;
                }
            }
        }
        if (changes > 0){
            return true;
        }
        // Then, we clean the rows and columns
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if(box.Triplets()){
                    changes++;
                }
            }
        }
        return changes > 0;
    }

    public Cell[][] GetCells(){
        return this.cells;
    }

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
}
