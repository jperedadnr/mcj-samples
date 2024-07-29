package org.modernclients.samples;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.modernclients.Sample;

import java.util.function.Consumer;

/**
 * @author Almas Baimagambetov <almaslvl@gmail.com>
 * @author Jonathan Giles <jonathan@jonathangiles.net>
 */
public class DialogsDemo implements Sample {

    @Override
    public void buildDemo(Pane container, Consumer<String> console) {
        VBox box = new VBox(10,
                createButton(new Alert(AlertType.INFORMATION),
                        "Information Alert", "Provides the user with information"),

                createButton(new Alert(AlertType.CONFIRMATION),
                        "Confirmation Alert", "Seeks confirmation from the user"),

                createButton(new Alert(AlertType.WARNING),
                        "Warning Alert", "Warns the user about some fact or action"),

                createButton(new Alert(AlertType.ERROR),
                        "Error Alert", "Suggests that something has gone wrong"),

                createButton(new ChoiceDialog<>("Cat",
                        "Dog", "Cat", "Mouse"),
                        "Choice Dialog", "Shows a list of choices to the user"),

                createButtonForBlockingDialog(new TextInputDialog("Hello World"),
                        "Blocking Dialog", "Shows a text input control to the user", console),

                createButtonForCustomDialog(console)
        );
        box.setAlignment(Pos.CENTER);

        container.getChildren().addAll(box);
    }

    private Button createButtonForBlockingDialog(Dialog<?> dialog, String dialogName, String dialogText, Consumer<String> console) {
        Button btn = createButton(dialog, dialogName, dialogText);
        btn.setOnAction(e -> {
            dialog.showAndWait()
                  .ifPresent(result -> console.accept("Result is " + result));
        });
        return btn;
    }

    private Button createButtonForCustomDialog(Consumer<String> console) {
        Dialog<Credential> dialog = new Dialog<>();

        TextField fieldUserName = new TextField();
        PasswordField fieldPassword = new PasswordField();

        Pane content = new VBox(5,
                new Label("Username:"),
                fieldUserName,
                new Label("Password:"),
                fieldPassword);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setHeaderText("Enter username and password...");
        dialogPane.setContent(content);

        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            // if the user pressed the OK button, return the credentials, otherwise
            // we return null
            if (buttonType == ButtonType.OK) {
                return new Credential(fieldUserName.getText(), fieldPassword.getText());
            }

            return null;
        });

        Button btn = createButton(dialog, "Custom Dialog", "Asks the user for credentials");
        btn.setOnAction(e -> {
            dialog.showAndWait()
                  .ifPresent(result -> console.accept("Username is " + result.username + ". Password is " + result.password));
        });
        return btn;
    }

    private Button createButton(Dialog<?> dialog, String dialogName, String dialogText) {
        dialog.setContentText(dialogText);

        Button btn = new Button("Show " + dialogName);
        btn.setOnAction(e -> dialog.show());
        return btn;
    }

    private static class Credential {
        private String username;
        private String password;

        public Credential(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
