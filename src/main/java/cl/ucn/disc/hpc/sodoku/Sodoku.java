package cl.ucn.disc.hpc.sodoku;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a sodoku.
 */
@Slf4j
public class Sodoku {
    // A matrix with the cells of the sodoku
    private final Cell[][] cells;
    // A matrix with the boxes of the sodoku
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

    public void Solve(){
        int securityAttempts = 200;
        while (securityAttempts > 0){
            boolean longRangerElimination = LoneRangerElimination();
            if (!IsTheSodokuSolved() && !longRangerElimination) {
                log.debug("Attempts: {}", 200 - securityAttempts);
                return;
            }
            securityAttempts--;
        }
    }

    /**
     * Check if the sodoku is solved.
     * @return true if the sodoku is solved.
     */
    public boolean IsTheSodokuSolved(){
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                if (!box.IsThisBoxValid()){
                    return false;
                }
            }
        }
        return  true;
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

    public void TwinsElimination(){
        for (int i = 0; i < boxes.length; i++){
            for (int j = 0; j < boxes.length; j++){
                Box box = boxes[i][j];
                box.Twins();
            }
        }
    }

    public void GetTwins(){
        for (int i = 0; i < cells.length; i++){
            for (int k = 0; k < cells.length; k++){
                log.debug("Cell values: {}", cells[i][k].GetPossibleValues());
                log.debug("Cell twins:");
                List<Integer[]> twins = cells[i][k].GetUniquePairs();
                for (int h = 0; h < twins.size(); h++){
                    log.debug("   [{},{}]", twins.get(h)[0], twins.get(h)[1]);
                }
            }
        }
    }

    public Cell[][] GetCells(){
        return this.cells;
    }

    public Box[][] GetBoxes(){
        return this.boxes;
    }
}
