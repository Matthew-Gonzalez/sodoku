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
     * Check if this is a valid box.
     * @return true if the box is valid.
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

    /**
     * Check if the rows of the box are valid.
     * @return true if the rows are valid.
     */
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

    /**
     * Check if the columns of the box are valid.
     * @return true if the rows are valid.
     */
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
     * Reduce possible values from cells using simple elimination technique.
     * @return true if any change was made.
     */
    public boolean SimpleElimination(){
        boolean anyChange = false;
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell cell = cells[i][j];
                // If the cell is not default and has multiple possible values
                if (!cell.GetIsByDefault() && !cell.HasOnlyOnePossibleValue()){
                    for (int value : cell.GetPossibleValues()){
                        // Is unique in box?
                        boolean isUniqueInBox = IsThisValueUniqueInBox(value, cell);
                        // Is unique in row?
                        boolean isUniqueInRow = IsThisValueUniqueInRow(value, i, cell);
                        // Is unique in column?
                        boolean isUniqueInColumn = IsThisValueUniqueInColumn(value, j, cell);
                        // Must clean?
                        if (isUniqueInBox || isUniqueInRow || isUniqueInColumn){
                            if (cell.RemovePossibleValueExceptOne(value)){
                                // Must clean from box, row and column
                                CleanPossibleValueInBox(value, cell);
                                CleanPossibleValueInRow(value, i, cell);
                                CleanPossibleValueInBox(value, cell);
                                return true;
                            }else{
                                log.debug("Tried to simple elimination on single value cell?");
                            }
                        }
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Reduce possible values from cells using loner rangers technique.
     * @return true if any change was made.
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
                    if (CleanPossibleValueInBox(value, cell) || CleanPossibleValueInRow(value, i, cell)
                            || CleanPossibleValueInColumn(value, j, cell)){
                        return true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Reduce possible values from cells using naked twins technique.
     * @return true if any change was made.
     */
    public boolean NakedTwins(){
        boolean anyChange = false;
        // Loop through the box cells
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We are looking for a not default cell with two possible values
                Cell cell = cells[i][j];
                if (cell.GetPossibleValues().size() == 2){
                    if (TryToNakedTwinsInBox(cell)){
                        return true;
                    }
                    if (TryToNakedTwinsInRow(cell, i)){
                        return true;
                    }
                    if (TryToNakedTwinsInColumn(cell, j)){
                        return true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Reduce possible values from box cells using naked twins technique.
     * @return true if any change was made.
     */
    public boolean NakedTwinsBox(){
        boolean anyChange = false;
        // Loop through the box cells
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We are looking for a not default cell with two possible values
                Cell cell = cells[i][j];
                if (cell.GetPossibleValues().size() == 2){
                    if (TryToNakedTwinsInBox(cell)){
                        return true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Reduce possible values from row cells using naked twins technique.
     * @return true if any change was made.
     */
    public boolean NakedTwinsRows(){
        boolean anyChange = false;
        // Loop through the box cells
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = 0; j < cells.length; j++){
                // We are looking for a not default cell with two possible values
                Cell cell = cells[i][j];
                if (cell.GetPossibleValues().size() == 2){
                    if (TryToNakedTwinsInRow(cell, i)){
                        return true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Reduce possible values from columns cells using naked twins technique.
     * @return true if any change was made.
     */
    public boolean NakedTwinsColumns(){
        boolean anyChange = false;
        // Loop through the box cells
        for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
            for (int i = 0; i < cells.length; i++){
                // We are looking for a not default cell with two possible values
                Cell cell = cells[i][j];
                if (cell.GetPossibleValues().size() == 2){
                    if (TryToNakedTwinsInColumn(cell, j)){
                        return true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Reduce possible values from cells using naked triplets technique.
     * @return true if any change was made.
     */
    public boolean NakedTriplets(){
        boolean anyChange = false;
        // Loop through the box cells
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We are looking for a not default cell with three possible values
                Cell cell = cells[i][j];
                if (cell.GetPossibleValues().size() == 3){
                    if (TryToNakedTripletsInBox(cell)){
                        return true;
                    }
                    if (TryToNakedTripletsInRow(cell, i)){
                        return true;
                    }
                    if (TryToNakedTripletsInColumn(cell, j)){
                        return true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Reduce possible values from box cells using naked triplets technique.
     * @return true if any change was made.
     */
    public boolean NakedTripletsBox(){
        boolean anyChange = false;
        // Loop through the box cells
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We are looking for a not default cell with three possible values
                Cell cell = cells[i][j];
                if (cell.GetPossibleValues().size() == 3){
                    if (TryToNakedTripletsInBox(cell)){
                        return true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Reduce possible values from rows cells using naked triplets technique.
     * @return true if any change was made.
     */
    public boolean NakedTripletsRows(){
        boolean anyChange = false;
        // Loop through the box cells
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = 0; j < cells.length; j++){
                // We are looking for a not default cell with three possible values
                Cell cell = cells[i][j];
                if (cell.GetPossibleValues().size() == 3){
                    if (TryToNakedTripletsInRow(cell, i)){
                        return true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Reduce possible values from columns cells using naked triplets technique.
     * @return true if any change was made.
     */
    public boolean NakedTripletsColumns(){
        boolean anyChange = false;
        // Loop through the box cells
        for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
            for (int i = 0; i < cells.length; i++){
                // We are looking for a not default cell with three possible values
                Cell cell = cells[i][j];
                if (cell.GetPossibleValues().size() == 3){
                    if (TryToNakedTripletsInColumn(cell, j)){
                        return true;
                    }
                }
            }
        }
        return anyChange;
    }

    /**
     * Reduce possible values from cells using hidden twins technique.
     * @return true if any change was made.
     */
    public boolean HiddenTwins(){
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We are looking for a not default cell with two or more possible values
                Cell cell = cells[i][j];
                if (cell.GetPossibleValues().size() >= 2){
                    List<Integer[]> pairs = cell.GetUniquePairs();
                    for (int k = 0; k < pairs.size(); k++){
                        if (TryToHiddenTwinsInBox(cell, pairs.get(k))){
                            return true;
                        }
                        if (TryToHiddenTwinsInRow(cell, pairs.get(k), i)){
                            return true;
                        }
                        if (TryToHiddenTwinsInColumn(cell, pairs.get(k), j)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Reduce possible values from cells using hidden triplets technique.
     * @return true if any change was made.
     */
    public boolean HiddenTriplets(){
        for(int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We are looking for a not default cell with two or more possible values
                Cell cell = cells[i][j];
                if (cell.GetPossibleValues().size() >= 3){
                    List<Integer[]> trios = cell.GetUniqueTrios();
                    for (int k = 0; k < trios.size(); k++){
                        if (TryToHiddenTripletsInBox(cell, trios.get(k))){
                            return true;
                        }
                        if (TryToHiddenTripletsInRow(cell, trios.get(k), i)){
                            return true;
                        }
                        if (TryToHiddenTripletsInColumn(cell, trios.get(k), j)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Try to use naked twins technique in box.
     * @param twin_1 the twin
     * @return true if any change was made.
     */
    private boolean TryToNakedTwinsInBox(Cell twin_1){
        boolean anyChange = false;
        Cell twin_2 = null;
        Stack<Cell> toClean = new Stack<>();
        // Loop through the box cells
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell temp = cells[i][j];
                // We skip twin_1 cell
                if (temp != twin_1){
                    // Is this cell the other twin?
                    if (temp.GetPossibleValues().equals(twin_1.GetPossibleValues())){
                        if (twin_2 != null){
                            return false;
                        }
                        twin_2 = temp;
                    }else if (!temp.HasOnlyOnePossibleValue()){
                        toClean.push(temp);
                    }
                }
            }
        }
        // If twins
        if (twin_2 != null){
            // Remove twins values from other cells
            while(!toClean.empty()){
                if (toClean.pop().RemovePossibleValues(twin_1.GetPossibleValues())){
                    anyChange = true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Try to use naked twins technique in row.
     * @param twin_1 the twin
     * @param row the row.
     * @return true if any change was made.
     */
    private boolean TryToNakedTwinsInRow(Cell twin_1, int row){
        boolean anyChange = false;
        Cell twin_2 = null;
        Stack<Cell> toClean = new Stack<>();
        // Loop through the box row
        for (int j = 0; j < cells.length; j++){
            Cell temp = cells[row][j];
            // We skip twin_1 cell
            if (temp != twin_1){
                // Is this cell the other twin?
                if (temp.GetPossibleValues().equals(twin_1.GetPossibleValues())){
                    if (twin_2 != null){
                        return false;
                    }
                    twin_2 = temp;
                }else if (!temp.HasOnlyOnePossibleValue()){
                    toClean.push(temp);
                }
            }
        }
        // If twins
        if (twin_2 != null){
            // Remove twins values from other cells
            while(!toClean.empty()){
                if (toClean.pop().RemovePossibleValues(twin_1.GetPossibleValues())){
                    anyChange = true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Try to use naked twins technique in column.
     * @param twin_1 the twin
     * @param column the column.
     * @return true if any change was made.
     */
    private boolean TryToNakedTwinsInColumn(Cell twin_1, int column){
        boolean anyChange = false;
        Cell twin_2 = null;
        Stack<Cell> toClean = new Stack<>();
        // Loop through the box column
        for (int i = 0; i < cells.length; i++){
            Cell temp = cells[i][column];
            // We skip twin_1 cell
            if (temp != twin_1){
                // Is this cell the other twin?
                if (temp.GetPossibleValues().equals(twin_1.GetPossibleValues())){
                    if (twin_2 != null){
                        return false;
                    }
                    twin_2 = temp;
                }else if (!temp.HasOnlyOnePossibleValue()){
                    toClean.push(temp);
                }
            }
        }
        // If twins
        if (twin_2 != null){
            // Remove twins values from other cells
            while(!toClean.empty()){
                if (toClean.pop().RemovePossibleValues(twin_1.GetPossibleValues())){
                    anyChange = true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Try to use naked triplets' technique in box.
     * @param triplet_1 the triplet.
     * @return true if any change was made.
     */
    private boolean TryToNakedTripletsInBox(Cell triplet_1){
        boolean anyChange = false;
        Cell triplet_2 = null;
        Cell triplet_3 = null;
        Stack<Cell> toClean = new Stack<>();
        // Loop through the box cells
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell temp = cells[i][j];
                // We skip twin_1 cell
                if (temp != triplet_1){
                    // Is this cell a triplet?
                    if (temp.GetPossibleValues().equals(triplet_1.GetPossibleValues())){
                        if (triplet_2 == null){
                            triplet_2 = temp;
                        }else if (triplet_3 == null){
                            triplet_3 = temp;
                        }else{
                            return false;
                        }
                    }else if (!temp.HasOnlyOnePossibleValue()){
                        toClean.push(temp);
                    }
                }
            }
        }
        // If triplets
        if (triplet_2 != null && triplet_3 != null){
            // Remove twins values from other cells
            while(!toClean.empty()){
                if (toClean.pop().RemovePossibleValues(triplet_1.GetPossibleValues())){
                    anyChange = true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Try to use naked triplets' technique in row.
     * @param triplet_1 the triplet.
     * @param row the row.
     * @return true if any change was made.
     */
    private boolean TryToNakedTripletsInRow(Cell triplet_1, int row){
        boolean anyChange = false;
        Cell triplet_2 = null;
        Cell triplet_3 = null;
        Stack<Cell> toClean = new Stack<>();
        // Loop through the box cells
        for (int j = 0; j < cells.length; j++){
            Cell temp = cells[row][j];
            // We skip twin_1 cell
            if (temp != triplet_1){
                // Is this cell a triplet?
                if (temp.GetPossibleValues().equals(triplet_1.GetPossibleValues())){
                    if (triplet_2 == null){
                        triplet_2 = temp;
                    }else if (triplet_3 == null){
                        triplet_3 = temp;
                    }else{
                        return false;
                    }
                }else if (!temp.HasOnlyOnePossibleValue()){
                    toClean.push(temp);
                }
            }
        }
        // If triplets
        if (triplet_2 != null && triplet_3 != null){
            // Remove twins values from other cells
            while(!toClean.empty()){
                if (toClean.pop().RemovePossibleValues(triplet_1.GetPossibleValues())){
                    anyChange = true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Try to use naked triplets' technique in column.
     * @param triplet_1 the triplet.
     * @param column the column.
     * @return true if any change was made.
     */
    private boolean TryToNakedTripletsInColumn(Cell triplet_1, int column){
        boolean anyChange = false;
        Cell triplet_2 = null;
        Cell triplet_3 = null;
        Stack<Cell> toClean = new Stack<>();
        // Loop through the box cells
        for (int i = 0; i < cells.length; i++){
            Cell temp = cells[i][column];
            // We skip twin_1 cell
            if (temp != triplet_1){
                // Is this cell a triplet?
                if (temp.GetPossibleValues().equals(triplet_1.GetPossibleValues())){
                    if (triplet_2 == null){
                        triplet_2 = temp;
                    }else if (triplet_3 == null){
                        triplet_3 = temp;
                    }else{
                        return false;
                    }
                }else if (!temp.HasOnlyOnePossibleValue()){
                    toClean.push(temp);
                }
            }
        }
        // If triplets
        if (triplet_2 != null && triplet_3 != null){
            // Remove twins values from other cells
            while(!toClean.empty()){
                if (toClean.pop().RemovePossibleValues(triplet_1.GetPossibleValues())){
                    anyChange = true;
                }
            }
        }
        return anyChange;
    }

    /**
     * Try to use hidden twins technique in box.
     * @param twin_1 the twin.
     * @param pair the pair.
     * @return true if any change was made.
     */
    private boolean TryToHiddenTwinsInBox(Cell twin_1, Integer[] pair){
        boolean anyChange = false;
        Cell twin_2 = null;
        // Loop through the box cells
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell temp = cells[i][j];
                // We skip twin_1 cell
                if (temp != twin_1){
                    // Is this a valid twin?
                    if (temp.HasPair(pair)){
                        // If a twins was already found
                        if (twin_2 != null){
                            return false;
                        }
                        twin_2 = temp;
                    }
                    // If the cell is not a twin but has any of the pair values, we cannot use twins
                    if (temp.HasPossibleValue(pair[0]) || temp.HasPossibleValue(pair[1])){
                        if (temp.HasOnlyOnePossibleValue()){
                            log.error("Cell with single value is not unique in the box after use twins | value: {} | axis: [{},{}]", temp.GetFirstPossibleValue(), i, j);
                            PrintCells();
                        }
                        return false;
                    }
                }
            }
        }
        // If twins we clean the other values from these cells
        if (twin_2 != null){
            if (twin_1.RemovePossibleValuesExceptPair(pair)){
                anyChange = true;
            }
            if (twin_2.RemovePossibleValuesExceptPair(pair)){
                anyChange = true;
            }
        }
        return anyChange;
    }

    /**
     * Try to use hidden twins technique in row.
     * @param twin_1 the twin.
     * @param pair the pair.
     * @param row the row.
     * @return true if any change was made.
     */
    private boolean TryToHiddenTwinsInRow(Cell twin_1, Integer[] pair, int row){
        boolean anyChange = false;
        Cell twin_2 = null;
        // Loop through the row
        for (int j = 0; j < cells.length; j++){
            Cell temp = cells[row][j];
            // We skip twin_1 cell
            if (temp != twin_1){
                // Is this a valid twin?
                if (temp.HasPair(pair)){
                    // If a twins was already found
                    if (twin_2 != null){
                        return false;
                    }
                    twin_2 = temp;
                }
                // If the cell is not a twin but has any of the pair values, we cannot use twins
                if (temp.HasPossibleValue(pair[0]) || temp.HasPossibleValue(pair[1])){
                    if (temp.HasOnlyOnePossibleValue()){
                        log.error("Cell with single value is not unique in the row after use twins | value: {} | axis: [{},{}]", temp.GetFirstPossibleValue(), row, j);
                        PrintCells();
                    }
                    return false;
                }
            }
        }
        // If twins we clean the other values from these cells
        if (twin_2 != null){
            if (twin_1.RemovePossibleValuesExceptPair(pair)){
                anyChange = true;
            }
            if (twin_2.RemovePossibleValuesExceptPair(pair)){
                anyChange = true;
            }
        }
        return anyChange;
    }

    /**
     * Try to use hidden twins technique in column.
     * @param twin_1 the twin.
     * @param pair the pair.
     * @param column the column.
     * @return true if any change was made.
     */
    private boolean TryToHiddenTwinsInColumn(Cell twin_1, Integer[] pair, int column){
        boolean anyChange = false;
        Cell twin_2 = null;
        // Loop through the column
        for (int i = 0; i < cells.length; i++){
            Cell temp = cells[i][column];
            // We skip twin_1 cell
            if (temp != twin_1){
                // Is this a valid twin?
                if (temp.HasPair(pair)){
                    // If a twins was already found
                    if (twin_2 != null){
                        return false;
                    }
                    twin_2 = temp;
                }
                // If the cell is not a twin but has any of the pair values, we cannot use twins
                if (temp.HasPossibleValue(pair[0]) || temp.HasPossibleValue(pair[1])){
                    if (temp.HasOnlyOnePossibleValue()){
                        log.error("Cell with single value is not unique in the column after use twins | value: {} | axis: [{},{}]", temp.GetFirstPossibleValue(), i, column);
                        PrintCells();
                    }
                    return false;
                }
            }
        }
        // If twins we clean the other values from these cells
        if (twin_2 != null){
            if (twin_1.RemovePossibleValuesExceptPair(pair)){
                anyChange = true;
            }
            if (twin_2.RemovePossibleValuesExceptPair(pair)){
                anyChange = true;
            }
        }
        return anyChange;
    }

    /**
     * Try to use hidden twins technique in box.
     * @param triplet_1 the triplet.
     * @param trio the trio.
     * @return true if any change was made.
     */
    private boolean TryToHiddenTripletsInBox(Cell triplet_1, Integer[] trio){
        boolean anyChange = false;
        Cell triplet_2 = null;
        Cell triplet_3 = null;
        // Loop through the box cells
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell temp = cells[i][j];
                // We skip twin_1 cell
                if (temp != triplet_1){
                    // Is this a valid triplet?
                    if (temp.HasTrio(trio)){
                        if (triplet_2 == null){
                            triplet_2 = temp;
                        }else if (triplet_3 == null){
                            triplet_3 = temp;
                        }else{
                            // There are more than three cells with the trio
                            return false;
                        }
                    }
                    // If the cell is not a triplet but has any of the trio values, we cannot use triplets
                    if (temp.HasPossibleValue(trio[0]) || temp.HasPossibleValue(trio[1]) || temp.HasPossibleValue(trio[2])){
                        if (temp.HasOnlyOnePossibleValue()){
                            log.error("Cell with single value is not unique in the box after use twins | value: {} | axis: [{},{}]", temp.GetFirstPossibleValue(), i, j);
                            PrintCells();
                        }
                        return false;
                    }
                }
            }
        }
        // If triplets we clean the other values from these cells
        if (triplet_2 != null && triplet_3 != null){
            if (triplet_1.RemovePossibleValuesExceptTrio(trio)){
                anyChange = true;
            }
            if (triplet_2.RemovePossibleValuesExceptTrio(trio)){
                anyChange = true;
            }
            if (triplet_3.RemovePossibleValuesExceptTrio(trio)){
                anyChange = true;
            }
        }
        return anyChange;
    }

    /**
     * Try to use hidden twins technique in row.
     * @param triplet_1 the triplet.
     * @param trio the trio.
     * @param row the row.
     * @return true if any change was made.
     */
    private boolean TryToHiddenTripletsInRow(Cell triplet_1, Integer[] trio, int row){
        boolean anyChange = false;
        Cell triplet_2 = null;
        Cell triplet_3 = null;
        // Loop through the box cells
        for (int j = 0; j < cells.length; j++){
            Cell temp = cells[row][j];
            // We skip twin_1 cell
            if (temp != triplet_1){
                // Is this a valid triplet?
                if (temp.HasTrio(trio)){
                    if (triplet_2 == null){
                        triplet_2 = temp;
                    }else if (triplet_3 == null){
                        triplet_3 = temp;
                    }else{
                        // There are more than three cells with the trio
                        return false;
                    }
                }
                // If the cell is not a triplet but has any of the trio values, we cannot use triplets
                if (temp.HasPossibleValue(trio[0]) || temp.HasPossibleValue(trio[1]) || temp.HasPossibleValue(trio[2])){
                    if (temp.HasOnlyOnePossibleValue()){
                        log.error("Cell with single value is not unique in the box after use twins | value: {} | axis: [{},{}]", temp.GetFirstPossibleValue(), row, j);
                        PrintCells();
                    }
                    return false;
                }
            }
        }
        // If triplets we clean the other values from these cells
        if (triplet_2 != null && triplet_3 != null){
            if (triplet_1.RemovePossibleValuesExceptTrio(trio)){
                anyChange = true;
            }
            if (triplet_2.RemovePossibleValuesExceptTrio(trio)){
                anyChange = true;
            }
            if (triplet_3.RemovePossibleValuesExceptTrio(trio)){
                anyChange = true;
            }
        }
        return anyChange;
    }

    /**
     * Try to use hidden twins technique in column.
     * @param triplet_1 the triplet.
     * @param trio the trio.
     * @param column the column.
     * @return true if any change was made.
     */
    private boolean TryToHiddenTripletsInColumn(Cell triplet_1, Integer[] trio, int column){
        boolean anyChange = false;
        Cell triplet_2 = null;
        Cell triplet_3 = null;
        // Loop through the box cells
        for (int i = 0; i < cells.length; i++){
            Cell temp = cells[i][column];
            // We skip twin_1 cell
            if (temp != triplet_1){
                // Is this a valid triplet?
                if (temp.HasTrio(trio)){
                    if (triplet_2 == null){
                        triplet_2 = temp;
                    }else if (triplet_3 == null){
                        triplet_3 = temp;
                    }else{
                        // There are more than three cells with the trio
                        return false;
                    }
                }
                // If the cell is not a triplet but has any of the trio values, we cannot use triplets
                if (temp.HasPossibleValue(trio[0]) || temp.HasPossibleValue(trio[1]) || temp.HasPossibleValue(trio[2])){
                    if (temp.HasOnlyOnePossibleValue()){
                        log.error("Cell with single value is not unique in the box after use twins | value: {} | axis: [{},{}]", temp.GetFirstPossibleValue(), i, column);
                        PrintCells();
                    }
                    return false;
                }
            }
        }
        // If triplets we clean the other values from these cells
        if (triplet_2 != null && triplet_3 != null){
            if (triplet_1.RemovePossibleValuesExceptTrio(trio)){
                anyChange = true;
            }
            if (triplet_2.RemovePossibleValuesExceptTrio(trio)){
                anyChange = true;
            }
            if (triplet_3.RemovePossibleValuesExceptTrio(trio)){
                anyChange = true;
            }
        }
        return anyChange;
    }

    /**
     * Check if a possible value is unique in this box.
     * @param cell the cell with the value.
     * @return true if the value is unique.
     */
    private boolean IsThisValueUniqueInBox(int value, Cell cell){
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                // We omit the main cell
                Cell temp = cells[i][j];
                if (temp != cell){
                    if (temp.HasPossibleValue(value)){
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
     * @param cell the cell with the value.
     * @return true if the value is unique.
     */
    private boolean IsThisValueUniqueInRow(int value, int row, Cell cell){
        for (int j = 0; j < cells.length; j++){
            Cell temp = cells[row][j];
            if (temp != cell){
                if (temp.HasPossibleValue(value)){
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
     * @param cell the cell with the value.
     * @return true if the value is unique.
     */
    private boolean IsThisValueUniqueInColumn(int value, int column, Cell cell){
        for (int i = 0; i < cells.length; i++){
            Cell temp = cells[i][column];
            if (temp != cell){
                if (cell.HasPossibleValue(value)){
                    return false;
                }
            }
        }
        return true;
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
     * @param cell the cell with the value.
     * @return true if the value was removed from any of the cells.
     */
    public boolean CleanPossibleValueInRow(int value, int row, Cell cell){
        boolean anyChange = false;
        // Loop through the row
        for (int j = 0; j < cells.length; j++){
            Cell temp = cells[row][j];
            if (temp != cell){
                if (temp.RemovePossibleValue(value)){
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
     * @param cell the cell with the value.
     * @return true if the value was removed from any of the cells.
     */
    public boolean CleanPossibleValueInColumn(int value, int column, Cell cell){
        boolean anyChange = false;
        // Loop through the column
        for (int i = 0; i < cells.length; i++){
            Cell temp = cells[i][column];
            if (temp != cell){
                if(temp.RemovePossibleValue(value)){
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
     * @param cell the cell to skip.
     * @return true if the value was removed from any of the cells.
     */
    public boolean CleanPossibleValueInBox(int value, Cell cell){
        boolean anyChange = false;
        // Loop through the cells inside the box
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            for(int j = xFromTo[0]; j <= xFromTo[1]; j++){
                Cell temp = cells[i][j];
                if (temp != cell){
                    if(temp.RemovePossibleValue(value)){
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

    /**
     * Get a clone of this box given a cells' matrix.
     * @param cells the cells' matrix for the clone.
     * @return the clone.
     */
    public Box GetClone(Cell[][] cells){
        return new Box(cells, this.xFromTo[0], this.xFromTo[1], this.yFromTo[0], this.yFromTo[1]);
    }

    /**
     * Print the cells of the sudoku from a box.
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
     * Print the cells of this box.
     */
    public void PrintBox(){
        for (int i = yFromTo[0]; i <= yFromTo[1]; i++){
            List<List<Integer>> row = new ArrayList<>();
            for (int j = xFromTo[0]; j <= xFromTo[1]; j++){
                row.add(cells[i][j].GetPossibleValues());
            }
            log.debug("{}", row);
        }
        log.debug("");
    }

    /**
     * Print a row of cells.
     * @param row the row.
     */
    public void PrintRow(int row){
        List<List<Integer>> toPrint = new ArrayList<>();
        for (int j = 0; j < cells.length; j++){
            toPrint.add(cells[row][j].GetPossibleValues());
        }
        log.debug("{}", toPrint);
        log.debug("");
    }

    /**
     * Print a column of cells.
     * @param column the column.
     */
    public void PrintColumn(int column){
        List<List<Integer>> toPrint = new ArrayList<>();
        for (int i = 0; i < cells.length; i++){
            toPrint.add(cells[i][column].GetPossibleValues());
        }
        log.debug("{}", toPrint);
        log.debug("");
    }
}
