/* Starter code for PERT algorithm (Project 4)
 * @author
 */

// change dsa to your netid
package dsa;

import dsa.Graph;
import dsa.Graph.Vertex;
import dsa.Graph.Edge;
import dsa.Graph.GraphAlgorithm;
import dsa.Graph.Factory;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack;

public class PERT extends GraphAlgorithm<PERT.PERTVertex> {
    LinkedList<Vertex> finishList;
    private static int criticalPathLength;
    private static int countofCriticalTasks;

    public static class PERTVertex implements Factory {
        // Add fields to represent attributes of vertices here
        public int name; // name of the vertex
        public boolean seen; // flag to check if the vertex has already been visited
        public boolean active; //active flag
        public int duration; // duration required for task
        public int EC; // earliest completion time when task is completed
        public int LC; // latest completion time when task is completed
        public int LS; // latest start
        public int slack; // slack

        public PERTVertex(Vertex u) {
            seen = false;
            active=false;
            EC=0;
            LS=0;
            LC=0;
            slack=0;
            duration = 0;
        }
        public PERTVertex make(Vertex u) { return new PERTVertex(u); }
    }

    // Constructor for PERT is private. Create PERT instances with static method pert().
    private PERT(Graph g) {
        super(g, new PERTVertex(null));
    }

    public void setDuration(Vertex u, int d) {
        get(u).duration = d;
    }

    // Implement the PERT algorithm. Returns false if the graph g is not a DAG.
    public boolean pert() {
        //calculate topological ordering
        finishList = topologicalOrder();

        //create a reverse stack of Vertex for calculating the LC
        Stack<Vertex> revStack = new Stack<Vertex>();
        for(Vertex v: finishList){
            revStack.push(v);
        }

        //calculate EC
        for(Graph.Vertex u : finishList){
            System.out.print(u.name + " ");
            get(u).EC = ec(u);
        }
        System.out.println();

        //calculate LC
        criticalPathLength = get(revStack.peek()).EC;
        while(!revStack.isEmpty()){
            Graph.Vertex u = revStack.pop();
            System.out.print(u.name + " ");
            get(u).LC = lc(u);
        }
        System.out.println();

        //calculate slack
        for(Graph.Vertex u : g){
            get(u).slack = slack(u);
        }

        show();
        return true;
    }

    //Stack of Vertex in topological order
    Stack<Vertex> stackVertex = new Stack<Vertex>();

    // Find a topological order of g using DFS
    LinkedList<Vertex> topologicalOrder() {
        //set all active and seen flag of all vertices to false
        for (Vertex u : g) {
            get(u).seen = false;
            get(u).active = false;
        }
        //call DFSVisit on each unseen Vertex
        for (Vertex u : g) {
            if(!get(u).seen){
                dfsVisit(u);
            }
        }
        LinkedList<Graph.Vertex> topologicalList = new LinkedList<Graph.Vertex>();
        while(!stackVertex.isEmpty()){
            topologicalList.add(stackVertex.pop());
        }
        return topologicalList;
    }

    void dfsVisit(Vertex u) {
        get(u).seen=true;// set u as seen
        get(u).active=true;// set u as active
        /*for each vertex adjacent to u check if it is seen*/
        for(Edge e: g.adj(u).outEdges) {
            if(!get(e.to).seen){// if it is not seen call DFSVisit for that adjacent vertex
                if(u.name == 100){
                    System.out.println("I'm going to " + e.to.name);
                }
                dfsVisit(e.to);
            }else if(get(e.to).active){// if the adjacent vertex is already active then throw exception
                try {
                    throw new Exception("Not a DAG");
                } catch (Exception e1) {
                    System.out.println(e1.getMessage());
                }
            }
        }
        //push u on stack
        stackVertex.push(u);
        get(u).active=false; //set u as not active
    }

    // The following methods are called after calling pert().

