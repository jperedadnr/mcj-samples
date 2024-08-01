package org.modernclients.highperformance;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class RandomCanvas extends Application {

	private static final int WIDTH = 800;
	private static final int HEIGHT = 600;

	public static void main(String[] args) {
		launch();
	}

	@Override
	public void start(Stage stage) throws Exception {
		Canvas canvas = new Canvas(WIDTH, HEIGHT);
		GraphicsContext gc = canvas.getGraphicsContext2D();
		for (int i = 0; i < canvas.getWidth(); i++) {
			for (int j = 0; j < canvas.getHeight(); j++) {
				gc.getPixelWriter().setColor(i, j, Color.color(Math.random(), Math.random(), Math.random()));
			}
		}
		stage.setTitle("Random Pixels");
		stage.setScene(new Scene(new StackPane(canvas), WIDTH, HEIGHT));
		stage.show();
	}

}

