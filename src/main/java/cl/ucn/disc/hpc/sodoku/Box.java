package cl.ucn.disc.hpc.sodoku;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Represent a box of n cells in the sodoku.
 */
@Slf4j
public class Box {
    // The matrix with the cells to evaluate
    private final Cell[][] cells;
    // The indexes covered by this box on the x-axis
    private final Integer[] xFromTo;

    // The indexes covered by this box on the y-axis
    private final Integer[] yFromTo;

    /**
     * The constructor of a box.
     * @param cells the matrix with the cells of hte sodoku.
     * @param xFrom min x index.
     * @param xTo max x index.
     * @param yFrom min y index.
     * @param yTo max y index.
     */
    public Box(Cell[][] cells, int xFrom, int xTo, int yFrom, int yTo){
        this.cells = cells;
        this.xFromTo = new Integer[]{xFrom, xTo};
        this.yFromTo = new Integer[]{yFrom, yTo};
    }

    /**
     * Check if this is a valid box
     * @return true if the box is valid
     */
    public boolean IsThisBoxValid(){
        List<Integer> values = new ArrayList<>();
        // Loop through the cells
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                // Check if the cell has only one possible value
                if (!cell.HasOnlyOnePossibleValue()){
                    return false;
                }
                // Check if the value is repeated
                int value = cell.GetFirstPossibleValue();
                if (values.contains(value)){
                    //log.warn("A box with the cell[{}][{}] has a repeated value", i, j);
                    return false;
                }
                values.add(value);
            }
        }
        return true;
    }

    public boolean AreTheBoxRowsValid(){
        // Loop through the cells
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            List<Integer> values = new ArrayList<>();
            for (int j = 0; j < cells.length; j++){
                Cell cell = cells[i][j];
                // Check if the cell has only one possible value
                if (!cell.HasOnlyOnePossibleValue()){
                    return false;
                }
                // Check if the value is repeated
                int value = cell.GetFirstPossibleValue();
                if (values.contains(value)){
                    //log.warn("A row with the cell[{}][{}] has a repeated value", i, j);
                    return false;
                }
                values.add(value);
            }
        }
        return true;
    }

    public boolean AreTheBoxColumnsValid(){
        // Loop through the cells
        for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
            List<Integer> values = new ArrayList<>();
            for (int i = 0; i < cells.length; i++){
                Cell cell = cells[i][j];
                // Check if the cell has only one possible value
                if (!cell.HasOnlyOnePossibleValue()){
                    return false;
                }
                // Check if the value is repeated
                int value = cell.GetFirstPossibleValue();
                if (values.contains(value)){
                    //log.warn("A column with the cell[{}][{}] has a repeated value", i, j);
                    return false;
                }
                values.add(value);
            }
        }
        return true;
    }

    /**
     * Cleans the box using simple elimination technique.
     * @return true if any change was made.
     */
    public boolean SimpleElimination(){
        boolean anyChange = false;
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                // If the cell is not default and has multiple possible values
                if (!cell.GetIsByDefault() && !cell.HasOnlyOnePossibleValue()){
                    for(int k = 0; k < cell.GetPossibleValues().size(); k++){
                        int value = cell.GetPossibleValues().get(k);
                        if (IsThisValueUniqueInBox(value, i, j) && IsThisValueUniqueInRow(value, i, j)
                                && IsThisValueUniqueInColumn(value, j, i)){
                            // This value is unique in its box, row and column, so we remove the other possible values
                            cell.RemovePossibleValuesExceptOne(value);
                            anyChange = true;
                            break;
                        }
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Clean the box using loner rangers technique.
     * @return true if a change was made.
     */
    public boolean LonerRangers(){
        boolean anyChange = false;
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We are looking for a not default cell with only one possible value
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && cell.HasOnlyOnePossibleValue()){
                    // Clean the box, row and column
                    int value = cell.GetFirstPossibleValue();
                    if (CleanPossibleValueInBox(value, cell) || CleanPossibleValuesInRow(value, i, j)
                            || CleanPossibleValuesInColumn(value, j, i)){
                        anyChange = true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Clean the box using twins technique.
     * @return true if any change was made.
     */
    public boolean Twins(){
        boolean anyChange = false;
        // Loop through the box cells
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We are looking for a not default cell with three or more possible values
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && cell.GetPossibleValues().size() >= 3){
                    List<Integer[]> pairs = cell.GetUniquePairs();
                    for (int k = 0; k < pairs.size(); k++){
                        Integer[] pair = pairs.get(k);
                        Stack<Cell> toClean = new Stack<>();
                        toClean.add(cell);
                        // If this pair only exist twice in the box
                        Cell inBox = IsThisPairOnyTwiceInBox(pair, cell);
                        if (inBox != null){
                            toClean.add(inBox);
                        }
                        // If this pair only exist twice in the row
                        Cell inRow = IsThisPairOnlyTwiceInRow(pair, i, cell);
                        if (inRow != null){
                            toClean.add(inRow);
                        }
                        // If this pair only exist twice in the column
                        Cell inColumn = IsThisPairOnlyTwiceInColumn(pair, j, cell);
                        if (inColumn != null){
                            toClean.add(inColumn);
                        }
                        if (toClean.size() >= 2){
                            while (!toClean.empty()){
                                Cell temp = toClean.pop();
                                temp.RemovePossibleValuesExceptPair(pair);
                            }
                            anyChange = true;
                            break;
                        }
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Clean the rows of the box using Triplets' technique
     * @return true if a change was made.
     */
    private boolean TripletsRow(){
        int changes = 0;
        // Loop through the box cells
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = 0; j < cells.length; j++){
                // We are looking for a not default cell with four or more possible values
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && cell.GetPossibleValues().size() >= 4){
                    List<Integer[]> triplets = cell.GetUniqueTriplets();
                    for (int k = 0; k < triplets.size(); k++){
                        // We need to check if only one candidate cell was found
                        Stack<Cell> toChange = new Stack<>();
                        toChange.add(cell);
                        for (int h = 0; h < cells.length; h++){
                            // We omit the main cell
                            if (h == j){
                                continue;
                            }
                            // Is this a valid candidate?
                            Cell temp = cells[i][h];
                            if (!temp.GetIsByDefault() && temp.GetPossibleValues().size() >= 3 && temp.HasTriplet(triplets.get(k))){
                                toChange.add(temp);
                                if (toChange.size() > 3){
                                    break;
                                }
                            }
                        }
                        // Do we need to clean the both cells?
                        if (toChange.size() == 3){
                            while (!toChange.empty()){
                                Cell temp = toChange.pop();
                                temp.RemovePossibleValuesExceptTriplet(triplets.get(k));
                            }
                            changes++;
                            break;
                        }
                    }
                }
            }
        }
        return changes > 0;
    }

    /**
     * Clean the columns of the box using Triplets' technique
     * @return true if a change was made.
     */
    private boolean TripletsColumn(){
        int changes = 0;
        // Loop through the box cells
        for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
            for (int i = 0; i < cells.length; i++){
                // We are looking for a not default cell with four or more possible values
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && cell.GetPossibleValues().size() >= 4){
                    List<Integer[]> triplets = cell.GetUniqueTriplets();
                    for (int k = 0; k < triplets.size(); k++){
                        // We need to check if only one candidate cell was found
                        Stack<Cell> toChange = new Stack<>();
                        toChange.add(cell);
                        for (int h = 0; h < cells.length; h++){
                            // We omit the main cell
                            if (h == i){
                                continue;
                            }
                            // Is this a valid candidate?
                            Cell temp = cells[h][j];
                            if (!temp.GetIsByDefault() && temp.GetPossibleValues().size() >= 3 && temp.HasTriplet(triplets.get(k))){
                                toChange.add(temp);
                                if (toChange.size() > 3){
                                    break;
                                }
                            }
                        }
                        // Do we need to clean the both cells?
                        if (toChange.size() == 3){
                            while (!toChange.empty()){
                                Cell temp = toChange.pop();
                                temp.RemovePossibleValuesExceptTriplet(triplets.get(k));
                            }
                            changes++;
                            break;
                        }
                    }
                }
            }
        }
        return changes > 0;
    }

    /**
     * Clean the box using Twins' technique
     * @return true if a change was made.
     */
    public boolean TripletsBox(){
        int changes = 0;
        // Loop through the box cells
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We are looking for a not default cell with three or more possible values
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && cell.GetPossibleValues().size() >= 3){
                    List<Integer[]> triplets = cell.GetUniqueTriplets();
                    for (int k = 0; k < triplets.size(); k++){
                        // We need to check if only one candidate cell was found
                        Stack<Cell> toChange = new Stack<>();
                        toChange.add(cell);
                        // We use a label to break the inner loop
                        find :
                        for (int l = yFromTo[0]; l <= yFromTo[1]; l++){
                            for(int m = xFromTo[0]; m <= xFromTo[1]; m++){
                                // We omit the main cell
                                if (l == i && m == j){
                                    continue;
                                }
                                // Is this a valid candidate?
                                Cell temp = cells[l][m];
                                if (!temp.GetIsByDefault() && temp.GetPossibleValues().size() >= 3 && temp.HasTriplet(triplets.get(k))){
                                    toChange.add(temp);
                                    if (toChange.size() > 3){
                                        break find;
                                    }
                                }
                            }
                        }
                        // Do we need to clean the both cells
                        if (toChange.size() == 3){
                            while (!toChange.empty()){
                                Cell temp = toChange.pop();
                                temp.RemovePossibleValuesExceptTriplet(triplets.get(k));
                            }
                            changes++;
                            break;
                        }
                    }
                }
            }
        }
        return changes > 0;
    }

    /**
     * Check if a possible value is unique in this box.
     * @return true if the value is unique.
     */
    private boolean IsThisValueUniqueInBox(int value, int cellToSkipRow, int cellToSkipColumn){
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We omit the main cell
                if (i != cellToSkipRow && j != cellToSkipColumn){
                    Cell cell = cells[i][j];
                    if (cell.HasPossibleValue(value)){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Check if a possible value is unique in a given row.
     * @param value the value.
     * @param row the row.
     * @param cellToSkipColumn the column of the cell who has the value.
     * @return true if the value is unique.
     */
    private boolean IsThisValueUniqueInRow(int value, int row, int cellToSkipColumn){
        for (int j = 0; j < cells.length; j++){
            // We omit the main cell
            if(j != cellToSkipColumn){
                Cell cell = cells[row][j];
                if (cell.HasPossibleValue(value)){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if a possible value is unique in a given column.
     * @param value the value.
     * @param column the column.
     * @param cellToSkipRow the row of the cell who has the value.
     * @return true if the value is unique.
     */
    private boolean IsThisValueUniqueInColumn(int value, int column, int cellToSkipRow){
        for (int i = 0; i < cells.length; i++){
            // We omit the main cell
            if(i != cellToSkipRow){
                Cell cell = cells[i][column];
                if (cell.HasPossibleValue(value)){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Check if a pair of possible values only exist twice in the box
     * @param pair the pair.
     * @param cell the cell who has the pair.
     * @return the cell who has the another pair.
     */
    private Cell IsThisPairOnyTwiceInBox(Integer[] pair, Cell cell){
        Cell candidate = null;
        boolean onlyTwice = false;
        find :
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell temp = cells[i][j];
                // We omit the main cell
                if (temp != cell){
                    // We want to avoid use twice elimination with cells with literally the same possible values
                    if (temp.GetPossibleValues().contains(cell.GetPossibleValues())){
                        onlyTwice = false;
                        break find;
                    }
                    // Is this a valid candidate?
                    if (!cell.GetIsByDefault() && cell.GetPossibleValues().size() >= 3 && cell.HasPair(pair)){
                        if (candidate != null){
                            onlyTwice = false;
                            break find;
                        }else{
                            candidate = temp;
                            onlyTwice = true;
                        }
                    }
                }
            }
        }
        if (onlyTwice){
            return candidate;
        }
        return null;
    }

    /**
     * Check if a pair of possible values only exist twice in a given row.
     * @param pair the pair.
     * @param row the row.
     * @param cell the cell who has the pair.
     * @return the other cell who has the pair.
     */
    private Cell IsThisPairOnlyTwiceInRow(Integer[] pair, int row, Cell cell){
        Cell candidate = null;
        boolean onlyTwice = false;
        for (int j = 0; j < cells.length; j++){
            // We omit the main cell
            Cell temp = cells[row][j];
            if (temp != cell){
                // We want to avoid use twice elimination with cells with literally the same possible values
                if (temp.GetPossibleValues().contains(cell.GetPossibleValues())){
                    onlyTwice = false;
                    break;
                }
                // Is this valid candidate?
                if (!cell.GetIsByDefault() && cell.GetPossibleValues().size() >= 3 && cell.HasPair(pair)){
                    if (candidate != null){
                        onlyTwice = false;
                        break;
                    }else{
                        candidate = temp;
                        onlyTwice = true;
                    }
                }
            }
        }
        if (onlyTwice){
            return  candidate;
        }
        return  null;
    }

    /**
     * Check if a pair of possible values only exist twice in a given column.
     * @param pair the pair.
     * @param column the column.
     * @param cell the cell who has the pair.
     * @return the other cell who has the pair.
     */
    private Cell IsThisPairOnlyTwiceInColumn(Integer[] pair, int column, Cell cell){
        Cell candidate = null;
        boolean onlyTwice = false;
        for (int i = 0; i < cells.length; i++){
            // We omit the main cell
            Cell temp = cells[i][column];
            if (temp != cell){
                // We want to avoid use twice elimination with cells with literally the same possible values
                if (temp.GetPossibleValues().contains(cell.GetPossibleValues())){
                    onlyTwice = false;
                    break;
                }
                // Is this valid candidate?
                if (!cell.GetIsByDefault() && cell.GetPossibleValues().size() >= 3 && cell.HasPair(pair)){
                    if (candidate != null){
                        onlyTwice = false;
                        break;
                    }else{
                        candidate = temp;
                        onlyTwice = true;
                    }
                }
            }
        }
        if (onlyTwice){
            return  candidate;
        }
        return  null;
    }

    /**
     * Find all default values in the box and clean them from each cell in the box.
     */
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

    /**
     * Clean a value from a row in the box.
     * @param value the value.
     * @param row the row index.
     * @param cellToSkipColumn the column of a cell that must be skipped.
     * @return true if the value was removed from any of the cells.
     */
    public boolean CleanPossibleValuesInRow(int value, int row, int cellToSkipColumn){
        boolean anyChange = false;
        // Loop through the row
        for (int j = 0; j < cells.length; j++){
            // Check if we must skip the cell
            if (j != cellToSkipColumn){
                Cell cell = cells[row][j];
                if (cell.RemovePossibleValue(value)){
                    anyChange = true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Clean a value from a column in the box.
     * @param value the value.
     * @param column the column index.
     * @param cellToSkipRow the row of a cell that must be skipped.
     * @return true if the value was removed from any of the cells.
     */
    public boolean CleanPossibleValuesInColumn(int value, int column, int cellToSkipRow){
        boolean anyChange = false;
        // Loop through the column
        for (int i = 0; i < cells.length; i++){
            // Check if we must skip the cell
            if (i != cellToSkipRow){
                Cell cell = cells[i][column];
                if(cell.RemovePossibleValue(value)){
                    anyChange = true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Clean a possible value from each cell in the box
     * @param value the value.
     * @return true if the value was removed from any of the cells.
     */
    public boolean CleanPossibleValueInBox(int value){
        boolean anyChange = false;
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if (cell.RemovePossibleValue(value)){
                    anyChange = true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Clean a possible value from each cell int the box skipping a cell
     * @param value the value.
     * @param toSkip the cell to skip.
     * @return true if the value was removed from any of the cells.
     */
    public boolean CleanPossibleValueInBox(int value, Cell toSkip){
        boolean anyChange = false;
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if (cell != toSkip){
                    if(cell.RemovePossibleValue(value)){
                        anyChange = true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Clean a set of possible values from each cell in the box
     * @param values a list with the values.
     * @return true if any of the values was removed from any of the cells.
     */
    public boolean CleanPossibleValuesInBox(List<Integer> values){
        boolean anyChange = false;
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if(cell.RemovePossibleValues(values)){
                    anyChange = true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Clean a set of possible values from each cell skipping a cell.
     * @param values a list with the values.
     * @param toSkip the cell to skip.
     * @return true if the value was removed from any of the cells.
     */
    public boolean CleanPossibleValuesInBox(List<Integer> values, Cell toSkip){
        boolean anyChange = false;
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if (cell != toSkip){
                    if(cell.RemovePossibleValues(values)){
                        anyChange = true;
                    }
                }
            }
        }
        return anyChange;
    }

}
