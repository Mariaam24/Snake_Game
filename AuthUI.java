import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AuthUI extends JFrame {
    public static int playerId = -1;
    public static String username = "";

    public AuthUI() {
        setTitle("Connexion au jeu Snake");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 1));

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton loginBtn = new JButton("Se connecter");
        JButton registerBtn = new JButton("S'enregistrer");
        JButton historyBtn = new JButton("Afficher l'historique");

        add(new JLabel("Nom d'utilisateur :"));
        add(userField);
        add(new JLabel("Mot de passe :"));
        add(passField);
        add(loginBtn);
        add(registerBtn);
        add(historyBtn);

        loginBtn.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            if (login(user, pass)) {
                JOptionPane.showMessageDialog(this, "Connexion réussie !");
                dispose();
                new GameFrame();
            } else {
                JOptionPane.showMessageDialog(this, "Identifiants incorrects.");
            }
        });

        registerBtn.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            if (register(user, pass)) {
                JOptionPane.showMessageDialog(this, "Compte créé ! Connectez-vous.");
            } else {
                JOptionPane.showMessageDialog(this, "Nom déjà utilisé.");
            }
        });

        historyBtn.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());
            if (login(user, pass)) {
                StringBuilder historique = new StringBuilder();
                try (Connection conn = DataBase.connect()) {
                    // Historique de l'utilisateur connecté
                    String sqlUserScores = "SELECT score FROM score WHERE player_id=? ORDER BY id DESC";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlUserScores)) {
                        pstmt.setInt(1, playerId);
                        ResultSet rs = pstmt.executeQuery();
                        int i = 1;
                        historique.append("👤 Historique de ").append(username).append(" :\n");
                        while (rs.next()) {
                            historique.append("  Partie ").append(i).append(" : ").append(rs.getInt("score")).append("\n");
                            i++;
                        }
                        if (i == 1) historique.append("  Aucune partie enregistrée.\n");
                    }

                    // Meilleur score global
                    String sqlBest = "SELECT p.username, s.score FROM score s JOIN player p ON s.player_id = p.id ORDER BY s.score DESC LIMIT 1";
                    try (PreparedStatement pstmt = conn.prepareStatement(sqlBest); ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            historique.append("\n🏆 Meilleur score : ")
                                .append(rs.getInt("score"))
                                .append(" par ")
                                .append(rs.getString("username"));
                        }
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur SQL : " + ex.getMessage());
                    return;
                }

                JOptionPane.showMessageDialog(this, historique.toString());
            } else {
                JOptionPane.showMessageDialog(this, "Connectez-vous pour consulter l'historique.");
            }
        });

        setVisible(true);
    }

    private boolean login(String username, String password) {
        String sql = "SELECT id FROM player WHERE username=? AND password=?";
        try (Connection conn = DataBase.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                AuthUI.playerId = rs.getInt("id");
                AuthUI.username = username;
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Erreur login : " + e.getMessage());
        }
        return false;
    }

    private boolean register(String username, String password) {
        String sql = "INSERT INTO player(username, password) VALUES(?, ?)";
        try (Connection conn = DataBase.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Erreur register : " + e.getMessage());
        }
        return false;
    }
}
