package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.sql.*;

public class LoginController {
    Connection conn;
    @FXML private TextField loginEmailField;
    @FXML private TextField loginPasswordField;
    @FXML private TextField registerUsernameField;
    @FXML private TextField registerEmailField;
    @FXML private TextField registerPasswordField;
    @FXML private TextField registerConfirmPasswordField;

    @FXML
    void login(ActionEvent actionEvent) {
        AlertController alertController = new AlertController();
        String email = loginEmailField.getText();
        String password = loginPasswordField.getText();
        if(email.isEmpty() || password.isEmpty()) {
            alertController.displayAlert(alertController.missingData());
            //DialogBoxController.alert("Missing Data", "Please complete all fields.");
        } else {
            conn = DatabaseController.dbConnect();
            String sql = "SELECT password FROM INSTRUCTOR WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                ResultSet rs = pstmt.executeQuery();
                if(rs.isClosed()) {
                    DialogBoxController.alert("Login Unsuccessful", "No account with that email address.\nPlease register and try again.");
                }
                else {
                    String storedPassword = rs.getString(1);
                    if(password.equals(storedPassword)) {
                        loginEmailField.setText(null);
                        loginPasswordField.setText(null);
                        DialogBoxController dc = new DialogBoxController();
                        dc.dialog("Login", "Login successful!");
                        //load instructor dashboard

                    } else {
                        DialogBoxController dc = new DialogBoxController();
                        dc.dialog("Error", "Password incorrect. Please try again.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        actionEvent.consume();
    }

    @FXML
    void registerNewUser(ActionEvent actionEvent) {
        String username = registerUsernameField.getText();
        String email = registerEmailField.getText();
        String password = registerPasswordField.getText();
        String confirmPassword = registerConfirmPasswordField.getText();

        if(username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            DialogBoxController.alert("Missing Data", "Please complete all fields.");
        } else if(!password.equals(confirmPassword)) {
            System.out.println("passwords must be equal");
            DialogBoxController.alert("Password Mismatch", "Passwords must be the same.");
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

                        DialogBoxController.alert("Successful Registration", "Registration completed!\nLogin with your username and password.");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    conn.close();
                    DialogBoxController.alert("Error", "A user with this email address is already registered.\nUse this email address to login or register with a different email address.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
