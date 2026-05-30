import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PromptDAO {

    public int insertPrompt(String originalPrompt) throws SQLException {
        String sql = "INSERT INTO prompts (original_prompt) VALUES (?)";
        System.out.println("[SQL] Executing: " + sql);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, originalPrompt);
            int rowsAffected = ps.executeUpdate();
            System.out.println("[DB] Rows inserted: " + rowsAffected);
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("No generated key for prompt insert");
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
    }

    public int insertOptimizedPrompt(int promptId, String optimizedPrompt) throws SQLException {
        String sql = "INSERT INTO optimized_prompts (prompt_id, optimized_prompt) VALUES (?, ?)";
        System.out.println("[SQL] Executing: " + sql);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, promptId);
            ps.setString(2, optimizedPrompt);
            int rowsAffected = ps.executeUpdate();
            System.out.println("[DB] Rows inserted: " + rowsAffected);
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("No generated key for optimized prompt insert");
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
    }

    public List<Object[]> getAllPrompts() throws SQLException {
        String sql = "SELECT p.prompt_id, p.original_prompt, op.optimized_prompt, p.created_at "
                + "FROM prompts p "
                + "LEFT JOIN optimized_prompts op ON p.prompt_id = op.prompt_id "
                + "ORDER BY p.created_at DESC";
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
                        rs.getInt("prompt_id"),
                        rs.getString("original_prompt"),
                        rs.getString("optimized_prompt"),
                        rs.getTimestamp("created_at")
                });
            }
            System.out.println("[DB] Rows fetched: " + rows.size());
            return rows;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
    }

    public List<Object[]> getJoinedAnalysis() throws SQLException {
        String sql = "SELECT p.prompt_id, p.original_prompt, a.token_count, a.complexity, "
                + "a.recommended_model, a.cost_category, a.suggestion "
                + "FROM prompts p "
                + "JOIN analysis a ON p.prompt_id = a.prompt_id "
                + "ORDER BY a.analyzed_at DESC";
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
                        rs.getInt("prompt_id"),
                        rs.getString("original_prompt"),
                        rs.getInt("token_count"),
                        rs.getString("complexity"),
                        rs.getString("recommended_model"),
                        rs.getString("cost_category"),
                        rs.getString("suggestion")
                });
            }
            System.out.println("[DB] Rows fetched: " + rows.size());
            return rows;
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
        }
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
