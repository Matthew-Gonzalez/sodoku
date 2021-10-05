package cl.ucn.disc.hpc.sodoku;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * The Main class
 */
@Slf4j
public class Main {

    public static void main(String[] args) {
        Sodoku sodoku = ReadTextFile("src/main/resources/sodoku.txt");
        PrintSodokuCells(sodoku);
        sodoku.Solve();
        PrintSodokuCells(sodoku);
        if (sodoku.IsTheSodokuSolved()){
            log.debug("Solved!");
        }else{
            log.debug("Not solved! :c");
        }
    }

    public static Sodoku ReadTextFile(String path) {
        try {
            File file = new File(path);
            Scanner scanner = new Scanner(file);

            // Get the size
            int size = Integer.parseInt(scanner.nextLine());

            // Initialize an int matrix for the sodoku values
            int [][] grid = new int[size][size];

            // Fill the grid
            int row = 0;
            while (scanner.hasNextLine()){
                String[] line = scanner.nextLine().split(" ");
                for (int i = 0; i < size; i++){
                    grid[row][i] = Integer.parseInt(line[i]);
                }
                row++;
            }

            return new Sodoku(grid);

        }catch (FileNotFoundException e){
            log.error("File not found!");
            return null;
        }
    }

    public static void PrintSodokuCells(Sodoku sodoku){
        for (int i = 0; i < sodoku.GetCells().length; i++){
            List<List<Integer>> row = new ArrayList<>();
            for (int j = 0; j < sodoku.GetCells().length; j++){
                row.add(sodoku.GetCells()[i][j].GetPossibleValues());
            }
            log.debug("{}", row);
        }
        log.debug("");
    }
}
