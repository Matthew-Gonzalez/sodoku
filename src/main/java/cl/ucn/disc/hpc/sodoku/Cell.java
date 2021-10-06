package cl.ucn.disc.hpc.sodoku;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
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
     * @return true if the value was removed.
     */
    public boolean RemovePossibleValue(int value){
        if (isByDefault || possibleValues.size() == 1){
            return false;
        }
        // Check if the value already exists
        int index = possibleValues.indexOf(value);
        if (index >= 0){
            possibleValues.remove(index);
            return true;
        }else{
            return false;
        }
    }

    /**
     * Remove a set of possible values.
     * @param values a list with the values.
     * @return true if any of the values was removed.
     */
    public boolean RemovePossibleValues(List<Integer> values){
        if (isByDefault || possibleValues.size() == 1){
            return false;
        }
        int removed = 0;
        for (Integer value : values) {
            // Check if the value already exists
            int index = possibleValues.indexOf(value);
            if (index >= 0){
                possibleValues.remove(index);
                removed++;
            }
        }
        return removed > 0;
    }

    /**
     * Given a value remove all possible values except that one.
     * @param value the value to maintain.
     */
    public void RemovePossibleValuesExceptOne(int value){
        possibleValues.clear();
        possibleValues.add(value);
    }

    /**
     * Given a pair remove all possible values except that pair.
     * @param pair the pair.
     */
    public void RemovePossibleValuesExceptPair(Integer[] pair){
        List<Integer> pairAsList = Arrays.asList(pair);
        possibleValues.retainAll(pairAsList);
    }

    /**
     * Given a triplet remove all possible values except that triplet.
     * @param triplet the triplet.
     */
    public void RemovePossibleValuesExceptTriplet(Integer[] triplet){
        List<Integer> tripletAstList = Arrays.asList(triplet);
        possibleValues.retainAll(tripletAstList);
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
     * Get a list with the unique pairs between the possible values for the cell
     * @return a list of arrays where each array is a pair.
     */
    public List<Integer[]> GetUniquePairs(){
        List<Integer[]> pairs = new ArrayList<>();
        for (int i = 0; i < this.possibleValues.size() - 1; i++){
            for (int j = i + 1; j < this.possibleValues.size(); j++){
                Integer[] pair = {this.possibleValues.get(i),this.possibleValues.get(j)};
                pairs.add(pair);
            }
        }
        return pairs;
    }

    /**
     * Check if the cell contains all values of a given pair.
     * @param pair the pair.
     * @return true if the cell contains all value of the pair.
     */
    public boolean HasPair(Integer[] pair){
        List<Integer> pairAsList = Arrays.asList(pair);
        return this.possibleValues.containsAll(pairAsList);
    }

    /**
     * Get a list with the unique triplets between the possible values for the cell
     * @return a list of arrays where each array is a pair.
     */
    public List<Integer[]> GetUniqueTriplets(){
        List<Integer[]> triplets = new ArrayList<>();
        for (int i = 0; i < this.possibleValues.size() - 2; i++){
            for (int j = i + 2; j < this.possibleValues.size(); j++){
                Integer[] triplet = {this.possibleValues.get(i), this.possibleValues.get(i + 1), this.possibleValues.get(j)};
                triplets.add(triplet);
            }
        }
        return triplets;
    }

    /**
     * Check if the cell contains all values of a given triplet.
     * @param triplet the triplet.
     * @return true if the cell contains all value of the triplet.
     */
    public boolean HasTriplet(Integer[] triplet){
        List<Integer> tripletAsList = Arrays.asList(triplet);
        return this.possibleValues.containsAll(tripletAsList);
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
