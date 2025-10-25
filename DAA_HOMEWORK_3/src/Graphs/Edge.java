package Graphs;

public class Edge implements Comparable<Edge> {
    private final String from;
    private final String to;
    private final int weight;

    public Edge(String from, String to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public String getFrom() { return from; }
    public String getTo() { return to; }
    public int getWeight() { return weight; }

    @Override
    public int compareTo(Edge other) {
        return Integer.compare(this.weight, other.weight);
    }

    @Override
    public String toString() {
        return String.format("%s-%s(%d)", from, to, weight);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Edge)) return false;
        Edge e = (Edge)o;
        // undirected equality
        return (from.equals(e.from) && to.equals(e.to) && weight == e.weight) ||
                (from.equals(e.to) && to.equals(e.from) && weight == e.weight);
    }

    @Override
    public int hashCode() {
        // make undirected insensitive hash
        String a = from.compareTo(to) <= 0 ? from + "|" + to : to + "|" + from;
        return (a + ":" + weight).hashCode();
    }
}