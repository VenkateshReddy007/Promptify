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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class TokenPanel extends JPanel {
    private final DashboardFrame dashboard;
    private final JTextArea promptArea;
    private final DefaultTableModel tableModel;
    private final JTable resultTable;

    public TokenPanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setPreferredSize(new Dimension(0, 200));

        JLabel enterLabel = new JLabel("Enter Prompt:");
        enterLabel.setFont(enterLabel.getFont().deriveFont(Font.BOLD));

        promptArea = new JTextArea(5, 40);
        promptArea.setLineWrap(true);
        promptArea.setWrapStyleWord(true);
        JScrollPane promptScroll = new JScrollPane(promptArea);

        JButton analyzeButton = new JButton("🔍 Analyze Prompt");
        analyzeButton.setBackground(new Color(0x4CAF50));
        analyzeButton.setForeground(Color.WHITE);
        analyzeButton.setFont(analyzeButton.getFont().deriveFont(Font.BOLD));
        analyzeButton.setFocusPainted(false);
        analyzeButton.addActionListener(e -> analyzePrompt());

        topPanel.add(enterLabel, BorderLayout.NORTH);
        topPanel.add(promptScroll, BorderLayout.CENTER);
        topPanel.add(analyzeButton, BorderLayout.SOUTH);

        String[] columns = {"Token Count", "Complexity", "Cost Category", "Recommended Model", "Suggestion"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        resultTable = new JTable(tableModel);
        resultTable.setRowHeight(28);
        resultTable.setDefaultRenderer(Object.class, new AlternatingRowRenderer());
        setColumnWidths();

        JScrollPane tableScroll = new JScrollPane(resultTable);

        JButton copyButton = new JButton("📋 Copy to Clipboard");
        copyButton.addActionListener(e -> copyTableToClipboard());

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(copyButton, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setColumnWidths() {
        int[] widths = {100, 100, 120, 150, 300};
        TableColumnModel cm = resultTable.getColumnModel();
        for (int i = 0; i < widths.length && i < cm.getColumnCount(); i++) {
            cm.getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private void analyzePrompt() {
        String promptText = promptArea.getText().trim();
        if (promptText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a prompt to analyze.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        dashboard.setStatus("Analyzing...");

        SwingWorker<Analysis, Void> worker = new SwingWorker<>() {
            @Override
            protected Analysis doInBackground() throws Exception {
                return Main.runFullAnalysis(promptText);
            }

            @Override
            protected void done() {
                try {
                    Analysis analysis = get();
                    tableModel.addRow(new Object[]{
                            analysis.getTokenCount(),
                            analysis.getComplexity(),
                            analysis.getCostCategory(),
                            analysis.getRecommendedModel(),
                            analysis.getSuggestion()
                    });
                    dashboard.setStatus("✅ Analysis Complete");
                    dashboard.refreshStats();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(TokenPanel.this,
                            "Analysis failed:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    dashboard.setStatus("Analysis failed.");
                }
            }
        };
        worker.execute();
    }

    private void copyTableToClipboard() {
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < tableModel.getColumnCount(); c++) {
            if (c > 0) {
                sb.append("\t");
            }
            sb.append(tableModel.getColumnName(c));
        }
        sb.append("\n");
        for (int r = 0; r < tableModel.getRowCount(); r++) {
            for (int c = 0; c < tableModel.getColumnCount(); c++) {
                if (c > 0) {
                    sb.append("\t");
                }
                Object val = tableModel.getValueAt(r, c);
                sb.append(val != null ? val.toString() : "");
            }
            sb.append("\n");
        }
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(sb.toString()), null);
        dashboard.setStatus("Table copied to clipboard.");
    }

    private static class AlternatingRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xf5f5f5));
            }
            return c;
        }
    }
}
