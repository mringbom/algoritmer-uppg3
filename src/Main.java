import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import java.util.Scanner;

public static void main(String[] args) {
    System.out.println("Kortaste ordväg");
    System.out.println("-------------------");

    List<String> words = new ArrayList<>();

    // Läs words.txt
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

    // Bygg graf
    Graph g = new Graph();
    for (String w : words) {
        g.addNode(w);
    }
    g.buildEdgesFromWords(words);

    // Hämta ord från användaren
    Scanner sc = new Scanner(System.in);
    System.out.print("Startord: ");
    String start = sc.nextLine().trim().toLowerCase();

    System.out.print("Målord: ");
    String goal = sc.nextLine().trim().toLowerCase();

    // Kör kortaste väg 
    List<String> path = g.shortestPath(start, goal);

    if (path == null) {
        System.out.println("Ingen väg finns mellan " + start + " och " + goal);
    } else {
        System.out.println("\nKortaste sekvens:");
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

@SuppressWarnings("serial")
class FileFormatException extends Exception { //Input file has the wrong format
    public FileFormatException(String message) {
        super(message);
    }
}

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

class TopSort {

    public void TopSort(Graph g) throws CycleFound {
        Map<Vertex, Integer> indegree = new HashMap<>();
        int counter = 0;
        List<Vertex> topOrder = new ArrayList<>();

        // Add indegree for each node
        for (Vertex v : g.nodes.values()) {
            indegree.put(v, v.indegree);
        }

        Queue q = new Queue();

        // Enqueue zero-indegree vertices
        for (Vertex v : g.nodes.values()) {
            if (indegree.get(v) == 0) {
                q.enqueue(v);
            }
        }

        while (!q.isEmpty()) {

            Vertex v = q.dequeue();
            topOrder.add(v);
            counter++;

            // Add indegree for each adjacent node
            for (Vertex w : v.adjacentNodes) {
                indegree.put(w, indegree.get(w) - 1);
                if (indegree.get(w) == 0) {
                    q.enqueue(w);
                }
            }
        }


        if (counter != g.nodes.size()) {
            throw new CycleFound("Cycle found in graph");
        }

        // If no cycle in graph, print topological order
        System.out.println("Topological order: ");
        for (Vertex v : topOrder) {
            System.out.println(v.name);
        }


    }
}


// CycleFound
class CycleFound extends Exception{
    public CycleFound(String message){
        super(message);
    }
}


class Queue {

    private class Node {
        // Used to hold references to nodes for the linked queue implementation
        private Vertex info;
        private Node link;
    }

    private Node first;
    private Node last;

    //Creates an empty queue
    public Queue() {
        first = null;
        last = null;
    }

    public void enqueue(Vertex v) {
        //Adds element at the rear of the queue
        Node newNode = new Node();
        newNode.info = v;
        newNode.link = null;
        if (last == null) {
            //If we are inserting into an empty queue
            first = newNode;
        }
        else {
            last.link = newNode;
        }
        last = newNode;
    }

    public Vertex dequeue() {
        if (!isEmpty()) {
            Vertex toReturn = first.info;
            first = first.link;
            if (first == null) {
                last = null;
            }
            return toReturn;
        }
        else {
            System.out.print("Dequeue attempted on empty queue!");
            return null;
        }
    }
    public boolean isEmpty() {
        // Checks if queue is empty
        return (last == null);
    }
}

import java.util.*;

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
