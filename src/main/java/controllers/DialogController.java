package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.DialogModel;

import java.io.IOException;
import java.util.Objects;

public class DialogController {
    private DialogController dialogController;
    private static Stage stage;
    @FXML private Text messageText;

    public void displayDialog(DialogModel dialog) {
        Parent root = null;
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("views/DialogView.fxml")));
            root = loader.load();
            dialogController = loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //set message text
        dialogController.messageText.setText(dialog.getMessageText());

        //set stage/window properties
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(new Image("images/inspector_logo.png"));
        stage.setTitle(dialog.getTitle());
        assert root != null;
        stage.setScene(new Scene(root, 300, 200));
        stage.showAndWait();
    }

    public DialogModel closeProgram(){
        return new DialogModel("Exit Application", "Are you sure you want to exit Inspector?");
    }

    @FXML
    private void closeDialog(ActionEvent e){
        e.consume();
        stage.close();
    }

    @FXML
    private void closeProgram(ActionEvent e) {
        e.consume();
        stage.close();
        Platform.exit();
    }
}
