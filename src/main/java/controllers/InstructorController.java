package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.InstructorModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class InstructorController {
    private InstructorModel instructor;
    InstructorController instructorController;
    @FXML private Text submissionDisplay;
    @FXML private Label username;
    @FXML private TreeView<File> treeView;

    private File importDirectory = new File("C:\\Users\\mcnei\\OneDrive - University of Limerick\\CS4617 FYP\\official documents\\Inspector\\assignments");

    void setupDashboard(InstructorController instructorController, String email){
        this.instructorController = instructorController;
        //sql query to initialize InstructorModel object
        Connection conn = DatabaseController.dbConnect();
        String sql = "SELECT instructorId, name FROM INSTRUCTOR WHERE email = ?";
        //String sql = "SELECT name, moduleCode, moduleName FROM INSTRUCTOR JOIN MODULE M on INSTRUCTOR.instructorId = M.instructorId WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            int instructorId = Integer.parseInt(rs.getString(1));
            String name = rs.getString(2);
            conn.close();

            instructor = new InstructorModel(instructorId, name, email);
            username.setText("Hello, " + instructor.getName());

            displayFileTree(treeView);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void setImportDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(new Stage());
        if(directory != null) {
            importDirectory = directory;
        }
        //load module directories from newly selected import directory
        displayFileTree(treeView);
    }

    void displayFileTree(TreeView<File> treeView) {
        //create root item
        TreeItem<File> rootItem = new TreeItem<>(importDirectory);
        //hide root item of treeView
        treeView.setShowRoot(false);
        System.out.println(rootItem.getValue().toString());
        treeView.setRoot(rootItem);

        treeView.setCellFactory(tv -> {
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
                    //display submission on click
                    TreeItem<File> treeItem = cell.getTreeItem();
                    File f = treeItem.getValue();
                    if (!f.isDirectory()) {
                        //TODO set submissionDisplay from Submission table
                        String studentIdDir = treeItem.getParent().getValue().toString();
                        String assignmentDir = treeItem.getParent().getParent().getValue().toString();
                        String moduleDir = treeItem.getParent().getParent().getParent().getValue().toString();

                        System.out.println(f);
                        System.out.println(studentIdDir);
                        System.out.println(assignmentDir);
                        System.out.println(moduleDir);

                        Connection conn = DatabaseController.dbConnect();
                        String getSubmission = "SELECT assignmentText FROM ASSIGNMENT_SUBMISSION WHERE assignmentId = ? AND moduleId = ? AND studentId = ? and filename = ?";
                        try(PreparedStatement pstmt = conn.prepareStatement(getSubmission)){
                            pstmt.setString(1, assignmentDir);
                            pstmt.setString(2, moduleDir);
                            pstmt.setString(3, studentIdDir);
                            pstmt.setString(4, f.toString());
                            ResultSet rs = pstmt.executeQuery();
                            submissionDisplay.setText(rs.getString(1));
                            conn.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            return cell;
        });

        //get file list from importDirectory
        File[] fileList = importDirectory.listFiles();

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
        } else {
            //read in source files and create records in Submission table
            readFile(file);
        }
    }

    //submitted assignment files should be in parent directory named with student ID number
    //e.g. CS4123/week03/0347345/helloWorld.c
    void readFile(File file) {
        String filepath = file.toString();
        //split directories in filepath - "\" for Windows and "/" for Unix/Mac
        String[] splitFilepath = file.toString().split("[\\\\/]");
        String module = splitFilepath[splitFilepath.length-4];
        String assignment = splitFilepath[splitFilepath.length-3];
        String studentID = splitFilepath[splitFilepath.length-2];
        String filename = splitFilepath[splitFilepath.length-1];
        String submission = "";

        System.out.println(filepath);

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
            submission = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO SQL insert records
        Connection conn = DatabaseController.dbConnect();
        String insertSubmission = "INSERT INTO ASSIGNMENT_SUBMISSION (assignmentId, moduleId, studentId, filename, assignmentText) VALUES (?, ?, ?, ?, ?)";
        try(PreparedStatement pstmt = conn.prepareStatement(insertSubmission)){
            pstmt.setString(1, assignment);
            pstmt.setString(2, module);
            pstmt.setString(3, studentID);
            pstmt.setString(4, filename);
            pstmt.setString(5, submission);
            pstmt.execute();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void importSubmission() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());
        if(file != null) {
            //submissionDisplay.setText(readFile(file));
        }
    }

    @FXML
    void importMultipleSubmissions() {
        FileChooser fileChooser = new FileChooser();
        List<File> fileList = fileChooser.showOpenMultipleDialog(new Stage());
        if(fileList != null) {
            StringBuilder sb = new StringBuilder();
            for (File file : fileList) {
                //sb.append(readFile(file)).append("\n**********EOF**********\n\n");
            }
            submissionDisplay.setText(sb.toString());
        }
    }

    @FXML
    void addModule() {
        System.out.println("add module ");
        //dialog with forms
        //pass form data to instructor.addModule() - update SQL and reload module view
    }


    @FXML
    void exitApplication(){
        DialogController dialogController = new DialogController();
        dialogController.displayDialog(dialogController.closeProgram());
    }
}
