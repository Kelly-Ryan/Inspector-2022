package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import models.AlertModel;
import models.DialogModel;
import models.InstructorModel;
import models.SubmissionModel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

public class SubmissionController {
    AlertController alertController = new AlertController();
    DialogController dialogController = new DialogController();
    private InstructorModel instructor;
    public File importDirectory;
    private SubmissionModel currentSubmission;
    private CodeArea codeArea;
    private final List<HBox> criteriaList = new ArrayList<>();        //stores rubric info
    private final List<TextField> marksList = new ArrayList<>();      //used by updateTotalMarks()
    @FXML
    private Label username;
    @FXML
    private ScrollPane sourceCodeScrollPane;
    @FXML
    TreeView<File> treeView;
    @ FXML
    VBox rubricVBox;
    @FXML
    VBox marksVBox;
    @FXML
    private Label maxMarksLabel;
    @FXML
    private Label marksReceivedLabel;
    @FXML
    private TextArea commentsTextArea;
    @FXML
    private TextField moduleCodeTextField;
    @FXML
    private TextField assignmentCodeTextField;

    void setInstructor(InstructorModel instructor) {
        this.instructor = instructor;
        username.setText("Hello, " + instructor.getName());

        if(instructor.getImportDirectory() == null) {
            importDirectory = new File("");
        } else {
            importDirectory = new File(instructor.getImportDirectory());
        }
    }

