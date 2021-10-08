package cl.ucn.disc.hpc.sodoku;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

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
    public boolean Solve(){
        return ReducePossibleValues();
    }

    /**
     * Try to reduce the possible values of each cell.
     * @return true if the sudoku was solved.
     */
    private boolean ReducePossibleValues(){
        int loops = -1;
        while(true){
            loops++;
            //PrintCells();
            // Is the sudoku solved?
            if (IsTheSodokuSolved()) {
                //log.debug("Loops required to solve only reducing: {}", loops);
                return true;
            }
            // If we use simple elimination
            if (SimpleElimination()){
                // We go back to the top of the loop
                //log.debug("A change was made by simple elimination:");
                continue;
            }
            // If we use loner rangers elimination
            if (LonerRangers()){
                // We go back to the top of the loop
                //log.debug("A change was made by loner rangers' elimination:");
                continue;
            }/*
            // If we use loner rangers elimination
            if (LonerRangers()){
                // We go back to the top of the loop
                //log.debug("A change was made by loner rangers' elimination:");
                continue;
            }*/

            // If we use naked twins elimination
            if (NakedTwins()){
                // We go back to the top of the loop
                //log.debug("A change was made by twins elimination:");
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
            // If we cannot make any change we solve the sudoku by brute force
            break;
        }
        //log.debug("Loops until reducing fails: {}", loops);
        return  false;
    }

    /**
     * Parallel check if the sodoku is solved.
     * @return true if the sodoku is solved.
     */
    public boolean IsTheSodokuSolved() {
        // Loop through the boxes
        for (int i = 0; i < boxes.length; i++) {
            for (int j = 0; j < boxes.length; j++) {
                Box box = boxes[i][j];
                boolean isThisBoxValid = box.IsThisBoxValid();
                boolean areTheBoxRowsValid = box.AreTheBoxRowsValid();
                boolean areTheBoxColumnsValid = box.AreTheBoxColumnsValid();
                if (!isThisBoxValid || !areTheBoxRowsValid || !areTheBoxColumnsValid) {
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
