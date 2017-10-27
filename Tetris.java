import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Timer;

class Tetris {

  public static void main(String[] args) {
    Game game = new Game();
    game.run();
  }
}

class Game implements KeyListener, ActionListener {
  Timer timer = new Timer(1000, this);
  Board board = new Board();
  Piece piece;
  Piece nextPiece;
  private boolean gameOver;

  public void run() {
    board.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        System.exit(0);
      }
    });
    board.addKeyListener(this);
    newGame();
  }

  private void newGame() {
    board.clear();
    piece = new Piece();
    nextPiece = new Piece();
    gameOver = false;
    timer.start();
    board.refresh(piece, nextPiece);
  }

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == VK_C && e.isControlDown()) {
      System.exit(0);
    } else if (gameOver) {
      newGame();
    } else {
      board.setText("");
      int keyCode = e.getKeyCode();
      switch (keyCode) {
        case VK_UP:
          // Rotate piece
          piece = piece.tryRotate(false, board);
          break;
        case VK_LEFT:
          // Move piece left
          piece.tryMove(board, -1, 0);
          break;
        case VK_RIGHT:
          // Move piece right
          piece.tryMove(board, 1, 0);
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
        default:
          board.setText(KeyEvent.getKeyText(keyCode));
      }
    }
    board.refresh(piece, nextPiece);
  }

  private void moveBlockDown() {
    piece = piece.dropOnto(board, nextPiece);
    if (piece == null) {
      nextPiece = null;
      gameOver = true;
      board.displayGameOver();
      // Wait 3 seconds so player can see that game is over
      // FIXME: This also disables ^C
      board.removeKeyListener(this);
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        System.exit(1);
      }
      board.addKeyListener(this);
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
    board.refresh(piece, nextPiece);
  }
}


