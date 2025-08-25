package com.patikadev.Helper;

import javax.swing.*;

public class Helper {
    public static void setLayout(){
        for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()){
            if("Nimbus".equals(info.getName())){
                try {
                    UIManager.setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                         UnsupportedLookAndFeelException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }
    public static boolean isFieldEmpty(JTextField field){
        return field.getText().trim().isEmpty();
    }

    public static void showMessage(String str){
        String message;
        String title;

        switch (str){
            case "fill" -> {
                message = "Please fill all fields!";
                title = "Error!";
            }
            case "done" -> {
                message = "Operation is Successful.";
                title = "Well Done!";
            }
            case "error" -> {
                message = "An error occurred";
                title = "Error!";
            }
            default -> {
                message = str;
                title = "Message";
            }
        }
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
