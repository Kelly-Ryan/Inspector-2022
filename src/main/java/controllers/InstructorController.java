package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import models.InstructorModel;
import models.SubmissionModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class InstructorController {
    InstructorController instructorController;
    private InstructorModel instructor;
    private SubmissionModel currentSubmission;
    @FXML private Text submissionDisplay;
    @FXML private Label username;
    @FXML private TreeView<File> treeView;
    @FXML private TextField criterion1MarkInput;
    @FXML private TextField criterion2MarkInput;
    @FXML private TextField criterion3MarkInput;
    @FXML private TextField criterion4MarkInput;
    @FXML private TextField criterion1NameInput;
    @FXML private TextField criterion2NameInput;
    @FXML private TextField criterion3NameInput;
    @FXML private TextField criterion4NameInput;
    @FXML private TextField criterion1Mark;
    @FXML private TextField criterion2Mark;
    @FXML private TextField criterion3Mark;
    @FXML private TextField criterion4Mark;
    @FXML private Label criterion1Name;
    @FXML private Label criterion2Name;
    @FXML private Label criterion3Name;
    @FXML private Label criterion4Name;
    @FXML private Label maxMarksLabel;
    @FXML private Label marksReceivedLabel;
    @FXML private TextArea feedbackTextArea;

    private File importDirectory = new File("C:\\Users\\mcnei\\OneDrive - University of Limerick\\CS4617 FYP\\official documents\\Inspector\\assignments");

    void setupDashboard(InstructorController instructorController, String email){
        this.instructorController = instructorController;

        //sql query to initialize InstructorModel object
        Connection conn = DatabaseController.dbConnect();
        String sql = "SELECT instructorId, name FROM INSTRUCTOR WHERE email = ?";
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
                    //display submission text when filename is clicked
                    TreeItem<File> treeItem = cell.getTreeItem();
                    File file = treeItem.getValue();
                    if (!file.isDirectory()) {
                        submissionDisplay.setText(loadSubmission(treeItem, file));
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

    //displays submission text in SubmissionDisplay pane and creates Submission object to hold submission info
    String loadSubmission(TreeItem<File> treeItem,File file) {
        String studentIdDir = treeItem.getParent().getValue().toString();
        String assignmentDir = treeItem.getParent().getParent().getValue().toString();
        String moduleDir = treeItem.getParent().getParent().getParent().getValue().toString();
        String submissionText = "";

        Connection conn = DatabaseController.dbConnect();
        String getSubmission = "SELECT assignmentText FROM SUBMISSION_FILES WHERE moduleId = ? AND assignmentId = ? AND studentId = ? and filename = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(getSubmission)){
            pstmt.setString(1, moduleDir);
            pstmt.setString(2, assignmentDir);
            pstmt.setString(3, studentIdDir);
            pstmt.setString(4, file.toString());
            ResultSet rs = pstmt.executeQuery();
            submissionText = rs.getString(1);
            conn.close();

            //TODO this exception is triggering when module, assignment or studentId are clicked
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //TODO SQL query to get all fields matching moduleDir, assignmentDir, studentIdDir and set text to allow for review of already graded submissions
       createSubmissionObject(moduleDir, assignmentDir, studentIdDir);

        return submissionText;
    }

    void createSubmissionObject(String moduleId, String assignmentId, String studentId) {
        Connection conn = DatabaseController.dbConnect();
        String getSubmission = "SELECT * FROM ASSIGNMENT_SUBMISSION WHERE moduleId = ? AND assignmentId = ? AND studentId = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(getSubmission)){
            pstmt.setString(1, moduleId);
            pstmt.setString(2, assignmentId);
            pstmt.setString(3, studentId);
            ResultSet rs = pstmt.executeQuery();
            String gradingRubric = rs.getString(7);
            String marksReceived = rs.getString(8);
            double maxMarks = Double.parseDouble(rs.getString(9));
            double totalMarks = Double.parseDouble(rs.getString(10));
            String comments = rs.getString(11);
            conn.close();

            currentSubmission = new SubmissionModel(moduleId, assignmentId, studentId, gradingRubric,
                    marksReceived, comments, maxMarks, totalMarks);

            //TODO set values of textfields from object fields

        } catch (SQLException e) {
            e.printStackTrace();
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
        //split directories in filepath - "\" for Windows and "/" for Unix/Mac
        String[] splitFilepath = file.toString().split("[\\\\/]");
        String module = splitFilepath[splitFilepath.length-4];
        String assignment = splitFilepath[splitFilepath.length-3];
        String studentID = splitFilepath[splitFilepath.length-2];
        String filename = splitFilepath[splitFilepath.length-1];
        String submissionText = "";

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
            submissionText = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Connection conn = DatabaseController.dbConnect();
        String importSubmissions = "INSERT OR REPLACE INTO ASSIGNMENT_SUBMISSION (instructorId, moduleId, assignmentId, studentId) VALUES (?, ?, ?, ?);";
        try(PreparedStatement pstmt = conn.prepareStatement(importSubmissions)){
            pstmt.setString(1, instructor.getInstructorId());
            pstmt.setString(2, module);
            pstmt.setString(3, assignment);
            pstmt.setString(4, studentID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String importSubmissionFiles = "INSERT OR REPLACE INTO SUBMISSION_FILES (moduleId, assignmentId, studentId, filename, assignmentText) VALUES (?, ?, ?, ?, ?);";
        try(PreparedStatement pstmt = conn.prepareStatement(importSubmissionFiles)){
            pstmt.setString(1, module);
            pstmt.setString(2, assignment);
            pstmt.setString(3, studentID);
            pstmt.setString(4, filename);
            pstmt.setString(5, submissionText);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void setGradingRubric() {
        //this will eventually be a loop to go through a list of dynamically created criteria fields but for now
        //it is hardcoded to four

        currentSubmission.setMaxMarks(Double.parseDouble(criterion1MarkInput.getText()) +
                Double.parseDouble(criterion2MarkInput.getText()) +
                Double.parseDouble(criterion3MarkInput.getText()) +
                Double.parseDouble(criterion4MarkInput.getText()));

        criterion1Name.setText("/" + criterion1MarkInput.getText() + " " + criterion1NameInput.getText());
        criterion2Name.setText("/" + criterion2MarkInput.getText() + " " + criterion2NameInput.getText());
        criterion3Name.setText("/" + criterion3MarkInput.getText() + " " + criterion3NameInput.getText());
        criterion4Name.setText("/" + criterion4MarkInput.getText() + " " + criterion4NameInput.getText());
        maxMarksLabel.setText("Max marks: " + currentSubmission.getMaxMarks());

        currentSubmission.setGradingRubric(criterion1MarkInput.getText() + ","+ criterion1NameInput.getText() + "," +
                criterion2MarkInput.getText() + "," + criterion2NameInput.getText() + "," + criterion3MarkInput + "," +
                criterion3NameInput.getText() + "," + criterion4MarkInput + "," + criterion4NameInput);

        handleNullMarks();
    }

    void handleNullMarks() {
        if(criterion1Mark.getText().isEmpty()) {
            criterion1Mark.setText("0");
        }

        if(criterion2Mark.getText().isEmpty()) {
            criterion2Mark.setText("0");
        }

        if(criterion3Mark.getText().isEmpty()) {
            criterion3Mark.setText("0");
        }

        if(criterion4Mark.getText().isEmpty()) {
            criterion4Mark.setText("0");
        }
    }

    @FXML
    void updateTotalMarks() {
        currentSubmission.setTotalMarks(Double.parseDouble(criterion1Mark.getText()) +
                Double.parseDouble(criterion2Mark.getText()) +
                Double.parseDouble(criterion3Mark.getText()) +
                Double.parseDouble(criterion4Mark.getText()));

        marksReceivedLabel.setText("Total marks: " + currentSubmission.getTotalMarks());
    }

    @FXML
    void saveMarks() {
        currentSubmission.setComments(feedbackTextArea.getText());

        currentSubmission.setMarksReceived(criterion1Mark.getText() + "," + criterion1Name.getText() + "," +
                        criterion2Mark.getText() + "," + criterion2Name.getText() + "," +
                        criterion3Mark.getText()  + "," +  criterion3Name.getText()  + "," +
                criterion4Mark.getText() + "," + criterion4Name.getText());

        System.out.println(currentSubmission.getMarksReceived());

        Connection conn = DatabaseController.dbConnect();
        String insertSubmission = "UPDATE ASSIGNMENT_SUBMISSION SET gradingRubric = ?, marksReceived = ?, maxMarks = ?, totalMarks = ?, comments = ? " +
                "WHERE  moduleId = ? AND assignmentId = ? AND studentId = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(insertSubmission)){
            pstmt.setString(1, currentSubmission.getGradingRubric());
            pstmt.setString(2, currentSubmission.getMarksReceived());
            pstmt.setString(3, Double.toString(currentSubmission.getMaxMarks()));
            pstmt.setString(4, Double.toString(currentSubmission.getTotalMarks()));
            pstmt.setString(5, currentSubmission.getComments());
            pstmt.setString(6, currentSubmission.getModuleId() );
            pstmt.setString(7, currentSubmission.getAssignmentId());
            pstmt.setString(8, currentSubmission.getStudentId());
            pstmt.execute();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void exitApplication(){
        DialogController dialogController = new DialogController();
        dialogController.displayDialog(dialogController.closeProgram());
    }
}
