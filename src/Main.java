import java.awt.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Main extends Application {
    private static Connection conn = null;
    //user should be able to set this at initial set up and amend it when required
    File importDirectory = new File("C:\\Users\\mcnei\\OneDrive - University of Limerick\\CS4617 FYP\\official documents\\Inspector\\assignments");
    Stage stage = new Stage();
    Text currentSubmissionDisplay = new Text();

    String currentModule, currentAssignment, currentStudentID;
    Label currentModuleLabel = new Label();
    Label currentAssignmentLabel = new Label();
    Label currentStudentIDLabel = new Label();
    Label submissionInfoLabel = new Label();

    TreeView<File> treeView = new TreeView<>();

    public static void main(String[] args) {
        //launch goes into Application and calls start()
        launch(args);
    }

    void displayFileTree(File inputDirectoryLocation) {
        //create root item
        TreeItem<File> rootItem = new TreeItem<>(inputDirectoryLocation);
        //hide root item of treeview
        treeView.setShowRoot(false);
        treeView.setRoot(rootItem);

        //create cell factory to render tree cells
        //treeView.setCellFactory(CheckBoxTreeCell.forTreeView());

        treeView.setCellFactory(treeView -> {
            TreeCell<File> cell = new TreeCell<>() {
                @Override
                public void updateItem(File file, boolean empty) {
                    super.updateItem(file, empty) ;
                    if (empty) {
                        setText(null);
                    } else {
                        setText(file.getName());
                    }
                }
            };
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    //TODO display submission on click
                    TreeItem<File> treeItem = cell.getTreeItem();
                    System.out.println(treeItem.getValue());
                }
            });
            return cell ;
        });

        //get file list from importDirectory
        File[] fileList = inputDirectoryLocation.listFiles();

        //populate tree
        assert fileList != null;
        for (File file : fileList) {
            createFileTree(file, rootItem);
        }
    }

    void createFileTree(File file, TreeItem<File> parent) {
        //create a new tree item with the file name and add it to parent
        TreeItem<File> fileItem = new TreeItem<>(new File(file.getName()));
        parent.getChildren().add(fileItem);
        //if this file is a directory then call this method on each file inside the directory
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                createFileTree(f, fileItem);
            }
        }
    }

    //entry point for JavaFX application
    @Override
    public void start(Stage stage) {
        //initialize database
        dbSetup();
        //set up JavaFX stage/window
            stage.getIcons().add(new Image("resources/images/inspector_logo.png")); //set application icon
        stage.setMaximized(true);
        stage.setTitle("Inspector");
        stage.setOnCloseRequest(e -> {
            e.consume();
            exitApplication();
        });

        Scene scene = new Scene(createSubmissionView());
        stage.setScene(scene);
        stage.show();
    }

    void dbSetup() {
        try{
            conn = dbConnect();

            //DEBUG
            System.out.println("database connection successful");

            Statement stmt = conn.createStatement();
            //define database tables
            String sql = "CREATE TABLE IF NOT EXISTS INSTRUCTOR (" +
                        "instructorId   INT AUTO_INCREMENT," +
                        "name           VARCHAR(100) NOT NULL," +
                        "email          VARCHAR(100) NOT NULL," +
                        "password       VARCHAR(20)  NOT NULL," +
                        "PRIMARY KEY (instructorId)," +
                        "UNIQUE (email)" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS MODULE (" +
                        "moduleId      INT  AUTO_INCREMENT," +
                        "moduleCode    VARCHAR(6)  NOT NULL," +
                        "moduleName    VARCHAR(50) NOT NULL," +
                        "instructorId  INT(10)     NOT NULL REFERENCES INSTRUCTOR(instructorId)," +
                        "PRIMARY KEY (moduleId)" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS STUDENT (" +
                        "studentId  VARCHAR(8)      NOT NULL," +
                        "PRIMARY KEY (studentId)" +
                        "); "+
                        "CREATE TABLE IF NOT EXISTS ASSIGNMENT (" +
                        "assignmentId   INT AUTO_INCREMENT," +
                        "moduleId       INT(10) NOT NULL REFERENCES MODULE(moduleId)," +
                        "academicYear   YEAR    NOT NULL," +
                        "semester       INT(1)  NOT NULL," +
                        "PRIMARY KEY (assignmentId)" +
                        ");" +
                        "CREATE TABLE IF NOT EXISTS ASSIGNMENT_SUBMISSION (" +
                        "assignmentId   INT(10) NOT NULL REFERENCES ASSIGNMENT(assignmentId)," +
                        "moduleId       INT(10) NOT NULL REFERENCES MODULE(moduleId)," +
                        "studentId      VARCHAR(8)  NOT NULL REFERENCES STUDENT(studentId)," +
                        "maxMarks       FLOAT," +
                        "receivedMarks  FLOAT," +
                        "assignmentText MEDIUMTEXT," +
                        "comments       VARCHAR(1000)," +
                        "PRIMARY KEY (assignmentId)" +
                        ");";
            //create database tables
            stmt.executeUpdate(sql);

            //DEBUG
            System.out.println("tables successfully created");

            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    Connection dbConnect() {
        try {
            Class.forName("org.sqlite.JDBC");
            //create database if it does not already exist and connect to it
            conn = DriverManager.getConnection("jdbc:sqlite:src/db/inspector.db");
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return conn;
    }

    MenuBar createMenuBar() {
        //Window menu bar
        MenuBar menuBar = new MenuBar();
        //File Menu
        Menu fileMenu = new Menu("_File");
        MenuItem openMenuItem = new MenuItem("_Open submission...");
        FileChooser fileChooser = new FileChooser();
        openMenuItem.setOnAction(e -> {
            File file = fileChooser.showOpenDialog(stage);
            if(file != null) {
                currentSubmissionDisplay.setText(readFile(file));
            }
        });
        MenuItem openMultipleMenuItem = new MenuItem("Open _multiple submissions...");
        openMultipleMenuItem.setOnAction(e -> {
            List<File> fileList = fileChooser.showOpenMultipleDialog(stage);
            if(fileList != null) {
                StringBuilder sb = new StringBuilder();
                for (File file : fileList) {
                    sb.append(readFile(file)).append("\n**********EOF**********\n\n");
                }
                currentSubmissionDisplay.setText(sb.toString());
            }
        });

        MenuItem exitMenuItem = new MenuItem("_Exit");
        exitMenuItem.setOnAction(e -> exitApplication());
        fileMenu.getItems().addAll(openMenuItem, openMultipleMenuItem, new SeparatorMenuItem(), exitMenuItem);
        menuBar.getMenus().addAll(fileMenu);

        return menuBar;
    }

    BorderPane createSubmissionView() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.getWidth();

        //set content layout
        HBox bottomMenu = new HBox();
        bottomMenu.setMinHeight(50);
        bottomMenu.setPadding(new Insets(10));
        Label label2 = new Label();
        Button closeButton = new Button("exit");
        closeButton.setOnAction(e -> exitApplication());
        bottomMenu.getChildren().addAll(label2, closeButton);
        bottomMenu.setAlignment(Pos.CENTER);

        VBox leftMenu = new VBox();
        leftMenu.setMinWidth(screenWidth * 0.15);
        leftMenu.setPadding(new Insets(10));
        displayFileTree(importDirectory);
        leftMenu.getChildren().addAll(currentModuleLabel, currentAssignmentLabel, currentStudentIDLabel, treeView);

        VBox centreDisplay = new VBox();
        centreDisplay.setMinWidth(screenWidth * 0.5);
        centreDisplay.setPadding(new Insets(10));  //Insets(top, right, bottom, left)
        centreDisplay.getChildren().addAll(submissionInfoLabel, currentSubmissionDisplay);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(centreDisplay);

        VBox rightMenu = new VBox();
        rightMenu.setMinWidth(screenWidth * 0.2);
        rightMenu.setPadding(new Insets(10));
        Label label4 = new Label("right menu");
        rightMenu.getChildren().add(label4);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(createMenuBar());
        borderPane.setBottom(bottomMenu);
        borderPane.setLeft(leftMenu);
        borderPane.setCenter(scrollPane);
        borderPane.setRight(rightMenu);

        return borderPane;
    }

    //submitted assignment files should be in parent directory named with student ID number
    String readFile(File file) {
        //split directories in filepath - "\" for Windows and "/" for Unix/Mac
        String[] filepath = file.getParentFile().toString().split("[\\\\/]");
        currentModule = filepath[filepath.length-3];
        //assignmentName could be a week number, e.g. labs
        currentAssignment = filepath[filepath.length-2];
        //parent directory of source files named with student ID
        currentStudentID = filepath[filepath.length-1];

        //set labels
        currentModuleLabel.setText("Module: " + currentModule);
        currentAssignmentLabel.setText("Assignment: " + currentAssignment);
        currentStudentIDLabel.setText("Student ID: " + currentStudentID);
        submissionInfoLabel.setText(currentModule + "\t/\t" + currentAssignment + "\t/\t" + currentStudentID + "    \n\n");

        //read file text
        StringBuilder sb = new StringBuilder();
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();

            while(line != null) {
                sb.append(line).append("\n");
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    void exitApplication() {
        Boolean instruction = DialogBox.display("Exit Application", "Are you sure you want to exit Inspector?");
        if(instruction) {
            Platform.exit();
        }
    }
}
