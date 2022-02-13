package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import models.InstructorModel;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;

public class InstructorController {
    private InstructorController instructorController;
    private InstructorModel instructor;
    private ActionEvent actionEvent;
    private Stage stage;
    private Parent root;

//    public InstructorController(InstructorModel instructor, ActionEvent actionEvent) {
//        this.instructor = instructor;
//        this.actionEvent = actionEvent;
//    }

    public void displayDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("views/InstructorView.fxml")));
            root = loader.load();
            instructorController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //set attributes
        //instructorController

        //set stage/window properties
        stage = (Stage)((Node) actionEvent.getSource()).getScene().getWindow();
//        stage.getIcons().add(new Image("images/inspector_logo.png"));
//        stage.setTitle("Inspector");
//        stage.setMaximized(true);
//        assert root != null;
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void addModule() {

    }

    private void addAssignment() {

    }

    void changePassword() {

    }

    @FXML
    void exitApplication(){
        DialogController dialogController = new DialogController();
        dialogController.displayDialog(dialogController.closeProgram());
    }
}
