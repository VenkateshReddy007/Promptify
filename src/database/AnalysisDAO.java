import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisDAO {

    public int insertAnalysis(Analysis analysis) throws SQLException {
        String sql = "INSERT INTO analysis (prompt_id, token_count, complexity, recommended_model, cost_category, suggestion) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        System.out.println("[SQL] Executing: " + sql);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, analysis.getPromptId());
            ps.setInt(2, analysis.getTokenCount());
            ps.setString(3, analysis.getComplexity());
            ps.setString(4, analysis.getRecommendedModel());
            ps.setString(5, analysis.getCostCategory());
            ps.setString(6, analysis.getSuggestion());
            int rowsAffected = ps.executeUpdate();
            System.out.println("[DB] Rows inserted: " + rowsAffected);
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("No generated key for analysis insert");
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
    }

    public int insertNotification(int analysisId, String message, String status) throws SQLException {
        String sql = "INSERT INTO notifications (analysis_id, message, status) VALUES (?, ?, ?)";
        System.out.println("[SQL] Executing: " + sql);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, analysisId);
            ps.setString(2, message);
            ps.setString(3, status);
            int rowsAffected = ps.executeUpdate();
            System.out.println("[DB] Rows inserted: " + rowsAffected);
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("No generated key for notification insert");
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
    }

    public List<Object[]> getAllAnalysis() throws SQLException {
        String sql = "SELECT * FROM analysis ORDER BY analyzed_at DESC";
        System.out.println("[SQL] Executing: " + sql);
        List<Object[]> rows = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("analysis_id"),
                        rs.getInt("prompt_id"),
                        rs.getInt("token_count"),
                        rs.getString("complexity"),
                        rs.getString("recommended_model"),
                        rs.getString("cost_category"),
                        rs.getString("suggestion"),
                        rs.getTimestamp("analyzed_at")
                });
            }
            System.out.println("[DB] Rows fetched: " + rows.size());
            return rows;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
    }

    public List<Object[]> getAllNotifications() throws SQLException {
        String sql = "SELECT * FROM notifications ORDER BY sent_time DESC";
        System.out.println("[SQL] Executing: " + sql);
        List<Object[]> rows = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("notification_id"),
                        rs.getInt("analysis_id"),
                        rs.getString("message"),
                        rs.getString("status"),
                        rs.getTimestamp("sent_time")
                });
            }
            System.out.println("[DB] Rows fetched: " + rows.size());
            return rows;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
    }

    public Map<String, Object> getDashboardStats() throws SQLException {
        Map<String, Object> stats = new HashMap<>();
        Connection conn = DBConnection.getInstance().getConnection();

        String sql1 = "SELECT COUNT(*) FROM prompts";
        System.out.println("[SQL] Executing: " + sql1);
        try (PreparedStatement ps = conn.prepareStatement(sql1);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.put("totalPrompts", rs.getLong(1));
            }
        }

        String sql2 = "SELECT AVG(token_count) FROM analysis";
        System.out.println("[SQL] Executing: " + sql2);
        try (PreparedStatement ps = conn.prepareStatement(sql2);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                double avg = rs.getDouble(1);
                stats.put("avgTokens", rs.wasNull() ? 0.0 : avg);
            }
        }

        String sql3 = "SELECT COUNT(*) FROM notifications";
        System.out.println("[SQL] Executing: " + sql3);
        try (PreparedStatement ps = conn.prepareStatement(sql3);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.put("notificationsSent", rs.getLong(1));
            }
        }

        String sql4 = "SELECT recommended_model, COUNT(*) as cnt FROM analysis "
                + "GROUP BY recommended_model ORDER BY cnt DESC LIMIT 1";
        System.out.println("[SQL] Executing: " + sql4);
        try (PreparedStatement ps = conn.prepareStatement(sql4);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                stats.put("mostUsedModel", rs.getString("recommended_model"));
            } else {
                stats.put("mostUsedModel", "N/A");
            }
        }

        return stats;
    }

    private void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception ignored) {
            }
        }
    }
}
