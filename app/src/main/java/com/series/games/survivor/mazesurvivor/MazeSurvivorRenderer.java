package com.series.games.survivor.mazesurvivor;

import android.opengl.GLSurfaceView;
import android.os.SystemClock;

import com.series.games.survivor.mazesurvivor.gameobjects.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Malvin on 4/18/2015.
 */
public class MazeSurvivorRenderer implements GLSurfaceView.Renderer {

    //The maze generator
    GenerateRandomMaze mazeGenerator;

    //Storage to store the cell information in the maze, 'w' mean wall, 'p' means path, 's' means survivor
    GenerateRandomMaze.Cell[][] maze;

    //The screen's height and width ratio
    private float ratio;

    //Set the startTime of a maze
    private long startTime;

    //numbers of row and column of the maze
    int row;
    int col;

    //Player object
    private Survivor survivor;

    //record the status of the player, if the player find the exit, set it to true
    private boolean findExit;

    //Constructor
    public MazeSurvivorRenderer(float ratio, int row, int col) {
        //Initializations
        this.ratio = ratio;
        startTime = SystemClock.uptimeMillis();
        this.row = row;
        this.col = col;
        survivor = new Survivor(row, col);
        findExit = false;
    }

    //All 3 colors needed to draw the maze
    private final float[] wallColor =  new float[]{1.0f, 0.0f, 0.0f, 1.0f };

    private final float[] pathColor = new float[]{0.0f, 1.0f, 0.0f, 1.0f};

    private final float[] survivorColor = new float[]{0.0f, 0.0f, 1.0f, 1.0f};

    private final float[] exitColor = new float[]{0.0f, 0.0f, 0.0f, 0.0f};

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        //Initialize the maze generator
        mazeGenerator = new GenerateRandomMaze();

        //Get the maze from generator
        maze = mazeGenerator.generateMaze(row, col, ratio, survivor.getX(), survivor.getY());//generate a M * N maze

        //set the background frame color
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public void onDrawFrame(GL10 gl) {
        //Redraw background color
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        drawMaze(gl);

        if(findExit) {//player run out of current maze, create a new maze for the player
            findExit = false;//set the boolean flag to false
            survivor = new Survivor(row, col);
            maze = mazeGenerator.generateMaze(row, col, ratio, survivor.getX(), survivor.getY());//generate a new M * N maze
            startTime = SystemClock.uptimeMillis();//reset the start time
        } else if((SystemClock.uptimeMillis() - startTime) / (1000L * row) > 0) {//player still in current maze, change the maze around every (maze row) seconds
            startTime = SystemClock.uptimeMillis();
            maze = mazeGenerator.generateMaze(row, col, ratio, survivor.getX(), survivor.getY());
        }
    }

    private void drawMaze(GL10 gl) {//draw maze function

        for(int r = 0; r < row; r++) {
            for(int c = 0; c < col; c++) {
                if(maze[r][c].Type == 'w') {//draw the wall cell
                    maze[r][c].mazeCell.draw(gl, wallColor);
                } else if(maze[r][c].Type == 'p') {//draw the path cell
                    maze[r][c].mazeCell.draw(gl, pathColor);
                } else if(maze[r][c].Type == 's') {//draw the survivor
                    maze[r][c].mazeCell.draw(gl, survivorColor);
                } else if(maze[r][c].Type == 'e') {//draw the exit
                    maze[r][c].mazeCell.draw(gl, exitColor);
                }
            }
        }
    }

    //update the survivor position according to the touch event
    public void updateSurvivor(String move) {
        switch(move) {
            case "LEFT":
                int moveLeft = Dir.LEFT.moveY(survivor.getY());
                if(moveLeft >=0 && maze[survivor.getX()][moveLeft].Type != 'w') {
                    if(maze[survivor.getX()][moveLeft].Type == 'e') {
                        findExit = true;
                    }
                    maze[survivor.getX()][survivor.getY()].Type = 'p';
                    survivor.updateY(moveLeft);
                    maze[survivor.getX()][survivor.getY()].Type = 's';
                }
                break;
            case "RIGHT":
                int moveRight = Dir.RIGHT.moveY(survivor.getY());
                if(moveRight < row && maze[survivor.getX()][moveRight].Type != 'w') {
                    if(maze[survivor.getX()][moveRight].Type == 'e') {
                        findExit = true;
                    }
                    maze[survivor.getX()][survivor.getY()].Type = 'p';
                    survivor.updateY(moveRight);
                    maze[survivor.getX()][survivor.getY()].Type = 's';
                }
                break;
            case "UP":
                int moveUp = Dir.UP.moveX(survivor.getX());
                if(moveUp >= 0 && maze[moveUp][survivor.getY()].Type != 'w') {
                    if(maze[moveUp][survivor.getY()].Type == 'e') {
                        findExit = true;
                    }
                    maze[survivor.getX()][survivor.getY()].Type = 'p';
                    survivor.updateX(moveUp);
                    maze[survivor.getX()][survivor.getY()].Type = 's';
                }
                break;
            case "DOWN":
                int moveDown = Dir.DOWN.moveX(survivor.getX());
                if(moveDown < col && maze[moveDown][survivor.getY()].Type != 'w') {
                    if(maze[moveDown][survivor.getY()].Type == 'e') {
                        findExit = true;
                    }
                    maze[survivor.getX()][survivor.getY()].Type = 'p';
                    survivor.updateX(moveDown);
                    maze[survivor.getX()][survivor.getY()].Type = 's';
                }
                break;
        }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
    }
}