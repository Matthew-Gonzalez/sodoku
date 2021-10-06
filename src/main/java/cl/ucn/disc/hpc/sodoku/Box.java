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
                    log.warn("A box with the cell[{}][{}] has a repeated value", i, j);
                    return false;
                }
                values.add(value);
            }
        }
        return true;
    }

    /**
     * Cleans the rows of the box using Elimination technique.
     * @return true if any change was made.
     */
    public boolean SimpleEliminationRow(){
        boolean anyChange = false;
        // Loop through the box rows
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = 0; j < cells.length; j++){
                //For each cell in the row, we must verify if for any of its possible values it is the only valid cell
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && !cell.HasOnlyOnePossibleValue()){
                    for (int k = 0; k < cell.GetPossibleValues().size(); k++){
                        int value = cell.GetPossibleValues().get(k);
                        boolean isUnique = true;
                        // We loop through the row again
                        for (int h = 0; h < cells.length; h++){
                            // We omit the main cell
                            if (h == j){
                                continue;
                            }
                            Cell temp = cells[i][h];
                            // If the value is not unique in the row
                            if (temp.HasPossibleValue(value)){
                                isUnique = false;
                                break;
                            }
                        }
                        // If the value was unique we clean the other possible values from the cell
                        if (isUnique){
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

    public boolean SimpleEliminationColumn(){
        boolean anyChange = false;
        // Loop through the box rows
        for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
            for (int i = 0; i < cells.length; i++){
                //For each cell in the row, we must verify if for any of its possible values it is the only valid cell
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && !cell.HasOnlyOnePossibleValue()){
                    for (int k = 0; k < cell.GetPossibleValues().size(); k++){
                        int value = cell.GetPossibleValues().get(k);
                        boolean isUnique = true;
                        // We loop through the columns again
                        for (int h = 0; h < cells.length; h++){
                            // We omit the main cell
                            if (h == i){
                                continue;
                            }
                            Cell temp = cells[h][j];
                            // If the value is not unique in the row
                            if (temp.HasPossibleValue(value)){
                                isUnique = false;
                                break;
                            }
                        }
                        // If the value was unique we clean the other possible values from the cell
                        if (isUnique){
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
     * Cleans the box using Elimination technique.
     * @return true if any change was made.
     */
    public boolean SimpleEliminationBox(){
        boolean anyChange = false;
        // Loop through the box rows
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                //For each cell in the row, we must verify if for any of its possible values it is the only valid cell
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && !cell.HasOnlyOnePossibleValue()){
                    for (int k = 0; k < cell.GetPossibleValues().size(); k++){
                        int value = cell.GetPossibleValues().get(k);
                        boolean isUnique = true;
                        // We loop through the row again. We use a label to break the inner loop
                        find :
                        for (int l = yFromTo[0]; l <= yFromTo[1]; l++){
                            for(int m = xFromTo[0]; m <= xFromTo[1]; m++){
                                // We omit the main cell
                                if (l == i && m == j){
                                    continue;
                                }
                                Cell temp = cells[l][m];
                                // If the value is not unique in the row
                                if (temp.HasPossibleValue(value)){
                                    isUnique = false;
                                    break find;
                                }
                            }
                        }
                        // If the value was unique we clean the other possible values from the cell
                        if (isUnique){
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
     * Clean the rows of the box using Lone Rangers' technique
     * @return true if a change was made.
     */
    public boolean LoneRangersRow(){
        int changes = 0;
        // Loop through the box rows
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = 0; j < cells.length; j++){
                // We are looking for a not default cell with only one possible value
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && cell.HasOnlyOnePossibleValue()){
                    // Clean the row
                    if(CleanPossibleValuesInRow(cell.GetFirstPossibleValue(), i, j)){
                        changes++;
                    }
                }
            }
        }
        return changes > 0;
    }

    /**
     * Clean the columns of the box using Lone Rangers' technique
     * @return true if a change was made.
     */
    public boolean LoneRangersColumn(){
        boolean anyChange = false;
        // Loop through the box columns
        for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
            for (int i = 0; i < cells.length; i++){
                // We are looking for a not default cell with only one possible value
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && cell.HasOnlyOnePossibleValue()){
                    // Clean the column
                    if (CleanPossibleValuesInColumn(cell.GetFirstPossibleValue(), j, i)){
                        anyChange = true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Clean the box using Lone Rangers' technique
     * @return true if a change was made.
     */
    public boolean LoneRangersBox(){
        boolean anyChange = false;
        // Loop through the box cells
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We are looking for a not default cell with only one possible value
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && cell.HasOnlyOnePossibleValue()){
                    // Clean the box
                    if (CleanPossibleValueInBox(cell.GetFirstPossibleValue(), cell)){
                        anyChange = true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Clean the rows of the box using Twins' technique
     * @return true if a change was made.
     */
    private boolean TwinsRow(){
        int changes = 0;
        // Loop through the box cells
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = 0; j < cells.length; j++){
                // We are looking for a not default cell with three or more possible values
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && cell.GetPossibleValues().size() >= 3){
                    List<Integer[]> pairs = cell.GetUniquePairs();
                    for (int k = 0; k < pairs.size(); k++){
                        // We need to check if only one candidate cell was found
                        Cell candidate = null;
                        boolean isValid = false;
                        for (int h = 0; h < cells.length; h++){
                            // We omit the main cell
                            if (h == j){
                                continue;
                            }
                            // Is this a valid candidate?
                            Cell temp = cells[i][h];
                            if (!temp.GetIsByDefault() && temp.GetPossibleValues().size() >= 3 && temp.HasPair(pairs.get(k))){
                                if (candidate != null){
                                    isValid = false;
                                    break;
                                }else{
                                    candidate = temp;
                                    isValid = true;
                                }
                            }
                        }
                        // Do we need to clean the both cells?
                        if (isValid){
                            cell.RemovePossibleValuesExceptPair(pairs.get(k));
                            candidate.RemovePossibleValuesExceptPair(pairs.get(k));
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
     * Clean the columns of the box using Twins' technique
     * @return true if a change was made.
     */
    private boolean TwinsColumn(){
        int changes = 0;
        // Loop through the box cells
        for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
            for (int i = 0; i < cells.length; i++){
                // We are looking for a not default cell with three or more possible values
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && cell.GetPossibleValues().size() >= 3){
                    List<Integer[]> pairs = cell.GetUniquePairs();
                    for (int k = 0; k < pairs.size(); k++){
                        // We need to check if only one candidate cell was found
                        Cell candidate = null;
                        boolean isValid = false;
                        for (int h = 0; h < cells.length; h++){
                            // We omit the main cell
                            if (h == i){
                                continue;
                            }
                            // Is this a valid candidate?
                            Cell temp = cells[i][h];
                            if (!temp.GetIsByDefault() && temp.GetPossibleValues().size() >= 3 && temp.HasPair(pairs.get(k))){
                                if (candidate != null){
                                    isValid = false;
                                    break;
                                }else{
                                    candidate = temp;
                                    isValid = true;
                                }
                            }
                        }
                        // Do we need to clean the both cells?
                        if (isValid){
                            cell.RemovePossibleValuesExceptPair(pairs.get(k));
                            candidate.RemovePossibleValuesExceptPair(pairs.get(k));
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
    public boolean TwinsBox(){
        int changes = 0;
        // Loop through the box cells
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We are looking for a not default cell with three or more possible values
                Cell cell = cells[i][j];
                if (!cell.GetIsByDefault() && cell.GetPossibleValues().size() >= 3){
                    List<Integer[]> pairs = cell.GetUniquePairs();
                    for (int k = 0; k < pairs.size(); k++){
                        // We need to check if only one candidate cell was found
                        Cell candidate = null;
                        boolean isValid = false;
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
                                if (!temp.GetIsByDefault() && temp.GetPossibleValues().size() >= 3 && temp.HasPair(pairs.get(k))){
                                    if (candidate != null){
                                        isValid = false;
                                        break find;
                                    }else{
                                        candidate = temp;
                                        isValid = true;
                                    }
                                }
                            }
                        }
                        // Do we need to clean the both cells?
                        if (isValid){
                            cell.RemovePossibleValuesExceptPair(pairs.get(k));
                            candidate.RemovePossibleValuesExceptPair(pairs.get(k));
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
        int changes = 0;
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if (cell.RemovePossibleValue(value)){
                    changes++;
                }
            }
        }
        return changes > 0;
    }

    /**
     * Clean a possible value from each cell int the box skipping a cell
     * @param value the value.
     * @param toSkip the cell to skip.
     * @return true if the value was removed from any of the cells.
     */
    public boolean CleanPossibleValueInBox(int value, Cell toSkip){
        int changes = 0;
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if (cell != toSkip){
                    if(cell.RemovePossibleValue(value)){
                        changes++;
                    }
                }
            }
        }
        return changes > 0;
    }

    /**
     * Clean a set of possible values from each cell in the box
     * @param values a list with the values.
     * @return true if any of the values was removed from any of the cells.
     */
    public boolean CleanPossibleValuesInBox(List<Integer> values){
        int changes = 0;
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if(cell.RemovePossibleValues(values)){
                    changes++;
                }
            }
        }
        return changes > 0;
    }

    /**
     * Clean a set of possible values from each cell skipping a cell.
     * @param values a list with the values.
     * @param toSkip the cell to skip.
     * @return true if the value was removed from any of the cells.
     */
    public boolean CleanPossibleValuesInBox(List<Integer> values, Cell toSkip){
        int changes = 0;
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                if (cell != toSkip){
                    if(cell.RemovePossibleValues(values)){
                        changes++;
                    }
                }
            }
        }
        return changes > 0;
    }

}
