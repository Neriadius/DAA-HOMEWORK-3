package Graphs;

import java.util.*;

public class Graph {
    private final List<String> nodes;
    private final List<Edge> edges;
    private final Map<String, List<Edge>> adjacency;

    public Graph(List<String> nodes, List<Edge> edges) {
        this.nodes = new ArrayList<>(nodes);
        this.edges = new ArrayList<>(edges);
        this.adjacency = new HashMap<>();
        for (String n : nodes) adjacency.put(n, new ArrayList<>());
        for (Edge e : edges) {
            adjacency.get(e.getFrom()).add(e);
            adjacency.get(e.getTo()).add(new Edge(e.getTo(), e.getFrom(), e.getWeight())); // store reverse for traversal
        }
    }

    public List<String> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
    public Map<String, List<Edge>> getAdjacency() { return adjacency; }

    // --- Prim algorithm (returns MST edges list) ---
    public MSTResult prim() {
        MSTResult res = new MSTResult();
        if (nodes.isEmpty()) return res;

        Set<String> visited = new HashSet<>();
        PriorityQueue<PEdge> pq = new PriorityQueue<>();
        String start = nodes.get(0);
        visited.add(start);
        for (Edge e : adjacency.get(start)) pq.add(new PEdge(e.getFrom(), e.getTo(), e.getWeight()));

        long t0 = System.nanoTime();
        while (!pq.isEmpty() && visited.size() < nodes.size()) {
            PEdge pe = pq.poll();
            res.operations++;
            if (visited.contains(pe.to)) continue;
            visited.add(pe.to);
            Edge chosen = new Edge(pe.from, pe.to, pe.weight);
            res.mstEdges.add(chosen);
            res.totalCost += pe.weight;

            for (Edge out : adjacency.get(pe.to)) {
                if (!visited.contains(out.getTo())) {
                    pq.add(new PEdge(out.getFrom(), out.getTo(), out.getWeight()));
                    res.operations++;
                }
            }
        }
        long t1 = System.nanoTime();
        res.timeMs = (t1 - t0) / 1_000_000.0;
        return res;
    }

    // --- Kruskal algorithm (returns MST edges list) ---
    public MSTResult kruskal() {
        MSTResult res = new MSTResult();
        List<Edge> sorted = new ArrayList<>(edges);
        Collections.sort(sorted);
        DisjointSet ds = new DisjointSet();
        ds.makeSet(nodes);

        long t0 = System.nanoTime();
        for (Edge e : sorted) {
            res.operations++;
            String a = e.getFrom();
            String b = e.getTo();
            String ra = ds.find(a);
            String rb = ds.find(b);
            if (!ra.equals(rb)) {
                ds.union(ra, rb);
                res.mstEdges.add(e);
                res.totalCost += e.getWeight();
                res.operations++;
            }
            if (res.mstEdges.size() == nodes.size() - 1) break;
        }
        long t1 = System.nanoTime();
        res.timeMs = (t1 - t0) / 1_000_000.0;
        return res;
    }

    // --- helpers & nested classes ---
    private static class PEdge implements Comparable<PEdge> {
        String from, to;
        int weight;
        PEdge(String f, String t, int w) { from=f; to=t; weight=w; }
        @Override public int compareTo(PEdge o) { return Integer.compare(weight, o.weight); }
    }

    private static class DisjointSet {
        private final Map<String, String> parent = new HashMap<>();
        public void makeSet(Collection<String> verts) { for (String v : verts) parent.put(v, v); }
        public String find(String v) {
            if (!parent.get(v).equals(v)) parent.put(v, find(parent.get(v)));
            return parent.get(v);
        }
        public void union(String a, String b) { parent.put(find(b), find(a)); }
    }

    public static class MSTResult {
        public List<Edge> mstEdges = new ArrayList<>();
        public int totalCost = 0;
        public int operations = 0;
        public double timeMs = 0.0;
    }
}