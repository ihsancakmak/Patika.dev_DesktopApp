package com.patikadev.Model;

import com.patikadev.Helper.DBConnector;
import com.patikadev.Helper.Helper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class User {
    private int id;
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
        String query = "SELECT * FROM users";
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



    public static boolean add(String name, String userName, String password, String userType){
        String query = "INSERT INTO users (name,username,password,usertype) VALUES (?,?,?,?)";
        if(!containSameUser(userName)){
            try {
                PreparedStatement preparedStatement = DBConnector.getInstance().prepareStatement(query);
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, userName);
                preparedStatement.setString(3, password);
                preparedStatement.setString(4, userType);
                return preparedStatement.executeUpdate() != -1;

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        Helper.showMessage("This username is already taken! Please enter different username.");
        return false;

    }
    public static boolean containSameUser(String userName){
        String query = "SELECT * FROM users WHERE username = ?";

        try{
            PreparedStatement prS = DBConnector.getInstance().prepareStatement(query);
            prS.setString(1, userName);
            ResultSet rs = prS.executeQuery();
            if(rs.next()){
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
    public static boolean delete(int id){
        String query = "DELETE FROM users WHERE id = ?";
        try {
            PreparedStatement prS = DBConnector.getInstance().prepareStatement(query);
            prS.setInt(1, id);
            return prS.executeUpdate() != -1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;

    }

    public static boolean update(int id, String name, String username, String password, String userType){
        String query = "UPDATE users SET name=?, username=?, password=?, usertype=? WHERE id=?";

        if(!containSameUser(username)){
            try {
                PreparedStatement prS = DBConnector.getInstance().prepareStatement(query);
                prS.setString(1, name);
                prS.setString(2, username);
                prS.setString(3, password);
                prS.setString(4, userType);
                prS.setInt(5, id);
                return prS.executeUpdate() != -1;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        Helper.showMessage("This username is already taken! Please enter different username.");
        return false;
    }




}