package controllers;

import javafx.fxml.FXML;

import javafx.scene.Parent;
import models.InstructorModel;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WindowController {
    WindowController windowController;
    public InstructorModel instructor;
    @FXML private Parent mainView;
    @FXML MainViewController mainViewController;

    void setup(WindowController windowController, String email) throws IOException, SQLException {
        this.windowController = windowController;

        //sql query to initialize InstructorModel object
        Connection conn = DatabaseController.dbConnect();
        String sql = "SELECT instructorId, name, importDirectory, resultsDirectory, lastUsedRubric FROM INSTRUCTOR WHERE email = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, email);
        ResultSet rs = pstmt.executeQuery();
        int instructorId = Integer.parseInt(rs.getString(1));
        String name = rs.getString(2);
        String importDirectory = rs.getString(3);
        String resultsDirectory = rs.getString(4);
        String lastUsedRubric = rs.getString(5);
        conn.close();

        // set instructor info
        instructor = new InstructorModel(instructorId, name, email, importDirectory, resultsDirectory, lastUsedRubric);
        mainViewController.setInstructor(instructor);

        // prepare source code display area
        mainViewController.setUpSourceCodeDisplay();

        // set key listener
        mainViewController.initializeListeners();

        // import files
        mainViewController.displayFileTree(mainViewController.treeView);
    }

    @FXML
    void setImportDirectory() throws IOException {
        mainViewController.setImportDirectory();
    }

    @FXML
    void setExportDirectory() {
        mainViewController.setExportDirectory();
    }

    @FXML
    void exitApplication(){
        DialogController dialogController = new DialogController(mainViewController);
        dialogController.displayDialog("ExitApplicationDialogView.fxml", "Exit Application");
    }

    @FXML
    void openUserManual() throws IOException {
        File htmlFile = new File("src/main/resources/documentation/usermanual.html");
        Desktop.getDesktop().browse(htmlFile.toURI());
    }

    @FXML
    void openAboutDoc() throws IOException {
        File htmlFile = new File("src/main/resources/documentation/about.html");
        Desktop.getDesktop().browse(htmlFile.toURI());
    }
}
