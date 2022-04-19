package controllers;

import javafx.fxml.FXML;

import javafx.scene.Parent;
import models.InstructorModel;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

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
//        try {
//            InputStream htmlFileStream = getClass().getResourceAsStream("/documentation/usermanual.html");
//            Path tempFilePath = Files.createTempFile("help", ".html");
//            assert htmlFileStream != null;
//            java.nio.file.Files.copy(
//                    htmlFileStream,
//                    tempFilePath,
//                    StandardCopyOption.REPLACE_EXISTING);
//
//            IOUtils.closeQuietly(htmlFileStream);
//            Desktop.getDesktop().browse(tempFilePath.toFile().toURI());
//        } catch(Exception ex){
//            throw new RuntimeException(ex);
//        }

        String url = "./documentation/usermanual.html";
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();

        System.out.println(os);
        try{
            if (os.contains("win")) {
                System.out.println("win");
                File htmlFile = new File("src/main/resources/documentation/usermanual.html");
                Desktop.getDesktop().browse(htmlFile.toURI());

//                rt.exec( "rundll32 url.dll,FileProtocolHandler " + url);
//                rt.exec( "start " + url);

            } else if (os.contains("mac")) {
                System.out.println("mac");
                rt.exec( "open " + url);

            } else if (os.contains("nix") || os.contains("nux")) {
                System.out.println("linux");

                // Do a best guess on unix until we get a platform independent way
                // Build a list of browsers to try, in this order.
                String[] browsers = {"chrome", "firefox", "epiphany", "mozilla", "konqueror",
                        "netscape","opera","links","lynx"};

                // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
                StringBuffer cmd = new StringBuffer();
                for (int i=0; i<browsers.length; i++)
                    cmd.append(i == 0 ? "" : " || ").append(browsers[i]).append(" \"").append(url).append("\" ");

                rt.exec(new String[] { "sh", "-c", cmd.toString() });

            }
        }catch (Exception ignore){

        }
    }

    @FXML
    void openAboutDoc() throws IOException {
        File htmlFile = new File("src/main/resources/documentation/about.html");
        Desktop.getDesktop().browse(htmlFile.toURI());
    }
}