    // Earliest time at which task u can be completed
    public int ec(Vertex u) {
        int EC = 0;
        if(u.inDegree() == 0)
            EC = get(u).duration;
        else {
            int maxEC = 0;
            for (Graph.Edge v : g.adj(u).inEdges) {
                if (maxEC <= get(v.from).EC) {
                    maxEC = get(v.from).EC;
                    EC = maxEC + get(u).duration;
                }
            }
        }
        return EC;
    }

    // Latest completion time of u
    public int lc(Vertex u) {
        int LC = 0;
            //set LC for the finish node = critical path length
            if(u.outDegree() == 0){
                LC = criticalPathLength;//set LC for the finish node = critical path length
            }
            else if (u.inDegree() == 0){
                LC = get(u).duration;
            }
            else{
                int minLC = Integer.MAX_VALUE;
                for (Graph.Edge e: g.adj(u).outEdges) {
                    Graph.Vertex v = e.to;
                    if(minLC > (get(v).LC-get(v).duration)){
                        minLC = get(v).LC-get(v).duration;
                        LC = minLC;
                    }
                }
            }
        return LC;
    }

    // Slack of u
    public int slack(Vertex u) {
        return  get(u).LC - get(u).EC;
    }

    // Length of a critical path (time taken to complete project)
    public int criticalPath() {
        return 0;
    }

    // Is u a critical vertex?
    public boolean critical(Vertex u) {
        return get(u).slack == 0;
    }

    // Number of critical vertices of g
    public int numCritical() {
        int count = 0;
        for (Graph.Vertex u : g) {
            if(get(u).slack == 0){ // calculate the total number of critical tasks
                count++;
            }
        }
        return count;
    }

    /* Create a PERT instance on g, runs the algorithm.
     * Returns PERT instance if successful. Returns null if G is not a DAG.
     */
    public static PERT pert(Graph g, int[] duration) {
        PERT p = new PERT(g);
        for(Vertex u: g) {
            p.setDuration(u, duration[u.getIndex()]);
        }
        // Run PERT algorithm.  Returns false if g is not a DAG
        if(p.pert()) {
            return p;
        } else {
            return null;
        }
    }

    public void show(){
        System.out.println("Number of critical vertices: " + numCritical() + " Total Duration: " + criticalPathLength);
        System.out.print("Nodes With No IN : ");
        for(Vertex u : g){
            if(u.inDegree() == 0)
                System.out.print(u.name + " ");
        }
        System.out.println();
        System.out.println("u\tDur\tEC\tLC\tSlack\tCritical");
        for(Graph.Vertex u: g) {
            System.out.println(u + "\t" + get(u).duration+ "\t" + get(u).EC + "\t" + get(u).LC + "\t" + get(u).slack + "\t" + critical(u));
        }
    }

    public static void main(String[] args) throws Exception {
        String graph = "10 13   1 2 1   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1";
        Scanner in;
        // If there is a command line argument, use it as file from which
        // input is read, otherwise use input from string.
//        in = args.length > 0 ? new Scanner(new File(args[0])) : new Scanner(graph);
        in = new Scanner(new File("test.txt"));
        Graph g = Graph.readDirectedGraph(in);
        g.printGraph(false);

        int[] duration = new int[g.size()];
        for(int i=0; i<g.size(); i++) {
            duration[i] = in.nextInt();
        }
        PERT p = pert(g, duration);
//        if(p == null) {
//            System.out.println("Invalid graph: not a DAG");
//        } else {
//            System.out.println("Number of critical vertices: " + p.numCritical());
//            System.out.println("u\tDur\tEC\tLC\tSlack\tCriticaal");
//            for(Graph.Vertex u: g) {
//                System.out.println(u + "\t" + duration[u.getIndex()] + "\t" + get(u).EC+ "\t" + p.lc(u) + "\t" + p.slack(u) + "\t" + p.critical(u));
//            }
//        }
    }
}
