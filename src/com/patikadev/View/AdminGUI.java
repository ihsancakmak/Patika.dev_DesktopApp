package com.patikadev.View;

import com.patikadev.Helper.Config;
import com.patikadev.Helper.Helper;
import com.patikadev.Model.Admin;
import com.patikadev.Model.User;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminGUI extends JFrame {
    private JPanel main_panel;
    private JLabel label_welcome;
    private JPanel panel_top;
    private JButton button_logout;
    private JPanel tab_admin;
    private JLabel label_users;
    private JScrollPane scrll_user_list;
    private JTable table_user_list;
    private JPanel panel_user_add_delete;
    private JTextField field_user_name;
    private JTextField field_username;
    private JTextField field_password;
    private JComboBox combo_user_type;
    private JButton button_add;
    private JTextField field_user_id;
    private JButton button_delete;
    private DefaultTableModel model_user_list;
    private Object[] row_user_list;

    private final Admin admin;

    public AdminGUI(Admin admin){
        this.admin = admin;

        add(main_panel);
        setSize(700,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle(Config.PROJECT_TITLE);
        setVisible(true);

        label_welcome.setText("Welcome " + admin.getName());

        // Model User List
        model_user_list = new DefaultTableModel(){
            @Override
            public boolean isCellEditable(int row, int column) {
                if(column == 0){
                    return false;
                }
                return super.isCellEditable(row, column);
            }
        };
        Object[] column_user_list = {"ID", "Full Name", "Username", "Password", "User Type"};
        model_user_list.setColumnIdentifiers(column_user_list);
        row_user_list = new Object[column_user_list.length];

        loadUserModel();

        table_user_list.setModel(model_user_list);
        table_user_list.getTableHeader().setReorderingAllowed(false);

        table_user_list.getSelectionModel().addListSelectionListener(e -> {
            try {
                String selected_user_id = table_user_list.getValueAt(table_user_list.getSelectedRow(), 0).toString();
                field_user_id.setText(selected_user_id);
            }
            catch (Exception exception){
                System.out.println(exception.getMessage());
            }
        });

        table_user_list.getModel().addTableModelListener(e -> {
            if(e.getType() == TableModelEvent.UPDATE){
                int user_id = Integer.parseInt(table_user_list.getValueAt(table_user_list.getSelectedRow(), 0).toString());
                String user_name = table_user_list.getValueAt(table_user_list.getSelectedRow(), 1).toString();
                String username = table_user_list.getValueAt(table_user_list.getSelectedRow(), 2).toString();
                String password = table_user_list.getValueAt(table_user_list.getSelectedRow(), 3).toString();
                String usertype = table_user_list.getValueAt(table_user_list.getSelectedRow(), 4).toString();

                if(User.update(user_id,user_name,username,password,usertype)){
                    Helper.showMessage("done");

                }
                loadUserModel();
            }

        });

        button_add.addActionListener(e -> {
            if(Helper.isFieldEmpty(field_user_name) || Helper.isFieldEmpty(field_username) || Helper.isFieldEmpty(field_password)){
                Helper.showMessage("fill");
            }
            else {
                String name = field_user_name.getText();
                String userName = field_username.getText();
                String password = field_password.getText();
                String userType = combo_user_type.getSelectedItem().toString();

                if(User.add(name, userName, password, userType)){
                    Helper.showMessage("done");
                    loadUserModel();
                }
                field_user_name.setText(null);
                field_username.setText(null);
                field_password.setText(null);

            }
        });
        button_delete.addActionListener(e -> {
            if(Helper.isFieldEmpty(field_user_id)){
                Helper.showMessage("Please enter a ID you want to delete.");
            }
            else {
                int user_id = Integer.parseInt(field_user_id.getText());
                if(User.delete(user_id)){
                    Helper.showMessage("done");
                    field_user_id.setText(null);
                    loadUserModel();

                }
                else {
                    Helper.showMessage("error");
                }
            }
        });
    }

    public void loadUserModel(){
        DefaultTableModel clearModel = (DefaultTableModel) table_user_list.getModel();
        clearModel.setRowCount(0);

        for(User obj : User.getList()){
            int i = 0;
            row_user_list[i++] = obj.getId();
            row_user_list[i++] = obj.getName();
            row_user_list[i++] = obj.getUserName();
            row_user_list[i++] = obj.getPassword();
            row_user_list[i++] = obj.getUserType();
            model_user_list.addRow(row_user_list);
        }
    }

    public static void main(String[] args) {
        Helper.setLayout();
        Admin admin1 = new Admin();


        admin1.setName("Ihsan Cakmak");

        AdminGUI adminGUI = new AdminGUI(admin1);

    }
}
