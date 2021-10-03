package cl.ucn.disc.hpc.sodoku;

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
     * Loop through each column or row in this box to apply the elimination technique.
     * @return true if the possible values of one or more cells were changed.
     */
    public boolean Elimination(){
        // Which is this going to evaluate?
        if (toEvaluate == ToEvaluate.Row){
            return RowElimination();
        }else if(toEvaluate == ToEvaluate.Column){
            return ColumnElimination();
        }

        return false;
    }

    public boolean RowElimination(){
        // Loop trough each row in the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = i; j < cells[0].length; j++){
                // Is this cell not by default and does it have only on possible value?
                if (!cells[i][j].GetIsByDefault() && cells[i][j].HasOnlyOnePossibleValue()){
                    // Evaluate if this cell is the only valid cell for the value
                    int value = cells[i][j].GetFirstPossibleValue();
                    boolean found = false;
                    for (int k = 0; k < cells[0].length; k++){
                        // Omit this cell
                        if (k != j){
                            if (cells[i][k].HasPossibleValue(value)){
                                found = true;
                                break;
                            }
                        }
                    }
                    // If the value was not found we clean the box, row and column
                }
            }
        }

        return false;
    }

    public boolean ColumnElimination(){
        return true;
    }

}
