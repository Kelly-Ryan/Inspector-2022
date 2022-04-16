package controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class DialogController {
    private static MainViewController mainViewController;
    private static Stage stage;
    private Parent root = null;

    public DialogController() {

    }

    public DialogController(MainViewController mainViewController) {
        DialogController.mainViewController = mainViewController;
    }

    public void displayDialog(String view, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader()
                    .getResource("views/" + view)));
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //set stage/window properties
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.getIcons().add(new Image("images/inspector_logo.png"));
        stage.setTitle(title);
        assert root != null;
        stage.setScene(new Scene(root));
        stage.showAndWait();
    }

    public void setGradingRubric() {
        stage.close();
        DialogController.mainViewController.setGradingRubric();
    }

    @FXML
    private void cancelDialog(ActionEvent e){
        e.consume();
        stage.close();
    }

    @FXML
    private void closeProgram(ActionEvent e) {
        e.consume();
        stage.close();
        try{
            DialogController.mainViewController.executorService.shutdownNow();
        } catch (NullPointerException ignored) {

        } finally {
            Platform.exit();
        }
    }
}
