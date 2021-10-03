package cl.ucn.disc.hpc.sodoku;

import java.util.ArrayList;
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
     * The constructor of a cell
     */
    public Cell(){
        // Initialize the list
        this.possibleValues = new ArrayList<>();

        this.isByDefault = false;
        this.isLock = false;
    }

    /**
     * If this is a default cell use this constructor
     * @param value the default value of the cell.
     */
    public Cell(int value){
        // Initialize the list
        this.possibleValues = new ArrayList<>();

        this.isByDefault = true;
        this.isLock = true;

        // Set the default value
        AddPossibleValue(value);
    }

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
     * Remove a set of possible values.
     * @param values a list with the values.
     */
    public void RemovePossibleValues(List<Integer> values){
        for (Integer value : values) {
            RemovePossibleValue(value);
        }
    }

    /**
     * Check if there is only one possible value.
     * @return true if the size of the list is 1.
     */
    public boolean HasOnlyOnePossibleValue(){
        return possibleValues.size() == 1;
    }

    /**
     * Check if this cell has a possible value
     * @param value the value.
     * @return true if the value exists.
     */
    public boolean HasPossibleValue(int value){
        return possibleValues.contains(value);
    }

    /**
     * Get the first possible value of the cell.
     * @return the first possible value.
     */
    public int GetFirstPossibleValue(){
        return this.possibleValues.get(0);
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
