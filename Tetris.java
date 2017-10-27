import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_UP;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class Tetris {

  public static void main(String[] args) {
    Game game = new Game();
    game.run();
  }
}

class Game implements KeyListener {

  Board board = new Board();
  Piece piece = new Piece();
  Piece nextPiece = new Piece();
  private boolean gameOver = false;

  public void run() {
    board.refresh(piece);

    board.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        System.exit(0);
      }
    });
    board.addKeyListener(this);
  }

  public void keyPressed(KeyEvent e) {
    if (gameOver) {
      board.setText("GAME OVER");
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
          break;
        case VK_SPACE:
          // Quick drop
          Piece currentPiece = piece;
          do {
            moveBlockDown();
          } while (piece == currentPiece);
          break;
        case VK_C:
          if (e.isControlDown()) {
            // Ctrl-C exits game
            System.exit(0);
          }
          // fall through
        default:
          board.setText(KeyEvent.getKeyText(keyCode));
      }
    }
    board.refresh(piece);
  }

  private void moveBlockDown() {
    piece = piece.dropOnto(board, nextPiece);
    if (piece == null) {
      gameOver = true;
    } else if (piece == nextPiece) {
      nextPiece = new Piece();
    }
  }

  public void keyReleased(KeyEvent e) {
  }

  public void keyTyped(KeyEvent e) {
  }
}


