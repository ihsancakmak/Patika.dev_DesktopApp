package com.patikadev.Model;

import com.patikadev.Helper.DBConnector;
import com.patikadev.Helper.Helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class User {
    private int id;
    private static int idOfSameUser;
    private String name;
    private String userName;
    private String password;
    private String userType;
    public User(){

    }

    public User(int id, String name, String userName, String userType) {
        this.id = id;
        this.name = name;
        this.userName = userName;
        this.userType = userType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static ArrayList<User> getList(){
        ArrayList<User> userArrayList = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY id";
        User obj;
        try {
            Statement statement = DBConnector.getInstance().createStatement();
            ResultSet resultSet = statement.executeQuery(query);



            while (resultSet.next()){
                obj = new User();
                obj.setId(resultSet.getInt("id"));
                obj.setName(resultSet.getString("name"));
                obj.setUserName(resultSet.getString("username"));
                obj.setPassword(resultSet.getString("password"));
                obj.setUserType(resultSet.getString("usertype"));
                userArrayList.add(obj);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return userArrayList;
    }

    public static ArrayList<User> getList(String fullName, String userName, String userType){
        ArrayList<User> userArrayList = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM users WHERE 1=1");
        ArrayList<String> params = new ArrayList<>();

        if(fullName != null && !fullName.trim().isEmpty()){
            query.append(" AND LOWER(name) LIKE LOWER(?)");
            params.add("%" + fullName.trim() + "%");
        }

        if(userName != null && !userName.trim().isEmpty()){
            query.append(" AND LOWER(username) LIKE LOWER(?)");
            params.add("%" + userName.trim() + "%");
        }

        if(userType != null && !userType.trim().isEmpty() && !"all".equalsIgnoreCase(userType.trim())){
            query.append(" AND usertype = ?");
            params.add(userType.trim());
        }

        query.append(" ORDER BY id");

        User obj;
        try {
            PreparedStatement preparedStatement = DBConnector.getInstance().prepareStatement(query.toString());
            for(int i = 0; i < params.size(); i++){
                preparedStatement.setString(i + 1, params.get(i));
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                obj = new User();
                obj.setId(resultSet.getInt("id"));
                obj.setName(resultSet.getString("name"));
                obj.setUserName(resultSet.getString("username"));
                obj.setPassword(resultSet.getString("password"));
                obj.setUserType(resultSet.getString("usertype"));
                userArrayList.add(obj);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return userArrayList;
    }



    public static boolean add(String name, String userName, String password, String userType){
        String query = "INSERT INTO users (id,name,username,password,usertype) VALUES (?,?,?,?,?)";
        String normalizedUserName = userName == null ? "" : userName.trim();
        if (!isLowerCaseUsername(normalizedUserName)) {
            Helper.showMessage("Username must be lowercase. Capital letters are not allowed.");
            return false;
        }

        if(!containSameUser(normalizedUserName)){
            Connection connection = null;
            try {
                connection = DBConnector.getInstance();
                connection.setAutoCommit(false);

                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setInt(1, findNextAvailableId(connection));
                preparedStatement.setString(2, name);
                preparedStatement.setString(3, normalizedUserName);
                preparedStatement.setString(4, password);
                preparedStatement.setString(5, userType);

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
        }
        Helper.showMessage("This username is already taken! Please enter different username.");
        return false;

    }
    public static boolean containSameUser(String userName){
        String query = "SELECT * FROM users WHERE username = ?";
        String normalizedUserName = userName == null ? "" : userName.trim();

        try{
            PreparedStatement prS = DBConnector.getInstance().prepareStatement(query);
            prS.setString(1, normalizedUserName);
            ResultSet rs = prS.executeQuery();
            if(rs.next()){
                idOfSameUser = rs.getInt(1);
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
    public static boolean delete(int id){
        String query = "DELETE FROM users WHERE id = ?";
        String normalizeQuery = "UPDATE users SET id = id - 1 WHERE id > ?";
        Connection connection = null;
        try {
            connection = DBConnector.getInstance();
            connection.setAutoCommit(false);

            PreparedStatement prS = connection.prepareStatement(query);
            prS.setInt(1, id);

            int deletedRows = prS.executeUpdate();
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

    private static int findNextAvailableId(Connection connection) throws SQLException {
        String query = "SELECT COALESCE(MIN(t.next_id), 1) AS next_id " +
                "FROM (" +
                " SELECT 1 AS next_id WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1)" +
                " UNION ALL" +
                " SELECT u1.id + 1 AS next_id" +
                " FROM users u1" +
                " LEFT JOIN users u2 ON u2.id = u1.id + 1" +
                " WHERE u2.id IS NULL" +
                ") t";

        PreparedStatement prS = connection.prepareStatement(query);
        ResultSet rs = prS.executeQuery();
        if (rs.next()) {
            return rs.getInt("next_id");
        }
        return 1;
    }

    private static void syncIdSequence(Connection connection) throws SQLException {
        String query = "SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 0) + 1, false)";
        PreparedStatement prS = connection.prepareStatement(query);
        prS.execute();
    }

    public static boolean update(int id, String name, String username, String password, String userType){
        String query = "UPDATE users SET name=?, username=?, password=?, usertype=? WHERE id=?";
        String normalizedUserName = username == null ? "" : username.trim();

        if (!isLowerCaseUsername(normalizedUserName)) {
            Helper.showMessage("Username must be lowercase. Capital letters are not allowed.");
            return false;
        }


        if(containSameUser(normalizedUserName)){
            if(idOfSameUser != id){
                Helper.showMessage("This username is already taken! Please enter different username.");
                return false;
            }
        }
        try {
            PreparedStatement prS = DBConnector.getInstance().prepareStatement(query);
            prS.setString(1, name);
            prS.setString(2, normalizedUserName);
            prS.setString(3, password);
            prS.setString(4, userType);
            prS.setInt(5, id);
            return prS.executeUpdate() != -1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return true;
    }

    public static User getFetch(String userName, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        String normalizedUserName = userName == null ? "" : userName.trim();

        if (!isLowerCaseUsername(normalizedUserName)) {
            return null;
        }

        try {
            PreparedStatement prS = DBConnector.getInstance().prepareStatement(query);
            prS.setString(1, normalizedUserName);
            prS.setString(2, password);
            ResultSet rs = prS.executeQuery();

            if (rs.next()) {
                User obj = new User();
                obj.setId(rs.getInt("id"));
                obj.setName(rs.getString("name"));
                obj.setUserName(rs.getString("username"));
                obj.setPassword(rs.getString("password"));
                obj.setUserType(rs.getString("usertype"));
                return obj;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public static boolean isLowerCaseUsername(String username) {
        if (username == null) {
            return false;
        }

        String trimmed = username.trim();
        return !trimmed.isEmpty() && trimmed.equals(trimmed.toLowerCase());
    }




}