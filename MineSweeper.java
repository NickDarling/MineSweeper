package minesweeper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.*;
import java.net.URL;
import javax.sound.sampled.*;
import java.applet.*;
import java.net.*;

/**
 *
 * @author Nick
 */
public class MineSweeper extends Canvas implements MouseListener {

    private BufferStrategy strategy;        // take advantage of accelerated graphics
    final int screenWidth;
    final int screenHeight;
    private boolean gameRunning = true;
    BufferedImage[] images;			// all images used
    private int updateInterval = 10;	// milliseconds

    int squareW;
    int squareH;

    int [][] gameBoard = new int [9][9];
    int [][] uncovered = new int [9][9];
    int numMines = 10;
    boolean gameFinish = false;
    boolean win = false;
    int row = -1;
    int col = -1;



    public MineSweeper () {
        // load images
        try {
            images = new BufferedImage[12];

            images[0] = ImageIO.read(new File("img/background.png"));	// background

            images[1] = ImageIO.read(new File("img/1.png"));		// 1
            images[2] = ImageIO.read(new File("img/2.png"));		// 2
            images[3] = ImageIO.read(new File("img/3.png"));		// 3
            images[4] = ImageIO.read(new File("img/4.png"));		// 4
            images[5] = ImageIO.read(new File("img/5.png"));		// 5
            images[6] = ImageIO.read(new File("img/6.png"));		// 6
            images[7] = ImageIO.read(new File("img/7.png"));		// 7
            images[8] = ImageIO.read(new File("img/8.png"));		// 8
            images[9] = ImageIO.read(new File("img/empty.png"));

            images[10] = ImageIO.read(new File("img/bomb.png"));
            images[11] = ImageIO.read(new File("img/x.png"));

            squareW = images[1].getWidth();
            squareH = images[1].getHeight();
        } catch (IOException e) {
            System.out.println("Image Error");
            System.exit(-1);
        }

        screenWidth = images[0].getWidth();
        screenHeight = images[0].getHeight();

        // create a frame to contain game
        JFrame container = new JFrame("MineSweeper");

        // get hold the content of the frame
        JPanel panel = (JPanel) container.getContentPane();

        // set up the resolution of the game
        panel.setPreferredSize(new Dimension(screenWidth, screenHeight));
        panel.setLayout(null);

        // set up canvas size (this) and add to frame
        setBounds(0, 0, screenWidth, screenHeight);
        panel.add(this);

        // Tell AWT not to bother repainting canvas since that will
        // be done using graphics acceleration
        setIgnoreRepaint(true);

        // make the window visible
        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        // if user closes window, shutdown game and jre
        container.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                System.exit(0);
            } // windowClosing
        });

        // add key listener to this canvas
        addKeyListener(new KeyInputHandler());

        // add mouse listener to this canvas
        addMouseListener(this);

        // request focus so key events are handled by this canvas
        requestFocus();

        // create buffer strategy to take advantage of accelerated graphics
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // start the game
        gameLoop();
    } // constructor

    /*
     * gameLoop input: none output: none purpose: Main game loop. Runs
     * throughout game play. Responsible for the following activities: -
     * calculates speed of the game loop to update moves - moves the game
     * entities - draws the screen contents (entities, text) - updates game
     * events - checks input
     */
    public void gameLoop() {
        long lastLoopTime = System.currentTimeMillis();
        initializeBoard(gameBoard, uncovered, numMines);

        // keep loop running until game ends
        while (gameRunning) {
            // calc. time since last update, will be used to calculate movement
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();

            // get graphics context for the accelerated surface and make it black
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.drawImage(images[0], 0, 0, null);

            if (row != -1 && col != -1 && !gameFinish) {
                gameFinish = exposeSquare(row, col, gameBoard, uncovered);
                win = checkWin(uncovered, numMines);
            }

            if (gameFinish || win) {
                // if lose
                if (uncovered[row][col] == -3) {
                    for (int i = 0; i < gameBoard.length; i++) {
                        for (int j = 0; j < gameBoard[i].length; j++) {
                            if (gameBoard[i][j] == -1) {
                                uncovered[i][j] = -1;
                            }
                        }
                    }
                    uncovered[row][col] = -3;
                    drawBoard(uncovered, g);
                }

                // if win
                if (win) {
                    drawBoard(gameBoard, g);
                }
            } else {
                drawBoard(uncovered, g);
            }

            // clear graphics and flip buffer
            g.dispose();
            strategy.show();

            pause(updateInterval);
        } // while
    } // gameLoop

    // pause
    public static void pause(int time) {
        try {
            Thread.currentThread().sleep(time);
        } catch (Exception e) {
        } // catch
    } // wait

     public static boolean checkWin(int [][] uncovered, int numMines){
        int remainingSquares = 0;

        for (int i = 0; i < uncovered.length; i++) {
            for (int j = 0; j < uncovered[0].length; j++) {
                if (uncovered[i][j] == -2) {
                    remainingSquares++;
                }
            }
        }

        if (remainingSquares == numMines) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean exposeSquare (int row, int col, int [][] board, int [][] uncovered) {
        uncovered[row][col] = board[row][col];

        // if no bombs adjacent
        if (uncovered[row][col] == 0) {
             // up
            if (col - 1 >= 0) {
                uncovered[row][col - 1] = board[row][col - 1];
            }

            // up right
            if ((col - 1 >= 0) && (row + 1 < board.length)) {
               uncovered[row + 1][col - 1] = board[row + 1][col - 1];
            }

            // right
            if (row + 1 < board.length) {
                uncovered[row + 1][col] = board[row + 1][col];
            }

            // down right
            if ((row + 1 < board.length) && col + 1 < board.length) {
                uncovered[row + 1][col + 1] = board[row + 1][col + 1];
            }


            // down
            if (col + 1 < board.length) {
                uncovered[row][col + 1] = board[row][col + 1];
            }

            // down left
            if ((row - 1 >= 0) && (col + 1 < board.length)) {
                uncovered[row - 1][col + 1] = board[row - 1][col + 1];
            }

            // left
            if (row - 1 >= 0) {
                uncovered[row - 1][col] = board[row - 1][col];
            }

            // up left
            if ((row - 1 >= 0) && (col - 1 >= 0)) {
                uncovered[row - 1][col - 1] = board[row - 1][col - 1];
            }
        } // if

        // check if bomb
        if (uncovered[row][col] == -1) {
            uncovered[row][col] = -3;
            return true;
        } else {
            return false;
        }
    } // exposeSquare

    public static void initializeBoard (int [][] board, int [][] uncovered, int numMines) {
        int x = 0;                  // x position of mine
        int y = 0;                  // y position of mine
        boolean newMine = false;    // false if theres already a mine here
        Random mineX = new Random();
        Random mineY = new Random();

        // place mines on board
        for (int i = 0; i < numMines; i++){
            do {
                x = mineX.nextInt(board.length);
                y = mineY.nextInt(board[0].length);

                if (board[x][y] != -1) {
                    board[x][y] = -1;
                    newMine = true;

                    recordAdjacentMines(board, x, y);
                } else {
                    newMine = false;
                }
            } while (!newMine);
        } // for

        // set all spaces to uncovered
        for (int i = 0; i < uncovered.length; i++) {
            for (int j = 0; j < uncovered[i].length; j++) {
                uncovered[i][j] = -2;
            }
        }
    } //initializeBoard

    public static void recordAdjacentMines(int [][] board, int row, int col) {
            // up
            if (col - 1 >= 0) {
                if (board[row][col - 1] != -1) {
                    board[row][col - 1]++;
                }
            }

            // up right
            if ((col - 1 >= 0) && (row + 1 < board.length)) {
                if (board[row + 1][col - 1] != -1) {
                    board[row + 1][col - 1]++;
                }
            }

            // right
            if (row + 1 < board.length) {
                if (board[row + 1][col] != -1) {
                    board[row + 1][col]++;
                }
            }

            // down right
            if ((row + 1 < board.length) && col + 1 < board.length) {
                if (board[row + 1][col + 1] != -1) {
                    board[row + 1][col + 1]++;
                }
            }

            // down
            if (col + 1 < board.length) {
                if (board[row][col + 1] != -1) {
                    board[row][col + 1]++;
                }
            }

            // down left
            if ((row - 1 >= 0) && (col + 1 < board.length)) {
                if (board[row - 1][col + 1] != -1) {
                    board[row - 1][col + 1]++;
                }
            }

            // left
            if (row - 1 >= 0) {
                if (board[row - 1][col] != -1) {
                    board[row - 1][col]++;
                }
            }

            // up left
            if ((row - 1 >= 0) && (col - 1 >= 0)) {
                if (board[row - 1][col - 1] != -1) {
                    board[row - 1][col - 1]++;
                }
            }
    } // recordAdjacentMines

    public void drawBoard (int [][] board, Graphics2D g) {
        for (int y = 0; y < board.length; y++) {
           for (int x = 0; x < board[y].length; x++) {
               if (board[x][y] == -1) {
                   g.drawImage(images[10], (8 + 7 * x + x * 47), (8 + 7 * y + y * 47), null);
               } else if (board[x][y] == 0) {
                   g.drawImage(images[9], (8 + 7 * x + x * 47), (8 + 7 * y + y * 47), null);
               } else if (board[x][y] == -3) {
                   g.drawImage(images[11], (8 + 7 * x + x * 47), (8 + 7 * y + y * 47), null);
               } else if (board[x][y] == 1){
                   g.drawImage(images[1], (8 + 7 * x + x * 47), (8 + 7 * y + y * 47), null);
               } else if (board[x][y] == 2){
                   g.drawImage(images[2], (8 + 7 * x + x * 47), (8 + 7 * y + y * 47), null);
               } else if (board[x][y] == 3){
                   g.drawImage(images[3], (8 + 7 * x + x * 47), (8 + 7 * y + y * 47), null);
               } else if (board[x][y] == 4){
                   g.drawImage(images[4], (8 + 7 * x + x * 47), (8 + 7 * y + y * 47), null);
               } else if (board[x][y] == 5){
                   g.drawImage(images[5], (8 + 7 * x + x * 47), (8 + 7 * y + y * 47), null);
               } else if (board[x][y] == 6){
                   g.drawImage(images[6], (8 + 7 * x + x * 47), (8 + 7 * y + y * 47), null);
               } else if (board[x][y] == 7){
                   g.drawImage(images[7], (8 + 7 * x + x * 47), (8 + 7 * y + y * 47), null);
               } else if (board[x][y] == 8){
                   g.drawImage(images[8], (8 + 7 * x + x * 47), (8 + 7 * y + y * 47), null);
               }
           }
           System.out.println();
        } // for
        System.out.println();
    } // drawBoard

    public void toGrid(MouseEvent me) {
        row = (me.getX() - 8) / 54;
        col = (me.getY() - 8) / 54;
    } // toGrid
    /*
     * inner class KeyInputHandler handles keyboard input from the user
     */
    private class KeyInputHandler extends KeyAdapter {
        public void keyTyped(KeyEvent e) {
            // if escape is pressed, end game
            if (e.getKeyChar() == 27) {
                System.exit(0);
            } // if escape pressed

        } // keyTyped
    } // class KeyInputHandler

    public void mousePressed(MouseEvent me) {
//       saySomething("Mouse pressed; # of clicks: " + e.getClickCount(), e);
        if (!gameFinish && !win) {
            toGrid(me);
        }
    } // mousePressed

    public void mouseReleased(MouseEvent e) {
//       saySomething("Mouse released; # of clicks: " + e.getClickCount(), e);
    }

    public void mouseEntered(MouseEvent e) {
//       saySomething("Mouse entered", e);
    }

    public void mouseExited(MouseEvent e) {
//       saySomething("Mouse exited", e);
    }

    public void mouseClicked(MouseEvent me) {
//       saySomething("Mouse clicked (# of clicks: " + e.getClickCount() + ")", e);
    }

    public static void main(String[] argv) {
        new MineSweeper(); // instantiate this object
    }
}