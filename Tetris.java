import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.Font;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import javax.swing.Timer;

class Tetris {

  public static void main(String[] args) {
    Game game = new Game();
    game.run();
  }
}

class Game extends Frame implements KeyListener, ActionListener {
  private static final Clock CLOCK = Clock.systemUTC();
  static final char[] PATTERNS = {' ', '#', '%', '&', '@', '$', '8', 'X'};
  /** Initially drop blocks 1 row per second. */
  private static final int INITIAL_DELAY_MS = 1000;
  /** Used to ensure the player sees that the game ended. */
  private static final Duration NEW_GAME_COOLDOWN = Duration.ofSeconds(3);
  private Instant noNewGameBefore = Instant.MIN;
  private static final double SPEED_DOUBLES_EVERY_N_POINTS = 30.0;
  static final int WIDTH = 10;
  private static final int[] EMPTY_ROW = new int[WIDTH];
  static final int HEIGHT = 10;
  private static final int WINDOW_WIDTH = 200;
  private static final int WINDOW_HEIGHT = 600;
  private static final int TOP_BORDER = 30;
  private static final int BORDER = 10;
  private String textBelow = "";
  private TextArea area = null;
  private int score = 0;
  private int topScore = 0;
  // The board is represented as an array of arrays, with 10 rows and 10 columns.
  final int[][] board = new int[HEIGHT][WIDTH];
  private final Timer timer = new Timer(INITIAL_DELAY_MS, this);
  private Piece piece;
  private Piece nextPiece;
  private boolean gameOver;

  Game() {
    super("Demo");
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    setLayout(null);
    setVisible(true);
    requestFocus();

    area = new TextArea("", WIDTH, HEIGHT, TextArea.SCROLLBARS_NONE);
    area.setBounds(BORDER, TOP_BORDER, WINDOW_WIDTH - 2 * BORDER,
        WINDOW_HEIGHT - BORDER - TOP_BORDER);
    area.setFont(new Font("Monospaced", Font.PLAIN, 24));
    area.setEditable(false);
    area.setFocusable(false);

    add(area);
  }

  /**
   * Speed increases smoothly and exponentially with score.
   * @return the current time delay between block drops
   */
  private int getTimeDelayMs() {
    return (int) (INITIAL_DELAY_MS * Math.pow(0.5, score / SPEED_DOUBLES_EVERY_N_POINTS));
  }

  public void set(int x, int y, int value) {
    board[y][x] = value;
  }

  /*
   * Append text to the demo text area.
   */
  private void setText(String text) {
    textBelow = text;
  }

  /**
   * Clears all full rows and scores points for them.
   */
  public void clearFullRows() {
    int rowsCleared = 0;
    outer:
    for (int row = 0; row < HEIGHT; row++) {
      for (int col = 0; col < WIDTH; col++) {
        if (board[row][col] == 0) {
          continue outer;
        }
      }
      // Row is cleared!
      rowsCleared++;
      for (int fallingRow = row - 1; fallingRow > 0; fallingRow--) {
        System.arraycopy(board[fallingRow], 0, board[fallingRow + 1], 0, WIDTH);
      }
      System.arraycopy(EMPTY_ROW, 0, board[0], 0, WIDTH);
    }
    score += rowsCleared * rowsCleared;
    timer.setInitialDelay(getTimeDelayMs());
  }

  /*
   * Updates the demo text area with the contents of the
   */
  private void refresh(Piece piece, Piece nextPiece) {
    StringBuilder sb = new StringBuilder();
    for (int col = 0; col < WIDTH + 2; col++) {
      sb.append("*");
    }
    sb.append("\n");

    for (int row = 0; row < HEIGHT; row++) {
      sb.append("|");
      for (int col = 0; col < WIDTH; col++) {
        int value = board[row][col];
        if (value == 0 && piece != null) {
          value = piece.shapeAt(col, row);
        }
        sb.append(PATTERNS[value]);
      }
      sb.append("|\n");
    }

    for (int col = 0; col < WIDTH + 2; col++) {
      sb.append("*");
    }
    sb.append("\n");
    sb.append(textBelow);
    sb.append(String.format("%nSCORE: %5d%nBEST:  %5d%n", score, topScore));
    if (nextPiece != null) {
      sb.append(nextPiece.toString());
    }
    area.setText(sb.toString());
  }

  /** Reset board and score to start a new game. */
  private void clear() {
    for (int row = 0; row < HEIGHT; row++) {
      System.arraycopy(EMPTY_ROW, 0, board[row], 0, WIDTH);
    }
    score = 0;
  }

  private void displayGameOver() {
    if (score > topScore) {
      topScore = score;
      setText("HIGH SCORE!");
    } else {
      setText("GAME OVER :(");
    }
    refresh(null, null);
  }

  public void run() {
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        System.exit(0);
      }
    });
    addKeyListener(this);
    newGame();
  }

  private void newGame() {
    clear();
    gameOver = false;
    piece = new Piece();
    nextPiece = new Piece();
    gameOver = false;
    timer.setInitialDelay(INITIAL_DELAY_MS);
    timer.start();
    refresh(piece, nextPiece);
  }

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == VK_C && e.isControlDown()) {
      System.exit(0);
    } else if (gameOver) {
      if (CLOCK.instant().isAfter(noNewGameBefore)) {
        newGame();
      }
    } else {
      setText("");
      int keyCode = e.getKeyCode();
      switch (keyCode) {
        case VK_UP:
          // Rotate piece
          piece = piece.tryRotate(false, this);
          break;
        case VK_LEFT:
          // Move piece left
          piece.tryMove(this, -1, 0);
          break;
        case VK_RIGHT:
          // Move piece right
          piece.tryMove(this, 1, 0);
          break;
        case VK_DOWN:
          moveBlockDown();
          timer.restart();
          break;
        case VK_SPACE:
          // Quick drop
          Piece currentPiece = piece;
          do {
            moveBlockDown();
          } while (piece == currentPiece);
          timer.restart();
          break;
      }
    }
    refresh(piece, nextPiece);
  }

  private void moveBlockDown() {
    piece = piece.dropOnto(this, nextPiece);
    if (piece == null) {
      nextPiece = null;
      gameOver = true;
      displayGameOver();
      noNewGameBefore = CLOCK.instant().plus(NEW_GAME_COOLDOWN);
    } else if (piece == nextPiece) {
      nextPiece = new Piece();
    }
  }

  public void keyReleased(KeyEvent e) {
  }

  public void keyTyped(KeyEvent e) {
  }

  @Override public void actionPerformed(ActionEvent e) {
    // Only called by the timer.
    moveBlockDown();
    timer.restart();
    refresh(piece, nextPiece);
  }
}


