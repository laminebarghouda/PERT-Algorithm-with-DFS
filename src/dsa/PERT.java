package dsa;

import dsa.Graph.Vertex;
import dsa.Graph.Edge;
import dsa.Graph.GraphAlgorithm;
import dsa.Graph.Factory;

import java.io.File;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Stack;

public class PERT extends GraphAlgorithm<PERT.PERTVertex> {

    // Class variables
    LinkedList<Vertex> finishList; // Contains the topological ordering of the vertices
    Stack<Vertex> tempVertexStack = new Stack<>(); // temporary stack used while browsing the graph and determining topological ordering

    public static class PERTVertex implements Factory {

        // Class variables
        public int ec; // earliest completion time
        public int lc; // latest completion time
        public boolean seen; // flag that would be used to check if the vertex has already been visited
        public boolean active; // flag that would indicate if that's the active flag or not
        public int duration; // duration of for task
        public int slack; // slack

        /**
         * PERTVertex constructor
         *
         * @param u Graph vertex
         */
        public PERTVertex(Vertex u) {
            ec = 0;
            lc = 0;
            slack = 0;
            duration = 0;
            seen = false;
            active = false;
        }

        /**
         * Creates a PERTVertex given a Graph vertex
         *
         * @param u
         * @return PERTVertex
         */
        public PERTVertex make(Vertex u) { return new PERTVertex(u); }
    }

    /**
     * Create PERT instances with static method pert()
     *
     * @param g graph
     */
    private PERT(Graph g) {
        super(g, new PERTVertex(null));
    }

    /**
     * Sets the duration of a given task
     *
     * @param u vertex
     * @param d duration of the task
     */
    public void setDuration(Vertex u, int d) {
        get(u).duration = d;
    }

    /**
     * Implementation of the PERT algorithm
     *
     * @return boolean : false if the graph g is not a DAG
     */
    public boolean pert() {
        // Calculates topological ordering of the graph using DFS Algorithm
        try {
            finishList = topologicalOrder();
        }
        catch (Exception e){
            return false;
        }


        // Calculating EC for each vertex of the graph
        for(Vertex u : finishList){
            get(u).ec = ec(u);
        }

        // Creating a reverse topological ordering
        Stack<Vertex> reverseTopologicalOrder = new Stack<>();
        for(Vertex v: finishList){
            reverseTopologicalOrder.push(v);
        }

        // Calculating LC
        while(!reverseTopologicalOrder.isEmpty()){
            Vertex u = reverseTopologicalOrder.pop();
            get(u).lc = lc(u);
        }

        // Calculating slack
        for(Vertex u : g){
            get(u).slack = slack(u);
        }

        return true;
    }


    /**
     * Determines the topological order of the graph using DFS
     *
     * @return LinkedList<Vertex>
     */
    LinkedList<Vertex> topologicalOrder() throws Exception {
        // Unmark all graph vertices flags
        for (Vertex u : g) {
            get(u).seen = false;
            get(u).active = false;
        }

        // Calling dfsVisit on each unseen Vertex
        for (Vertex u : g) {
            if(!get(u).seen){
                dfsVisit(u);
            }
        }

        // Initializing a list for the result
        LinkedList<Vertex> topologicalList = new LinkedList<>();
        while(!tempVertexStack.isEmpty()){
            topologicalList.add(tempVertexStack.pop());
        }

        return topologicalList;
    }

    /**
     * @param u active vertex
     */
    void dfsVisit(Vertex u) throws Exception {
        // Mark the actual vertex
        get(u).seen = true;
        get(u).active = true;

        // Check if the adjacent vertices are seen
        for(Edge e: g.adj(u).outEdges) {
            if(! get(e.to).seen){
                // DFSVisit for the adjacent vertex if it was not seen
                dfsVisit(e.to);
            }else if(get(e.to).active){
                /* If the adjacent vertex is active then the graph is not a DAG
                and in this case, an exception is thrown and will be caught on
                pert method to return false (Not a DAG) */
                throw new Exception();
            }
        }

        get(u).active = false;
        tempVertexStack.push(u);
    }

