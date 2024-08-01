package org.modernclients.highperformance;

import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;

public class GameOfLifePublisherConsumer extends GraphicApp {

    final int WIDTH = 1200;
    final int HEIGHT = 800;
    final int CELL_SIZE = 2;
    boolean[][] currentGeneration;
    int columns = WIDTH / CELL_SIZE;
    int rows = HEIGHT / CELL_SIZE;
    // this is the desired number of frames
    int numberOfFramesPerSecond = 0;
    private GameOfLife gameOfLife;
    ConcurrentLinkedQueue<boolean[][]> cellsQueue;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void setup() {
        cellsQueue = new ConcurrentLinkedQueue<>();
        width = WIDTH;
        height = HEIGHT;
        gameOfLife = new GameOfLife(columns, rows, CELL_SIZE);
        currentGeneration = gameOfLife.newCells();
        Task<Void> producerTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    cellsQueue.add(currentGeneration);
                    currentGeneration = gameOfLife.newGeneration(currentGeneration);
                }
            }
        };
        Task<Void> consumerTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    while (!cellsQueue.isEmpty()) {
                        boolean[][] data = cellsQueue.poll();
                        Platform.runLater(() -> {
                            // we need to draw the background because we are not using draw loop anymore
                            graphicContext.setFill(Color.LIGHTGRAY);
                            graphicContext.fillRect(0, 0, width, height);
                            gameOfLife.drawCells(data, graphicContext);
                        });
                        if (numberOfFramesPerSecond > 0) {
                            Thread.sleep(1000 / numberOfFramesPerSecond);
                        }
                    }
                }
            }
        };
        Thread producerThread = new Thread(producerTask);
        producerThread.setDaemon(true);
        Thread consumerThread = new Thread(consumerTask);
        consumerThread.setDaemon(true);
        producerThread.start();
        consumerThread.start();
        frames(0);
        title("Game of Life Using High-Density Data Pattern");
    }

    @Override
    public void draw() {
        // we don't use the main loop anymore, but we have to
        // draw the background in draw cells
    }
}

