package cl.ucn.disc.hpc.sodoku;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a box of n cells in the sodoku.
 */
public class Box {

    /**
     * A box can evaluate rows, columns or neither.
     */
    public enum ToEvaluate{
        Row,
        Column,
        None
    }

    // The matrix with the cells to evaluate
    private Cell[][] cells;
    // A box can evaluate rows, columns or neither
    private ToEvaluate toEvaluate;
    // The indexes covered by this box on the x-axis
    private Integer[] xFromTo;
    // The indexes covered by this box on the y-axis
    private Integer[] yFromTo;

    /**
     * The constructor of a box.
     * @param cells the matrix with the cells of hte sodoku.
     * @param toEvaluate what is this box going to evaluate?
     * @param xFrom min x index.
     * @param xTo max x index.
     * @param yFrom min y index.
     * @param yTo max y index.
     */
    public Box(Cell[][] cells,ToEvaluate toEvaluate, int xFrom, int xTo, int yFrom, int yTo){
        this.cells = cells;
        this.toEvaluate = toEvaluate;
        this.xFromTo = new Integer[]{xFrom, xTo};
        this.yFromTo = new Integer[]{yFrom, yTo};
    }

    /**
     * Clean a possible value from each cell in the box
     * @param value the value.
     */
    public void CleanPossibleValueInBox(int value){
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && !cell.HasOnlyOnePossibleValue()){
                    cell.RemovePossibleValue(value);
                }
            }
        }
    }

    /**
     * Clean a possible value from each cell int the box skipping a cell
     * @param value the value.
     * @param toSkip the cell to skip.
     */
    public void ClenPossibleValueInBox(int value, Cell toSkip){
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if (cell != toSkip && !cell.GetIsByDefault() && !cell.HasOnlyOnePossibleValue()){
                    cell.RemovePossibleValue(value);
                }
            }
        }
    }

    /**
     * Clean a set of possible values from each cell in the box
     * @param values a list with the values.
     */
    public void CleanPossibleValuesInBox(List<Integer> values){
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && !cell.HasOnlyOnePossibleValue()){
                    cell.RemovePossibleValues(values);
                }
            }
        }
    }

    /**
     * Clean a set of possible values from each cell skipping a cell.
     * @param values a list with the values.
     * @param toSkip the cell to skip.
     */
    public void CleanPossibleValuesInBox(List<Integer> values, Cell toSkip){
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if (cell != toSkip && !cell.GetIsByDefault() && !cell.HasOnlyOnePossibleValue()){
                    cell.RemovePossibleValues(values);
                }
            }
        }
    }

    public void CleanDefaultValuesInBox(){
        // Store the default values
        List<Integer> boxDefaultValues = new ArrayList<>();
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if (cell.GetIsByDefault()){
                    boxDefaultValues.add(cell.GetFirstPossibleValue());
                }
            }
        }
        // Clean the values
        CleanPossibleValuesInBox(boxDefaultValues);
    }
}
