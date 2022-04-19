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
    void openUserManual(){
        openURL("src/main/resources/documentation/usermanual.html");
    }

    @FXML
    void openAboutDoc() {
        openURL("src/main/resources/documentation/about.html");
    }

    void openURL(String url){
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();

        try{
            if (os.contains("win")) {
                File htmlFile = new File(url);
                Desktop.getDesktop().browse(htmlFile.toURI());
            } else if (os.contains("mac")) {
                rt.exec( "open " + url);

            } else if (os.contains("nix") || os.contains("nux")) {
                String[] browsers = {"gooogle-chrome", "firefox", "vivaldi", "brave-browser", "epiphany", "mozilla", "konqueror",
                        "netscape","opera","links","lynx"};

                StringBuffer cmd = new StringBuffer();
                for (int i=0; i<browsers.length; i++) {
                    cmd.append(i == 0 ? "" : " || ").append(browsers[i]).append(" \"").append(url).append("\" ");
                }
                rt.exec(new String[] {"sh", "-c", cmd.toString()});
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}