package com.patikadev.View;

import com.patikadev.Helper.Config;
import com.patikadev.Helper.Helper;
import com.patikadev.Model.Admin;
import com.patikadev.Model.LearningPath;
import com.patikadev.Model.User;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
 import java.awt.ContainerOrderFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class AdminGUI extends JFrame {
    private static final String CARD_USERS = "card_users";
    private static final String CARD_LEARNING_PATHS = "card_learning_paths";

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
    private JPanel panel_user_search;
    private JTextField field_search_full_name;
    private JTextField field_search_username;
    private JComboBox combo_search_user_type;
    private JButton button_search;
    private DefaultTableModel model_user_list;
    private Object[] row_user_list;

    private JPanel panel_content_cards;
    private JPanel panel_search_row;
    private JComponent panel_search_spacer;
    private JPanel panel_welcome_row;
    private JPanel panel_page_navigation;
    private JButton button_users_page;
    private JButton button_learning_paths_page;
    private JPanel panel_learning_paths;
    private JTextField field_learning_path_name;
    private JTextField field_learning_path_id;
    private JButton button_learning_path_add;
    private JButton button_learning_path_update;
    private JButton button_learning_path_delete;
    private JTable table_learning_paths;
    private DefaultTableModel model_learning_paths;
    private Object[] row_learning_path;

    private final Admin admin;

    public AdminGUI(Admin admin){
        this.admin = admin;

        add(main_panel);
        setupMainLayout();
        setSize(700,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Avoid LayoutComparator ClassCastException with dynamically re-parented controls.
        setFocusTraversalPolicy(new ContainerOrderFocusTraversalPolicy());
        setTitle(Config.PROJECT_TITLE);
        setVisible(true);

        label_welcome.setText("Welcome " + admin.getName());
        setupSearchBar();
        setupAdminPages();

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

                if (!User.isLowerCaseUsername(username)) {
                    Helper.showMessage("Username must be lowercase. Capital letters are not allowed.");
                    applySearch();
                    return;
                }

                if(User.update(user_id,user_name,username,password,usertype)){
                    Helper.showMessage("done");

                }
                applySearch();
            }

        });

        button_add.addActionListener(e -> {
            if(Helper.isFieldEmpty(field_user_name) || Helper.isFieldEmpty(field_username) || Helper.isFieldEmpty(field_password)){
                Helper.showMessage("fill");
            }
            else {
                String name = field_user_name.getText();
                String userName = field_username.getText().trim();
                String password = field_password.getText();
                String userType = combo_user_type.getSelectedItem() == null ? "student" : combo_user_type.getSelectedItem().toString();

                if (!User.isLowerCaseUsername(userName)) {
                    Helper.showMessage("Username must be lowercase. Capital letters are not allowed.");
                    return;
                }

                if(User.add(name, userName, password, userType)){
                    Helper.showMessage("done");
                    applySearch();
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
                    applySearch();

                }
                else {
                    Helper.showMessage("error");
                }
            }
        });

        button_search.addActionListener(e -> applySearch());
        button_logout.addActionListener(e -> {
            new LoginGUI();
            dispose();
        });
    }

    private void setupMainLayout() {
        // Use a simple top/content split so the top panel height does not steal space from cards.
        main_panel.removeAll();
        main_panel.setLayout(new BorderLayout(0, 6));
        main_panel.add(panel_top, BorderLayout.NORTH);
        main_panel.add(tab_admin, BorderLayout.CENTER);
    }

    private void setupSearchBar(){
        if(field_search_full_name == null){
            field_search_full_name = new JTextField();
        }
        if(field_search_username == null){
            field_search_username = new JTextField();
        }
        if(combo_search_user_type == null){
            combo_search_user_type = new JComboBox();
        }
        if(button_search == null){
            button_search = new JButton("Search");
        }

        if(combo_search_user_type.getItemCount() == 0){
            combo_search_user_type.addItem("all");
            combo_search_user_type.addItem("student");
            combo_search_user_type.addItem("educator");
            combo_search_user_type.addItem("admin");
        }

        field_search_full_name.setPreferredSize(new Dimension(130, 28));
        field_search_username.setPreferredSize(new Dimension(130, 28));
        combo_search_user_type.setPreferredSize(new Dimension(110, 28));
        button_search.setText("Search");

        panel_welcome_row = new JPanel(new BorderLayout());
        panel_welcome_row.add(label_welcome, BorderLayout.WEST);
        panel_welcome_row.add(button_logout, BorderLayout.EAST);

        panel_search_row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel_search_row.add(new JLabel("Full Name"));
        panel_search_row.add(field_search_full_name);
        panel_search_row.add(new JLabel("Username"));
        panel_search_row.add(field_search_username);
        panel_search_row.add(new JLabel("User Type"));
        panel_search_row.add(combo_search_user_type);
        panel_search_row.add(button_search);

        rebuildTopSection();
    }

    private void rebuildTopSection() {
        panel_top.removeAll();
        panel_top.setLayout(new BoxLayout(panel_top, BoxLayout.Y_AXIS));

        if (panel_welcome_row != null) {
            panel_top.add(panel_welcome_row);
        }

        if (panel_page_navigation != null) {
            panel_top.add(Box.createVerticalStrut(2));
            panel_top.add(panel_page_navigation);
        }

        if (panel_search_row != null) {
            panel_search_spacer = (JComponent) Box.createVerticalStrut(2);
            panel_top.add(panel_search_spacer);
            panel_top.add(panel_search_row);
        }

        panel_top.revalidate();
        panel_top.repaint();
    }

    private void setupAdminPages() {
        JPanel panel_users = new JPanel(new BorderLayout(10, 10));
        panel_users.add(scrll_user_list, BorderLayout.CENTER);
        panel_users.add(panel_user_add_delete, BorderLayout.EAST);

        panel_learning_paths = new JPanel(new BorderLayout(10, 10));
        JPanel panel_learning_path_add = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        field_learning_path_id = new JTextField();
        field_learning_path_id.setPreferredSize(new Dimension(60, 30));
        field_learning_path_id.setEnabled(false);
        field_learning_path_name = new JTextField();
        field_learning_path_name.setPreferredSize(new Dimension(300, 30));
        button_learning_path_add = new JButton("Add");
        button_learning_path_update = new JButton("Edit");
        button_learning_path_delete = new JButton("Delete");

        panel_learning_path_add.add(new JLabel("ID"));
        panel_learning_path_add.add(field_learning_path_id);
        panel_learning_path_add.add(new JLabel("Learning Path"));
        panel_learning_path_add.add(field_learning_path_name);
        panel_learning_path_add.add(button_learning_path_add);
        panel_learning_path_add.add(button_learning_path_update);
        panel_learning_path_add.add(button_learning_path_delete);

        model_learning_paths = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        model_learning_paths.setColumnIdentifiers(new Object[]{"ID", "Learning Path"});
        row_learning_path = new Object[2];
        table_learning_paths = new JTable(model_learning_paths);
        table_learning_paths.getTableHeader().setReorderingAllowed(false);

        JPanel panel_learning_paths_main = new JPanel(new GridLayout(1, 1));
        panel_learning_paths_main.add(new JScrollPane(table_learning_paths));

        panel_learning_paths.add(panel_learning_path_add, BorderLayout.NORTH);
        panel_learning_paths.add(panel_learning_paths_main, BorderLayout.CENTER);

        button_users_page = new JButton("Users");
        button_learning_paths_page = new JButton("Learning Paths");

        panel_page_navigation = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        panel_page_navigation.add(button_users_page);
        panel_page_navigation.add(button_learning_paths_page);

        panel_content_cards = new JPanel(new CardLayout());
        panel_content_cards.add(panel_users, CARD_USERS);
        panel_content_cards.add(panel_learning_paths, CARD_LEARNING_PATHS);

        tab_admin.removeAll();
        tab_admin.setLayout(new BorderLayout());
        tab_admin.add(panel_content_cards, BorderLayout.CENTER);
        tab_admin.revalidate();
        tab_admin.repaint();
        rebuildTopSection();

        button_users_page.addActionListener(e -> switchPage(CARD_USERS));
        button_learning_paths_page.addActionListener(e -> {
            switchPage(CARD_LEARNING_PATHS);
            loadLearningPathModel();
        });
        button_learning_path_add.addActionListener(e -> addLearningPath());
        button_learning_path_update.addActionListener(e -> updateLearningPath());
        button_learning_path_delete.addActionListener(e -> deleteLearningPath());

        table_learning_paths.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = table_learning_paths.getSelectedRow();
            if (selectedRow >= 0) {
                field_learning_path_id.setText(table_learning_paths.getValueAt(selectedRow, 0).toString());
                field_learning_path_name.setText(table_learning_paths.getValueAt(selectedRow, 1).toString());
            }
        });

        switchPage(CARD_USERS);
    }

    private void switchPage(String pageName) {
        CardLayout cardLayout = (CardLayout) panel_content_cards.getLayout();
        cardLayout.show(panel_content_cards, pageName);

        boolean isUsersPage = CARD_USERS.equals(pageName);
        if (panel_search_row != null) {
            panel_search_row.setVisible(isUsersPage);
        }
        if (panel_search_spacer != null) {
            panel_search_spacer.setVisible(isUsersPage);
        }
        panel_top.revalidate();
        panel_top.repaint();
    }

    private void addLearningPath() {
        if (Helper.isFieldEmpty(field_learning_path_name)) {
            Helper.showMessage("Please enter a learning path name.");
            return;
        }

        if (LearningPath.add(field_learning_path_name.getText().trim())) {
            Helper.showMessage("done");
            clearLearningPathFields();
            loadLearningPathModel();
        } else {
            Helper.showMessage("This learning path name is already exists.");
        }
    }

    private void updateLearningPath() {
        if (Helper.isFieldEmpty(field_learning_path_id) || Helper.isFieldEmpty(field_learning_path_name)) {
            Helper.showMessage("Please select a learning path to edit.");
            return;
        }

        int pathId = Integer.parseInt(field_learning_path_id.getText());
        String pathName = field_learning_path_name.getText().trim();

        if (LearningPath.update(pathId, pathName)) {
            Helper.showMessage("done");
            clearLearningPathFields();
            loadLearningPathModel();
        } else {
            Helper.showMessage("This learning path name is already exists.");
        }
    }

    private void deleteLearningPath() {
        if (Helper.isFieldEmpty(field_learning_path_id)) {
            Helper.showMessage("Please select a learning path to delete.");
            return;
        }

        int pathId = Integer.parseInt(field_learning_path_id.getText());
        if (LearningPath.delete(pathId)) {
            Helper.showMessage("done");
            clearLearningPathFields();
            loadLearningPathModel();
        } else {
            Helper.showMessage("error");
        }
    }

    private void clearLearningPathFields() {
        field_learning_path_id.setText(null);
        field_learning_path_name.setText(null);
        table_learning_paths.clearSelection();
    }

    private void loadLearningPathModel() {
        model_learning_paths.setRowCount(0);
        for (LearningPath learningPath : LearningPath.getList()) {
            row_learning_path[0] = learningPath.getId();
            row_learning_path[1] = learningPath.getName();
            model_learning_paths.addRow(row_learning_path);
        }
    }

    public void loadUserModel(){
        loadUserModel("", "", "all");
    }

    public void loadUserModel(String fullName, String userName, String userType){
        DefaultTableModel clearModel = (DefaultTableModel) table_user_list.getModel();
        clearModel.setRowCount(0);

        for(User obj : User.getList(fullName, userName, userType)){
            int i = 0;
            row_user_list[i++] = obj.getId();
            row_user_list[i++] = obj.getName();
            row_user_list[i++] = obj.getUserName();
            row_user_list[i++] = obj.getPassword();
            row_user_list[i++] = obj.getUserType();
            model_user_list.addRow(row_user_list);
        }
    }

    private void applySearch(){
        String fullName = field_search_full_name.getText();
        String userName = field_search_username.getText();
        String userType = combo_search_user_type.getSelectedItem() == null ? "all" : combo_search_user_type.getSelectedItem().toString();
        loadUserModel(fullName, userName, userType);
    }

    public static void main(String[] args) {
        Helper.setLayout();
        new LoginGUI();

    }
}