    /**
     * Determines the earliest time at which task u can be completed
     *
     * @param u vertex
     * @return int : the earliest completion time
     */
    public int ec(Vertex u) {
        int EC = 0;
        // If it's the entry index, ec = duration
        if(u.inDegree() == 0)
            EC = get(u).duration;
        else {
            // Searching the maximum ec of adjacent incoming edge vertices
            int maxEc = 0;
            for (Edge v : g.adj(u).inEdges) {
                if (get(v.from).ec >= maxEc) {
                    maxEc = get(v.from).ec;
                    EC = maxEc + get(u).duration;
                }
            }
        }

        return EC;
    }

    /**
     * Determines the Latest completion time at which task u can be completed
     *
     * @param u vertex
     * @return int : the latest completion time
     */
    public int lc(Vertex u) {
        int LC = 0;
        // If it's the finish vertex, lc = maximumEc
        if(u.outDegree() == 0){
            LC = criticalPath();
        }
//        // If it's the entry vertex, lc = duration
//        else if (u.inDegree() == 0){
//            LC = get(u).duration;
//        }
        else{
            // Searching the minimum lc of adjacent outgoing edge vertices
            int minLc = Integer.MAX_VALUE;
            for (Edge e: g.adj(u).outEdges) {
                Vertex v = e.to;
                if((get(v).lc - get(v).duration) < minLc){
                    minLc = get(v).lc - get(v).duration;
                    LC = minLc;
                }
            }
        }

        return LC;
    }

    /**
     * Determines the slack of a given vertex u
     *
     * @param u vertex
     * @return int : slack of the vertex
     */
    public int slack(Vertex u) {
        return  get(u).lc - get(u).ec;
    }

    /**
     * Determines the length of a critical path (time taken to complete project)
     *
     * @return int : length of a critical path
     */
    public int criticalPath() {
        int maximumEc = 0;
        for(Vertex u : finishList){
            if(get(u).ec > maximumEc){
                maximumEc = get(u).ec;
            }
        }
        return maximumEc;
    }

    /**
     * Checks whether a given vertex is critical
     *
     * @param u vertex
     * @return boolean : true if the vertex is critical, else false
     */
    public boolean critical(Vertex u) {
        return get(u).slack == 0;
    }

    /**
     * Determines the number of critical vertices of g
     *
     * @return int : number of critical vertices
     */
    public int numCritical() {
        int count = 0;
        for (Vertex u : g) {
            if(get(u).slack == 0){
                count++;
            }
        }
        return count;
    }

    /**
     * Create a PERT instance on g, runs the algorithm.
     *
     * @param g graph
     * @param duration graph vertices duration
     * @return PERT : an instance of PERT, null if G is not a DAG
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

    /**
     * If the graph is DAG, show the output results
     */
    private void showResult(){
        System.out.println("Number of critical vertices: " + numCritical());
        System.out.println(criticalPath());
        System.out.println("u\tDur\tEC\tLC\tSlack\tCritical");
        for(Vertex u: g) {
            System.out.println(u + "\t" + get(u).duration + "\t" + get(u).ec + "\t" + get(u).lc + "\t" + get(u).slack + "\t" + critical(u));
        }
    }

    public static void main(String[] args) throws Exception {
        String graph = "10 13   1 2 1   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1";
        Scanner in;
        // If there is a command line argument, use it as file from which
        // input is read, otherwise use input from string.
        in = args.length > 0 ? new Scanner(new File(args[0])) : new Scanner(graph);
        Graph g = Graph.readDirectedGraph(in);
        g.printGraph(false);

        int[] duration = new int[g.size()];
        for(int i=0; i<g.size(); i++) {
            duration[i] = in.nextInt();
        }
        PERT p = pert(g, duration);
        if(p == null) {
            System.out.println("Invalid graph: not a DAG");
        } else {
            p.showResult();
        }
    }
}
