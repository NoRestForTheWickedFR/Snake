import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;



public class Snake extends JPanel implements ActionListener {

    private static final String HIGH_SCORE_FILE = "highscore.txt";
    private int highScore = 0;


    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.err.println("Could not save high score: " + e.getMessage());
        }
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
        } catch (IOException e) {
            System.err.println("Could not load high score: " + e.getMessage());
        }
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private Direction currentDirection = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;

    private static final int CELL_SIZE = 20;
    private static final int FRAME_RATE = 10;
    private static final int SAFE_MARGIN = CELL_SIZE * 2;

    private boolean gameStarted = false;
    private boolean gameOver = false;
    private boolean paused = false;

    private final List<GamePoint> snake = new ArrayList<>();

    private GamePoint food;
    private static final Color FOOD_COLOR = Color.RED;
    private static final Color INITIAL_BACKGROUND_COLOR = Color.BLACK;
    private static final Color GAME_BACKGROUND_COLOR = Color.BLACK;
    private Color currentBackgroundColor = INITIAL_BACKGROUND_COLOR;
    private static final Color BORDER_COLOR = Color.WHITE;

    private int score = 0;

    public Snake() {
        super();
        setPreferredSize(new Dimension(400, 400));
        setBackground(INITIAL_BACKGROUND_COLOR);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (!gameStarted || gameOver) {
                        startGame();
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    paused = !paused;
                } else if (e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_UP) {
                    if (currentDirection != Direction.DOWN) {
                        nextDirection = Direction.UP;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_S || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (currentDirection != Direction.UP) {
                        nextDirection = Direction.DOWN;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if (currentDirection != Direction.RIGHT) {
                        nextDirection = Direction.LEFT;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_D || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (currentDirection != Direction.LEFT) {
                        nextDirection = Direction.RIGHT;
                    }
                }
            }
        });
        loadHighScore();
        new Timer(1000 / FRAME_RATE, this).start();
    }

    public void startGame() {
        resetGameData();
        generateFood();
        currentBackgroundColor = GAME_BACKGROUND_COLOR;
        gameStarted = true;
        gameOver = false;
    }

    private void resetGameData() {
        snake.clear();
        int width = getWidth();
        int height = getHeight();
        int startX = width / 2 - CELL_SIZE / 2;
        int startY = height / 2 - CELL_SIZE / 2;
        snake.add(new GamePoint(startX, startY));
        score = 0;
        gameOver = false;
        gameStarted = false;
        currentBackgroundColor = INITIAL_BACKGROUND_COLOR;
    }

    private void generateFood() {
        Random rand = new Random();
        int x = SAFE_MARGIN + rand.nextInt((getWidth() - 2 * SAFE_MARGIN) / CELL_SIZE) * CELL_SIZE;
        int y = SAFE_MARGIN + rand.nextInt((getHeight() - 2 * SAFE_MARGIN) / CELL_SIZE) * CELL_SIZE;
        food = new GamePoint(x, y);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        final Graphics2D graphics2D = (Graphics2D) graphics;
        setBackground(currentBackgroundColor);

        int topBorderOffset = (int) (CELL_SIZE * 0.6);
        int bottomBorderOffset = (int) (CELL_SIZE * 0.5);

        graphics2D.setColor(BORDER_COLOR);
        graphics2D.drawRect(SAFE_MARGIN, SAFE_MARGIN - topBorderOffset,
                getWidth() - 2 * SAFE_MARGIN,
                getHeight() - 2 * SAFE_MARGIN + topBorderOffset - bottomBorderOffset);

        if (!gameStarted) {
            drawStartScreen(graphics2D);
        } else if (gameOver) {
            drawGameOverScreen(graphics2D);
        } else {
            drawGameScreen(graphics2D);
        }

        if (paused) {
            drawPausedOverlay(graphics2D);
        }
    }

    private void drawStartScreen(Graphics2D graphics2D) {
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(graphics2D.getFont().deriveFont(30f));
        int currentHeight = getHeight() / 4;
        final var frc = graphics2D.getFontRenderContext();
        final String message = "Welcome to Snake2Go\nYou can start it by pressing the space bar\nHigh Score: " + highScore + "\nLast Score: " + score;
        for (final var line : message.split("\n")) {
            final var layout = new TextLayout(line, graphics2D.getFont(), frc);
            final var bounds = layout.getBounds();
            final var targetWidth = (float) (getWidth() - bounds.getWidth()) / 2;
            layout.draw(graphics2D, targetWidth, currentHeight);
            currentHeight += graphics2D.getFontMetrics().getHeight();
        }
    }

    private void drawGameOverScreen(Graphics2D graphics2D) {
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(graphics2D.getFont().deriveFont(30f));
        int currentHeight = getHeight() / 4;
        final var frc = graphics2D.getFontRenderContext();
        final String endMessage = "Game Over\nScore: " + score + "\nHigh Score: " + highScore + "\nPress Space to Restart";
        for (final var line : endMessage.split("\n")) {
            final var layout = new TextLayout(line, graphics2D.getFont(), frc);
            final var bounds = layout.getBounds();
            final var targetWidth = (float) (getWidth() - bounds.getWidth()) / 2;
            layout.draw(graphics2D, targetWidth, currentHeight);
            currentHeight += graphics2D.getFontMetrics().getHeight();
        }
    }

    private void drawGameScreen(Graphics2D graphics2D) {
        Color normalGreen = new Color(0, 255, 0);
        Color darkGreen = new Color(0, 128, 0);

        for (int i = 0; i < snake.size(); i++) {
            GamePoint point = snake.get(i);
            if (i == snake.size() - 1) {
                graphics2D.setColor(darkGreen);
            } else {
                graphics2D.setColor(normalGreen);
            }
            graphics2D.fillRect(point.x, point.y, CELL_SIZE, CELL_SIZE);
        }

        graphics2D.setColor(FOOD_COLOR);
        graphics2D.fillRect(food.x, food.y, CELL_SIZE, CELL_SIZE);

        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(graphics2D.getFont().deriveFont(20f));
        String scoreText = "Score: " + score;
        int scoreY = getHeight() - SAFE_MARGIN + 20;
        graphics2D.drawString(scoreText, SAFE_MARGIN, scoreY);
    }

    private void drawPausedOverlay(Graphics2D graphics2D) {
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(graphics2D.getFont().deriveFont(40f));
        String pauseText = "PAUSED";
        FontRenderContext frc = graphics2D.getFontRenderContext();
        TextLayout layout = new TextLayout(pauseText, graphics2D.getFont(), frc);
        Rectangle2D bounds = layout.getBounds();
        int x = (getWidth() - (int) bounds.getWidth()) / 2;
        int y = (getHeight() - (int) bounds.getHeight()) / 2;
        layout.draw(graphics2D, x, y);
    }


    private boolean isTouchingFood(GamePoint head, GamePoint food) {
        return head.x < food.x + CELL_SIZE &&
                head.x + CELL_SIZE > food.x &&
                head.y < food.y + CELL_SIZE &&
                head.y + CELL_SIZE > food.y;
    }

    private void move() {
        if (snake.isEmpty()) {
            return;
        }

        currentDirection = nextDirection;

        final GamePoint currentHead = snake.get(0);
        final int newHeadX = switch (currentDirection) {
            case UP -> currentHead.x;
            case DOWN -> currentHead.x;
            case LEFT -> currentHead.x - CELL_SIZE;
            case RIGHT -> currentHead.x + CELL_SIZE;
        };
        final int newHeadY = switch (currentDirection) {
            case UP -> currentHead.y - CELL_SIZE;
            case DOWN -> currentHead.y + CELL_SIZE;
            case LEFT -> currentHead.y;
            case RIGHT -> currentHead.y;
        };

        if (isCollision(newHeadX, newHeadY)) {
            if (score > highScore) {
                highScore = score;
                saveHighScore();
            }
            gameOver = true;
            gameStarted = false;

            Timer timer = new Timer(2000, e -> {
                resetGameData();
                repaint();
            });
            timer.setRepeats(false);
            timer.start();

            return;
        }

        final GamePoint newHead = new GamePoint(newHeadX, newHeadY);
        snake.add(0, newHead);

        if (isTouchingFood(newHead, food)) {
            score++;
            generateFood();

        } else {

            snake.remove(snake.size() - 1);
        }
    }


    private boolean isCollision(int x, int y) {
        final int panelWidth = getWidth();
        final int panelHeight = getHeight();

        final int topMargin = SAFE_MARGIN - CELL_SIZE;
        final int bottomMargin = getHeight() - SAFE_MARGIN;

        final boolean outOfBoundsX = x < SAFE_MARGIN || x + CELL_SIZE > panelWidth - SAFE_MARGIN;
        final boolean outOfBoundsY = y < topMargin || y + CELL_SIZE > bottomMargin;

        return outOfBoundsX || outOfBoundsY || snake.stream().skip(1).anyMatch(p -> p.x == x && p.y == y);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (gameStarted && !gameOver && !paused) {
            move();
        }
        repaint();
    }

    private record GamePoint(int x, int y) {
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            GamePoint other = (GamePoint) obj;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return 31 * x + y;
        }
    }


}