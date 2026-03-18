package com.patikadev.Model;

import com.patikadev.Helper.DBConnector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class StudentEnrollment {
    private int id;
    private int studentId;
    private int learningPathId;
    private String learningPathName;
    private String studentName;
    private String studentUserName;
    private String enrolledAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getLearningPathId() {
        return learningPathId;
    }

    public void setLearningPathId(int learningPathId) {
        this.learningPathId = learningPathId;
    }

    public String getLearningPathName() {
        return learningPathName;
    }

    public void setLearningPathName(String learningPathName) {
        this.learningPathName = learningPathName;
    }

    public String getEnrolledAt() {
        return enrolledAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentUserName() {
        return studentUserName;
    }

    public void setStudentUserName(String studentUserName) {
        this.studentUserName = studentUserName;
    }

    public void setEnrolledAt(String enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public static boolean enroll(int studentId, int learningPathId) {
        ensureTable();

        String query = "INSERT INTO student_enrollments (student_id, learning_path_id) VALUES (?, ?) " +
                "ON CONFLICT (student_id, learning_path_id) DO NOTHING";

        try {
            PreparedStatement prS = DBConnector.getInstance().prepareStatement(query);
            prS.setInt(1, studentId);
            prS.setInt(2, learningPathId);
            return prS.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public static ArrayList<StudentEnrollment> getByStudent(int studentId) {
        ensureTable();
        ArrayList<StudentEnrollment> enrollments = new ArrayList<>();

        String query = "SELECT se.id, se.student_id, se.learning_path_id, lp.name AS learning_path_name, " +
                "TO_CHAR(se.enrolled_at, 'YYYY-MM-DD HH24:MI') AS enrolled_at " +
                "FROM student_enrollments se " +
                "JOIN learning_paths lp ON lp.id = se.learning_path_id " +
                "WHERE se.student_id = ? " +
                "ORDER BY se.id";

        try {
            PreparedStatement prS = DBConnector.getInstance().prepareStatement(query);
            prS.setInt(1, studentId);
            ResultSet rs = prS.executeQuery();

            while (rs.next()) {
                StudentEnrollment enrollment = new StudentEnrollment();
                enrollment.setId(rs.getInt("id"));
                enrollment.setStudentId(rs.getInt("student_id"));
                enrollment.setLearningPathId(rs.getInt("learning_path_id"));
                enrollment.setLearningPathName(rs.getString("learning_path_name"));
                enrollment.setEnrolledAt(rs.getString("enrolled_at"));
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return enrollments;
    }

    public static ArrayList<StudentEnrollment> getByLearningPath(int learningPathId) {
        ensureTable();
        ArrayList<StudentEnrollment> enrollments = new ArrayList<>();

        String query = "SELECT se.id, se.student_id, se.learning_path_id, lp.name AS learning_path_name, " +
                "u.name AS student_name, u.username AS student_username, " +
                "TO_CHAR(se.enrolled_at, 'YYYY-MM-DD HH24:MI') AS enrolled_at " +
                "FROM student_enrollments se " +
                "JOIN learning_paths lp ON lp.id = se.learning_path_id " +
                "JOIN users u ON u.id = se.student_id " +
                "WHERE se.learning_path_id = ? " +
                "ORDER BY se.id";

        try {
            PreparedStatement prS = DBConnector.getInstance().prepareStatement(query);
            prS.setInt(1, learningPathId);
            ResultSet rs = prS.executeQuery();

            while (rs.next()) {
                StudentEnrollment enrollment = new StudentEnrollment();
                enrollment.setId(rs.getInt("id"));
                enrollment.setStudentId(rs.getInt("student_id"));
                enrollment.setLearningPathId(rs.getInt("learning_path_id"));
                enrollment.setLearningPathName(rs.getString("learning_path_name"));
                enrollment.setStudentName(rs.getString("student_name"));
                enrollment.setStudentUserName(rs.getString("student_username"));
                enrollment.setEnrolledAt(rs.getString("enrolled_at"));
                enrollments.add(enrollment);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return enrollments;
    }

    public static boolean unenroll(int enrollmentId, int studentId) {
        ensureTable();

        String query = "DELETE FROM student_enrollments WHERE id = ? AND student_id = ?";
        try {
            PreparedStatement prS = DBConnector.getInstance().prepareStatement(query);
            prS.setInt(1, enrollmentId);
            prS.setInt(2, studentId);
            return prS.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    private static void ensureTable() {
        String query = "CREATE TABLE IF NOT EXISTS student_enrollments (" +
                "id SERIAL PRIMARY KEY, " +
                "student_id INT NOT NULL, " +
                "learning_path_id INT NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE, " +
                "enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE (student_id, learning_path_id)" +
                ")";

        try {
            Statement statement = DBConnector.getInstance().createStatement();
            statement.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

