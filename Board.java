import java.awt.Font;
import java.awt.Frame;
import java.awt.TextArea;

/*
 * Board extends an AWT window object that displays the tetris game board.
 */
class Board extends Frame {

  static final char[] PATTERNS = {' ', '#', '%', '&', '@', '$', '8', 'X'};
  static int WIDTH = 10;
  static int HEIGHT = 10;
  private static final int[] EMPTY_ROW = new int[WIDTH];
  static int WINDOW_WIDTH = 200;
  static int WINDOW_HEIGHT = 600;
  static int TOP_BORDER = 30;
  static int BORDER = 10;
  String textBelow = "";
  TextArea area = null;
  int score = 0;
  int topScore = 0;
  // The board is represented as an array of arrays, with 10 rows and 10 columns.
  int[][] board = new int[HEIGHT][WIDTH];

  Board() {
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

  public int[][] getBoard() {
    return board;
  }

  public void set(int x, int y, int value) {
    board[y][x] = value;
  }

  /*
   * Append text to the demo text area.
   */
  public void setText(String text) {
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
  }

  /*
   * Updates the demo text area with the contents of the board.
   */
  public void refresh(Piece piece, Piece nextPiece) {
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
  public void clear() {
    for (int row = 0; row < HEIGHT; row++) {
      System.arraycopy(EMPTY_ROW, 0, board[row], 0, WIDTH);
    }
    score = 0;
  }

  public void displayGameOver() {
    if (score > topScore) {
      topScore = score;
      setText("HIGH SCORE :)");
    } else {
      setText("GAME OVER  :(");
    }
    refresh(null, null);
  }
}
