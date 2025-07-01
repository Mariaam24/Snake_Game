public class SnakeGame {
    public static void main(String[] args) {
        DataBase.createTable();  // S'assure que la table existe
        new GameFrame();         // Lance le jeu
    }
}
