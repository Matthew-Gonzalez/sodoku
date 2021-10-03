package cl.ucn.disc.hpc.sodoku;

import java.util.Collections;
import java.util.List;

/**
 * Represents a cell inside the sodoku.
 */
public class Cell {
    // A list that store the possible values of the cell
    private List<Integer> possibleValues;
    // True if the cell value comes by default
    private boolean isByDefault;
    // A cell can be accessed by only one thread at a time
    private boolean isLock;

    /**
     * Add a possible value.
     * @param value the value.
     */
    public void AddPossibleValue(int value){
        // Check if the value already exists
        if (!possibleValues.contains(value)){
            possibleValues.add(value);

            // Then we sort the list
            Collections.sort(possibleValues);
        }
    }

    /**
     * Remove a possible value.
     * @param value the value.
     */
    public void RemovePossibleValue(int value){
        // Check if the value already exists
        if(possibleValues.contains(value)){
            possibleValues.remove(value);
        }
    }

    /**
     * Check if there is only one possible value.
     * @return true if the size of the list is 1.
     */
    public boolean HasOnlyOneValue(){
        return possibleValues.size() == 1;
    }

    /**
     * Get a list with the current possible values for the cell.
     * @return a list with the possible values.
     */
    public List<Integer> GetPossibleValues() {
        return this.possibleValues;
    }


    /**
     * A cell value can be by default.
     * @return true if this is a default cell.
     */
    public boolean GetIsByDefault(){
        return this.isByDefault;
    }

    /**
     * A cell can be locked to prevent data errors.
     * @return true if the cell is locked.
     */
    public boolean GetIsLock(){
        return this.isLock;
    }

    /**
     * A cell can be locked to prevent data errors.
     * @param isLock true to lock this cell.
     */
    public void SetIsLock(boolean isLock){
        this.isLock = isLock;
    }
}
