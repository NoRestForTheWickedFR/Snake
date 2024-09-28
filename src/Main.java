import javax.swing.JFrame;
import java.awt.Dimension;

public class Main {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    public static void main(String[] args) {
        final JFrame frame = new JFrame("Snake2Go");
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        Snake game = new Snake();
        game.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        frame.add(game);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        game.startGame();
    }
}
