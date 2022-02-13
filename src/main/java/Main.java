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
    //user should be able to set this at initial set up and amend it when required
//    private final File importDirectory = new File("C:\\Users\\mcnei\\OneDrive - University of Limerick\\CS4617 FYP\\official documents\\assignments");
//    private final Stage stage = new Stage();
//    private final Text currentSubmissionDisplay = new Text();
//    private final Label currentModuleLabel = new Label();
//    private final Label currentAssignmentLabel = new Label();
//    private final Label currentStudentIDLabel = new Label();
//    private final Label submissionInfoLabel = new Label();
//    private final TreeView<File> treeView = new TreeView<>();

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
//    void displayFileTree(File inputDirectoryLocation) {
//        //create root item
//        TreeItem<File> rootItem = new TreeItem<>(inputDirectoryLocation);
//        //hide root item of treeview
//        treeView.setShowRoot(false);
//        treeView.setRoot(rootItem);
//
//        //create cell factory to render tree cells
//        //treeView.setCellFactory(CheckBoxTreeCell.forTreeView());
//
//        treeView.setCellFactory(treeView -> {
//            TreeCell<File> cell = new TreeCell<>() {
//                @Override
//                public void updateItem(File file, boolean empty) {
//                    super.updateItem(file, empty) ;
//                    if (empty) {
//                        setText(null);
//                    } else {
//                        setText(file.getName());
//                    }
//                }
//            };
//            cell.setOnMouseClicked(event -> {
//                if (!cell.isEmpty()) {
//                    //display submission on click
//                    TreeItem<File> treeItem = cell.getTreeItem();
//                    System.out.println(treeItem.getValue());
//                }
//            });
//            return cell ;
//        });
//
//        //get file list from importDirectory
//        File[] fileList = inputDirectoryLocation.listFiles();
//
//        //populate tree
//        assert fileList != null;
//        for (File file : fileList) {
//            createFileTree(file, rootItem);
//        }
//    }
//
//    void createFileTree(File file, TreeItem<File> parent) {
//        //create a new tree item with the file name and add it to parent
//        TreeItem<File> fileItem = new TreeItem<>(new File(file.getName()));
//        parent.getChildren().add(fileItem);
//        //if this file is a directory then call this method on each file inside the directory
//        if (file.isDirectory()) {
//            for (File f : Objects.requireNonNull(file.listFiles())) {
//                createFileTree(f, fileItem);
//            }
//        }
//    }
//
//    //submitted assignment files should be in parent directory named with student ID number
//    String readFile(File file) {
//        //split directories in filepath - "\" for Windows and "/" for Unix/Mac
//        String[] filepath = file.getParentFile().toString().split("[\\\\/]");
//        String currentModule = filepath[filepath.length - 3];
//        //assignmentName could be a week number, e.g. labs
//        String currentAssignment = filepath[filepath.length - 2];
//        //parent directory of source files named with student ID
//        String currentStudentID = filepath[filepath.length - 1];
//
//        //set labels
//        currentModuleLabel.setText("Module: " + currentModule);
//        currentAssignmentLabel.setText("Assignment: " + currentAssignment);
//        currentStudentIDLabel.setText("Student ID: " + currentStudentID);
//        submissionInfoLabel.setText(currentModule + "\t/\t" + currentAssignment + "\t/\t" + currentStudentID + "    \n\n");
//
//        //read file text
//        StringBuilder sb = new StringBuilder();
//        try {
//            FileReader fr = new FileReader(file);
//            BufferedReader br = new BufferedReader(fr);
//            String line = br.readLine();
//
//            while(line != null) {
//                sb.append(line).append("\n");
//                line = br.readLine();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return sb.toString();
//    }

}
