import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class OptimizerPanel extends JPanel {
    private final DashboardFrame dashboard;
    private final JTextArea originalArea;
    private final JTextArea optimizedArea;
    private final JButton optimizeButton;
    private final PromptOptimizer promptOptimizer = new PromptOptimizer();
    private final PromptDAO promptDAO = new PromptDAO();

    public OptimizerPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel("Shadow Optimizer — Improve weak prompts automatically");
        header.setFont(header.getFont().deriveFont(Font.ITALIC));
        header.setForeground(Color.GRAY);
        header.setPreferredSize(new Dimension(0, 50));
        add(header, BorderLayout.NORTH);

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        JLabel originalLabel = new JLabel("Original Prompt:");
        originalLabel.setFont(originalLabel.getFont().deriveFont(Font.BOLD));

        originalArea = new JTextArea(8, 30);
        originalArea.setLineWrap(true);
        originalArea.setWrapStyleWord(true);
        JScrollPane originalScroll = new JScrollPane(originalArea);

        optimizeButton = new JButton("✨ Optimize with Groq");
        optimizeButton.setBackground(new Color(0x7C4DFF));
        optimizeButton.setForeground(Color.WHITE);
        optimizeButton.setFocusPainted(false);
        optimizeButton.addActionListener(e -> runOptimization());

        leftPanel.add(originalLabel, BorderLayout.NORTH);
        leftPanel.add(originalScroll, BorderLayout.CENTER);
        leftPanel.add(optimizeButton, BorderLayout.SOUTH);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        JLabel optimizedLabel = new JLabel("Optimized Prompt:");
        optimizedLabel.setFont(optimizedLabel.getFont().deriveFont(Font.BOLD));
        optimizedLabel.setForeground(new Color(0x7C4DFF));

        optimizedArea = new JTextArea(8, 30);
        optimizedArea.setLineWrap(true);
        optimizedArea.setWrapStyleWord(true);
        optimizedArea.setEditable(false);
        optimizedArea.setBackground(new Color(0xf8f0ff));
        JScrollPane optimizedScroll = new JScrollPane(optimizedArea);

        JButton copyButton = new JButton("📋 Copy Optimized");
        copyButton.addActionListener(e -> {
            String text = optimizedArea.getText();
            if (!text.isBlank()) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(text), null);
                dashboard.setStatus("Optimized prompt copied to clipboard.");
            }
        });

        rightPanel.add(optimizedLabel, BorderLayout.NORTH);
        rightPanel.add(optimizedScroll, BorderLayout.CENTER);
        rightPanel.add(copyButton, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setDividerLocation(0.5);

        add(splitPane, BorderLayout.CENTER);
    }

    private void runOptimization() {
        String text = originalArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a prompt to optimize.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        optimizeButton.setEnabled(false);
        optimizeButton.setText("Optimizing...");
        dashboard.setStatus("Optimizing prompt with Groq...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                String optimized = promptOptimizer.optimizePrompt(text);
                if (optimized != null && !optimized.startsWith("Error:")) {
                    int promptId = promptDAO.insertPrompt(text);
                    promptDAO.insertOptimizedPrompt(promptId, optimized);
                }
                return optimized;
            }

            @Override
            protected void done() {
                optimizeButton.setEnabled(true);
                optimizeButton.setText("✨ Optimize with Groq");
                try {
                    String optimized = get();
                    if (optimized == null || optimized.startsWith("Error:")) {
                        optimizedArea.setText("");
                        JOptionPane.showMessageDialog(OptimizerPanel.this,
                                "Groq optimization failed:\n" + optimized,
                                "Optimization Error", JOptionPane.ERROR_MESSAGE);
                        dashboard.setStatus("Optimization failed.");
                        return;
                    }
                    optimizedArea.setText(optimized);
                    dashboard.setStatus("✅ Optimization complete.");
                    dashboard.refreshStats();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(OptimizerPanel.this,
                            "Optimization failed:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    dashboard.setStatus("Optimization failed.");
                }
            }
        };
        worker.execute();
    }
}
