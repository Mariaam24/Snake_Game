import java.sql.*;

public class DataBase {

    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:snake.db";
            conn = DriverManager.getConnection(url);
            System.out.println("✅ Connexion SQLite établie.");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver manquant : " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ Erreur SQLite : " + e.getMessage());
        }
        return conn;
    }

    public static void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS score (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player TEXT NOT NULL,
                score INTEGER NOT NULL
            );
        """;

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table score prête.");
        } catch (SQLException e) {
            System.out.println("❌ Erreur création de table : " + e.getMessage());
        }
    }

    public static int getBestScore() {
        int best = 0;
        String sql = "SELECT MAX(score) AS max_score FROM score";
        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                best = rs.getInt("max_score");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur récupération bestScore : " + e.getMessage());
        }
        return best;
    }
}
