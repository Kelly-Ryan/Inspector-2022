import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class Main extends Application {
    private static Connection conn = null;

    public static void main(String[] args) {
        dbSetup();
        //launch goes into Application and calls start()
        launch(args);
    }

    public static void dbSetup() {
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

    static Connection dbConnect() {
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

    static String readFile() {
        //submitted assignment files should be in parent directory named with student ID number
        File file = new File("assignments/CS4023/week03/0347345/copyFile.c");
        //split directories in filepath - "\" for Windows and "/" for Unix/Mac
        String[] filepath = file.getParentFile().toString().split("[\\\\/]");
        String moduleCode = filepath[filepath.length-3];
        //assignmentName could be a week number, e.g. labs
        String assignmentName = filepath[filepath.length-2];
        //parent directory of source files named with student ID
        String studentID = filepath[filepath.length-1];

        //DEBUG
        for(String s : filepath){
            System.out.print(s + " ");
        }
        System.out.println("\nModule Code: " + moduleCode);
        System.out.println("Assignment: " + assignmentName);
        System.out.println("Student ID: " + studentID);

        StringBuilder sb = new StringBuilder();

        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();

            while(line != null) {
                sb.append(line).append("\n");
                line = br.readLine();
            }


            //String fileText = sb.toString();
            //display file in text area
            //System.out.println(fileText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    //entry point for JavaFX application
    @Override
    public void start(Stage stage) {
        stage.setMaximized(true);
        stage.setTitle("Inspector");
        stage.setOnCloseRequest(e -> {
            e.consume();
            exitApplication();
        });

        //Menu
        Menu fileMenu = new Menu("File");
        fileMenu.getItems().add(new MenuItem("Import Files..."));
        fileMenu.getItems().add(new MenuItem("Export Grades..."));
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(new MenuItem("Settings..."));
        fileMenu.getItems().add(new SeparatorMenuItem());

        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(e -> exitApplication());
        fileMenu.getItems().add(exitMenuItem);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu);

        HBox bottomMenu = new HBox();
        bottomMenu.setMinHeight(100);
        Label label2 = new Label();
        Button closeButton = new Button("exit");
        closeButton.setOnAction(e -> exitApplication());
        bottomMenu.getChildren().addAll(label2, closeButton);
        bottomMenu.setAlignment(Pos.CENTER);

        VBox leftMenu = new VBox();
        leftMenu.setMinWidth(200);
        Label moduleCodeLabel = new Label("CS4023");
        Label assignmentNameLabel = new Label("week03");
        Label studentIDLabel = new Label("0347345");
        leftMenu.getChildren().addAll(moduleCodeLabel, assignmentNameLabel, studentIDLabel);

        VBox rightMenu = new VBox();
        rightMenu.setMinWidth(200);
        Label label4 = new Label("right menu");
        rightMenu.getChildren().add(label4);

        VBox centreDisplay = new VBox();
        Label displayAssignment = new Label(readFile());
        centreDisplay.getChildren().add(displayAssignment);

        BorderPane borderPane = new BorderPane();
        //Insets(top, right, bottom, left)
        borderPane.setPadding(new Insets(0, 10, 10, 10 ));
        borderPane.setTop(menuBar);
        borderPane.setBottom(bottomMenu);
        borderPane.setLeft(leftMenu);
        borderPane.setRight(rightMenu);
        borderPane.setCenter(centreDisplay);

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.show();
    }

    void exitApplication() {
        Boolean instruction = DialogBox.display("Exit Application", "Are you sure you want to exit Inspector?");
        if(instruction) {
            Platform.exit();
        }
    }
}
