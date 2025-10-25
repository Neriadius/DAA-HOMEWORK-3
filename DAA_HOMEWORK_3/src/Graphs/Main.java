package Graphs;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Main extends JPanel {
    private final Graph graph;
    private Graph.MSTResult primResult;
    private Graph.MSTResult kruskalResult;
    private Map<String, Point> positions = new HashMap<>();
    private String mode = "Original"; // "Original", "Prim", "Kruskal"

    // visual settings
    private final int PAD = 60;
    private final int NODE_R = 18;

    public Main(Graph g) {
        this.graph = g;
        this.primResult = g.prim();
        this.kruskalResult = g.kruskal();
        computeCircularLayout();
        setPreferredSize(new Dimension(800, 600));
    }

    private void computeCircularLayout() {
        List<String> nodes = graph.getNodes();
        int n = nodes.size();
        int w = 800 - 2*PAD;
        int h = 600 - 2*PAD;
        int cx = PAD + w/2;
        int cy = PAD + h/2;
        int R = Math.min(w, h)/2 - 40;
        for (int i = 0; i < n; i++) {
            double ang = 2*Math.PI*i/n - Math.PI/2;
            int x = cx + (int)(R * Math.cos(ang));
            int y = cy + (int)(R * Math.sin(ang));
            positions.put(nodes.get(i), new Point(x,y));
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // background
        g.setColor(Color.WHITE);
        g.fillRect(0,0,getWidth(),getHeight());

        // draw all edges faintly
        g.setStroke(new BasicStroke(2f));
        g.setColor(Color.LIGHT_GRAY);
        for (Edge e : graph.getEdges()) {
            drawEdge(g, e, Color.LIGHT_GRAY, 1);
        }

        // draw MST edges depending on mode
        List<Edge> highlight = null;
        Color highlightColor = Color.RED;
        if ("Prim".equals(mode)) highlight = primResult.mstEdges;
        else if ("Kruskal".equals(mode)) highlight = kruskalResult.mstEdges;

        if (highlight != null) {
            g.setStroke(new BasicStroke(4f));
            g.setColor(highlightColor);
            for (Edge e : highlight) drawEdge(g, e, highlightColor, 4);
        }

        // draw nodes
        for (String node : graph.getNodes()) {
            Point p = positions.get(node);
            g.setColor(new Color(30, 144, 255));
            g.fillOval(p.x - NODE_R/2, p.y - NODE_R/2, NODE_R, NODE_R);
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(1f));
            g.drawOval(p.x - NODE_R/2, p.y - NODE_R/2, NODE_R, NODE_R);
            // label
            FontMetrics fm = g.getFontMetrics();
            String label = node;
            int lw = fm.stringWidth(label);
            g.drawString(label, p.x - lw/2, p.y + fm.getAscent()/2);
        }

        // legend & stats
        g.setColor(Color.BLACK);
        g.drawString("Mode: " + mode, 10, 16);
        g.drawString("Prim cost: " + primResult.totalCost + "  (ops: " + primResult.operations + ", ms: " + String.format("%.2f", primResult.timeMs) + ")", 10, 34);
        g.drawString("Kruskal cost: " + kruskalResult.totalCost + "  (ops: " + kruskalResult.operations + ", ms: " + String.format("%.2f", kruskalResult.timeMs) + ")", 10, 52);
    }

    private void drawEdge(Graphics2D g, Edge e, Color color, float width) {
        Point a = positions.get(e.getFrom());
        Point b = positions.get(e.getTo());
        if (a == null || b == null) return;
        g.setColor(color);
        g.setStroke(new BasicStroke(width));
        g.drawLine(a.x, a.y, b.x, b.y);
        // draw weight midpoint
        int mx = (a.x + b.x)/2;
        int my = (a.y + b.y)/2;
        String w = String.valueOf(e.getWeight());
        FontMetrics fm = g.getFontMetrics();
        int lw = fm.stringWidth(w);
        // white background for weight
        g.setColor(Color.WHITE);
        g.fillRect(mx - lw/2 - 3, my - fm.getAscent()/2 - 2, lw + 6, fm.getAscent() + 4);
        g.setColor(Color.BLACK);
        g.drawString(w, mx - lw/2, my + fm.getAscent()/2 - 2);
    }

    // small UI wrapper
    private static void createAndShow(Graph graph) {
        JFrame frame = new JFrame("Graph Visualizer (Prim vs Kruskal)");
        Main panel = new Main(graph);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lbl = new JLabel("View:");
        String[] modes = {"Original", "Prim", "Kruskal"};
        JComboBox<String> combo = new JComboBox<>(modes);
        combo.addActionListener(e -> {
            panel.mode = (String)combo.getSelectedItem();
            panel.repaint();
        });
        top.add(lbl);
        top.add(combo);

        frame.setLayout(new BorderLayout());
        frame.add(top, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // demo main: builds example graph and shows GUI
    public static void main(String[] args) {
        // Example graph - same as in assignment
        List<String> nodes = Arrays.asList("A","B","C","D","E");
        List<Edge> edges = Arrays.asList(
                new Edge("A","B",4),
                new Edge("A","C",3),
                new Edge("B","C",2),
                new Edge("B","D",5),
                new Edge("C","D",7),
                new Edge("C","E",8),
                new Edge("D","E",6)
        );
        Graph graph = new Graph(nodes, edges);

        SwingUtilities.invokeLater(() -> createAndShow(graph));
    }
}
