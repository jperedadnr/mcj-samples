package org.modernclients.bridgefx.multifxinswing.dynamicenhanced;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javafx.application.Platform;

import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        // Do everything on the event thread to be Swing compliant
        SwingUtilities.invokeLater(() -> {
            var frame = new JFrame("JavaFX 23 integrated in Swing (multiple, dynamic)");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            var northJfxPanel = new JFXPanel();            
            var northButton = new Button("Hello FX North");
            var northScene = new Scene(northButton);
            northJfxPanel.setScene(northScene);
            northJfxPanel.setPreferredSize(new Dimension(200,50));
            var panel = new JPanel(new BorderLayout());
            panel.add(northJfxPanel, BorderLayout.NORTH);

            var northSwingButton = new JButton("Remove FX Scene in North");
            northSwingButton.addActionListener(e -> {
               panel.remove(northJfxPanel);
               frame.pack();
            });
            var southSwingButton = new JButton("Add FX Scene in South");
            southSwingButton.addActionListener(e -> {
                var southJfxPanel = new JFXPanel();            
                var southButton = new Button("Hello FX South");
                var southScene = new Scene(southButton);

                southJfxPanel.setPreferredSize(new Dimension(200,50));
                panel.add(southJfxPanel, BorderLayout.SOUTH);
                Platform.runLater(() -> {
                    southJfxPanel.setScene(southScene);
                    SwingUtilities.invokeLater(frame::pack);
                });
            });
            var swingInside = new JPanel(new BorderLayout());
            swingInside.add(northSwingButton, BorderLayout.NORTH);
            swingInside.add(southSwingButton, BorderLayout.SOUTH);
            panel.add(swingInside, BorderLayout.CENTER);
            frame.setContentPane(panel);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Platform.setImplicitExit(false);
        });
    }

}