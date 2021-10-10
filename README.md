# Sudoku Solver 
Paralell Sodoku Solver developed in HPC-2021-2

&nbsp;

## The solver
The sudoku solver is developed in Java trying to be the most readable possible for anyone who want use this code.

&nbsp;

## How does it work?
The solver tries to solve the sudoku by following these two steps:

1. Reduce: Try to remove as many values as possible from each cell using humanistic techniques like naked twins, hidden triplets, etc.
2. Brute force: If the sudoku was not solved then we use "brute force", and that is, for each possible value in a cell we clone the sudoku with that value and try to solve it with reduction again. This is an iterative process until the solution is found.

For both steps we can use parallelism, but I recommend using it only for "brute force", the reduction algorithm is incredibly fast on its own and using parallelism will only make it slower. Anyway, most of the time using reduction will be enough to solve the sudoku puzzle.
