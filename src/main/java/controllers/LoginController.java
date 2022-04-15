package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class LoginController {
    AlertController alertController = new AlertController();
    @FXML private TextField loginEmailField;
    @FXML private TextField loginPasswordField;
    @FXML private TextField registerUsernameField;
    @FXML private TextField registerEmailField;
    @FXML private TextField registerPasswordField;
    @FXML private TextField registerConfirmPasswordField;

    @FXML
    void login(ActionEvent actionEvent) {
        actionEvent.consume();

        //set focus to loginEmailField
        loginEmailField.requestFocus();

        String email = loginEmailField.getText();
        String password = loginPasswordField.getText();
        if(email.isEmpty() || password.isEmpty()) {
            alertController.displayAlert(alertController.missingData());
        } else {
            Connection conn = DatabaseController.dbConnect();
            String sql = "SELECT password, name FROM INSTRUCTOR WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();
                if(rs.isClosed()) {
                    conn.close();
                    alertController.displayAlert(alertController.userNotFound());
                }
                else {
                    String storedPassword = rs.getString(1);
                    conn.close();
                    if(password.equals(storedPassword)) {
                        // clear text fields and load new scene root (instructor dashboard)
                        loginEmailField.setText(null);
                        loginPasswordField.setText(null);

                        // create new scene root
                        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("views/WindowView.fxml")));
                        Parent newRoot = loader.load();
                        //get current scene
                        Scene scene = loginEmailField.getScene();
                        // set new root in current scene
                        scene.setRoot(newRoot);

                        // get controller for current scene
                        WindowController windowController = loader.getController();
                        windowController.setup(windowController, email);
                    } else {
                        alertController.displayAlert(alertController.incorrectPassword());
                    }
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void registerNewUser(ActionEvent actionEvent) {
        actionEvent.consume();
        String username = registerUsernameField.getText();
        String email = registerEmailField.getText();
        String password = registerPasswordField.getText();
        String confirmPassword = registerConfirmPasswordField.getText();

        //check all fields completed
        if(username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            alertController.displayAlert(alertController.missingData());
        } else if(!password.equals(confirmPassword)) {
            alertController.displayAlert(alertController.passwordMismatch());
        } else {
            //check if user is already registered
            Connection conn = DatabaseController.dbConnect();
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
                        ps2.setString(3, password);
                        ps2.executeUpdate();
                        conn.close();

                        registerUsernameField.setText(null);
                        registerEmailField.setText(null);
                        registerPasswordField.setText(null);
                        registerConfirmPasswordField.setText(null);
                        alertController.displayAlert(alertController.registrationSuccess());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    conn.close();
                    alertController.displayAlert(alertController.userExists());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
