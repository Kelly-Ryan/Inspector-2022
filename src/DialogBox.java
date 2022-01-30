import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogBox {
    static boolean instruction;

    public static Boolean display(String title, String text) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setMinWidth(250);
        stage.setMinHeight(250);
        //Defines a modal window that blocks events from being delivered to any other application window.
        stage.initModality(Modality.APPLICATION_MODAL);

        Label messageText = new Label(text);
        Button confirmButton = new Button("Confirm");
        Button cancelButton = new Button("Cancel");

        confirmButton.setOnAction(e -> {
            instruction = true;
            stage.close();
        });

        cancelButton.setOnAction(e -> {
            instruction = false;
            stage.close();
        });

        VBox layout = new VBox(15);
        layout.getChildren().addAll(messageText, confirmButton, cancelButton);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        //Shows this stage and waits for it to be hidden (closed) before returning to the caller.
        stage.showAndWait();

        return instruction;
    }
}
