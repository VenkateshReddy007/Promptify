import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

    public static Analysis runFullAnalysis(String promptText) throws Exception {
        System.out.println("\n========== STARTING ANALYSIS ==========");
        System.out.println("[App] Prompt received: " + promptText);

        TokenAnalyzer tokenAnalyzer = new TokenAnalyzer();
        ModelSelector modelSelector = new ModelSelector();
        PromptDAO promptDAO = new PromptDAO();
        AnalysisDAO analysisDAO = new AnalysisDAO();
        TelegramNotifier telegram = new TelegramNotifier();

        int tokens = tokenAnalyzer.estimateTokens(promptText);
        String complexity = tokenAnalyzer.getComplexity(tokens);
        String costCategory = tokenAnalyzer.getCostCategory(tokens);
        String suggestion = tokenAnalyzer.getSuggestion(promptText, tokens);
        String model = modelSelector.recommendModel(tokens);

        System.out.println("[App] Complexity: " + complexity);
        System.out.println("[App] Recommended Model: " + model);
        System.out.println("[App] Cost Category: " + costCategory);

        int promptId = promptDAO.insertPrompt(promptText);

        Analysis analysis = new Analysis();
        analysis.setPromptId(promptId);
        analysis.setTokenCount(tokens);
        analysis.setComplexity(complexity);
        analysis.setRecommendedModel(model);
        analysis.setCostCategory(costCategory);
        analysis.setSuggestion(suggestion);
        int analysisId = analysisDAO.insertAnalysis(analysis);
        analysis.setAnalysisId(analysisId);

        List<String> alerts = new ArrayList<>();
        if (tokens > 300) {
            alerts.add("⚠️ <b>High Token Usage</b>\nTokens: " + tokens + "\n💡 Break into smaller tasks.");
        }
        if (tokenAnalyzer.isWeakPrompt(promptText, tokens)) {
            alerts.add("⚠️ <b>Weak Prompt Detected</b>\nTokens: " + tokens + "\n💡 Add more context.");
        }
        if ("HIGH".equals(complexity)) {
            alerts.add("⚠️ <b>High Complexity</b>\nTokens: " + tokens + "\n🤖 Model: " + model);
        }

        for (String alert : alerts) {
            boolean sent = telegram.sendAlert(alert);
            String status = sent ? "SENT" : "FAILED";
            analysisDAO.insertNotification(analysisId, alert, status);
        }

        System.out.println("========== ANALYSIS COMPLETE ==========\n");
        return analysis;
    }

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  AI Credit Cost Analyzer - Starting   ");
        System.out.println("========================================");

        try {
            DBConnection.getInstance().getConnection();
            System.out.println("[Main] Database connection: OK");
        } catch (Exception e) {
            System.err.println("[Main] Database connection FAILED: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Database connection failed!\n" + e.getMessage(),
                    "Startup Error", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            DashboardFrame frame = new DashboardFrame();
            frame.setVisible(true);
            System.out.println("[Main] UI launched successfully.");
        });
    }
}
