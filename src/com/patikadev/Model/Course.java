package com.patikadev.Model;

import com.patikadev.Helper.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Course {
    private int id;
    private String name;
    private int learningPathId;
    private String learningPathName;
    private int educatorId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getEducatorId() {
        return educatorId;
    }

    public void setEducatorId(int educatorId) {
        this.educatorId = educatorId;
    }

    public static ArrayList<Course> getList() {
        ensureTable();
        ArrayList<Course> courses = new ArrayList<>();

        String query = "SELECT c.id, c.name, c.learning_path_id, c.educator_id, lp.name AS learning_path_name " +
                "FROM courses c JOIN learning_paths lp ON lp.id = c.learning_path_id ORDER BY c.id";

        try {
            Statement statement = DBConnector.getInstance().createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                courses.add(mapFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return courses;
    }

    public static ArrayList<Course> getListByEducator(int educatorId) {
        ensureTable();
        ArrayList<Course> courses = new ArrayList<>();

        String query = "SELECT c.id, c.name, c.learning_path_id, c.educator_id, lp.name AS learning_path_name " +
                "FROM courses c JOIN learning_paths lp ON lp.id = c.learning_path_id " +
                "WHERE c.educator_id = ? OR c.educator_id = 0 ORDER BY c.id";

        try {
            PreparedStatement preparedStatement = DBConnector.getInstance().prepareStatement(query);
            preparedStatement.setInt(1, educatorId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                courses.add(mapFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return courses;
    }

    public static ArrayList<Course> getListByLearningPathId(int learningPathId) {
        ensureTable();
        ArrayList<Course> courses = new ArrayList<>();

        String query = "SELECT c.id, c.name, c.learning_path_id, c.educator_id, lp.name AS learning_path_name " +
                "FROM courses c JOIN learning_paths lp ON lp.id = c.learning_path_id " +
                "WHERE c.learning_path_id = ? ORDER BY c.id";

        try {
            PreparedStatement preparedStatement = DBConnector.getInstance().prepareStatement(query);
            preparedStatement.setInt(1, learningPathId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                courses.add(mapFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return courses;
    }

    public static boolean add(String name, int learningPathId, int educatorId) {
        ensureTable();
        String query = "INSERT INTO courses (id, name, learning_path_id, educator_id) VALUES (?, ?, ?, ?)";
        Connection connection = null;
        try {
            connection = DBConnector.getInstance();
            connection.setAutoCommit(false);

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, findNextAvailableId(connection));
            preparedStatement.setString(2, name);
            preparedStatement.setInt(3, learningPathId);
            preparedStatement.setInt(4, educatorId);

            boolean isAdded = preparedStatement.executeUpdate() > 0;
            if (isAdded) {
                syncIdSequence(connection);
            }

            connection.commit();
            return isAdded;
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ignored) {
            }
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }

        return false;
    }

    public static boolean update(int id, String name, int learningPathId, int educatorId) {
        ensureTable();

        String query = "UPDATE courses SET name = ?, learning_path_id = ?, educator_id = ? WHERE id = ? AND (educator_id = ? OR educator_id = 0)";
        try {
            PreparedStatement preparedStatement = DBConnector.getInstance().prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, learningPathId);
            preparedStatement.setInt(3, educatorId);
            preparedStatement.setInt(4, id);
            preparedStatement.setInt(5, educatorId);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public static boolean delete(int id, int educatorId) {
        ensureTable();
        String query = "DELETE FROM courses WHERE id = ? AND (educator_id = ? OR educator_id = 0)";
        String normalizeQuery = "UPDATE courses SET id = id - 1 WHERE id > ?";
        Connection connection = null;
        try {
            connection = DBConnector.getInstance();
            connection.setAutoCommit(false);

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            preparedStatement.setInt(2, educatorId);

            int deletedRows = preparedStatement.executeUpdate();
            if (deletedRows <= 0) {
                connection.rollback();
                return false;
            }

            PreparedStatement normalizeStatement = connection.prepareStatement(normalizeQuery);
            normalizeStatement.setInt(1, id);
            normalizeStatement.executeUpdate();

            syncIdSequence(connection);
            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ignored) {
            }
            System.out.println(e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException ignored) {
            }
        }

        return false;
    }

    private static void ensureTable() {
        LearningPath.getList();

        String query = "CREATE TABLE IF NOT EXISTS courses (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "learning_path_id INT NOT NULL REFERENCES learning_paths(id) ON DELETE CASCADE, " +
                "educator_id INT NOT NULL DEFAULT 0, " +
                "status VARCHAR(50) NOT NULL DEFAULT 'Draft'" +
                ")";

        try {
            Statement statement = DBConnector.getInstance().createStatement();
            statement.execute(query);
            statement.execute("ALTER TABLE courses ADD COLUMN IF NOT EXISTS educator_id INT NOT NULL DEFAULT 0");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Course mapFromResultSet(ResultSet resultSet) throws SQLException {
        Course course = new Course();
        course.setId(resultSet.getInt("id"));
        course.setName(resultSet.getString("name"));
        course.setLearningPathId(resultSet.getInt("learning_path_id"));
        course.setLearningPathName(resultSet.getString("learning_path_name"));
        course.setEducatorId(resultSet.getInt("educator_id"));
        return course;
    }

    private static int findNextAvailableId(Connection connection) throws SQLException {
        String query = "SELECT COALESCE(MIN(t.next_id), 1) AS next_id " +
                "FROM (" +
                " SELECT 1 AS next_id WHERE NOT EXISTS (SELECT 1 FROM courses WHERE id = 1)" +
                " UNION ALL" +
                " SELECT c1.id + 1 AS next_id" +
                " FROM courses c1" +
                " LEFT JOIN courses c2 ON c2.id = c1.id + 1" +
                " WHERE c2.id IS NULL" +
                ") t";

        PreparedStatement prS = connection.prepareStatement(query);
        ResultSet rs = prS.executeQuery();
        if (rs.next()) {
            return rs.getInt("next_id");
        }
        return 1;
    }

    private static void syncIdSequence(Connection connection) throws SQLException {
        String query = "SELECT setval(pg_get_serial_sequence('courses', 'id'), COALESCE((SELECT MAX(id) FROM courses), 0) + 1, false)";
        PreparedStatement prS = connection.prepareStatement(query);
        prS.execute();
    }
}

