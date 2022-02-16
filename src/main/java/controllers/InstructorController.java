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

//    @FXML private MenuItem importSubmissionMenuItem;
//    @FXML private MenuItem importMultiSubmissionsMenuItem;
    @FXML private Text submissionDisplay;
    @FXML private Label username;
    @FXML private TreeView<File> treeView;
    @FXML private TitledPane titledPane1;

    private File importDirectory = new File("C:\\Users\\mcnei\\OneDrive - University of Limerick\\CS4617 FYP\\official documents\\Inspector\\assignments");

    void setupDashboard(String email){
        //sql query to initialize InstructorModel object
        Connection conn = DatabaseController.dbConnect();
        String sql = "SELECT instructorId, name FROM INSTRUCTOR WHERE email = ?";
        //String sql = "SELECT name, moduleCode, moduleName FROM INSTRUCTOR JOIN MODULE M on INSTRUCTOR.instructorId = M.instructorId WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            int instructorId = Integer.parseInt(rs.getString(1));
            String name = rs.getString(2);

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
                        //set submissionDisplay text to file text
                        System.out.println(treeItem.getValue());
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
            //call readFile() and insert sql records from there
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

    @FXML
    void importSubmission() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(new Stage());
        if(file != null) {
            submissionDisplay.setText(readFile(file));
        }
    }

    @FXML
    void importMultipleSubmissions() {
        FileChooser fileChooser = new FileChooser();
        List<File> fileList = fileChooser.showOpenMultipleDialog(new Stage());
        if(fileList != null) {
            StringBuilder sb = new StringBuilder();
            for (File file : fileList) {
                sb.append(readFile(file)).append("\n**********EOF**********\n\n");
            }
            submissionDisplay.setText(sb.toString());
        }
    }

    //submitted assignment files should be in parent directory named with student ID number
    String readFile(File file) {
        //split directories in filepath - "\" for Windows and "/" for Unix/Mac
        String[] filepath = file.getParentFile().toString().split("[\\\\/]");
        String module = filepath[filepath.length-3];
        //assignmentName could be a week number, e.g. labs
        String assignment = filepath[filepath.length-2];
        //parent directory of source files named with student ID
        String studentID = filepath[filepath.length-1];

        titledPane1.setText(module);

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

        //SQL insert records

        return sb.toString();
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
