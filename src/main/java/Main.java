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

public class Main extends Application {

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
        dialogController.displayDialog(dialogController.closeProgram());
    }

//    Scene createSubmissionScene() {
//        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//        double screenWidth = screenSize.getWidth();
//
//        //set content layout
//        Button closeButton = new Button("exit");
//        closeButton.setOnAction(e -> exitApplication());
//        HBox bottomMenu = new HBox(closeButton);
//        bottomMenu.setMinHeight(50);
//        bottomMenu.setPadding(new Insets(10));
//        bottomMenu.setSpacing(20);
//        bottomMenu.setAlignment(Pos.CENTER);
//
//        displayFileTree(importDirectory);
//        VBox leftMenu = new VBox(currentModuleLabel, currentAssignmentLabel, currentStudentIDLabel, treeView);
//        leftMenu.setMinWidth(screenWidth * 0.15);
//        leftMenu.setPadding(new Insets(10));
//
//        VBox centreDisplay = new VBox(submissionInfoLabel, currentSubmissionDisplay);
//        centreDisplay.setMinWidth(screenWidth * 0.5);
//        centreDisplay.setPadding(new Insets(10));  //Insets(top, right, bottom, left)
//        ScrollPane scrollPane = new ScrollPane();
//        scrollPane.setContent(centreDisplay);
//
//        Label label4 = new Label("right menu");
//        VBox rightMenu = new VBox(label4);
//        rightMenu.setMinWidth(screenWidth * 0.2);
//        rightMenu.setPadding(new Insets(10));
//
//        BorderPane borderPane = new BorderPane();
//        borderPane.setTop(createMenuBar());
//        borderPane.setBottom(bottomMenu);
//        borderPane.setLeft(leftMenu);
//        borderPane.setCenter(scrollPane);
//        borderPane.setRight(rightMenu);
//
//        return new Scene(borderPane);
//    }

//    MenuBar createMenuBar() {
//        //Window menu bar
//        MenuBar menuBar = new MenuBar();
//        //File Menu
//        Menu fileMenu = new Menu("_File");
//        MenuItem openMenuItem = new MenuItem("_Open submission...");
//        FileChooser fileChooser = new FileChooser();
//        openMenuItem.setOnAction(e -> {
//            File file = fileChooser.showOpenDialog(stage);
//            if(file != null) {
//                currentSubmissionDisplay.setText(readFile(file));
//            }
//        });
//        MenuItem openMultipleMenuItem = new MenuItem("Open _multiple submissions...");
//        openMultipleMenuItem.setOnAction(e -> {
//            List<File> fileList = fileChooser.showOpenMultipleDialog(stage);
//            if(fileList != null) {
//                StringBuilder sb = new StringBuilder();
//                for (File file : fileList) {
//                    sb.append(readFile(file)).append("\n**********EOF**********\n\n");
//                }
//                currentSubmissionDisplay.setText(sb.toString());
//            }
//        });
//
//        MenuItem exitMenuItem = new MenuItem("_Exit");
//        exitMenuItem.setOnAction(e -> exitApplication());
//        fileMenu.getItems().addAll(openMenuItem, openMultipleMenuItem, new SeparatorMenuItem(), exitMenuItem);
//        menuBar.getMenus().addAll(fileMenu);
//
//        return menuBar;
//    }
//


}
