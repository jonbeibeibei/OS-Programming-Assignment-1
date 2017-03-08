[//]: # (Programming Assignment 1)
[//]: # (Author 1: Jonathan Bei Qi Yang )
[//]: # (ID: 1001619)
[//]: # (Author 2: Ruth Wong Nam Ying)
[//]: # (ID: 1001795 )
[//]: # (Date: 08/03/2017)

# OS-Programming-Assignment-1

## Purpose of our program
Process management is an important concept in the study of computer systems engineering. This program aids the understanding of this concept by modelling the control and data dependencies of parent and child processes through the traversal of a Directed Acyclic Graph (DAG).
 Commands are written in an input text file, which is parsed and converted into a DAG according to its dependencies. Independent processes can start first, while child processes wait on their parent processes before execution. This graph is traversed, executing the command using Java’s ProcessBuilder. 

## Compilation
The code has already been compiled in the `out/production/OSProgrammingAssignment1` directory.

Run the Java program with the commands input text as the first argument.

Example:

`java ProcessManagement testproc.txt`

The output files from the program will be saved in the same directory.

## What it does

---
###Node class
This class stores the information of the processes specified in the commands input text. There are several parameters in this class:

- id: the process ID.
- program: the process information, stored in the first argument of the command.
- input: the input file for the process, stored in the third argument of the command.
- output: the output file for the process, stored in the fourth argument of the command. 
- children: the ArrayList containing all the IDs of its child processes.
- parents: the ArrayList containing all the IDs of its parent processes. 

The class also stores the status variables: 
- INELIGIBLE: the process cannot be run yet, because its parent processes are incomplete or its inputs are not ready yet. 
- READY: the process is ready for execution, waiting for the process manager to run it.
- RUNNING: the process is running.
- FINISHED: the process has finished running. 

---
###ProcessManagement class
This is the class containing the main method.

####instantiateNodes()
This method where the command input text is read, parsed and converted into nodes of the DAG. These nodes are stored and returned in an ArrayList.

####runNodes()
This method takes the process nodes as input, then uses multi-threading to handle the running of these processes. When these processes are complete, it prints an indication that all processes have finished execution. 

---
###ProgramThread class
This class extends the Thread class, allowing the processes to be executed concurrently. 

####run{}
This method takes the program information from the processes, breaks it down and executes it using Java’s Process Builder. When the process is successfully executed, it updates the static nodesCompleted variable so that runNodes() knows when to stop. 