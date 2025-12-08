import java.io.*;
import java.util.*;
import javax.swing.*;


public class Main{

    public static void main(String[] args) {

        // Choose a file in the folder Graphs in the current directory
        JFileChooser jf = new JFileChooser("Graphs");
        int result = jf.showOpenDialog(null);
        File selectedFile = jf.getSelectedFile();
        Graph g = readGraph(selectedFile);

        System.out.println("Shortest path");
        System.out.println("-------------------");

        List<String> words = new ArrayList<>();

        // Read words.txt
        try (BufferedReader br = new BufferedReader(new FileReader("Words.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty()) words.add(line);
            }
        } catch (IOException e) {
            System.out.println("Kunde inte läsa Words.txt");
            return;
        }

        // Build graf
        Graph g = new Graph();
        for (String w : words) {
            g.addNode(w);
        }
        g.buildEdgesFromWords(words);

        // Read user input
        Scanner sc = new Scanner(System.in);
        System.out.print("First word: ");
        String start = sc.nextLine().trim().toLowerCase();

        System.out.print("Second word: ");
        String goal = sc.nextLine().trim().toLowerCase();

        // Check shortest path and if it exists  
        List<String> path = g.shortestPath(start, goal);

        if (path == null) {
            System.out.println("There is no path between " + start + " and " + goal);
        } else {
            System.out.println("\nShortest path:");
            System.out.println(String.join(" -> ", path));
        }
    }



    // Read in a graph from a file, print out the adjacency list, returns the graph
    public static Graph readGraph(File selectedFile) throws IOException, FileFormatException {

        Graph g = new Graph();
        BufferedReader r = new BufferedReader(new FileReader(selectedFile));
        String line=null;

        try {
            // Skip over comment lines in the beginning of the file
            while ( !(line = r.readLine()).equalsIgnoreCase("[Vertex]") ) {} ;

            // Read all vertex definitions
            while (!(line=r.readLine()).equalsIgnoreCase("[Edges]") ) {
                if (line.trim().length() > 0) {  // Skip empty lines
                    try {
                        // Split the line into a comma separated list V1,V2 etc
                        String[] nodeNames=line.split(",");

                        for (String n:nodeNames) {
                            String node = n.trim();
                            // Add node to graph
                            g.addNode(node);
                        }

                    } catch (Exception e) {   // Something wrong in the graph file
                        r.close();
                        throw new FileFormatException("Error in vertex definitions");
                    }
                }
            }

        } catch (NullPointerException e1) {  // The input file has wrong format
            throw new FileFormatException(" No [Vertex] or [Edges] section found in the file " + selectedFile.getName());
        }

        // Read all edge definitions
        while ( (line=r.readLine()) !=null ) {
            if (line.trim().length() > 0) {  // Skip empty lines
                try {
                    String[] edges=line.split(",");           // Edges are comma separated pairs e1:e2

                    for (String e:edges) {       // For all edges
                        String[] edgePair = e.trim().split(":"); //Split edge components v1:v2
                        String v = edgePair[0].trim();
                        String w = edgePair[1].trim();
                        // Add edges to graph
                        g.addEdge(v, w);
                    }

                } catch (Exception e) { //Something is wrong, Edges should be in format v1:v2
                    r.close();
                    throw new FileFormatException("Error in edge definition");
                }
            }
        }
        r.close();  // Close the reader
        return g;
    }
}


/* 
    FILE ERROR
*/

@SuppressWarnings("serial")
class FileFormatException extends Exception { //Input file has the wrong format
    public FileFormatException(String message) {
        super(message);
    }
}


/*
    GRAPH
*/

class Graph {
    // Save nodes/vertex as a Map, for easier access to Vertex object
    Map<String, Vertex> nodes;

    public Graph() {
        nodes = new HashMap<>();
    }
    // Create Vertex using given string, add to nodes
    public void addNode(String name) {
        nodes.put(name, new Vertex(name));
    }
    // Get the source Vertex and destination Vertex from nodes by searching with the given string
    public void addEdge(String v, String w) {
        Vertex source = nodes.get(v);
        Vertex destination = nodes.get(w);
        // Add destination as adjacent to source, increase indegree of destination
        source.addAdjacentNode(destination);
        destination.addDegree();
    }

    // Print, for troubleshooting
    public void printGraph() {
        for (String vertex : nodes.keySet()) {
            for (Vertex edges : nodes.get(vertex).adjacentNodes) {
                System.out.println (vertex + " -> " + edges.name );
            }
        }
    }
    public List<String> shortestPath(String start, String goal) {
        if (!nodes.containsKey(start) || !nodes.containsKey(goal)) {
        return null;
    }
        return Dijkstra.shortestPath(this, start, goal);
    }
}

class Vertex {
    String name;
    int indegree;
    List<Vertex> adjacentNodes;

    public Vertex(String name) {
        this.name = name;
        this.indegree = 0;
        this.adjacentNodes = new ArrayList<>();
    }

    public void addAdjacentNode(Vertex node) {
        this.adjacentNodes.add(node);
    }

    public void addDegree() {
        this.indegree++;
    }

}

class Dijkstra {

    public static List<String> shortestPath(Graph graph, String start, String goal) {

        Queue<String> queue = new LinkedList<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(goal)) {
                List<String> path = new ArrayList<>();
                for (String node = goal; node != null; node = previous.get(node)) {
                    path.add(node);
                }
                Collections.reverse(path);
                return path;
            }

            Vertex v = graph.nodes.get(current);

            for (Vertex neighbor : v.adjacentNodes) {
                String name = neighbor.name;

                if (!visited.contains(name)) {
                    visited.add(name);
                    previous.put(name, current);
                    queue.add(name);
                }
            }
        }

        return null; // ingen väg hittades
    }
}
