import java.io.*;
import java.util.*;

public class MST_Comparison {

    // --- Edge class ---
    static class Edge implements Comparable<Edge> {
        String from, to;
        int weight;
        Edge(String f, String t, int w) {
            this.from = f;
            this.to = t;
            this.weight = w;
        }
        public int compareTo(Edge e) {
            return this.weight - e.weight;
        }
        public String toString() {
            return String.format("{\"from\": \"%s\", \"to\": \"%s\", \"weight\": %d}", from, to, weight);
        }
    }

    // --- Disjoint Set (Union-Find) for Kruskal ---
    static class DisjointSet {
        private final Map<String, String> parent = new HashMap<>();
        public void makeSet(Collection<String> vertices) {
            for (String v : vertices) parent.put(v, v);
        }
        public String find(String v) {
            if (!parent.get(v).equals(v))
                parent.put(v, find(parent.get(v)));
            return parent.get(v);
        }
        public void union(String a, String b) {
            String rootA = find(a);
            String rootB = find(b);
            if (!rootA.equals(rootB)) parent.put(rootB, rootA);
        }
    }

    // --- Result container ---
    static class MSTResult {
        List<Edge> edges = new ArrayList<>();
        int totalCost = 0;
        int operationsCount = 0;
        double execTimeMs = 0.0;
    }

    // --- Kruskal algorithm ---
    public static MSTResult kruskal(List<String> nodes, List<Edge> edges) {
        MSTResult result = new MSTResult();
        long start = System.nanoTime();
        int operations = 0;

        Collections.sort(edges);
        operations += edges.size();

        DisjointSet ds = new DisjointSet();
        ds.makeSet(nodes);
        operations += nodes.size();

        for (Edge e : edges) {
            operations++;
            String rootA = ds.find(e.from);
            String rootB = ds.find(e.to);
            if (!rootA.equals(rootB)) {
                result.edges.add(e);
                result.totalCost += e.weight;
                ds.union(rootA, rootB);
            }
            if (result.edges.size() == nodes.size() - 1) break;
        }

        long end = System.nanoTime();
        result.execTimeMs = (end - start) / 1_000_000.0;
        result.operationsCount = operations;
        return result;
    }

    // --- Prim algorithm ---
    public static MSTResult prim(Map<String, List<Edge>> graph, List<String> nodes) {
        MSTResult result = new MSTResult();
        Set<String> visited = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>();

        long start = System.nanoTime();
        String startNode = nodes.get(0);
        visited.add(startNode);
        pq.addAll(graph.get(startNode));

        while (!pq.isEmpty() && visited.size() < nodes.size()) {
            Edge edge = pq.poll();
            result.operationsCount++;
            if (visited.contains(edge.to)) continue;

            visited.add(edge.to);
            result.edges.add(edge);
            result.totalCost += edge.weight;

            for (Edge next : graph.get(edge.to)) {
                if (!visited.contains(next.to)) {
                    pq.add(next);
                    result.operationsCount++;
                }
            }
        }

        long end = System.nanoTime();
        result.execTimeMs = (end - start) / 1_000_000.0;
        return result;
    }

