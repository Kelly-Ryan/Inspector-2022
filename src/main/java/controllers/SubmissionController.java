package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import models.InstructorModel;
import models.SubmissionModel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Arrays;
import java.util.Objects;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

public class SubmissionController {
    private InstructorModel instructor;
    private File importDirectory = new File("C:\\Users\\mcnei\\OneDrive - University of Limerick\\CS4617 FYP\\official documents\\Inspector\\assignments");
    private SubmissionModel currentSubmission;
    String[] rubric, marks;
    @FXML
    private Label username;
    @FXML
    private Text submissionDisplay;
    @FXML
    TreeView<File> treeView;
    @FXML
    private TextField criterion1MarkInput;
    @FXML
    private TextField criterion2MarkInput;
    @FXML
    private TextField criterion3MarkInput;
    @FXML
    private TextField criterion4MarkInput;
    @FXML
    private TextField criterion1NameInput;
    @FXML
    private TextField criterion2NameInput;
    @FXML
    private TextField criterion3NameInput;
    @FXML
    private TextField criterion4NameInput;
    @FXML
    private TextField criterion1Mark;
    @FXML
    private TextField criterion2Mark;
    @FXML
    private TextField criterion3Mark;
    @FXML
    private TextField criterion4Mark;
    @FXML
    private Label criterion1Name;
    @FXML
    private Label criterion2Name;
    @FXML
    private Label criterion3Name;
    @FXML
    private Label criterion4Name;
    @FXML
    private Label maxMarksLabel;
    @FXML
    private Label marksReceivedLabel;
    @FXML
    private TextArea feedbackTextArea;

    void setInstructor(InstructorModel instructor) {
        this.instructor = instructor;
        username.setText("Hello, " + instructor.getName());
    }

    @FXML
    void setImportDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(new Stage());
        if (directory != null) {
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
                    super.updateItem(file, empty);
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
                    if (treeItem.isLeaf()) {
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
            //read in source files and create records in ASSIGNMENT_SUBMISSION and SUBMISSION_FILES tables
            readFile(file);
        }
    }

    //submitted assignment files should be in parent directory named with student ID number
    //e.g. CS4123/week03/0347345/helloWorld.c
    void readFile(File file) {
        //split directories in filepath - "\" for Windows and "/" for Unix/Mac
        String[] splitFilepath = file.toString().split("[\\\\/]");
        String module = splitFilepath[splitFilepath.length - 4];
        String assignment = splitFilepath[splitFilepath.length - 3];
        String studentID = splitFilepath[splitFilepath.length - 2];
        String filename = splitFilepath[splitFilepath.length - 1];
        String submissionText = "";

        //read file text
        StringBuilder sb = new StringBuilder();
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();

            while (line != null) {
                sb.append(line).append("\n");
                line = br.readLine();
            }
            submissionText = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Connection conn = DatabaseController.dbConnect();
        String importSubmissions = "INSERT OR IGNORE INTO ASSIGNMENT_SUBMISSION (instructorId, moduleId, assignmentId, studentId, studentEmail) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(importSubmissions)) {
            pstmt.setString(1, instructor.getInstructorId());
            pstmt.setString(2, module);
            pstmt.setString(3, assignment);
            pstmt.setString(4, studentID);
            pstmt.setString(5, studentID + "@studentmail.ul.ie");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String importSubmissionFiles = " INSERT OR IGNORE INTO SUBMISSION_FILES (moduleId, assignmentId, studentId, filename, assignmentText) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(importSubmissionFiles)) {
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

    //displays submission text in SubmissionDisplay pane and creates Submission object to hold submission info
    String loadSubmission(TreeItem<File> treeItem, File file) {
        String studentIdDir = treeItem.getParent().getValue().toString();
        String assignmentDir = treeItem.getParent().getParent().getValue().toString();
        String moduleDir = treeItem.getParent().getParent().getParent().getValue().toString();
        String submissionText = "";

        Connection conn = DatabaseController.dbConnect();
        String getSubmission = "SELECT assignmentText FROM SUBMISSION_FILES WHERE moduleId = ? AND assignmentId = ? AND studentId = ? and filename = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(getSubmission)) {
            pstmt.setString(1, moduleDir);
            pstmt.setString(2, assignmentDir);
            pstmt.setString(3, studentIdDir);
            pstmt.setString(4, file.toString());
            ResultSet rs = pstmt.executeQuery();
            submissionText = rs.getString(1);
            conn.close();


        } catch (SQLException e) {
            e.printStackTrace();
        }

        createSubmissionObject(moduleDir, assignmentDir, studentIdDir);

        return submissionText;
    }

