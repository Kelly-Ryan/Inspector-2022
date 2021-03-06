package controllers;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseController {
    static Connection conn;

    public static Connection dbConnect() {
        try {
            //connect to database
            Class.forName("org.sqlite.JDBC");
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
                    "name               VARCHAR(100) NOT NULL," +
                    "email              VARCHAR(100) NOT NULL," +
                    "password           VARCHAR(20)  NOT NULL," +
                    "importDirectory    VARCHAR(250)," +
                    "resultsDirectory   VARCHAR(250)," +
                    "lastUsedRubric     VARCHAR(250) DEFAULT ''," +
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
                    "studentId    VARCHAR(8) NOT NULL PRIMARY KEY," +
                    "studentEmail VARCHAR(30)" +
                    "); "+
                    "CREATE TABLE IF NOT EXISTS ASSIGNMENT (" +
                    "assignmentId INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "moduleId       INT(10) NOT NULL REFERENCES MODULE(moduleId)" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS ASSIGNMENT_SUBMISSION (" +
                    "instructorId	INT(10) NOT NULL REFERENCES INSTRUCTOR(InstructorId)," +
                    "academicYear	VARCHAR(5) DEFAULT 'year not set'," +
                    "semester		VARCHAR(20) DEFAULT 'semester not set'," +
                    "moduleId       INT(10) NOT NULL REFERENCES MODULE(moduleId)," +
                    "assignmentId   INT(10) NOT NULL REFERENCES ASSIGNMENT(assignmentId)," +
                    "studentId      VARCHAR(8)  NOT NULL REFERENCES STUDENT(studentId)," +
                    "studentEmail   VARCHAR(30)," +
                    "gradingRubric VARCHAR(250) DEFAULT 'rubric not set'," +
                    "marksReceived VARCHAR(250) DEFAULT  'marks not set'," +
                    "maxMarks       FLOAT DEFAULT 0.0," +
                    "totalMarks     FLOAT DEFAULT 0.0," +
                    "comments       VARCHAR(1000)," +
                    "CONSTRAINT COMP_KEY PRIMARY KEY (instructorId, moduleId, assignmentId, studentId)" +
                    ");" +
                    "CREATE TABLE IF NOT EXISTS SUBMISSION_FILES (" +
                    "instructorId	INT(10) NOT NULL REFERENCES INSTRUCTOR(InstructorId)," +
                    "moduleId       INT(10) NOT NULL REFERENCES MODULE(moduleId)," +
                    "assignmentId   INT(10) NOT NULL REFERENCES ASSIGNMENT(assignmentId)," +
                    "studentId      VARCHAR(8)  NOT NULL REFERENCES STUDENT(studentId)," +
                    "filename       VARCHAR(50) NOT NULL," +
                    "assignmentText MEDIUMTEXT NOT NULL," +
                    "CONSTRAINT COMP_KEY PRIMARY KEY (instructorId, moduleId, assignmentId, studentId, filename)" +
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
