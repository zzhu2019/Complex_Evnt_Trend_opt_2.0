# CET

## Usage

1. Compile and run the project by executing below:

````shell
./autoCompile.sh
````

The graph to traverse can either be random or read from existing file. The program will ask for a specified path if there is any. *(Might be changing to reading from file for the ease of modification and running multiple times)*

2. The program will ask for the number of times to run the algorithm, and the algorithm to run. Currently there are 5 existing ones available for experiment:
   - BFS
   - DFS
   - M_CET
   - T_CET
   - Anchor-Node algorithm (with 3 attributes to choose from)
       - Sequential / Concurrent - execution
       - Single / Double - layer concatenation
            - BFS / DFS - for each layer
       - Largest Degree / Equally spacing / Random - anchor selection

   
3. File intake: As long as the file conform the format of a valid DAG, it would be traverse successfully, the running time of each run and the average would be recorded in file for later comparison.The file format will be discussed in the next section.



## File intake format:


### Graph format 
*Note: this is when you choose to run execution a graph stored in an existing file.*

| Type                                                         | Format                                                    |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| Grid<br /> (Adjacency matrix)                                |  Grid  <img width=400/>  <br />6 <br />0,0,1,0,1,0, <br />1,0,0,0,0,0, <br />0,0,0,0,0,0, <br />1,0,0,0,0,0, <br />0,0,1,0,0,0, <br />0,0,0,1,0,0,  |
| Pair<br />Each pair is in the form of:<br />  `< Source Node, Dest Node>` | Sparse <br />10 <br />0,4 <br />2,5 <br />3,1 <br />4,2 <br />6,1 <br />6,2 <br />7,4 <br />7,6 <br />7,8 <br />7,9 <br />9,2 <br />9,5  |
| CSR<br />  The last two lines are column indices and row indices respectively | CSR <br/> 10 <br/> col: 4 5 0 8 9 9 1 5 6 9 4 4 4 2 4  <br/>  row: 0 2 5 6 9 10 11 12 13 15 15 <br/> |  

For file naming, in order to be able to easily recognize which type it belongs to, it is strongly suggested to use the form of: \<type\>\<num of nodes\>.txt

- Grid7.txt
- CSR10.txt
- Pair20.txt
- ...  
