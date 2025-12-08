import java.io.*;
import java.util.*;
import javax.swing.*;


public class Main{

    public static void main(String[] args) {

        // Let's user pick a file
        JFileChooser jf = new JFileChooser(".");
        int result = jf.showOpenDialog(null);
        File selectedFile = jf.getSelectedFile();

        System.out.println("Shortest path");
        System.out.println("-------------------");

        List<String> words = new ArrayList<>();

        // Read words.txt
        try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (!line.isEmpty()){
                    words.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read file " + selectedFile.getName());
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
        System.out.print("Start word: ");
        String start = sc.nextLine().trim().toLowerCase();

        System.out.print("End word: ");
        String goal = sc.nextLine().trim().toLowerCase();

        // Check shortest path and if it exists  
        List<String> path = g.shortestPath(start, goal);

        if (path == null) {
            System.out.println("There is no path between " + start + " and " + goal);
        } else {
            System.out.println("\nShortest path:");
            System.out.println(String.join(" -> ", path));
        }

        sc.close();
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

        // Safety check
        if (source == null || destination == null) return; 

        // Add destination as adjacent to source, increase indegree of destination
        source.addAdjacentNode(destination);
        destination.addDegree();
    }

    // Builds path between words
    public void buildEdgesFromWords(List<String> words){
        int n = words.size();

        for (int i = 0; i < n; i++){
            String a = words.get(i);

            for (int j = i + 1; j < n; j++){
                String b = words.get(j);

                if (differsByOne(a,b)){
                    addEdge(a, b);
                    addEdge(b, a);
                }
            }
        }
    }

    // Check if words differ by one char
    public boolean differsByOne(String a, String b){
        
        if (a.length() != b.length()){
            return false;
        }
        
        int diff = 0;

        for (int i = 0; i < a.length(); i++){
            if (a.charAt(i) != b.charAt(i)){
                diff++;
            }
            if (diff > 1){
                return false;
            }
        }

        return diff == 1;
    }

    // Shortest path using Dikjstra's algorithm
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

        // No path found
        return null; 
    }
}
