package controllers;

import javafx.event.ActionEvent;
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
import java.util.Objects;

public class AlertController {
    private static AlertController alertController;
    private static Stage stage;
    @FXML private Text messageText;

    void displayAlert(AlertModel alert) {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("views/AlertView.fxml")));
            root = loader.load();
            alertController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //set message text
        alertController.messageText.setText(alert.getMessageText());

        //set stage/window properties
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(new Image("images/inspector_logo.png"));
        stage.setTitle(alert.getTitle());
        assert root != null;
        stage.setScene(new Scene(root, 300, 200));
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

    AlertModel userExists() {
        return new AlertModel("Error", "A user with this email address is already registered.\nUse this email address to login or register with a different email address.");
    }

    AlertModel passwordMismatch() {
        return new AlertModel("Password Mismatch", "Passwords must be the same.");
    }

    AlertModel registrationSuccess() {
        return new AlertModel("Successful Registration", "Registration completed!\nLogin with your username and password.");
    }

    @FXML
    private void closeAlert(ActionEvent actionEvent) {
        actionEvent.consume();
        AlertController.stage.close();
    }
}
