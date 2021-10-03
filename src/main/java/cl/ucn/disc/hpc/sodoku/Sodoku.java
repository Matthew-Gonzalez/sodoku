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
    private Cell[][] cells;
    // A matrix with the boxes of the sodoku
    private Box[][] boxes;

    /**
     * The constructor of the sodoku.
     * @param grid an int matrix with the initial values of the sodoku cells.
     */
    public Sodoku(int[][] grid){
        // Initialize the cells matrix
        this.cells = new Cell[grid.length][grid.length];


        // First we need the default values of each row and column
        List<List<Integer>> rowDefaultValues = new ArrayList<>(cells.length);
        List<List<Integer>> columnDefaultValues = new ArrayList<>(cells.length);
        for (int i = 0; i < cells.length; i++){
            rowDefaultValues.add(new ArrayList<>());
            columnDefaultValues.add(new ArrayList<>());
        }
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

        // Then we loop through the grid looking for the possible values of each cell
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

        // Finally, is necessary to create the boxes
        int boxesLength = (int)Math.sqrt(cells.length);
        boxes = new Box[boxesLength][boxesLength];
        int minX = 0, maxX = boxesLength - 1, minY = 0, maxY = boxesLength - 1;
        for (int i = 0; i < boxesLength; i++){
            for (int j = 0; j < boxesLength; j++){
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

                // Determines the range of the next box in the x-axis
                if (maxX == cells.length - 1){
                    minX = 0;
                    maxX = boxesLength - 1;
                }else{
                    minX = maxX + 1;
                    maxX += boxesLength;
                }
            }
            // Determines the range of the next box en int y-axis
            minY = maxY + 1;
            maxY += boxesLength;
        }

    }

    public Cell[][] GetCells(){
        return this.cells;
    }

    public Box[][] GetBoxes(){
        return this.boxes;
    }
}
