/* Programming Assignment 1 
* Author 1 : Jonathan Bei Qi Yang
* ID: 1001619
* Author 2 : Ruth Wong Name Ying
* ID: 1001795
* Date: 08/03/2017 */

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class ProcessManagement{

    //number of nodes that have completed execution
    public static int nodesCompleted = 0;

    public static void main(String[] args) {
        //name of text file that contains the graph information to be passed as an argument
        String fileName = args[0];

        //Set current directory of java program to be the working directory
        File currentDirectory = new File(System.getProperty("user.dir"));

        //Instantiating the nodes
        ArrayList<Node> nodes = instantiateNodes(fileName);

        printGraph(nodes);

        runNodes(nodes, currentDirectory);

    }

    /**
     * Create the nodes based on text file input given as argument
     * @param fileName - name of the text file which contains information about processes and the graph
     * @return ArrayList of nodes, each of which is a user program to be run
    * */
    private static ArrayList<Node> instantiateNodes(String fileName){
        // Creating an empty ArrayList of nodes to be populated
        ArrayList<Node> nodes = new ArrayList<Node>();
        try{
            //creating FileReader to read file input, and Buffered reader to read the contents of the file.
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line; //each line of the file we read

            // default start ID of process will be 0. Increment for each next process. (0 to n-1)
            int id = 0;

            //parse the file by lines, get the relevant information for processes
            while((line = bufferedReader.readLine())!= null){
                //splitting the lines according to ":" in the text file
                String[] nodeInformation = line.split(":");
                // creating an empty ArrayList of children, which IDs are stored as integers
                ArrayList<Integer> children = new ArrayList<Integer>();

                // Getting the children of a node.

                for (String child: nodeInformation[1].split(" ")){
                    if(child.equals("none")){
                        children = new ArrayList<>() ;
                        break; // break if no children found for current node
                    }
                    else{
                        try {
                            children.add(Integer.parseInt(child));
                        }
                        catch (NumberFormatException numberFormatException){
                            System.out.println("Invalid ID format for children of node "+ id);
                        }
                    }
                }

                //Create new Node for each line with the order: ID, Program, input, output, children.

                try{
                    nodes.add(new Node(id, nodeInformation[0], nodeInformation[2], nodeInformation[3], children));
                }
                catch (ArrayIndexOutOfBoundsException e){
                    System.out.println("Missing arguments from file, unable to initialize node");
                }
                catch (Exception e){
                    System.out.println("Invalid nodes detected, unable to initialize node");
                }
                id++;

            }
            bufferedReader.close();
            fileReader.close();



        } catch (FileNotFoundException e) {
            System.out.println("File does not exist!");
        } catch (IOException e) {
            System.out.println("IO Exception");
        }

        //Add parent information to each Node
        try{
            for (Node node: nodes){
                if(node.getChildren() != null){
                    for(Integer children : node.getChildren()){
                        nodes.get(children).addParent(node.getId());
                    }
                }
            }
        }
        catch (Exception e){
            System.out.println("Could not add parent information to node, child not found");
        }

        //If a node does not have any parents, it does not have any dependencies and can therefore be run.
        //Set such nodes as READY
        for(Node node: nodes){
            if(node.getNumberOfParents() == 0){
                node.setStatus(Node.READY);
            }
        }

        //returning instantiated nodes as an ArrayList of Nodes
        return nodes;


    }

    /**
     * Method that prints out the relevant details of every node in the graph. Prints the node's parents,
     * children, Command, InputFile, OutputFile, whether its runnable, as well as whether its been executed
     * @param nodes
     */

    public static void printGraph(ArrayList<Node> nodes){
        System.out.println();
        System.out.println("Graph info:");
        try{
            for(Node node: nodes){
                System.out.println("Node " + node.getId() + ": \nParent: ");
                if (node.getParents().isEmpty()){
                    System.out.print("none");
                }
                else {
                    for (int parentNode : node.getParents()) {
                        System.out.print(parentNode + " ");
                    }
                }
                System.out.println("\nChildren: ");
                try {
                    if (node.getChildren().isEmpty()) {
                        System.out.print("none");
                    } else {
                        for (int childNode : node.getChildren()) {
                            System.out.print(childNode + " ");
                        }
                    }
                }
                //Catch exception in the event that issues with children node parsing
                catch (NullPointerException e){
                    System.out.println(" ");
                }
                System.out.print("\nCommand: "+node.getProgram()+"    ");
                System.out.print("\nInput File: "+ node.getInput()+"    ");
                System.out.println("\nOutput File: " + node.getOutput() + "    ");
                System.out.println("Runnable: " + (node.getNumberOfParentsDone() == node.getNumberOfParents() && node.getStatus() == Node.READY));
                System.out.println("Executed: "+ (node.getStatus() == 3));
                System.out.println("\n");
            }
        }
        // Catch any overall exceptions to this method, to allow the program to continue in the event that
        // printing the graph fails
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Exception while printing graph!");
            return;
        }
    }

    /**
     * Method to execute nodes, depending on their current status: Run nodes that are READY and set to RUNNING while executing
     * @param nodes
     * @param currentDirectory
     */
    public static void runNodes(ArrayList<Node> nodes, File currentDirectory) {
        //ArrayList of Threads created
        ArrayList<ProgramThread> programThreads = new ArrayList<ProgramThread>();

        //Loop over all nodes till all have finished execution successfully
        while(nodesCompleted < nodes.size()){

            //Use an iterator to join and remove the threads that have finished execution
            for(Iterator<ProgramThread> iterator = programThreads.iterator(); iterator.hasNext();){
                ProgramThread programThread = iterator.next();
                try{
                    programThread.join();
                } catch (InterruptedException e) {
                    System.out.println("thread interrupted");
                }
                iterator.remove();
            }

            //Once a node's parents have finished execution, set the status of this node to READY
            for(Node node : nodes){
                if(node.getNumberOfParentsDone() == node.getNumberOfParents() && node.getStatus() == Node.INELIGIBLE){
                    node.setStatus(Node.READY);
                }
            }

            //Create a new Thread for each node that is READY for execution
            for(Node node: nodes){
                if(node.getStatus() == Node.READY){
                    System.out.println("Node " + node.getId() + " has begun execution");
                    //Defining new Thread and start execution
                    ProgramThread programThread = new ProgramThread(node, nodes, currentDirectory);
                    programThread.start();

                    //Adding thread to thread ArrayList
                    programThreads.add(programThread);

                    //Set the status of the current thread to be RUNNING
                    node.setStatus(Node.RUNNING);

                }
            }
        }

        System.out.println("All nodes have completed execution.");
    }


}

