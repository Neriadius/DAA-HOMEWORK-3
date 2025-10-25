import java.io.*;
import java.util.*;

public class TESTING {

    public static void main(String[] args) {
        String inputPath = "C:\\Users\\musee\\Documents\\github\\DAA-HOMEWORK-3\\DAA_HOMEWORK_3\\src\\ass_3_input.json";
        String outputJsonPath = "C:\\Users\\musee\\Documents\\github\\DAA-HOMEWORK-3\\DAA_HOMEWORK_3\\src\\ass_3_output.json";
        String csvPath = "C:\\Users\\musee\\Documents\\github\\DAA-HOMEWORK-3\\DAA_HOMEWORK_3\\src\\PaK.csv";

        try {
            File inFile = new File(inputPath);
            if (!inFile.exists()) {
                System.err.println("Input file not found: " + inputPath);
                return;
            }

            String json = new String(java.nio.file.Files.readAllBytes(inFile.toPath()));
            List<Map<String, Object>> graphs = extractGraphs(json);

            // Prepare JSON output builder
            StringBuilder outJson = new StringBuilder();
            outJson.append("{\n  \"results\": [\n");

            // Prepare CSV file: if not exists, write header
            File csvFile = new File(csvPath);
            boolean needHeader = !csvFile.exists();
            BufferedWriter csvWriter = new BufferedWriter(new FileWriter(csvFile, true)); // append mode
            if (needHeader) {
                String header = "graph_id,vertices,edges,prim_total,kruskal_total,prim_time_ms,kruskal_time_ms,prim_ops,kruskal_ops,cost_equal,structure_equal";
                csvWriter.write(header);
                csvWriter.newLine();
                csvWriter.flush();
            }

            for (int i = 0; i < graphs.size(); i++) {
                Map<String, Object> g = graphs.get(i);
                int id = (int) g.get("id");
                @SuppressWarnings("unchecked")
                List<String> nodes = (List<String>) g.get("nodes");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> edgeMaps = (List<Map<String, Object>>) g.get("raw_edges");

                // Build edge lists for kruskal_algorithm.Edge and prim_algorithm.Edge
                List<kruskal_algorithm.Edge> kruskalEdges = new ArrayList<>();
                List<prim_algorithm.Edge> primEdgesList = new ArrayList<>(); // flatten list for graph creation
                for (Map<String, Object> em : edgeMaps) {
                    String from = (String) em.get("from");
                    String to = (String) em.get("to");
                    int w = (int) em.get("weight");
                    // create edges for kruskal
                    kruskalEdges.add(new kruskal_algorithm.Edge(from, to, w));
                    // create edges for prim adjacency lists later (we'll create prim_algorithm.Edge objects)
                    primEdgesList.add(new prim_algorithm.Edge(from, to, w));
                }

                // Build adjacency list for Prim (Map<String, List<prim_algorithm.Edge>>)
                Map<String, List<prim_algorithm.Edge>> adj = new HashMap<>();
                for (String n : nodes) adj.put(n, new ArrayList<>());
                for (prim_algorithm.Edge e : primEdgesList) {
                    adj.get(e.from).add(new prim_algorithm.Edge(e.from, e.to, e.weight));
                    adj.get(e.to).add(new prim_algorithm.Edge(e.to, e.from, e.weight));
                }

                // Run Prim (returns prim_algorithm.MSTResult)
                prim_algorithm.MSTResult primResult = prim_algorithm.prim(adj, nodes);

                // Run Kruskal (returns Map<String,Object>)
                Map<String, Object> kruskalResult = kruskal_algorithm.kruskalMST(nodes, kruskalEdges);
                @SuppressWarnings("unchecked")
                List<kruskal_algorithm.Edge> kruskalMstEdges = (List<kruskal_algorithm.Edge>) kruskalResult.get("mst_edges");
                int kruskalTotal = (int) kruskalResult.get("total_cost");
                int kruskalOps = (int) kruskalResult.get("operations_count");
                double kruskalTime = ((Number) kruskalResult.get("execution_time_ms")).doubleValue();
                int kruskalVertices = (int) kruskalResult.get("vertices");
                int kruskalEdgesCount = (int) kruskalResult.get("edges");

                // Compare costs
                boolean costEqual = primResult.totalCost == kruskalTotal;

                // Compare structures: compare sets of string representations (order-independent)
                Set<String> primSet = new HashSet<>();
                for (prim_algorithm.Edge e : primResult.edges) {
                    primSet.add(edgeToStringKey(e.from, e.to, e.weight));
                }
                Set<String> kruskalSet = new HashSet<>();
                for (kruskal_algorithm.Edge e : kruskalMstEdges) {
                    kruskalSet.add(edgeToStringKey(e.from, e.to, e.weight));
                }
                boolean structureEqual = primSet.equals(kruskalSet);

                // Build JSON block for this graph
                outJson.append("    {\n");
                outJson.append("      \"graph_id\": ").append(id).append(",\n");
                outJson.append("      \"input_stats\": {\"vertices\": ").append(nodes.size()).append(", \"edges\": ").append(edgeMaps.size()).append("},\n");

                // Prim block
                outJson.append("      \"prim\": {\n");
                outJson.append("        \"mst_edges\": [\n");
                for (int j = 0; j < primResult.edges.size(); j++) {
                    prim_algorithm.Edge e = primResult.edges.get(j);
                    outJson.append("          {\"from\": \"").append(e.from).append("\", \"to\": \"").append(e.to).append("\", \"weight\": ").append(e.weight).append("}");
                    if (j < primResult.edges.size() - 1) outJson.append(",");
                    outJson.append("\n");
                }
                outJson.append("        ],\n");
                outJson.append("        \"total_cost\": ").append(primResult.totalCost).append(",\n");
                outJson.append("        \"operations_count\": ").append(primResult.operationsCount).append(",\n");
                outJson.append("        \"execution_time_ms\": ").append(String.format("%.2f", primResult.execTimeMs)).append("\n");
                outJson.append("      },\n");

                // Kruskal block
                outJson.append("      \"kruskal\": {\n");
                outJson.append("        \"mst_edges\": [\n");
                for (int j = 0; j < kruskalMstEdges.size(); j++) {
                    kruskal_algorithm.Edge e = kruskalMstEdges.get(j);
                    outJson.append("          {\"from\": \"").append(e.from).append("\", \"to\": \"").append(e.to).append("\", \"weight\": ").append(e.weight).append("}");
                    if (j < kruskalMstEdges.size() - 1) outJson.append(",");
                    outJson.append("\n");
                }
                outJson.append("        ],\n");
                outJson.append("        \"total_cost\": ").append(kruskalTotal).append(",\n");
                outJson.append("        \"operations_count\": ").append(kruskalOps).append(",\n");
                outJson.append("        \"execution_time_ms\": ").append(String.format("%.2f", kruskalTime)).append("\n");
                outJson.append("      },\n");

                // Comparison block
                outJson.append("      \"comparison\": {\n");
                outJson.append("        \"cost_equal\": ").append(costEqual).append(",\n");
                outJson.append("        \"structure_equal\": ").append(structureEqual).append("\n");
                outJson.append("      }\n");

                outJson.append("    }");
                if (i < graphs.size() - 1) outJson.append(",");
                outJson.append("\n");

                // Append CSV row
                String csvRow = buildCsvRow(id, nodes.size(), edgeMaps.size(),
                        primResult.totalCost, kruskalTotal,
                        primResult.execTimeMs, kruskalTime,
                        primResult.operationsCount, kruskalOps,
                        costEqual, structureEqual);
                csvWriter.write(csvRow);
                csvWriter.newLine();
                csvWriter.flush();
            }

            outJson.append("  ]\n}\n");

            // Write JSON output
            try (BufferedWriter jsonWriter = new BufferedWriter(new FileWriter(outputJsonPath))) {
                jsonWriter.write(outJson.toString());
            }

            csvWriter.close();

            System.out.println("Done. JSON saved to: " + outputJsonPath);
            System.out.println("CSV appended at: " + csvPath);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Helper: make canonical undirected edge key so A-B and B-A map same
    private static String edgeToStringKey(String a, String b, int w) {
        if (a.compareTo(b) <= 0) return a + "-" + b + ":" + w;
        else return b + "-" + a + ":" + w;
    }

    // Helper: build CSV row
    private static String buildCsvRow(int id, int vertices, int edges,
                                      int primTotal, int kruskalTotal,
                                      double primTime, double kruskalTime,
                                      int primOps, int kruskalOps,
                                      boolean costEqual, boolean structureEqual) {
        // ensure using dot decimal separator for times
        return String.format(Locale.US, "%d,%d,%d,%d,%d,%.2f,%.2f,%d,%d,%s,%s",
                id, vertices, edges, primTotal, kruskalTotal,
                primTime, kruskalTime, primOps, kruskalOps,
                costEqual ? "TRUE" : "FALSE", structureEqual ? "TRUE" : "FALSE");
    }

    // Minimal JSON parser: returns list of graphs with id, nodes (List<String>) and raw_edges (List<Map>)
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

            List<Map<String, Object>> rawEdges = new ArrayList<>();
            int edgesStart = chunk.indexOf("[", chunk.indexOf("\"edges\""));
            int edgesEnd = chunk.indexOf("]", edgesStart);
            String edgesContent = chunk.substring(edgesStart + 1, edgesEnd);
            String[] edgeParts = edgesContent.split("\\},");
            for (String e : edgeParts) {
                String from = extractValue(e, "from");
                String to = extractValue(e, "to");
                String w = extractValue(e, "weight");
                if (from != null && to != null && w != null) {
                    Map<String, Object> em = new HashMap<>();
                    em.put("from", from);
                    em.put("to", to);
                    em.put("weight", Integer.parseInt(w));
                    rawEdges.add(em);
                }
            }
            g.put("raw_edges", rawEdges);
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
