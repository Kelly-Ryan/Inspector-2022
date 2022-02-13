package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.InstructorModel;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class LoginController {
    Connection conn;
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
        String email = loginEmailField.getText();
        String password = loginPasswordField.getText();
        if(email.isEmpty() || password.isEmpty()) {
            alertController.displayAlert(alertController.missingData());
        } else {
            conn = DatabaseController.dbConnect();
            String sql = "SELECT password, name FROM INSTRUCTOR WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();
                if(rs.isClosed()) {
                    alertController.displayAlert(alertController.userNotFound());
                }
                else {
                    String storedPassword = rs.getString(1);
                    String username = rs.getString(2);
                    if(password.equals(storedPassword)) {
                        InstructorModel instructor = new InstructorModel(username, loginEmailField.getText());
                        loginEmailField.setText(null);
                        loginPasswordField.setText(null);

                        // get current stage and set InstructorView scene
                        Stage stage = (Stage) loginEmailField.getScene().getWindow();
                        Parent root = new FXMLLoader(Objects.requireNonNull(getClass().getClassLoader().getResource("views/InstructorView.fxml"))).load();
                        stage.setScene(new Scene(root));
                    } else {
                        alertController.displayAlert(alertController.incorrectPassword());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
