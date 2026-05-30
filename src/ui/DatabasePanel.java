import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class DatabasePanel extends JPanel {
    private final DashboardFrame dashboard;
    private final JComboBox<String> tableSelector;
    private final DefaultTableModel tableModel;
    private final JTable dataTable;
    private final JLabel rowCountLabel;

    private final PromptDAO promptDAO = new PromptDAO();
    private final AnalysisDAO analysisDAO = new AnalysisDAO();

    public DatabasePanel(DashboardFrame dashboard) {
        this.dashboard = dashboard;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topBar = new JPanel();
        topBar.add(new JLabel("View Table:"));
        tableSelector = new JComboBox<>(new String[]{
                "prompts + optimized", "analysis", "notifications", "joined view"
        });
        topBar.add(tableSelector);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        dataTable = new JTable(tableModel);
        rowCountLabel = new JLabel("Rows loaded: 0");

        JButton loadButton = new JButton("🔄 Load Data");
        loadButton.addActionListener(e -> loadSelectedTable());
        topBar.add(loadButton);

        JButton clearButton = new JButton("🗑️ Clear Table");
        clearButton.addActionListener(e -> {
            tableModel.setRowCount(0);
            rowCountLabel.setText("Rows loaded: 0");
            dashboard.setStatus("Table display cleared.");
        });
        topBar.add(clearButton);
        add(topBar, BorderLayout.NORTH);
        add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(rowCountLabel, BorderLayout.WEST);

        JButton exportButton = new JButton("📤 Export SQL");
        exportButton.addActionListener(e -> showExportPopup());
        bottom.add(exportButton, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);
    }

    private void loadSelectedTable() {
        String selected = (String) tableSelector.getSelectedItem();
        dashboard.setStatus("Loading " + selected + "...");

        SwingWorker<LoadResult, Void> worker = new SwingWorker<>() {
            @Override
            protected LoadResult doInBackground() throws Exception {
                System.out.println("[DatabasePanel] Loading table: " + selected);
                List<Object[]> rows;
                String[] headers;

                switch (selected) {
                    case "analysis" -> {
                        headers = new String[]{"ID", "Prompt ID", "Tokens", "Complexity", "Model", "Cost", "Suggestion", "Date"};
                        rows = analysisDAO.getAllAnalysis();
                    }
                    case "notifications" -> {
                        headers = new String[]{"ID", "Analysis ID", "Message", "Status", "Sent Time"};
                        rows = analysisDAO.getAllNotifications();
                    }
                    case "joined view" -> {
                        headers = new String[]{"Prompt ID", "Original Prompt", "Tokens", "Model"};
                        rows = promptDAO.getJoinedAnalysis();
                    }
                    default -> {
                        headers = new String[]{"ID", "Original Prompt", "Optimized Prompt", "Created At"};
                        rows = promptDAO.getAllPrompts();
                    }
                }
                System.out.println("[DatabasePanel] Rows loaded: " + rows.size());
                return new LoadResult(headers, rows);
            }

            @Override
            protected void done() {
                try {
                    LoadResult result = get();
                    tableModel.setColumnIdentifiers(result.headers);
                    tableModel.setRowCount(0);
                    for (Object[] row : result.rows) {
                        Object[] displayRow = row;
                        if ("joined view".equals(selected) && row.length >= 5) {
                            displayRow = new Object[]{row[0], row[1], row[2], row[4]};
                        }
                        tableModel.addRow(displayRow);
                    }
                    rowCountLabel.setText("Rows loaded: " + result.rows.size());
                    dashboard.setStatus("Loaded " + result.rows.size() + " rows from " + selected);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(DatabasePanel.this,
                            "Failed to load data:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    dashboard.setStatus("Load failed.");
                }
            }
        };
        worker.execute();
    }

    private void showExportPopup() {
        String selected = (String) tableSelector.getSelectedItem();
        String sql = resolveExportSql(selected);
        StringBuilder content = new StringBuilder();
        content.append("-- Export query\n");
        content.append(sql).append("\n\n-- Results (").append(tableModel.getRowCount()).append(" rows)\n");

        for (int r = 0; r < tableModel.getRowCount(); r++) {
            for (int c = 0; c < tableModel.getColumnCount(); c++) {
                if (c > 0) {
                    content.append(" | ");
                }
                Object val = tableModel.getValueAt(r, c);
                content.append(val != null ? val.toString() : "NULL");
            }
            content.append("\n");
        }

        JTextArea area = new JTextArea(content.toString(), 20, 60);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(this, scroll, "SQL Export — " + selected, JOptionPane.INFORMATION_MESSAGE);
    }

    private String resolveExportSql(String selected) {
        return switch (selected) {
            case "analysis" -> "SELECT * FROM analysis ORDER BY analyzed_at DESC";
            case "notifications" -> "SELECT * FROM notifications ORDER BY sent_time DESC";
            case "joined view" -> "SELECT p.prompt_id, p.original_prompt, a.token_count, a.recommended_model "
                    + "FROM prompts p JOIN analysis a ON p.prompt_id = a.prompt_id ORDER BY a.analyzed_at DESC";
            default -> "SELECT p.prompt_id, p.original_prompt, op.optimized_prompt, p.created_at "
                    + "FROM prompts p LEFT JOIN optimized_prompts op ON p.prompt_id = op.prompt_id "
                    + "ORDER BY p.created_at DESC";
        };
    }

    private static class LoadResult {
        final String[] headers;
        final List<Object[]> rows;

        LoadResult(String[] headers, List<Object[]> rows) {
            this.headers = headers;
            this.rows = rows;
        }
    }
}
