import java.awt.*;

import java.io.*;
import java.sql.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Objects;

public class Main extends Application {
    private static Connection conn = null;
    //user should be able to set this at initial set up and amend it when required
    private final File importDirectory = new File("C:\\Users\\mcnei\\OneDrive - University of Limerick\\CS4617 FYP\\official documents\\assignments");
    private final Stage stage = new Stage();
    private final Text currentSubmissionDisplay = new Text();
    private final Label currentModuleLabel = new Label();
    private final Label currentAssignmentLabel = new Label();
    private final Label currentStudentIDLabel = new Label();
    private final Label submissionInfoLabel = new Label();
    private final TreeView<File> treeView = new TreeView<>();

    public static void main(String[] args) {
        //launch calls start() from Application class
        launch(args);
    }

//    @Override
//    public void start(Stage stage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("views/SampleView.fxml"));
//        stage.setTitle("Hello World");
//        stage.setMaximized(true);
//        stage.setScene(new Scene(root));
//        stage.show();
//    }

    //entry point for JavaFX application
    @Override
    public void start(Stage stage) throws Exception {
        //initialize database
        dbSetup();
        //set up JavaFX stage/window
    Parent root = FXMLLoader.load(getClass().getResource("views/LoginView.fxml"));
        stage.getIcons().add(new Image("resources/images/inspector_logo.png")); //set application icon
        stage.setMaximized(true);
        stage.setTitle("Inspector");
        stage.setOnCloseRequest(e -> {
            e.consume();
            exitApplication();
        });

        Scene scene = new Scene(root);
        //Scene scene = createSubmissionScene();
        //Scene scene = createLoginScene();
        scene.getStylesheets().add("resources/css/styles.css");
        stage.setScene(scene);
        stage.show();
    }

