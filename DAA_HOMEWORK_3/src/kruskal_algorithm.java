import java.io.*;
import java.util.*;

public class kruskal_algorithm {

    // --- Edge structure ---
    static class Edge implements Comparable<Edge> {
        String from, to;
        int weight;

        Edge(String from, String to, int weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        @Override
        public int compareTo(Edge other) {
            return this.weight - other.weight;
        }

        @Override
        public String toString() {
            return String.format("{\"from\": \"%s\", \"to\": \"%s\", \"weight\": %d}", from, to, weight);
        }
    }

    // --- Disjoint Set (Union-Find) ---
    static class DisjointSet {
        private final Map<String, String> parent = new HashMap<>();

        public void makeSet(Collection<String> vertices) {
            for (String v : vertices) {
                parent.put(v, v);
            }
        }

        public String find(String v) {
            if (!parent.get(v).equals(v)) {
                parent.put(v, find(parent.get(v))); // Path compression
            }
            return parent.get(v);
        }

        public void union(String a, String b) {
            String rootA = find(a);
            String rootB = find(b);
            if (!rootA.equals(rootB)) {
                parent.put(rootB, rootA);
            }
        }
    }

    // --- Kruskal algorithm ---
    public static Map<String, Object> kruskalMST(List<String> nodes, List<Edge> edges) {
        long start = System.nanoTime();
        int operations = 0;

        Collections.sort(edges);
        operations += edges.size();

        DisjointSet ds = new DisjointSet();
        ds.makeSet(nodes);
        operations += nodes.size();

        List<Edge> mst = new ArrayList<>();
        int totalCost = 0;

        for (Edge edge : edges) {
            operations++;
            String rootA = ds.find(edge.from);
            String rootB = ds.find(edge.to);

            if (!rootA.equals(rootB)) {
                mst.add(edge);
                totalCost += edge.weight;
                ds.union(rootA, rootB);
            }

            if (mst.size() == nodes.size() - 1) break;
        }

        long end = System.nanoTime();
        double execTimeMs = (end - start) / 1_000_000.0;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mst_edges", mst);
        result.put("total_cost", totalCost);
        result.put("operations_count", operations);
        result.put("execution_time_ms", execTimeMs);

        return result;
    }

    // --- MAIN ---
    public static void main(String[] args) {
        try {
            System.out.println("Working directory: " + System.getProperty("user.dir"));

            // Input file
            File file = new File("C:\\Users\\musee\\Documents\\github\\DAA-HOMEWORK-3\\DAA_HOMEWORK_3\\src\\ass_3_input.json");
            if (!file.exists()) {
                System.out.println("Error: input file not found!");
                return;
            }

            // Read input file
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            String json = sb.toString();
            List<Map<String, Object>> graphs = extractGraphs(json);

            // Prepare output content
            StringBuilder output = new StringBuilder();
            output.append("[\n");

            for (int i = 0; i < graphs.size(); i++) {
                Map<String, Object> graph = graphs.get(i);
                int id = (int) graph.get("id");
                List<String> nodes = (List<String>) graph.get("nodes");
                List<Edge> edges = (List<Edge>) graph.get("edges");

                Map<String, Object> mstResult = kruskalMST(nodes, edges);

                // Print to console
                System.out.println("\nGraph ID: " + id);
                System.out.println("Total cost: " + mstResult.get("total_cost"));
                System.out.println("Edges in MST:");
                @SuppressWarnings("unchecked")
                List<Edge> mstEdges = (List<Edge>) mstResult.get("mst_edges");
                for (Edge e : mstEdges) {
                    System.out.println("  " + e.from + " - " + e.to + " (" + e.weight + ")");
                }
                System.out.println("Operations: " + mstResult.get("operations_count"));
                System.out.println("Time: " + mstResult.get("execution_time_ms") + " ms");

                // Write to output JSON
                output.append("  {\n");
                output.append("    \"id\": ").append(id).append(",\n");
                output.append("    \"total_cost\": ").append(mstResult.get("total_cost")).append(",\n");
                output.append("    \"operations_count\": ").append(mstResult.get("operations_count")).append(",\n");
                output.append("    \"execution_time_ms\": ").append(mstResult.get("execution_time_ms")).append(",\n");
                output.append("    \"mst_edges\": [\n");
                for (int j = 0; j < mstEdges.size(); j++) {
                    output.append("      ").append(mstEdges.get(j).toString());
                    if (j < mstEdges.size() - 1) output.append(",");
                    output.append("\n");
                }
                output.append("    ]\n");
                output.append("  }");
                if (i < graphs.size() - 1) output.append(",");
                output.append("\n");
            }

            output.append("]\n");

            // Write result to file
            File outFile = new File("C:\\Users\\musee\\Documents\\github\\DAA-HOMEWORK-3\\DAA_HOMEWORK_3\\src\\ass_3_output.json");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
                writer.write(output.toString());
            }

            System.out.println("\nOutput written to: " + outFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Simplified JSON parsing ---
    private static List<Map<String, Object>> extractGraphs(String json) {
        List<Map<String, Object>> graphs = new ArrayList<>();

        String[] parts = json.split("\\{\\s*\"id\"\\s*:");
        for (int i = 1; i < parts.length; i++) {
            String chunk = parts[i];
            Map<String, Object> graph = new HashMap<>();

            int idEnd = chunk.indexOf(",");
            int id = Integer.parseInt(chunk.substring(0, idEnd).trim());
            graph.put("id", id);

            int nodesStart = chunk.indexOf("[", chunk.indexOf("\"nodes\""));
            int nodesEnd = chunk.indexOf("]", nodesStart);
            String[] nodeArray = chunk.substring(nodesStart + 1, nodesEnd).replace("\"", "").split(",");
            List<String> nodes = new ArrayList<>();
            for (String n : nodeArray) {
                n = n.trim();
                if (!n.isEmpty()) nodes.add(n);
            }
            graph.put("nodes", nodes);

            List<Edge> edges = new ArrayList<>();
            int edgesStart = chunk.indexOf("[", chunk.indexOf("\"edges\""));
            int edgesEnd = chunk.indexOf("]", edgesStart);
            String edgesContent = chunk.substring(edgesStart + 1, edgesEnd);
            String[] edgeParts = edgesContent.split("\\},");
            for (String e : edgeParts) {
                String from = extractValue(e, "from");
                String to = extractValue(e, "to");
                String w = extractValue(e, "weight");
                if (from != null && to != null && w != null) {
                    edges.add(new Edge(from, to, Integer.parseInt(w)));
                }
            }
            graph.put("edges", edges);

            graphs.add(graph);
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
        String val = text.substring(colon + 1, comma).replaceAll("[^A-Za-z0-9]", "").trim();
        return val.isEmpty() ? null : val;
    }
}
