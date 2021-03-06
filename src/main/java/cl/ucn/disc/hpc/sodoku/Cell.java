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

    /**
     * The constructor of a cell
     */
    public Cell(){
        // Initialize the list
        this.possibleValues = new ArrayList<>();
        this.isByDefault = false;
    }

    /**
     * If this is a default cell use this constructor
     * @param value the default value of the cell.
     */
    public Cell(int value){
        // Initialize the list
        this.possibleValues = new ArrayList<>();
        this.isByDefault = true;

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
        boolean anyRemoved = false;
        for (Integer value : values) {
            // Check if the value already exists
            int index = possibleValues.indexOf(value);
            if (index >= 0){
                possibleValues.remove(index);
                anyRemoved = true;
            }
        }
        return anyRemoved;
    }

    /**
     * Given a value remove all possible values except that one.
     * @param value the value to maintain.
     */
    public boolean RemovePossibleValueExceptOne(int value){
        if (possibleValues.size() == 1){
            return false;
        }
        possibleValues.clear();
        possibleValues.add(value);
        return true;
    }

    /**
     * Given a pair remove all possible values except that pair.
     * @param pair the pair.
     * @return true if any of the values was removed.
     */
    public boolean RemovePossibleValuesExceptPair(Integer[] pair){
        List<Integer> pairAsList = Arrays.asList(pair);
        if (this.possibleValues.equals(pairAsList)){
            return false;
        }
        return possibleValues.retainAll(pairAsList);
    }

    /**
     * Given a trio remove all possible values except that trio.
     * @param trio the triplet.
     */
    public boolean RemovePossibleValuesExceptTrio(Integer[] trio){
        List<Integer> tripletAstList = Arrays.asList(trio);
        if (this.possibleValues.equals(tripletAstList)){
            return false;
        }
        return possibleValues.retainAll(tripletAstList);
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
     * Get a list with the unique trios between the possible values for the cell
     * @return a list of arrays where each array is a trio.
     */
    public List<Integer[]> GetUniqueTrios(){
        List<Integer[]> trios = new ArrayList<>();
        for (int i = 0; i < this.possibleValues.size() - 2; i++){
            for (int j = i + 2; j < this.possibleValues.size(); j++){
                Integer[] trio = {this.possibleValues.get(i), this.possibleValues.get(i + 1), this.possibleValues.get(j)};
                trios.add(trio);
            }
        }
        return trios;
    }

    /**
     * Check if the cell contains all values of a given trio.
     * @param trio the trio.
     * @return true if the cell contains all value of the trio.
     */
    public boolean HasTrio(Integer[] trio){
        List<Integer> trioAsList = Arrays.asList(trio);
        return this.possibleValues.containsAll(trioAsList);
    }

    /**
     * A cell value can be by default.
     * @return true if this is a default cell.
     */
    public boolean GetIsByDefault(){
        return this.isByDefault;
    }

    /**
     * Get a clone of this cell.
     * @return the clone.
     */
    public Cell GetClone(){
        if (this.isByDefault){
            return new Cell(possibleValues.get(0));
        }else{
            Cell clone = new Cell();
            clone.GetPossibleValues().addAll(this.possibleValues);
            return  clone;
        }
    }
}
