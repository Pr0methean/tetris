import java.util.Random;

public class Piece {

  private int x, y;
  private final int[][] shape;
  private static final int INIT_X = (Game.WIDTH - 2) / 2;
  private static final Random RNG = new Random();
  private static final int[] EMPTY_ROW = {0, 0, 0, 0};
  public static final int[][] SHAPE_I = {EMPTY_ROW, {1, 1, 1, 1}, EMPTY_ROW EMPTY_ROW};
  public static final int[][] SHAPE_O = {EMPTY_ROW, {0, 2, 2, 0}, {0, 2, 2, 0}, EMPTY_ROW};
  public static final int[][] SHAPE_J = {EMPTY_ROW, {0, 3, 3, 3}, {0, 0, 0, 3}, EMPTY_ROW};
  public static final int[][] SHAPE_L = {EMPTY_ROW, {4, 4, 4, 0}, {4, 0, 0, 0}, EMPTY_ROW};
  public static final int[][] SHAPE_S = {EMPTY_ROW, {0, 5, 5, 0}, {5, 5, 0, 0}, EMPTY_ROW};
  public static final int[][] SHAPE_Z = {EMPTY_ROW, {0, 6, 6, 0}, {0, 0, 6, 6}, EMPTY_ROW};
  public static final int[][] SHAPE_T = {EMPTY_ROW, {7, 7, 7, 0}, {0, 7, 0, 0}, EMPTY_ROW};
  private static final int[][][] BLOCK_SHAPES =
      new int[][][]{SHAPE_I, SHAPE_O, SHAPE_J, SHAPE_L, SHAPE_S, SHAPE_Z, SHAPE_T};

  private Piece(int x, int y, int[][] sourceShape) {
    this.x = x;
    this.y = y;
    shape = new int[][]{sourceShape[0].clone(), sourceShape[1].clone(), sourceShape[2].clone(),
        sourceShape[3].clone()};
  }

  public Piece() {
    // Random shape, start in top center
    this(INIT_X, -1, BLOCK_SHAPES[RNG.nextInt(BLOCK_SHAPES.length)]);
  }

  /**
   * Indicates whether this piece can move in a given direction without colliding.
   * @param board the board to check for collisions.
   * @param dx x offset to move in.
   * @param dy y offset to move in.
   * @return true if this piece can fall down one step without colliding; false otherwise.
   */
  private boolean canMove(Game board, int dx, int dy) {
    for (int ix = 0; ix < 4; ix++) {
      for (int iy = 0; iy < 4; iy++) {
        if (shape[iy][ix] != 0 && (!isOnBoard(x + ix + dx, y + iy + dy)
            || board.board[y + iy + dy][x + ix + dx] != 0)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Moves this piece if possible.
   * @param board the board to check for collisions.
   * @param dx x offset to move in.
   * @param dy y offset to move in.
   * @return true if moved; false otherwise.
   */
  public void tryMove(Game board, int dx, int dy) {
    if (canMove(board, dx, dy)) {
      x += dx;
      y += dy;
    }
  }

  public Piece tryRotate(boolean counterClockwise, Game board) {
    int[][] rotatedShape = new int[4][4];
    for (int ix = 0; ix < 4; ix++) {
      for (int iy = 0; iy < 4; iy++) {
        if (shape[iy][ix] != 0) {
          int rotatedIx = counterClockwise ? iy : 3 - iy;
          int rotatedIy = counterClockwise ? 3 - ix : ix;
          if (!isOnBoard(x + rotatedIx, y + rotatedIy)
              || board.board[y + rotatedIy][x + rotatedIx] != 0) {
            // Can't rotate that direction
            return this;
          }
          rotatedShape[rotatedIy][rotatedIx] = shape[iy][ix];
        }
      }
    }
    // Rotation successful
    return new Piece(x, y, rotatedShape);
  }

  private boolean isOnBoard(int x, int y) {
    return x >= 0 && x < Game.WIDTH && y >= 0 && y < Game.HEIGHT;
  }

  public int shapeAt(int x, int y) {
    if (x < this.x || x >= this.x + 4 || y < this.y || y >= this.y + 4) {
      return 0;
    }
    return shape[y - this.y][x - this.x];
  }

  @Override public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    for (int y = 0; y < 4; y++) {
      for (int x = 0; x < 4; x++) {
        stringBuilder.append(Game.PATTERNS[shape[y][x]]);
      }
      stringBuilder.append('\n');
    }
    return stringBuilder.toString();
  }

  /**
   * Drops this piece by one step if possible; if not, lands it.
   * @param board the board we're playing on.
   * @param nextPiece the next piece.
   * @return the currently-falling piece after dropping, or null if the game is over.
   */
  public Piece dropOnto(Game board, Piece nextPiece) {
    if (canMove(board, 0, 1)) {
      // Piece falls a step
      y++;
      return this;
    }
    // Anchor this piece
    anchor(board);
    board.clearFullRows();
    if (!nextPiece.canMove(board, 0, 1)) {
      // Game over! Display the collision
      nextPiece.anchor(board);
      return null;
    } else {
      nextPiece.y = 0;
      return nextPiece;
    }
  }

  private void anchor(Game board) {
    for (int ix = 0; ix < 4; ix++) {
      for (int iy = 0; iy < 4; iy++) {
        if (isOnBoard(x + ix, y + iy) && board.board[y + iy][x + ix] == 0) {
          board.set(x + ix, y + iy, shape[iy][ix]);
        }
      }
    }
  }
}