    @FXML
    void setImportDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(new Stage());
        if (directory != null) {
            importDirectory = directory;
            //TODO write to DB

            Connection conn = DatabaseController.dbConnect();
            String sql = "UPDATE INSTRUCTOR SET importDirectory = ? where email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, importDirectory.toString());
                pstmt.setString(2, instructor.getEmail());
                pstmt.execute();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }


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
                        codeArea.clear();
                        codeArea.replaceText(0, 0, loadSubmission(treeItem, file));
                    }
                }
            });
            return cell;
        });

        //get file list from importDirectory
        File[] fileList = importDirectory.listFiles();

        try {
            //populate tree
            assert fileList != null;
            for (File file : fileList) {
                createFileTree(file, rootItem);
            }
        } catch (RuntimeException e) {
            alertController.displayAlert(new AlertModel("Import Directory", "Import directory not set." +
                    "\nSelect an import directory from\nFile > set import directory"));
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

        String importSubmissionFiles = " INSERT OR IGNORE INTO SUBMISSION_FILES (instructorId, moduleId, assignmentId, studentId, filename, assignmentText) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(importSubmissionFiles)) {
            pstmt.setString(1, instructor.getInstructorId());
            pstmt.setString(2, module);
            pstmt.setString(3, assignment);
            pstmt.setString(4, studentID);
            pstmt.setString(5, filename);
            pstmt.setString(6, submissionText);
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void setUpSourceCodeDisplay() {
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea = new CodeArea();
        codeArea.setEditable(false);
        codeArea.setMinWidth(1200);
        codeArea.setMinHeight(700);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        sourceCodeScrollPane.setContent(codeArea);
    }

    //displays submission text, grading rubric and saved marks and creates Submission object to hold submission info
    String loadSubmission(TreeItem<File> treeItem, File file) {
        criteriaList.clear();
        marksList.clear();

        String studentIdDir = treeItem.getParent().getValue().toString();
        String assignmentDir = treeItem.getParent().getParent().getValue().toString();
        String moduleDir = treeItem.getParent().getParent().getParent().getValue().toString();
        String submissionText = "";

        Connection conn = DatabaseController.dbConnect();
        String getSubmission = "SELECT assignmentText FROM SUBMISSION_FILES WHERE instructorId = ? AND moduleId = ? AND assignmentId = ? AND studentId = ? and filename = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(getSubmission)) {
            pstmt.setString(1, instructor.getInstructorId());
            pstmt.setString(2, moduleDir);
            pstmt.setString(3, assignmentDir);
            pstmt.setString(4, studentIdDir);
            pstmt.setString(5, file.toString());
            ResultSet rs = pstmt.executeQuery();
            submissionText = rs.getString(1);
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        createSubmissionObject(moduleDir, assignmentDir, studentIdDir);
        loadGradingRubric();
        loadMarksReceived();
        loadComments();

        return submissionText;
    }

    void createSubmissionObject(String moduleId, String assignmentId, String studentId) {
        Connection conn = DatabaseController.dbConnect();
        String getSubmission = "SELECT * FROM ASSIGNMENT_SUBMISSION WHERE instructorId = ? AND moduleId = ? AND assignmentId = ? AND studentId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(getSubmission)) {
            pstmt.setString(1, instructor.getInstructorId());
            pstmt.setString(2, moduleId);
            pstmt.setString(3, assignmentId);
            pstmt.setString(4, studentId);
            ResultSet rs = pstmt.executeQuery();
            String gradingRubric = rs.getString(8);
            String marksReceived = rs.getString(9);
            double maxMarks = Double.parseDouble(rs.getString(10));
            double totalMarks = Double.parseDouble(rs.getString(11));
            String comments = rs.getString(12);
            conn.close();

            currentSubmission = new SubmissionModel(moduleId, assignmentId, studentId, gradingRubric,
                    marksReceived, maxMarks, totalMarks, comments);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // dynamically add criteria to grading rubric
    public void addCriterion() {
        TextField criterionMarkInput = new TextField();
        criterionMarkInput.setMaxWidth(30);
        TextField criterionNameInput = new TextField();
        criterionNameInput.setMaxWidth(150);
        Button removeButton = new Button("X");
        HBox hBox = new HBox(criterionMarkInput, criterionNameInput, removeButton);
        hBox.setSpacing(10);
        removeButton.setOnAction(e -> removeCriterion(hBox));

        rubricVBox.getChildren().add(hBox);
        criteriaList.add(hBox);
    }

    public void removeCriterion(HBox hBox) {
        System.out.println("criteriaList size before:" + criteriaList.size());
        rubricVBox.getChildren().remove(hBox);
        System.out.println(criteriaList.contains(hBox));
        criteriaList.remove(hBox);
        System.out.println("criteriaList size after:" + criteriaList.size());
    }

    //TODO prevent existing marks in marks section from being reset when new criteria are
    // added to rubric - get marks from marksList?
    @FXML
    void setGradingRubric() {
        int maxMarks = 0;
        StringBuilder sb = new StringBuilder();     //to store grading rubric info as comma separated values
        marksVBox.getChildren().clear();
        marksList.clear();

        for(HBox h : criteriaList) {
            Node markNode = h.getChildren().get(0);
            Node nameNode = h.getChildren().get(1);

            String criterionMark = ((TextField)markNode).getText();
            String criterionName = ((TextField)nameNode).getText();

            //populate marking section with rubric info
            TextField markTextField = new TextField();
            markTextField.setMaxWidth(30);

            //dynamically update marks total as marks are added
            markTextField.setOnMouseExited((e -> updateTotalMarks()));

            Label criterionLabel = new Label("/" + criterionMark + " " + criterionName);
            HBox hBox = new HBox(markTextField, criterionLabel);
            hBox.setAlignment(Pos.CENTER_LEFT);
            marksVBox.getChildren().add(hBox);

            maxMarks += Integer.parseInt(criterionMark);
            marksList.add(markTextField);

            sb.append(criterionMark).append(",").append(criterionName).append(",");
        }

        currentSubmission.setMaxMarks(maxMarks);
        maxMarksLabel.setText("Max Marks: " + maxMarks);
        currentSubmission.setGradingRubric(sb.toString());

        handleNullMarks();
    }

    @FXML
    void updateTotalMarks() {
        double totalMarks = 0;

        for(TextField mark : marksList) {
            totalMarks += Double.parseDouble(mark.getText());
        }

        currentSubmission.setTotalMarks(totalMarks);
        marksReceivedLabel.setText("Total marks: " + currentSubmission.getTotalMarks());
    }

    void loadGradingRubric() {
        rubricVBox.getChildren().clear();
        if (!currentSubmission.getGradingRubric().equals("rubric not set")) {
            String[] rubric = currentSubmission.getGradingRubric().split(",");
            for (int i = 0; i <= rubric.length - 2; i += 2) {
                TextField criterionMarkInput = new TextField();
                criterionMarkInput.setMaxWidth(30);
                criterionMarkInput.setText(rubric[i]);
                TextField criterionNameInput = new TextField();
                criterionNameInput.setMaxWidth(150);
                criterionNameInput.setText(rubric[i + 1]);
                criterionNameInput.setMaxWidth(150);
                Button removeButton = new Button("X");
                HBox hBox = new HBox(criterionMarkInput, criterionNameInput, removeButton);
                hBox.setSpacing(10);
                rubricVBox.getChildren().add(hBox);

                criteriaList.add(hBox);

                removeButton.setOnAction(event -> removeCriterion(  hBox));
            }
        }
    }

    void handleNullMarks() {
        for(TextField mark : marksList) {
            if(mark.getText().isEmpty()) {
                mark.setText("0");
            }
        }
    }

    void setMarksReceived() {
        // create string of comma separated values to store marks received
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < criteriaList.size(); i++) {
            Node markNode = criteriaList.get(i).getChildren().get(0);
            Node nameNode = criteriaList.get(i).getChildren().get(1);

            String mark = marksList.get(i).getText();
            String criterionMark = ((TextField)markNode).getText();
            String criterionName = ((TextField)nameNode).getText();

            sb.append(mark).append(",").append("/").append(criterionMark).append(" ").append(criterionName).append(",");
        }

        currentSubmission.setMarksReceived(sb.toString());
        criteriaList.clear();
    }

    void loadMarksReceived() {
        marksVBox.getChildren().clear();
        if (!currentSubmission.getMarksReceived().equals("marks not set")) {
            String[] marks = currentSubmission.getMarksReceived().split(",");

            for (int i = 0; i <= marks.length - 2; i += 2) {
                TextField markTextField = new TextField();
                markTextField.setMaxWidth(30);
                markTextField.setText(marks[i]);
                markTextField.setOnMouseExited(e -> updateTotalMarks());

                marksList.add(markTextField);

                Label criterionLabel = new Label(marks[i + 1]);
                HBox hBox = new HBox(markTextField, criterionLabel);
                hBox.setAlignment(Pos.CENTER_LEFT);
                hBox.setSpacing(10);
                marksVBox.getChildren().add(hBox);
            }
        }

        maxMarksLabel.setText("Max marks: " + currentSubmission.getMaxMarks());
        updateTotalMarks();
        marksReceivedLabel.setText("Total marks: " + currentSubmission.getTotalMarks());
    }

    void setComments() {
        currentSubmission.setComments(commentsTextArea.getText());
    }

    void loadComments() {
        commentsTextArea.setText(currentSubmission.getComments());
    }

    @FXML
    void saveMarks() {
        setMarksReceived();
        setComments();

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
        String module = moduleCodeTextField.getText();
        String assignment = assignmentCodeTextField.getText();

        // clear textfields
        moduleCodeTextField.clear();
        assignmentCodeTextField.clear();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HH_mm_ss_");
        LocalDateTime now = LocalDateTime.now();
        String csvFile = "C:\\Users\\mcnei\\Desktop\\Inspector\\results\\" + dtf.format(now) + module + "_" + assignment +"_results.csv";

        Connection conn = DatabaseController.dbConnect();
        String sql = "SELECT moduleId, assignmentId, studentId, studentEmail, gradingRubric, marksReceived, maxMarks, " +
                "totalMarks, comments FROM ASSIGNMENT_SUBMISSION WHERE instructorId = ? AND moduleId = ? AND assignmentId = ?";
        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, instructor.getInstructorId());
            pstmt.setString(2, module);
            pstmt.setString(3, assignment);
            ResultSet rs = pstmt.executeQuery();

            // create CSV file
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFile));

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
                    rs.getString(9));
            }
            conn.close();
            csvPrinter.flush();
            csvPrinter.close();

            alertController.displayAlert(new AlertModel("Data Export", "Results successfully exported."));

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}