    void dbSetup() {
        try{
            conn = dbConnect();
            Statement stmt = conn.createStatement();
            //define database tables
            String sql = "CREATE TABLE IF NOT EXISTS INSTRUCTOR (" +
                    "instructorId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "name           VARCHAR(100) NOT NULL," +
                    "email          VARCHAR(100) NOT NULL," +
                    "password       VARCHAR(20)  NOT NULL," +
                    "UNIQUE (email)" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS MODULE (" +
                    "moduleId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "moduleCode    VARCHAR(6)  NOT NULL," +
                    "moduleName    VARCHAR(50) NOT NULL," +
                    "instructorId  INT(10)     NOT NULL REFERENCES INSTRUCTOR(instructorId)" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS STUDENT (" +
                    "studentId  VARCHAR(8) NOT NULL PRIMARY KEY" +
                    "); "+
                    "CREATE TABLE IF NOT EXISTS ASSIGNMENT (" +
                    "assignmentId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "moduleId       INT(10) NOT NULL REFERENCES MODULE(moduleId)," +
                    "academicYear   YEAR    NOT NULL," +
                    "semester       INT(1)  NOT NULL" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS ASSIGNMENT_SUBMISSION (" +
                    "assignmentId   INT(10) NOT NULL REFERENCES ASSIGNMENT(assignmentId)," +
                    "moduleId       INT(10) NOT NULL REFERENCES MODULE(moduleId)," +
                    "studentId      VARCHAR(8)  NOT NULL REFERENCES STUDENT(studentId)," +
                    "maxMarks       FLOAT," +
                    "receivedMarks  FLOAT," +
                    "assignmentText MEDIUMTEXT," +
                    "comments       VARCHAR(1000)," +
                    "CONSTRAINT COMP_KEY PRIMARY KEY (assignmentId, moduleId, studentId)" +
                    ");";
            //create database tables
            stmt.executeUpdate(sql);
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

    Scene createLoginScene() {
        Group logoImageGroup = new Group(new ImageView(new Image(Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("resources/images/inspector logo.png")))));

        //login to existing user account
        Label emailLabel = new Label("EMAIL ");
        Label passwordLabel = new Label("PASSWORD ");
        Label emptyLabel1 = new Label();
        VBox existingAccLabels = new VBox(emailLabel, passwordLabel, emptyLabel1);
        existingAccLabels.setAlignment(Pos.CENTER_RIGHT);
        existingAccLabels.setPadding(new Insets(10));
        existingAccLabels.setSpacing(20);

        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginBtn = new Button("LOGIN");
        loginBtn.setOnAction(e -> login(emailField.getText(), passwordField.getText()));
        HBox loginBtnContainer = new HBox(loginBtn);
        loginBtnContainer.setAlignment(Pos.CENTER_RIGHT);
        VBox existingAccFields = new VBox(emailField, passwordField, loginBtnContainer);
        existingAccFields.setAlignment(Pos.CENTER_RIGHT);
        existingAccFields.setSpacing(10);
        HBox existingAccLabelsAndFields = new HBox(existingAccLabels, existingAccFields);
        existingAccLabelsAndFields.setAlignment(Pos.CENTER);

        VBox existingAccount = new VBox(existingAccLabelsAndFields);
        existingAccount.setAlignment(Pos.TOP_CENTER);
        existingAccount.setSpacing(10);

        //register new user account
        Label newUsernameLabel = new Label("USERNAME");
        Label newUserEmailLabel = new Label("EMAIL");
        Label newPasswordLabel = new Label("PASSWORD");
        Label confirmPasswordLabel = new Label ("CONFIRM PASSWORD");
        Label emptyLabel2 = new Label();
        VBox newUserLabels = new VBox(newUsernameLabel, newUserEmailLabel, newPasswordLabel, confirmPasswordLabel, emptyLabel2);
        newUserLabels.setAlignment(Pos.CENTER_RIGHT);
        newUserLabels.setPadding(new Insets(10));
        newUserLabels.setSpacing(20);

        TextField newUsernameField = new TextField();
        TextField newUserEmailField = new TextField();
        PasswordField newPasswordField = new PasswordField();
        PasswordField confirmPasswordField = new PasswordField();
        Button registerAccBtn = new Button("REGISTER");
        registerAccBtn.setOnAction(e -> registerNewUser(newUsernameField.getText(), newUserEmailField.getText(),
                newPasswordField.getText(), confirmPasswordField.getText()));
        HBox regBtnContainer = new HBox(registerAccBtn);
        regBtnContainer.setAlignment(Pos.CENTER_RIGHT);
        VBox newUserFields = new VBox(newUsernameField, newUserEmailField, newPasswordField, confirmPasswordField, regBtnContainer);
        newUserFields.setAlignment(Pos.CENTER_RIGHT);
        newUserFields.setSpacing(10);

        HBox newUserLabelsAndFields = new HBox(newUserLabels, newUserFields);
        newUserLabelsAndFields.setAlignment(Pos.CENTER);

        VBox registerAccount = new VBox(newUserLabelsAndFields);
        registerAccount.setAlignment(Pos.TOP_CENTER);
        registerAccount.setSpacing(10);

        HBox loginHbox = new HBox(existingAccount, registerAccount);
        loginHbox.setAlignment(Pos.CENTER);
        loginHbox.setSpacing(20);
        VBox loginView = new VBox(logoImageGroup,loginHbox);
        loginView.setAlignment(Pos.CENTER);
        loginView.setSpacing(30);

        return new Scene(loginView);
    }

    void login(String emailAddress, String password) {
        if(emailAddress.isEmpty() || password.isEmpty()) {
            DialogBox.alert("Missing Data", "Please complete all fields.");
        } else {
            conn = dbConnect();
            String sql = "SELECT password FROM INSTRUCTOR WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, emailAddress);
                ResultSet rs = pstmt.executeQuery();
                if(rs.isClosed()) {
                    DialogBox.alert("Login Unsuccessful", "No account with that email address.\nPlease register and try again.");
                }
                else {
                    String storedPassword = rs.getString(1);
                    if(password.equals(storedPassword)) {
                        DialogBox.alert("Login", "Login successful!");
                        //load instructor dashboard

                    } else {
                        DialogBox.alert("Error", "Password incorrect. Please try again.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    void registerNewUser(String username, String email, String pw1, String pw2) {
        if(username.isEmpty() || email.isEmpty() || pw1.isEmpty() || pw2.isEmpty()) {
            DialogBox.alert("Missing Data", "Please complete all fields.");
        } else if(!pw1.equals(pw2)) {
            System.out.println("passwords must be equal");
            DialogBox.alert("Password Mismatch", "Passwords must be the same.");
        } else {
            //check if user is already registered
            Connection conn = dbConnect();
            String existingUserCheck = "SELECT 1 FROM INSTRUCTOR WHERE email = ?";
            String insertNewUser = "INSERT INTO INSTRUCTOR (name, email, password) VALUES(?, ?, ?)";

            try (PreparedStatement ps1 = conn.prepareStatement(existingUserCheck)) {
                ps1.setString(1, email);
                ResultSet rs = ps1.executeQuery();
                if (rs.isClosed()) {
                    try {
                        PreparedStatement ps2 = conn.prepareStatement(insertNewUser);
                        ps2.setString(1, username);
                        ps2.setString(2, email);
                        ps2.setString(3, pw1);
                        ps2.executeUpdate();
                        conn.close();
                        DialogBox.alert("Successful Registration", "Registration completed!\nLogin with your username and password.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    conn.close();
                    DialogBox.alert("Error", "A user with this email address is already registered.\nUse this email address to login or register with a different email address.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    Scene createSubmissionScene() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double screenWidth = screenSize.getWidth();

        //set content layout
        Button closeButton = new Button("exit");
        closeButton.setOnAction(e -> exitApplication());
        HBox bottomMenu = new HBox(closeButton);
        bottomMenu.setMinHeight(50);
        bottomMenu.setPadding(new Insets(10));
        bottomMenu.setSpacing(20);
        bottomMenu.setAlignment(Pos.CENTER);

        displayFileTree(importDirectory);
        VBox leftMenu = new VBox(currentModuleLabel, currentAssignmentLabel, currentStudentIDLabel, treeView);
        leftMenu.setMinWidth(screenWidth * 0.15);
        leftMenu.setPadding(new Insets(10));

        VBox centreDisplay = new VBox(submissionInfoLabel, currentSubmissionDisplay);
        centreDisplay.setMinWidth(screenWidth * 0.5);
        centreDisplay.setPadding(new Insets(10));  //Insets(top, right, bottom, left)
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(centreDisplay);

        Label label4 = new Label("right menu");
        VBox rightMenu = new VBox(label4);
        rightMenu.setMinWidth(screenWidth * 0.2);
        rightMenu.setPadding(new Insets(10));

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(createMenuBar());
        borderPane.setBottom(bottomMenu);
        borderPane.setLeft(leftMenu);
        borderPane.setCenter(scrollPane);
        borderPane.setRight(rightMenu);

        return new Scene(borderPane);
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
                    //display submission on click
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

    //submitted assignment files should be in parent directory named with student ID number
    String readFile(File file) {
        //split directories in filepath - "\" for Windows and "/" for Unix/Mac
        String[] filepath = file.getParentFile().toString().split("[\\\\/]");
        String currentModule = filepath[filepath.length - 3];
        //assignmentName could be a week number, e.g. labs
        String currentAssignment = filepath[filepath.length - 2];
        //parent directory of source files named with student ID
        String currentStudentID = filepath[filepath.length - 1];

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
        Boolean instruction = DialogBox.dialog("Exit Application", "Are you sure you want to close Inspector?");
        if(instruction) {
            Platform.exit();
        }
    }
}