    // --- Main program ---
    public static void main(String[] args) {
        try {
            File inputFile = new File("C:\\Users\\musee\\Documents\\github\\DAA-HOMEWORK-3\\DAA_HOMEWORK_3\\src\\ass_3_input.json");
            if (!inputFile.exists()) {
                System.out.println("Input file not found!");
                return;
            }

            String json = new String(java.nio.file.Files.readAllBytes(inputFile.toPath()));
            List<Map<String, Object>> graphs = extractGraphs(json);

            File outFile = new File("C:\\Users\\musee\\Documents\\github\\DAA-HOMEWORK-3\\DAA_HOMEWORK_3\\src\\ass_3_output.json");
            BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
            bw.write("{\n  \"results\": [\n");

            for (int i = 0; i < graphs.size(); i++) {
                Map<String, Object> g = graphs.get(i);
                int id = (int) g.get("id");
                List<String> nodes = (List<String>) g.get("nodes");
                List<Edge> edges = (List<Edge>) g.get("edges");

                // Build adjacency list for Prim
                Map<String, List<Edge>> graph = new HashMap<>();
                for (String n : nodes) graph.put(n, new ArrayList<>());
                for (Edge e : edges) {
                    graph.get(e.from).add(new Edge(e.from, e.to, e.weight));
                    graph.get(e.to).add(new Edge(e.to, e.from, e.weight));
                }

                // Run both algorithms
                MSTResult primRes = prim(graph, nodes);
                MSTResult kruskalRes = kruskal(nodes, edges);

                boolean costEqual = primRes.totalCost == kruskalRes.totalCost;
                boolean structureEqual = primRes.edges.equals(kruskalRes.edges);

                System.out.println("Graph " + id + " — Prim cost: " + primRes.totalCost + ", Kruskal cost: " + kruskalRes.totalCost);

                // Write JSON output
                bw.write("    {\n");
                bw.write("      \"graph_id\": " + id + ",\n");
                bw.write("      \"input_stats\": {\"vertices\": " + nodes.size() + ", \"edges\": " + edges.size() + "},\n");
                bw.write("      \"prim\": {\n");
                bw.write("        \"mst_edges\": [\n");
                for (int j = 0; j < primRes.edges.size(); j++) {
                    bw.write("          " + primRes.edges.get(j).toString());
                    if (j < primRes.edges.size() - 1) bw.write(",");
                    bw.write("\n");
                }
                bw.write("        ],\n");
                bw.write("        \"total_cost\": " + primRes.totalCost + ",\n");
                bw.write("        \"operations_count\": " + primRes.operationsCount + ",\n");
                bw.write("        \"execution_time_ms\": " + String.format("%.2f", primRes.execTimeMs) + "\n");
                bw.write("      },\n");

                bw.write("      \"kruskal\": {\n");
                bw.write("        \"mst_edges\": [\n");
                for (int j = 0; j < kruskalRes.edges.size(); j++) {
                    bw.write("          " + kruskalRes.edges.get(j).toString());
                    if (j < kruskalRes.edges.size() - 1) bw.write(",");
                    bw.write("\n");
                }
                bw.write("        ],\n");
                bw.write("        \"total_cost\": " + kruskalRes.totalCost + ",\n");
                bw.write("        \"operations_count\": " + kruskalRes.operationsCount + ",\n");
                bw.write("        \"execution_time_ms\": " + String.format("%.2f", kruskalRes.execTimeMs) + "\n");
                bw.write("      },\n");

                bw.write("      \"comparison\": {\n");
                bw.write("        \"cost_equal\": " + costEqual + ",\n");
                bw.write("        \"structure_equal\": " + structureEqual + "\n");
                bw.write("      }\n");

                bw.write("    }");
                if (i < graphs.size() - 1) bw.write(",");
                bw.write("\n");
            }

            bw.write("  ]\n}");
            bw.close();
            System.out.println("\nResults written to: " + outFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Simple JSON extractor (no libraries) ---
    private static List<Map<String, Object>> extractGraphs(String json) {
        List<Map<String, Object>> graphs = new ArrayList<>();
        String[] parts = json.split("\\{\\s*\"id\"\\s*:");
        for (int i = 1; i < parts.length; i++) {
            String chunk = parts[i];
            Map<String, Object> g = new HashMap<>();

            int idEnd = chunk.indexOf(",");
            int id = Integer.parseInt(chunk.substring(0, idEnd).trim());
            g.put("id", id);

            int nodesStart = chunk.indexOf("[", chunk.indexOf("\"nodes\""));
            int nodesEnd = chunk.indexOf("]", nodesStart);
            String[] nodeArray = chunk.substring(nodesStart + 1, nodesEnd).replace("\"", "").split(",");
            List<String> nodes = new ArrayList<>();
            for (String n : nodeArray) {
                n = n.trim();
                if (!n.isEmpty()) nodes.add(n);
            }
            g.put("nodes", nodes);

            List<Edge> edges = new ArrayList<>();
            int edgesStart = chunk.indexOf("[", chunk.indexOf("\"edges\""));
            int edgesEnd = chunk.indexOf("]", edgesStart);
            String edgesContent = chunk.substring(edgesStart + 1, edgesEnd);
            String[] edgeParts = edgesContent.split("\\},");
            for (String e : edgeParts) {
                String from = extractValue(e, "from");
                String to = extractValue(e, "to");
                String w = extractValue(e, "weight");
                if (from != null && to != null && w != null)
                    edges.add(new Edge(from, to, Integer.parseInt(w)));
            }
            g.put("edges", edges);
            graphs.add(g);
        }
        return graphs;
    }

    private static String extractValue(String text, String key) {
        int idx = text.indexOf("\"" + key + "\"");
        if (idx == -1) return null;
        int colon = text.indexOf(":", idx);
        if (colon == -1) return null;
        int comma = text.indexOf(",", colon);
        if (comma == -1) comma = text.length();
        return text.substring(colon + 1, comma).replaceAll("[^A-Za-z0-9]", "").trim();
    }
}
