package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.AlertModel;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class AlertController {
    @FXML private Text messageText;
    private Parent root;
    Stage stage;
    URL url;

    void displayAlert(AlertModel alert) {
        System.out.println(getClass().getResource("/views/AlertView.fxml"));
        try {
            root = FXMLLoader.load(Objects.requireNonNull(getClass()
                    .getClassLoader().getResource("/views/AlertView.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(root);
        stage = new Stage();
        stage.getIcons().add(new Image("resources/images/inspector_logo.png"));
        stage.setMinWidth(300);
        stage.setMinHeight(200);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(alert.getTitle());
        messageText = new Text(alert.getMessageText());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }

    AlertModel loginSuccess(){
        return new AlertModel("Login", "Login successful!");
    }

    AlertModel incorrectPassword(){
        return new AlertModel("Login Failure", "Password incorrect. Please try again.");
    }

    AlertModel missingData() {
        return new AlertModel("Empty Fields", "Please complete all fields.");
    }

    AlertModel userNotFound() {
        return new AlertModel("Login", "No account with that email address.\nPlease register and try again.");
    }

    @FXML private void closeAlert() {
        stage.close();
    }
}
