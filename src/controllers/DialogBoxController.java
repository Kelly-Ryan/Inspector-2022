package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class DialogBoxController {
    private static boolean instruction;
    @FXML private Button confirmButton = new Button();
    @FXML private Button cancelButton = new Button();
    @FXML private static Button actionButton = new Button();
    @FXML private static Text messageText = new Text();
    @FXML private Parent parent;

    @FXML
    public Boolean dialog(String title, String text) {
        try {
            parent = new FXMLLoader(getClass().getResource("/views/DialogView.fxml")).load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Stage stage = new Stage();
        stage.getIcons().add(new Image("resources/images/inspector_logo.png"));
        stage.setTitle(title);
        stage.setMinWidth(300);
        stage.setMinHeight(150);
        //Defines a modal window that blocks events from being delivered to any other application window.
        stage.initModality(Modality.APPLICATION_MODAL);

        messageText.setText(text);

        confirmButton.setOnAction(e -> {
            instruction = true;
            stage.close();
        });
        cancelButton.setOnAction(e -> {
            instruction = false;
            stage.close();
        });

        Scene scene = new Scene(parent);
        stage.setScene(scene);
        //Shows this stage and waits for it to be hidden (closed) before returning to the caller.
        stage.showAndWait();

        return instruction;
    }

    static void alert(String title, String text) {
//        Stage stage = new Stage();
//        stage.getIcons().add(new Image("resources/images/inspector_logo.png"));
//        stage.setTitle(title);
//        stage.setMinWidth(600);
//        stage.setMinHeight(200);
//        stage.initModality(Modality.APPLICATION_MODAL);
//
//        messageText.setText(text);
//        actionButton.setOnAction(e -> stage.close());
//
//        Scene scene = new Scene(layout);
//        stage.setScene(scene);
//        stage.showAndWait();
    }
}
