package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseController {
    static Connection conn;

    public static Connection dbConnect() {
        try {
            Class.forName("org.sqlite.JDBC");
            //create database if it does not already exist and connect to it
            conn = DriverManager.getConnection("jdbc:sqlite:src/main/resources/db/inspector.db");
        } catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return conn;
    }

    public static void dbSetup() {
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
                    "academicYear   YEAR    NOT NULL," +
                    "semester       INT(1)  NOT NULL," +
                    "instructorId  INT(10)     NOT NULL REFERENCES INSTRUCTOR(instructorId)" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS STUDENT (" +
                    "studentId  VARCHAR(8) NOT NULL PRIMARY KEY" +
                    "); "+
                    "CREATE TABLE IF NOT EXISTS ASSIGNMENT (" +
                    "assignmentId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "moduleId       INT(10) NOT NULL REFERENCES MODULE(moduleId)" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS ASSIGNMENT_SUBMISSION (" +
                    "assignmentId   INT(10) NOT NULL REFERENCES ASSIGNMENT(assignmentId)," +
                    "moduleId       INT(10) NOT NULL REFERENCES MODULE(moduleId)," +
                    "studentId      VARCHAR(8)  NOT NULL REFERENCES STUDENT(studentId)," +
                    "filename       VARCHAR(50) NOT NULL," +
                    "maxMarks       FLOAT," +
                    "receivedMarks  FLOAT," +
                    "assignmentText MEDIUMTEXT NOT NULL," +
                    "comments       VARCHAR(1000)," +
                    "CONSTRAINT COMP_KEY PRIMARY KEY (assignmentId, moduleId, studentId, filename)" +
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
}
