import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class GamePanel extends JPanel implements ActionListener {
    static final int SCREEN_WIDTH = 600;
    static final int SCREEN_HEIGHT = 600;
    static final int UNIT_SIZE = 25;
    static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 100;

    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];

    int bodyParts = 6;
    int applesEaten = 0;
    int bestScore = 0;

    int redAppleX, redAppleY;
    int greenAppleX = -UNIT_SIZE, greenAppleY = -UNIT_SIZE;
    boolean greenAppleVisible = false;

    private Timer timer;
    private Timer greenAppleTimer;

    char direction = 'R';
    boolean running = false;
    Random random;

    public GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        bestScore = DataBase.getBestScore();
        startGame();
    }

    public void startGame() {
        running = true;
        generateRedApple();
        generateGreenApple();
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
        if (!running) gameOver(g);
    }

    public void draw(Graphics g) {
        g.setColor(Color.gray);
        for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
            g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
            g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
        }

        // Pomme rouge
        g.setColor(Color.RED);
        g.fillOval(redAppleX, redAppleY, UNIT_SIZE, UNIT_SIZE);

        // Pomme verte (si visible)
        if (greenAppleVisible) {
            g.setColor(Color.GREEN);
            g.fillOval(greenAppleX, greenAppleY, UNIT_SIZE, UNIT_SIZE);
        }

        // Serpent
        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) g.setColor(Color.YELLOW);
            else g.setColor(new Color(45, 180, 0));
            g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
        }

        // Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Ink Free", Font.BOLD, 30));
        g.drawString("Score: " + applesEaten, 10, g.getFont().getSize());
        g.drawString("Best: " + bestScore, SCREEN_WIDTH - 200, g.getFont().getSize());
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U' -> y[0] -= UNIT_SIZE;
            case 'D' -> y[0] += UNIT_SIZE;
            case 'L' -> x[0] -= UNIT_SIZE;
            case 'R' -> x[0] += UNIT_SIZE;
        }
    }

    public void checkApple() {
        // Mange pomme rouge
        if (x[0] == redAppleX && y[0] == redAppleY) {
            applesEaten++;
            bodyParts++;
            generateRedApple();
            generateGreenApple(); // peut déclencher apparition de pomme verte
        }

        // Mange pomme verte
        if (greenAppleVisible && x[0] == greenAppleX && y[0] == greenAppleY) {
            applesEaten = Math.max(0, applesEaten - 1);
            greenAppleVisible = false;
            greenAppleX = -UNIT_SIZE;
            greenAppleY = -UNIT_SIZE;
            if (greenAppleTimer != null && greenAppleTimer.isRunning()) {
                greenAppleTimer.stop();
            }
        }
    }

    private boolean isOnSnake(int xPos, int yPos) {
        for (int i = 0; i < bodyParts; i++) {
            if (x[i] == xPos && y[i] == yPos) return true;
        }
        return false;
    }

    public void generateRedApple() {
        do {
            redAppleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            redAppleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
        } while (isOnSnake(redAppleX, redAppleY));
    }

    public void generateGreenApple() {
        if (greenAppleVisible) return;

        if (random.nextInt(100) < 30) { // 30% de chance
            do {
                greenAppleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
                greenAppleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
            } while (
                isOnSnake(greenAppleX, greenAppleY) ||
                (greenAppleX == redAppleX && greenAppleY == redAppleY)
            );

            greenAppleVisible = true;
            repaint();

            greenAppleTimer = new Timer(5000, e -> {
                greenAppleVisible = false;
                greenAppleX = -UNIT_SIZE;
                greenAppleY = -UNIT_SIZE;
                repaint();
            });
            greenAppleTimer.setRepeats(false);
            greenAppleTimer.start();
        }
    }

    public void checkCollision() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) running = false;
        }

        if (x[0] < 0 || x[0] >= SCREEN_WIDTH || y[0] < 0 || y[0] >= SCREEN_HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
            if (greenAppleTimer != null) greenAppleTimer.stop();
        }
    }

    public void gameOver(Graphics g) {
        saveScore(AuthUI.username, applesEaten);
        if (applesEaten > bestScore) bestScore = applesEaten;

        g.setColor(Color.RED);
        g.setFont(new Font("Ink Free", Font.BOLD, 75));
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2 - 50);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Ink Free", Font.BOLD, 50));
        g.drawString("Score: " + applesEaten,
                (SCREEN_WIDTH - metrics.stringWidth("Score: " + applesEaten)) / 2,
                SCREEN_HEIGHT / 2 + 50);
    }

    public void resetGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        running = true;

        for (int i = 0; i < GAME_UNITS; i++) {
            x[i] = 0;
            y[i] = 0;
        }

        generateRedApple();
        generateGreenApple();

        if (timer != null) timer.start();
    }

    public void saveScore(String player, int score) {
        String sql = "INSERT INTO score(player_id, score) VALUES(?,?)";
        try (Connection conn = DataBase.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, AuthUI.playerId);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("Erreur saveScore : " + e.getMessage());
        }
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT -> { if (direction != 'R') direction = 'L'; }
                case KeyEvent.VK_RIGHT -> { if (direction != 'L') direction = 'R'; }
                case KeyEvent.VK_UP -> { if (direction != 'D') direction = 'U'; }
                case KeyEvent.VK_DOWN -> { if (direction != 'U') direction = 'D'; }
                case KeyEvent.VK_SPACE -> resetGame();
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollision();
        }
        repaint();
    }
}

