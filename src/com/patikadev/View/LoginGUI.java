package com.patikadev.View;

import com.patikadev.Helper.Config;
import com.patikadev.Helper.Helper;
import com.patikadev.Model.Admin;
import com.patikadev.Model.User;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class LoginGUI extends JFrame {
    private JTextField field_username;
    private JPasswordField field_password;
    private JButton button_login;

    public LoginGUI() {
        setTitle(Config.PROJECT_TITLE + " - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 360);
        setLocationRelativeTo(null);
        setContentPane(buildContent());
        setVisible(true);

        button_login.addActionListener(e -> login());
        field_password.addActionListener(e -> login());
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel branding = new JPanel(new BorderLayout());
        branding.add(createLogoLabel(), BorderLayout.CENTER);
        root.add(branding, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username"), gbc);

        field_username = new JTextField();
        field_username.setPreferredSize(new Dimension(190, 28));
        gbc.gridx = 1;
        form.add(field_username, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(new JLabel("Password"), gbc);

        field_password = new JPasswordField();
        field_password.setPreferredSize(new Dimension(190, 28));
        gbc.gridx = 1;
        form.add(field_password, gbc);

        JPanel formWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        formWrapper.add(form);
        root.add(formWrapper, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        button_login = new JButton("Login");
        actions.add(button_login);
        root.add(actions, BorderLayout.SOUTH);

        return root;
    }

    private JLabel createLogoLabel() {
        String[] resourcePaths = {
                "/com/patikadev/resources/patika-login.webp",
                "/com/patikadev/resources/patika_logo.png",
                "/patika-login.webp",
                "/patika_logo.png",
                "/logo.png"
        };

        for (String path : resourcePaths) {
            URL resource = getClass().getResource(path);
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                Image scaledImage = icon.getImage().getScaledInstance(300, 120, Image.SCALE_SMOOTH);
                JLabel logo = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
                logo.setPreferredSize(new Dimension(300, 120));
                return logo;
            }
        }

        String[] filePaths = {
                "src/com/patikadev/resources/patika-login.webp",
                "src/com/patikadev/resources/patika_logo.png",
                "patika-login.webp",
                "patika_logo.png",
                "logo.png"
        };

        for (String path : filePaths) {
            File file = new File(path);
            if (file.exists()) {
                ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                Image scaledImage = icon.getImage().getScaledInstance(300, 120, Image.SCALE_SMOOTH);
                JLabel logo = new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
                logo.setPreferredSize(new Dimension(300, 120));
                return logo;
            }
        }

        JLabel fallback = new JLabel("Patika.dev", SwingConstants.CENTER);
        fallback.setFont(fallback.getFont().deriveFont(Font.BOLD, 24f));
        fallback.setPreferredSize(new Dimension(300, 90));
        return fallback;
    }

    private void login() {
        if (Helper.isFieldEmpty(field_username) || new String(field_password.getPassword()).trim().isEmpty()) {
            Helper.showMessage("fill");
            return;
        }

        String userName = field_username.getText().trim();
        String password = new String(field_password.getPassword());

        if (!User.isLowerCaseUsername(userName)) {
            Helper.showMessage("Username must be lowercase. Capital letters are not allowed.");
            return;
        }

        User user = User.getFetch(userName, password);
        if (user == null) {
            Helper.showMessage("Username or password is incorrect.");
            return;
        }

        openRoleMainScreen(user);
        dispose();
    }

    private void openRoleMainScreen(User user) {
        String userType = user.getUserType() == null ? "" : user.getUserType().toLowerCase();

        switch (userType) {
            case "admin" -> {
                Admin admin = new Admin();
                admin.setId(user.getId());
                admin.setName(user.getName());
                admin.setUserName(user.getUserName());
                admin.setPassword(user.getPassword());
                admin.setUserType(user.getUserType());
                new AdminGUI(admin);
            }
            case "educator" -> new EducatorGUI(user);
            case "student" -> new StudentGUI(user);
            default -> {
                Helper.showMessage("Unknown user type: " + user.getUserType());
                new LoginGUI();
            }
        }
    }
}

