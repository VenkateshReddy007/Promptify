import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class ModelSwitcherPanel extends JPanel {
    private final DashboardFrame dashboard;
    private final JTextArea promptArea;
    private final JProgressBar complexityBar;
    private final JLabel modelNameLabel;
    private final JLabel tokenCountLabel;
    private final JLabel complexityLevelLabel;
    private final JPanel resultPanel;

    private final TokenAnalyzer tokenAnalyzer = new TokenAnalyzer();
    private final ModelSelector modelSelector = new ModelSelector();

    public ModelSwitcherPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Smart Model Switcher");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel enterLabel = new JLabel("Enter Prompt:");
        enterLabel.setFont(enterLabel.getFont().deriveFont(Font.BOLD));
        center.add(enterLabel);
        center.add(Box.createVerticalStrut(5));

        promptArea = new JTextArea(4, 40);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        center.add(new JScrollPane(promptArea));
        center.add(Box.createVerticalStrut(10));

        JButton recommendButton = new JButton("🔀 Recommend Model");
        recommendButton.setBackground(new Color(0xFF5722));
        recommendButton.setForeground(Color.WHITE);
        recommendButton.setFocusPainted(false);
        recommendButton.setAlignmentX(LEFT_ALIGNMENT);
        recommendButton.setMaximumSize(new Dimension(220, 36));
        recommendButton.addActionListener(e -> recommendModel());
        center.add(recommendButton);
        center.add(Box.createVerticalStrut(20));

        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setVisible(false);

        complexityBar = new JProgressBar(0, 100);
        complexityBar.setStringPainted(true);
        complexityBar.setAlignmentX(LEFT_ALIGNMENT);
        complexityBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel modelTitle = new JLabel("Recommended Model:");
        modelTitle.setFont(modelTitle.getFont().deriveFont(Font.BOLD));

        modelNameLabel = new JLabel("—");
        modelNameLabel.setFont(modelNameLabel.getFont().deriveFont(Font.BOLD, 18f));

        tokenCountLabel = new JLabel("Token Count: —");
        tokenCountLabel.setForeground(Color.GRAY);
        complexityLevelLabel = new JLabel("Complexity Level: —");
        complexityLevelLabel.setForeground(Color.GRAY);

        resultPanel.add(complexityBar);
        resultPanel.add(Box.createVerticalStrut(10));
        resultPanel.add(modelTitle);
        resultPanel.add(modelNameLabel);
        resultPanel.add(Box.createVerticalStrut(5));
        resultPanel.add(tokenCountLabel);
        resultPanel.add(complexityLevelLabel);

        center.add(resultPanel);
        add(center, BorderLayout.CENTER);
    }

    private void recommendModel() {
        String text = promptArea.getText().trim();
        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a prompt.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        dashboard.setStatus("Recommending model...");

        SwingWorker<ModelResult, Void> worker = new SwingWorker<>() {
            @Override
            protected ModelResult doInBackground() {
                int tokens = tokenAnalyzer.estimateTokens(text);
                String complexity = tokenAnalyzer.getComplexity(tokens);
                String model = modelSelector.recommendModel(tokens);
                return new ModelResult(tokens, complexity, model);
            }

            @Override
            protected void done() {
                try {
                    ModelResult result = get();
                    applyResult(result);
                    resultPanel.setVisible(true);
                    dashboard.setStatus("✅ Model recommendation ready.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(ModelSwitcherPanel.this,
                            "Recommendation failed:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    dashboard.setStatus("Recommendation failed.");
                }
            }
        };
        worker.execute();
    }

    private void applyResult(ModelResult result) {
        switch (result.complexity) {
            case "LOW" -> {
                complexityBar.setValue(25);
                complexityBar.setForeground(new Color(0x4CAF50));
                complexityBar.setString("LOW");
            }
            case "MEDIUM" -> {
                complexityBar.setValue(60);
                complexityBar.setForeground(new Color(0xFF9800));
                complexityBar.setString("MEDIUM");
            }
            default -> {
                complexityBar.setValue(100);
                complexityBar.setForeground(new Color(0xF44336));
                complexityBar.setString("HIGH");
            }
        }

        modelNameLabel.setText(result.model);
        switch (result.model) {
            case "Gemini Flash" -> modelNameLabel.setForeground(new Color(0x2196F3));
            case "Gemini Pro" -> modelNameLabel.setForeground(new Color(0x4CAF50));
            case "Gemini Advanced" -> modelNameLabel.setForeground(new Color(0xF44336));
            default -> modelNameLabel.setForeground(Color.BLACK);
        }

        tokenCountLabel.setText("Token Count: " + result.tokens);
        complexityLevelLabel.setText("Complexity Level: " + result.complexity);
    }

    private static class ModelResult {
        final int tokens;
        final String complexity;
        final String model;

        ModelResult(int tokens, String complexity, String model) {
            this.tokens = tokens;
            this.complexity = complexity;
            this.model = model;
        }
    }
}
