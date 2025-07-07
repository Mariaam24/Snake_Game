import java.sql.*;

public class DataBase {
    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            String url = "jdbc:sqlite:snake.db";
            conn = DriverManager.getConnection(url);
        } catch (Exception e) {
            System.out.println("Erreur connexion DB : " + e.getMessage());
        }
        return conn;
    }

    public static void createTable() {
        String sqlPlayer = """
            CREATE TABLE IF NOT EXISTS player (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            );
        """;

        String sqlScore = """
            CREATE TABLE IF NOT EXISTS score (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_id INTEGER,
                score INTEGER NOT NULL,
                FOREIGN KEY(player_id) REFERENCES player(id)
            );
        """;

        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlPlayer);
            stmt.execute(sqlScore);
        } catch (SQLException e) {
            System.out.println("Erreur création table : " + e.getMessage());
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
            System.out.println("Erreur récupération bestScore : " + e.getMessage());
        }
        return best;
    }

    public static void showScoreHistory(int playerId) {
        String sql = "SELECT score FROM score WHERE player_id=? ORDER BY id DESC";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
            ResultSet rs = pstmt.executeQuery();
            int i = 1;
            System.out.println("\n📊 Historique des parties de " + AuthUI.username);
            while (rs.next()) {
                System.out.println(" Partie " + i + " : " + rs.getInt("score"));
                i++;
            }
        } catch (SQLException e) {
            System.out.println("Erreur historique : " + e.getMessage());
        }
    }
}