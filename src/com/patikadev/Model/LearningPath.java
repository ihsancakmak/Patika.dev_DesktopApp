package com.patikadev.Model;

import com.patikadev.Helper.DBConnector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class LearningPath {
    private int id;
    private String name;

    public LearningPath() {
    }

    public LearningPath(int id, String name) {
        this.id = id;
        this.name = name;
    }

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

    public static ArrayList<LearningPath> getList() {
        ensureTable();
        ArrayList<LearningPath> learningPaths = new ArrayList<>();
        String query = "SELECT id, name FROM learning_paths ORDER BY id";

        try {
            Statement statement = DBConnector.getInstance().createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                LearningPath learningPath = new LearningPath();
                learningPath.setId(resultSet.getInt("id"));
                learningPath.setName(resultSet.getString("name"));
                learningPaths.add(learningPath);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return learningPaths;
    }

    public static boolean add(String name) {
        ensureTable();
        if (containsSameName(name, null)) {
            return false;
        }
        String query = "INSERT INTO learning_paths (name) VALUES (?)";

        try {
            PreparedStatement preparedStatement = DBConnector.getInstance().prepareStatement(query);
            preparedStatement.setString(1, name);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public static boolean update(int id, String name) {
        ensureTable();
        if (containsSameName(name, id)) {
            return false;
        }

        String query = "UPDATE learning_paths SET name = ? WHERE id = ?";
        try {
            PreparedStatement preparedStatement = DBConnector.getInstance().prepareStatement(query);
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public static boolean delete(int id) {
        ensureTable();
        String query = "DELETE FROM learning_paths WHERE id = ?";

        try {
            PreparedStatement preparedStatement = DBConnector.getInstance().prepareStatement(query);
            preparedStatement.setInt(1, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public static boolean containsSameName(String name, Integer ignoreId) {
        ensureTable();
        String query = "SELECT id FROM learning_paths WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))";
        if (ignoreId != null) {
            query += " AND id <> ?";
        }

        try {
            PreparedStatement preparedStatement = DBConnector.getInstance().prepareStatement(query);
            preparedStatement.setString(1, name);
            if (ignoreId != null) {
                preparedStatement.setInt(2, ignoreId);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void ensureTable() {
        String query = "CREATE TABLE IF NOT EXISTS learning_paths (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL" +
                ")";

        try {
            Statement statement = DBConnector.getInstance().createStatement();
            statement.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

