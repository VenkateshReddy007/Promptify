import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class DashboardFrame extends JFrame {
    private final JLabel statusLabel;
    private final JLabel totalPromptsValue = new JLabel("0");
    private final JLabel avgTokensValue = new JLabel("0");
    private final JLabel alertsSentValue = new JLabel("0");
    private final JLabel topModelValue = new JLabel("N/A");

    public DashboardFrame() {
        super("AI Credit Cost Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(0x1a1a2e));
        topPanel.setPreferredSize(new Dimension(1200, 60));

        JLabel titleLabel = new JLabel("🤖 AI Credit Cost Analyzer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);

        JLabel poweredLabel = new JLabel("Powered by Groq API");
        poweredLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        poweredLabel.setForeground(new Color(0xaaaaaa));
        topPanel.add(poweredLabel);

        JPanel statsPanel = buildStatsPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📊 Token Analyzer", new TokenPanel(this));
        tabbedPane.addTab("✨ Optimizer", new OptimizerPanel(this));
        tabbedPane.addTab("🔀 Model Switcher", new ModelSwitcherPanel(this));
        tabbedPane.addTab("🗄️ Database Viewer", new DatabasePanel(this));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(tabbedPane, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(0xf0f0f0));
        statusPanel.setPreferredSize(new Dimension(1200, 30));
        statusLabel = new JLabel("Ready.");
        statusPanel.add(statusLabel);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        refreshStats();
    }

    private JPanel buildStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        statsPanel.add(createStatCard("Total Prompts", totalPromptsValue));
        statsPanel.add(createStatCard("Avg Tokens", avgTokensValue));
        statsPanel.add(createStatCard("Alerts Sent", alertsSentValue));
        statsPanel.add(createStatCard("Top Model", topModelValue));

        return statsPanel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(0xe8f4f8));
        card.setBorder(BorderFactory.createLineBorder(new Color(0xcccccc)));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(new Color(0x1565C0));

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xcccccc)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
    }

    public void refreshStats() {
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() throws Exception {
                AnalysisDAO dao = new AnalysisDAO();
                return dao.getDashboardStats();
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> stats = get();
                    totalPromptsValue.setText(String.valueOf(stats.getOrDefault("totalPrompts", 0)));
                    Object avg = stats.get("avgTokens");
                    double avgVal = avg instanceof Number ? ((Number) avg).doubleValue() : 0.0;
                    avgTokensValue.setText(String.format("%.1f", avgVal));
                    alertsSentValue.setText(String.valueOf(stats.getOrDefault("notificationsSent", 0)));
                    topModelValue.setText(String.valueOf(stats.getOrDefault("mostUsedModel", "N/A")));
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(DashboardFrame.this,
                            "Failed to load dashboard stats:\n" + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
