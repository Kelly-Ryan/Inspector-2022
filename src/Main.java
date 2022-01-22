import java.nio.file.FileSystems;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class Main extends Application {
    private static Connection conn = null;

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

    //entry point for JavaFX application
    @Override
    public void start(Stage stage) {
        GridPane gridPane = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(25);
        gridPane.getColumnConstraints().addAll(col1,col2,col3);

        Scene scene = new Scene(gridPane);
        stage.setTitle("Inspector");
        stage.setMaximized(true);
        //scene.getStylesheets().add("http://font.samples/web?family=samples")
        stage.setScene(scene);
        stage.show();
    }

    static void readFile() {
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

        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while(line != null) {
                sb.append(line).append("\n");
                line = br.readLine();
            }
            String fileText = sb.toString();

            //display file in text area
            //System.out.println(fileText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        dbSetup();
        readFile();

        //launch standalone application - use Platform.exit() to stop application
        //launch();
    }
}
