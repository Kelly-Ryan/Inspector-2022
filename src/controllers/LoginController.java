package controllers;

import java.sql.*;

public class LoginController {
    Connection conn;

    void login(String emailAddress, String password) {
        if(emailAddress.isEmpty() || password.isEmpty()) {
            DialogBoxController.alert("Missing Data", "Please complete all fields.");
        } else {
            conn = DatabaseController.dbConnect();
            String sql = "SELECT password FROM INSTRUCTOR WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, emailAddress);
                ResultSet rs = pstmt.executeQuery();
                if(rs.isClosed()) {
                    DialogBoxController.alert("Login Unsuccessful", "No account with that email address.\nPlease register and try again.");
                }
                else {
                    String storedPassword = rs.getString(1);
                    if(password.equals(storedPassword)) {
                        DialogBoxController.alert("Login", "Login successful!");
                        //load instructor dashboard

                    } else {
                        DialogBoxController.alert("Error", "Password incorrect. Please try again.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    void registerNewUser(String username, String email, String pw1, String pw2) {
        if(username.isEmpty() || email.isEmpty() || pw1.isEmpty() || pw2.isEmpty()) {
            DialogBoxController.alert("Missing Data", "Please complete all fields.");
        } else if(!pw1.equals(pw2)) {
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
                        ps2.setString(3, pw1);
                        ps2.executeUpdate();
                        conn.close();
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
