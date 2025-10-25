import java.io.*;
import java.util.*;

public class prim_algorithm {

    static class Edge {
        String from, to;
        int weight;
        Edge(String f, String t, int w) {
            this.from = f;
            this.to = t;
            this.weight = w;
        }
    }

    static class NodeEdge implements Comparable<NodeEdge> {
        String from, to;
        int weight;
        NodeEdge(String f, String t, int w) {
            this.from = f;
            this.to = t;
            this.weight = w;
        }
        public int compareTo(NodeEdge e) {
            return this.weight - e.weight;
        }
    }

    static class MSTResult {
        List<Edge> edges;
        int totalCost;
        int operationsCount;
        double execTimeMs;
        MSTResult() {
            edges = new ArrayList<>();
            totalCost = 0;
            operationsCount = 0;
            execTimeMs = 0.0;
        }
    }

    // === Prim's Algorithm ===
    public static MSTResult prim(Map<String, List<Edge>> graph, List<String> nodes) {
        MSTResult result = new MSTResult();
        Set<String> visited = new HashSet<>();
        PriorityQueue<NodeEdge> pq = new PriorityQueue<>();

        long start = System.nanoTime();

        String startNode = nodes.get(0);
        visited.add(startNode);

        for (Edge e : graph.get(startNode)) {
            pq.add(new NodeEdge(e.from, e.to, e.weight));
        }

        while (!pq.isEmpty() && visited.size() < nodes.size()) {
            NodeEdge edge = pq.poll();
            result.operationsCount++;

            if (visited.contains(edge.to)) continue;

            visited.add(edge.to);
            result.edges.add(new Edge(edge.from, edge.to, edge.weight));
            result.totalCost += edge.weight;

            for (Edge next : graph.get(edge.to)) {
                if (!visited.contains(next.to)) {
                    pq.add(new NodeEdge(next.from, next.to, next.weight));
                    result.operationsCount++;
                }
            }
        }

        long end = System.nanoTime();
        result.execTimeMs = (end - start) / 1_000_000.0;
        return result;
    }

    public static void main(String[] args) {
        try {
            // === 1. (Optional) Read JSON ===
            StringBuilder jsonBuilder = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\musee\\Documents\\github\\DAA-HOMEWORK-3\\DAA_HOMEWORK_3\\src\\ass_3_input.json"));
            String line;
            while ((line = br.readLine()) != null) jsonBuilder.append(line);
            br.close();

            // === Graph 1 ===
            System.out.println("Processing Graph 1...");
            List<String> nodes1 = Arrays.asList("A","B","C","D","E");
            List<Edge> edges1 = Arrays.asList(
                    new Edge("A","B",4),
                    new Edge("A","C",3),
                    new Edge("B","C",2),
                    new Edge("B","D",5),
                    new Edge("C","D",7),
                    new Edge("C","E",8),
                    new Edge("D","E",6)
            );

            Map<String, List<Edge>> graph1 = new HashMap<>();
            for (String n : nodes1) graph1.put(n, new ArrayList<>());
            for (Edge e : edges1) {
                graph1.get(e.from).add(new Edge(e.from, e.to, e.weight));
                graph1.get(e.to).add(new Edge(e.to, e.from, e.weight));
            }

            MSTResult r1 = prim(graph1, nodes1);

            // === Graph 2 ===
            System.out.println("Processing Graph 2...");
            List<String> nodes2 = Arrays.asList("A","B","C","D");
            List<Edge> edges2 = Arrays.asList(
                    new Edge("A","B",1),
                    new Edge("A","C",4),
                    new Edge("B","C",2),
                    new Edge("C","D",3),
                    new Edge("B","D",5)
            );

            Map<String, List<Edge>> graph2 = new HashMap<>();
            for (String n : nodes2) graph2.put(n, new ArrayList<>());
            for (Edge e : edges2) {
                graph2.get(e.from).add(new Edge(e.from, e.to, e.weight));
                graph2.get(e.to).add(new Edge(e.to, e.from, e.weight));
            }

            MSTResult r2 = prim(graph2, nodes2);

            // === 3. Write output JSON (with all required info) ===
            BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\musee\\Documents\\github\\DAA-HOMEWORK-3\\DAA_HOMEWORK_3\\src\\ass_3_output.json"));
            bw.write("{\n  \"results\": [\n");

            // --- Graph 1 Output ---
            bw.write("    {\n");
            bw.write("      \"graph_id\": 1,\n");
            bw.write("      \"input_stats\": {\"vertices\": " + nodes1.size() + ", \"edges\": " + edges1.size() + "},\n");
            bw.write("      \"prim\": {\n");
            bw.write("        \"edges_in_mst\": [\n");
            for (int i = 0; i < r1.edges.size(); i++) {
                Edge e = r1.edges.get(i);
                bw.write("          {\"from\": \"" + e.from + "\", \"to\": \"" + e.to + "\", \"weight\": " + e.weight + "}");
                if (i < r1.edges.size() - 1) bw.write(",");
                bw.write("\n");
            }
            bw.write("        ],\n");
            bw.write("        \"total_cost\": " + r1.totalCost + ",\n");
            bw.write("        \"operations_count\": " + r1.operationsCount + ",\n");
            bw.write("        \"execution_time_ms\": " + String.format("%.2f", r1.execTimeMs) + "\n");
            bw.write("      }\n    },\n");

            // --- Graph 2 Output ---
            bw.write("    {\n");
            bw.write("      \"graph_id\": 2,\n");
            bw.write("      \"input_stats\": {\"vertices\": " + nodes2.size() + ", \"edges\": " + edges2.size() + "},\n");
            bw.write("      \"prim\": {\n");
            bw.write("        \"edges_in_mst\": [\n");
            for (int i = 0; i < r2.edges.size(); i++) {
                Edge e = r2.edges.get(i);
                bw.write("          {\"from\": \"" + e.from + "\", \"to\": \"" + e.to + "\", \"weight\": " + e.weight + "}");
                if (i < r2.edges.size() - 1) bw.write(",");
                bw.write("\n");
            }
            bw.write("        ],\n");
            bw.write("        \"total_cost\": " + r2.totalCost + ",\n");
            bw.write("        \"operations_count\": " + r2.operationsCount + ",\n");
            bw.write("        \"execution_time_ms\": " + String.format("%.2f", r2.execTimeMs) + "\n");
            bw.write("      }\n    }\n");

            bw.write("  ]\n}");
            bw.close();

            System.out.println("Results saved to ass_3_output.json");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
