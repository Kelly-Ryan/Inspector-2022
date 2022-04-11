import controllers.DialogController;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.util.Objects;

import controllers.DatabaseController;

public class Launcher extends Application {

    public static void main(String[] args) {
        //launch calls start() from Application class
        launch(args);
    }

    //entry point for JavaFX application
    @Override
    public void start(Stage stage) throws Exception {
        //initialize database
        DatabaseController.dbSetup();

        //set up JavaFX stage/window
        Parent root = new FXMLLoader(Objects.requireNonNull(getClass().getResource("views/LoginView.fxml"))).load();
        stage.getIcons().add(new Image("/images/inspector_logo.png")); //set application icon
        stage.setMaximized(true);
        stage.setTitle("Inspector");

        stage.setOnCloseRequest(e -> {
            e.consume();
            exitApplication();
        });

        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void exitApplication() {
        DialogController dialogController = new DialogController();
        dialogController.displayDialog("ExitApplicationDialogView.fxml", "Exit Application");
    }
}
