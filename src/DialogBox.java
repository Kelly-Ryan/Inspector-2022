import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogBox {
    static boolean instruction;

    public static Boolean dialog(String title, String text) {
        Stage stage = new Stage();
        stage.getIcons().add(new Image("resources/images/inspector_logo.png"));
        stage.setTitle(title);
        stage.setMinWidth(300);
        stage.setMinHeight(150);
        //Defines a modal window that blocks events from being delivered to any other application window.
        stage.initModality(Modality.APPLICATION_MODAL);

        Label messageText = new Label(text);
        Button confirmButton = new Button("Confirm");
        Button cancelButton = new Button("Cancel");

        HBox buttonContainer = new HBox();
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.setSpacing(20);
        confirmButton.setOnAction(e -> {
            instruction = true;
            stage.close();
        });
        cancelButton.setOnAction(e -> {
            instruction = false;
            stage.close();
        });
        buttonContainer.getChildren().addAll(confirmButton, cancelButton);

        VBox layout = new VBox(15);
        layout.getChildren().addAll(messageText, buttonContainer);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        //Shows this stage and waits for it to be hidden (closed) before returning to the caller.
        stage.showAndWait();

        return instruction;
    }

    public static void alert(String title, String text) {
        Stage stage = new Stage();
        stage.getIcons().add(new Image("resources/images/inspector_logo.png"));
        stage.setTitle(title);
        stage.setMinWidth(600);
        stage.setMinHeight(200);
        stage.initModality(Modality.APPLICATION_MODAL);

        Label messageText = new Label(text);
        Button actionButton = new Button("Close");
        actionButton.setOnAction(e -> stage.close());

        VBox layout = new VBox(messageText, actionButton);
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(20);
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
