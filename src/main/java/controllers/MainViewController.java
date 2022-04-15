package controllers;

import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import models.AlertModel;
import models.InstructorModel;
import models.SubmissionModel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class MainViewController {
    AlertController alertController = new AlertController();
    ExecutorService executorService;
    private InstructorModel instructor;
    private File importDirectory, resultsDirectory, currentFile;
    private SubmissionModel currentSubmission;
    private static Pattern languagePattern;
    private CodeArea codeArea;
    private final List<HBox> criteriaList = new ArrayList<>();        //stores rubric info
    private final List<TextField> marksList = new ArrayList<>();      //used by updateTotalMarks()
    @FXML private Label username;
    @FXML private ScrollPane sourceCodeScrollPane;
    @FXML TreeView<File> treeView;
    @FXML VBox rubricVBox;
    @FXML VBox marksVBox;
    @FXML private Label maxMarksLabel;
    @FXML private Label marksReceivedLabel;
    @FXML private TextArea commentsTextArea;
    @FXML private TextField moduleCodeTextField;
    @FXML private TextField assignmentCodeTextField;

    private static final String[] C_KEYWORDS = new String[] {
            "auto", "break", "case", "char", "const", "continue",
            "default", "do", "double", "else", "enum", "extern",
            "float", "for", "goto", "if", "inline", "int", "long",
            "register", "restrict", "return", "short", "signed",
            "sizeof", "static", "struct", "switch", "typdef",
            "union", "unsigned", "void", "volatile", "while"
    };

    private static final String[] JAVA_KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
    };

    private static final String[] JULIA_KEYWORDS = new String[] {
            "baremodule", "begin", "break", "catch", "const",
            "continue", "do", "else", "elseif", "end", "export",
            "false", "finally", "for", "function", "global",
            "if", "import", "let", "local", "macro", "module",
            "quote", "return", "struct", "true", "try", "using",
            "while"
    };

    private static final String[] PYTHON_KEYWORDS = new String[] {
            "and", "as", "assert", "break", "class", "continue",
            "def", "del", "elif", "else", "except", "False",
            "finally", "for", "from", "global", "if", "import",
            "in", "is", "lambda", "None", "nonlocal", "not", "or",
            "pass", "raise", "return", "True", "try", "while",
            "with", "yield"
    };

    private static final String C_KEYWORD_PATTERN = "\\b(" + String.join("|", C_KEYWORDS) + ")\\b";
    private static final String JAVA_KEYWORD_PATTERN = "\\b(" + String.join("|", JAVA_KEYWORDS) + ")\\b";
    private static final String JULIA_KEYWORD_PATTERN = "\\b(" + String.join("|", JULIA_KEYWORDS) + ")\\b";
    private static final String PYTHON_KEYWORD_PATTERN = "\\b(" + String.join("|", PYTHON_KEYWORDS) + ")\\b";

    private static final String PARENTHESES_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern C_PATTERN = Pattern.compile(
            "(?<KEYWORD>" + C_KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PARENTHESES_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")",
            Pattern.MULTILINE
    );

    private static final Pattern JAVA_PATTERN = Pattern.compile(
            "(?<KEYWORD>" + JAVA_KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PARENTHESES_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")",
            Pattern.MULTILINE
    );

    private static final Pattern JULIA_PATTERN = Pattern.compile(
            "(?<KEYWORD>" + JULIA_KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PARENTHESES_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")",
            Pattern.MULTILINE
    );

    private static final Pattern PYTHON_PATTERN = Pattern.compile(
            "(?<KEYWORD>" + PYTHON_KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PARENTHESES_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")",
            Pattern.MULTILINE
    );

    void initializeListeners() {
        Scene scene = treeView.getScene();

        KeyCombination ctrlS = new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN);
        KeyCombination ctrlE = new KeyCodeCombination(KeyCode.E, KeyCodeCombination.CONTROL_DOWN);
        KeyCombination ctrlA = new KeyCodeCombination(KeyCode.A, KeyCodeCombination.CONTROL_DOWN);
        KeyCombination ctrlR = new KeyCodeCombination(KeyCode.R, KeyCodeCombination.CONTROL_DOWN);

        scene.setOnKeyReleased(event -> {
            if (ctrlS.match(event)) {
                saveMarks();
            }  else if (ctrlE.match(event)) {
                export();
            }  else if (ctrlA.match(event)) {
                addCriterion();
            } else if (ctrlR.match(event)) {
                modifyGradingRubric();
            }
        });

        //display submission text when source code files are selected with arrow keys
        treeView.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.UP) || keyEvent.getCode().equals(KeyCode.DOWN)) {
                TreeItem<File> treeItem = treeView.getSelectionModel().getSelectedItem();
                if (treeItem != null) {
                    if (treeItem.isLeaf()) {
                        codeArea.replaceText(0, 0, loadSubmission(treeItem));
                    }
                }
            }
        });
    }

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
    void setImportDirectory() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(new Stage());
        if (directory != null) {
            importDirectory = directory;
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

    void displayFileTree(TreeView<File> treeView) throws IOException {
        //create root item
        TreeItem<File> rootItem = new TreeItem<>(importDirectory);
        //hide root item of treeView
        treeView.setShowRoot(false);
        treeView.setRoot(rootItem);

        // TreeView/TreeItem styling
        PseudoClass parentElementPseudoClass = PseudoClass.getPseudoClass("parent-tree-item");
        PseudoClass gradedElementPseudoClass = PseudoClass.getPseudoClass("graded-tree-item");

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

            //display submission text when filename is clicked
            cell.setOnMouseClicked(event -> {
                if (!cell.isEmpty()) {
                    TreeItem<File> treeItem = cell.getTreeItem();
                    if (treeItem.isLeaf()) {
                        codeArea.replaceText(0, 0, loadSubmission(treeItem));
                    }
                }
            });

            // apply CSS PseudoClasses to cells
            cell.treeItemProperty().addListener((obs, oldTreeItem, newTreeItem) -> cell.pseudoClassStateChanged(parentElementPseudoClass,
                    newTreeItem != null && newTreeItem.getParent() == cell.getTreeView().getRoot()));

            cell.treeItemProperty().addListener((obs, oldTreeItem, newTreeItem) -> {
                try {
                    cell.pseudoClassStateChanged(gradedElementPseudoClass,
                            newTreeItem != null && checkSubmissionGraded(newTreeItem));
                } catch (SQLException e) {
                    e.printStackTrace();
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

            treeView.requestFocus();

        } catch (RuntimeException e) {
            alertController.displayAlert(new AlertModel("Import Directory", "Import directory not set." +
                    "\nSelect an import directory from\nFile > set import directory"));
        }
    }

    void createFileTree(File file, TreeItem<File> parent) throws IOException {
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

    void readFile(File file) throws IOException {
        //split directories in filepath - "\" for Windows and "/" for Unix/Mac
        String[] splitFilepath = file.toString().split("[\\\\/]");
        String module = splitFilepath[splitFilepath.length - 4];
        String assignment = splitFilepath[splitFilepath.length - 3];
        String studentID = splitFilepath[splitFilepath.length - 2];
        String filename = splitFilepath[splitFilepath.length - 1];
        String submissionText;

        //read file text
        StringBuilder sb = new StringBuilder();
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();

        while (line != null) {
            sb.append(line).append("\n");
            line = br.readLine();
        }
        submissionText = sb.toString();

        Connection conn = DatabaseController.dbConnect();
        String importSubmissions = "INSERT OR IGNORE INTO ASSIGNMENT_SUBMISSION (instructorId, moduleId, assignmentId, " +
                "studentId, studentEmail) VALUES (?, ?, ?, ?, ?);";
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

        String importSubmissionFiles = " INSERT OR IGNORE INTO SUBMISSION_FILES (instructorId, moduleId, assignmentId, " +
                "studentId, filename, assignmentText) VALUES (?, ?, ?, ?, ?, ?);";
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

    boolean checkSubmissionGraded(TreeItem<File> treeItem) throws SQLException {
        // for studentId folders containing source code files
        boolean graded = false;

        try{
            if(treeItem.getChildren().get(0).isLeaf()) {
                String studentId = treeItem.getValue().toString();
                String assignment = treeItem.getParent().getValue().toString();
                String module = treeItem.getParent().getParent().getValue().toString();

                Connection conn = DatabaseController.dbConnect();
                String marksReceived;
                String sql = "SELECT marksReceived FROM ASSIGNMENT_SUBMISSION WHERE instructorId = ? AND moduleId = ? AND " +
                        "assignmentId = ? AND studentId = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, instructor.getInstructorId());
                pstmt.setString(2, module);
                pstmt.setString(3, assignment);
                pstmt.setString(4, studentId);
                ResultSet rs = pstmt.executeQuery();
                marksReceived = rs.getString(1);
                conn.close();

                if(!marksReceived.equals("marks not set")) {
                    graded = true;
                }
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            return graded;
        }

        return graded;
    }

    void setUpSourceCodeDisplay() {
        executorService = Executors.newSingleThreadExecutor();
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setEditable(false);
        codeArea.setFocusTraversable(false);
        codeArea.setMinWidth(900);
        codeArea.setMinHeight(700);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        sourceCodeScrollPane.setContent(codeArea);

        codeArea.multiPlainChanges()
        .successionEnds(Duration.ofMillis(500))
        .retainLatestUntilLater(executorService)
        .supplyTask(this::computeHighlightingAsync)
        .awaitLatest(codeArea.multiPlainChanges())
        .filterMap(t -> {
            if(t.isSuccess()) {
                return Optional.of(t.get());
            } else {
                t.getFailure().printStackTrace();
                return Optional.empty();
            }
        })
        .subscribe(this::applyHighlighting);
    }

    private Task<StyleSpans<Collection<String>>> computeHighlightingAsync() {
        String sourceCode = codeArea.getText();
        Task<StyleSpans<Collection<String>>> task = new Task<>() {
            @Override
            protected StyleSpans<Collection<String>> call() {
                return computeHighlighting(sourceCode);
            }
        };
        executorService.execute(task);
        return task;
    }

    private Pattern patternChooser() {
        Pattern patt = Pattern.compile("a^");
        if(currentFile.toString().endsWith(".c")) {
            patt = C_PATTERN;

        } else if (currentFile.toString().endsWith(".java")) {
            patt = JAVA_PATTERN;

        } else if (currentFile.toString().endsWith(".jl")) {
            patt = JULIA_PATTERN;

        } else if (currentFile.toString().endsWith(".py")) {
            patt = PYTHON_PATTERN;
        }

        return patt;
    }

    private static StyleSpans<Collection<String>> computeHighlighting(String sourceCode) {
        Matcher matcher = languagePattern.matcher(sourceCode);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while(matcher.find()) {
            String styleClass = "keyword";
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), sourceCode.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void applyHighlighting(StyleSpans<Collection<String>> highlighting) {
        codeArea.setStyleSpans(0, highlighting);
    }

    //displays submission text, grading rubric and saved marks and creates Submission object to hold submission info
    String loadSubmission(TreeItem<File> treeItem) {
        codeArea.clear();
        criteriaList.clear();
        marksList.clear();

        currentFile = treeItem.getValue();

        String studentIdDir = treeItem.getParent().getValue().toString();
        String assignmentDir = treeItem.getParent().getParent().getValue().toString();
        String moduleDir = treeItem.getParent().getParent().getParent().getValue().toString();
        String submissionText = "";

        Connection conn = DatabaseController.dbConnect();
        String getSubmission = "SELECT assignmentText FROM SUBMISSION_FILES WHERE instructorId = ? AND moduleId = ? AND " +
                "assignmentId = ? AND studentId = ? and filename = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(getSubmission)) {
            pstmt.setString(1, instructor.getInstructorId());
            pstmt.setString(2, moduleDir);
            pstmt.setString(3, assignmentDir);
            pstmt.setString(4, studentIdDir);
            pstmt.setString(5, currentFile.toString());
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

        languagePattern = patternChooser();

        return submissionText;
    }

    void createSubmissionObject(String moduleId, String assignmentId, String studentId) {
        Connection conn = DatabaseController.dbConnect();
        String getSubmission = "SELECT * FROM ASSIGNMENT_SUBMISSION WHERE instructorId = ? AND moduleId = ? AND " +
                "assignmentId = ? AND studentId = ?";
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
        criterionMarkInput.setMaxWidth(35);
        TextField criterionNameInput = new TextField();
        criterionNameInput.setMaxWidth(150);
        Button removeButton = new Button("X");
        HBox hBox = new HBox(criterionMarkInput, criterionNameInput, removeButton);
        hBox.setSpacing(10);
        removeButton.setOnAction(e -> removeCriterion(hBox));

        rubricVBox.getChildren().add(hBox);
        criteriaList.add(hBox);

        hBox.getChildren().get(0).requestFocus();
    }

    public void removeCriterion(HBox hBox) {
        rubricVBox.getChildren().remove(hBox);
        criteriaList.remove(hBox);
    }

    @FXML
    public void modifyGradingRubric() {
        if(!currentSubmission.getGradingRubric().equals("rubric not set")) {
            DialogController dialogController = new DialogController(this);
            dialogController.displayDialog("ModifyRubricDialogView.fxml", "Change Grading Rubric");
        } else {
            setGradingRubric();
        }
    }

    public void setGradingRubric() {
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
            markTextField.setMaxWidth(35);

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
        instructor.setLastUsedRubric(sb.toString());

        handleNullMarks();

        marksVBox.getChildren().get(0).requestFocus();
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

    void parseGradingRubric(String rubricString) {
        String[] rubric = rubricString.split(",");
        for (int i = 0; i <= rubric.length - 2; i += 2) {
            TextField criterionMarkInput = new TextField();
            criterionMarkInput.setMaxWidth(35);
            criterionMarkInput.setText(rubric[i]);
            TextField criterionNameInput = new TextField();
            criterionNameInput.setMaxWidth(150);
            criterionNameInput.setText(rubric[i + 1]);
            criterionNameInput.setMaxWidth(150);
            Button removeButton = new Button("X");
            HBox hBox = new HBox(criterionMarkInput, criterionNameInput, removeButton);
            hBox.setSpacing(10);
            removeButton.setOnAction(event -> removeCriterion(hBox));

            rubricVBox.getChildren().add(hBox);

            criteriaList.add(hBox);
        }
    }

    void loadGradingRubric() {
        rubricVBox.getChildren().clear();

        // if assignment has not been graded load last saved rubric
        if (currentSubmission.getGradingRubric().equals("rubric not set")) {
            if(instructor.getLastUsedRubric() != null) {
                parseGradingRubric(instructor.getLastUsedRubric());
            }
        } else {
            parseGradingRubric(currentSubmission.getGradingRubric());
        }
    }

    void saveLastUsedRubric() {
        Connection conn = DatabaseController.dbConnect();
        String  sql= "UPDATE INSTRUCTOR SET lastUsedRubric = ? WHERE  instructorId = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, instructor.getLastUsedRubric());
            pstmt.setString(2, instructor.getInstructorId());
            pstmt.execute();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
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
    }

    void loadMarksReceived() {
        marksVBox.getChildren().clear();
        if (!currentSubmission.getMarksReceived().equals("marks not set")) {
            String[] marks = currentSubmission.getMarksReceived().split(",");

            for (int i = 0; i <= marks.length - 2; i += 2) {
                TextField markTextField = new TextField();
                markTextField.setMaxWidth(35);
                markTextField.setText(marks[i]);
                markTextField.setOnMouseExited(e -> updateTotalMarks());

                marksList.add(markTextField);

                Label criterionLabel = new Label(marks[i + 1]);
                HBox hBox = new HBox(markTextField, criterionLabel);
                hBox.setAlignment(Pos.CENTER_LEFT);
                marksVBox.getChildren().add(hBox);
            }
        }

        updateTotalMarks();
        maxMarksLabel.setText("Max marks: " + currentSubmission.getMaxMarks());
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
        saveLastUsedRubric();
        setMarksReceived();
        setComments();

        Connection conn = DatabaseController.dbConnect();
        String insertSubmission = "UPDATE ASSIGNMENT_SUBMISSION SET gradingRubric = ?, marksReceived = ?, maxMarks = ?, " +
                "totalMarks = ?, comments = ? WHERE  moduleId = ? AND assignmentId = ? AND studentId = ?";
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

            alertController.displayAlert(new AlertModel("Data Saved", "Results successfully saved."));

            // reset focus and selection after save
            treeView.requestFocus();
            treeView.getSelectionModel().selectNext();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void setExportDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(new Stage());
        if (directory != null) {
            resultsDirectory = directory;
            Connection conn = DatabaseController.dbConnect();
            String sql = "UPDATE INSTRUCTOR SET resultsDirectory = ? where email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, resultsDirectory.toString());
                pstmt.setString(2, instructor.getEmail());
                pstmt.execute();
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void export() {
        String module = moduleCodeTextField.getText();
        String assignment = assignmentCodeTextField.getText();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd_HH_mm_ss_");
        LocalDateTime now = LocalDateTime.now();
        String csvFile = resultsDirectory + "\\" + dtf.format(now) + module + "_" + assignment +"_results.csv";

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

            // clear textfields
            moduleCodeTextField.clear();
            assignmentCodeTextField.clear();

            alertController.displayAlert(new AlertModel("Data Export", "Results successfully exported."));

        } catch (NoSuchFileException e) {
            alertController.displayAlert(new AlertModel("Results Directory", "Results directory not set." +
                    "\nSelect a results directory from\nFile > set results directory"));

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}