/**
 * Class that allows separate processes/Programs to run as Threads
 * Passes the process along to processbuilder to be run within thread, notifies user once process has finished
 * Handles the event of stdin or stdout not being the default, redirecting inputs and outputs in that case
 */
class ProgramThread extends Thread{
    private Node runningNode; //node which is currently executing
    private File currentDirectory;
    private ArrayList<Node> nodes; //list of all nodes

    private ProcessBuilder processBuilder;

    /**
     * Constructor for ProgramThread, requires 3 arguments.
     * @param runningNode - node which is currently executing
     * @param nodes - list of all nodes
     * @param currentDirectory - directory which program is running in
     */
    public ProgramThread(Node runningNode, ArrayList<Node> nodes, File currentDirectory){
        this.runningNode = runningNode;
        this.nodes = nodes;
        this.currentDirectory = currentDirectory;
    }

    public void run(){
        try{
            processBuilder = new ProcessBuilder();
            processBuilder.command(runningNode.getProgram().split(" "));
            processBuilder.directory(currentDirectory); //set working directory

            //redirect input file for the case of not stdin
            if(!runningNode.getInput().equals("stdin")){
                File input = new File(currentDirectory.getAbsolutePath() + "/" + runningNode.getInput());
                processBuilder.redirectInput(input);
            }
            //redirect output file for the case of not stdout
            if(!runningNode.getOutput().equals("stdout")){
                File output = new File(currentDirectory.getAbsolutePath() + "/" + runningNode.getOutput());
                processBuilder.redirectOutput(output);
            }

            //Start the current process
            Process process = processBuilder.start();
            process.waitFor();

            //Notify user that process has executed successfully
            System.out.println("Node " + runningNode.getId() + " has finished execution");

            //Synchronized method for safety
            synchronized (this){
                runningNode.setStatus(Node.FINISHED); // set the node's status to FINISHED

                //notify nodes of children of FINISHED status of node
                if(!runningNode.getChildren().isEmpty()){
                    for(Integer child: runningNode.getChildren()){
                        nodes.get(child).parentDone();
                    }
                }

                //Notify program that node has completed execution
                ProcessManagement.nodesCompleted++;
                System.out.println(ProcessManagement.nodesCompleted + " nodes have finished execution");
            }

        } catch (IOException e) {
            System.out.println("Node " + runningNode.getId() + " did not execute successfully, this may impact the progress" +
                    " of other nodes");
        } catch (InterruptedException e) {
            System.out.println("Node " + runningNode.getId() + " did not execute successfully, this may impact the progress" +
                    " of other nodes");
        }catch (IndexOutOfBoundsException e){
            System.out.println("node: " + runningNode.getId() + " Index out of bounds exception");

        }

    }
}


 /**
  * Class that defines the node structure that will represent processes in the program
  * Consists of the current status of the node, as well as its relationships to other nodes (parents and children)
  * Also consists of the program to be run, the input and output filenames
 ***/

class Node{

    // Status constants
	public static final int INELIGIBLE = 0;
	public static final int READY = 1;
	public static final int RUNNING = 2;
	public static final int FINISHED = 3;

	//class attributes 
	private int id;            // corresponds to number of lines in a graph text file
	private String program;    // program + arguments
	private String input;      //input filename
	private String output;     //output filename
	private ArrayList<Integer> parents = new ArrayList<>();
	private ArrayList<Integer> children = new ArrayList<>();

	private int finishedParents = 0; //number of parents nodes that have completed execution
	private int status = 0;          // 0: INELIGIBLE, 1: READY, 2: RUNNING, 3: FINISHED

    //Default Class Constructor
    public Node(int id, String program, String input, String output, ArrayList<Integer> children){
        this.id = id;
        this.program = program;
        this.input = input;
        this.output = output;
        this.children = children;
        this.parents = new ArrayList<>();
    }

    //getter methods
    public int getId() {
        return id;
    }

    public String getProgram() {
        return program;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }

    public ArrayList<Integer> getChildren() {
        return children;
    }

    public int getStatus() {
        return status;
    }

    public int getNumberOfParents(){
        return parents.size();
    }

    public int getNumberOfParentsDone(){
        return finishedParents;
    }

    public ArrayList<Integer> getParents(){
        return parents;
    }

    //setter methods

    public void setStatus(int status) {
        this.status = status;
    }

    // Method for adding parent information to node
     public void addParent(int parent){
        parents.add(parent);
     }

     // increase the number of parents that have completed execution
     public void parentDone(){
         finishedParents++;
     }

}