    void createSubmissionObject(String moduleId, String assignmentId, String studentId) {
        Connection conn = DatabaseController.dbConnect();
        String getSubmission = "SELECT * FROM ASSIGNMENT_SUBMISSION WHERE moduleId = ? AND assignmentId = ? AND studentId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(getSubmission)) {
            pstmt.setString(1, moduleId);
            pstmt.setString(2, assignmentId);
            pstmt.setString(3, studentId);
            ResultSet rs = pstmt.executeQuery();
            String gradingRubric = rs.getString(8);
            String marksReceived = rs.getString(9);
            double maxMarks = Double.parseDouble(rs.getString(10));
            double totalMarks = Double.parseDouble(rs.getString(11));
            String comments = rs.getString(12);
            conn.close();

            currentSubmission = new SubmissionModel(moduleId, assignmentId, studentId, gradingRubric,
                    marksReceived, maxMarks, totalMarks, comments);

            if (gradingRubric.equals("rubric not set")) {
                rubric = new String[8];
                Arrays.fill(rubric, "");
            } else {
                rubric = gradingRubric.split(",");
            }

            criterion1MarkInput.setText(rubric[0]);
            criterion1NameInput.setText(rubric[1]);
            criterion2MarkInput.setText(rubric[2]);
            criterion2NameInput.setText(rubric[3]);
            criterion3MarkInput.setText(rubric[4]);
            criterion3NameInput.setText(rubric[5]);
            criterion4MarkInput.setText(rubric[6]);
            criterion4NameInput.setText(rubric[7]);

            if (marksReceived.equals("marks not set")) {
                marks = new String[8];
                Arrays.fill(marks, "");
            } else {
                marks = marksReceived.split(",");
            }

            criterion1Mark.setText(marks[0]);
            criterion1Name.setText(marks[1]);
            criterion2Mark.setText(marks[2]);
            criterion2Name.setText(marks[3]);
            criterion3Mark.setText(marks[4]);
            criterion3Name.setText(marks[5]);
            criterion4Mark.setText(marks[6]);
            criterion4Name.setText(marks[7]);

            maxMarksLabel.setText("Max marks: " + maxMarks);
            marksReceivedLabel.setText("Total marks: " + totalMarks);
            feedbackTextArea.setText(comments);

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

        currentSubmission.setGradingRubric(criterion1MarkInput.getText() + "," + criterion1NameInput.getText() + "," +
                criterion2MarkInput.getText() + "," + criterion2NameInput.getText() + "," + criterion3MarkInput.getText() + "," +
                criterion3NameInput.getText() + "," + criterion4MarkInput.getText() + "," + criterion4NameInput.getText());

        handleNullMarks();
    }

    void handleNullMarks() {
        if (criterion1Mark.getText().isEmpty()) {
            criterion1Mark.setText("0");
        }

        if (criterion2Mark.getText().isEmpty()) {
            criterion2Mark.setText("0");
        }

        if (criterion3Mark.getText().isEmpty()) {
            criterion3Mark.setText("0");
        }

        if (criterion4Mark.getText().isEmpty()) {
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
                criterion3Mark.getText() + "," + criterion3Name.getText() + "," +
                criterion4Mark.getText() + "," + criterion4Name.getText());

        System.out.println(currentSubmission.getMarksReceived());

        Connection conn = DatabaseController.dbConnect();
        String insertSubmission = "UPDATE ASSIGNMENT_SUBMISSION SET gradingRubric = ?, marksReceived = ?, maxMarks = ?, totalMarks = ?, comments = ? " +
                "WHERE  moduleId = ? AND assignmentId = ? AND studentId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSubmission)) {
            pstmt.setString(1, currentSubmission.getGradingRubric());
            pstmt.setString(2, currentSubmission.getMarksReceived());
            pstmt.setString(3, Double.toString(currentSubmission.getMaxMarks()));
            pstmt.setString(4, Double.toString(currentSubmission.getTotalMarks()));
            pstmt.setString(5, currentSubmission.getComments());
            pstmt.setString(6, currentSubmission.getModuleId());
            pstmt.setString(7, currentSubmission.getAssignmentId());
            pstmt.setString(8, currentSubmission.getStudentId());
            pstmt.execute();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void export() {
        Connection conn = DatabaseController.dbConnect();
        String getSubmission = "SELECT * FROM ASSIGNMENT_SUBMISSION";
        try(Statement stmt = conn.createStatement()){
            ResultSet rs = stmt.executeQuery(getSubmission);

            // create CSV file
            BufferedWriter writer = Files.newBufferedWriter(Paths.get("data.csv"));

            // add headers to CSV file
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);
            csvPrinter.printHeaders(rs);

            // Add data rows to CSV file.
            while (rs.next()) {
                csvPrinter.printRecord(
                    rs.getString(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4),
                    rs.getString(5),
                    rs.getString(6),
                    rs.getString(7),
                    rs.getString(8),
                    rs.getString(9),
                    rs.getString(10),
                    rs.getString(11),
                    rs.getString(12));
            }
            conn.close();
            csvPrinter.flush();
            csvPrinter.close();

            // Message stating export successful.
            System.out.println("Data export successful.");

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

    }
}