package com.patikadev.View;

import com.patikadev.Helper.Config;
import com.patikadev.Helper.Helper;
import com.patikadev.Model.Course;
import com.patikadev.Model.LearningPath;
import com.patikadev.Model.StudentEnrollment;
import com.patikadev.Model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StudentGUI extends JFrame {
    private static final String CARD_ALL_PATHS = "card_all_paths";
    private static final String CARD_MY_ENROLLMENTS = "card_my_enrollments";

    private final User student;
    private final DefaultTableModel modelAllPaths;
    private final DefaultTableModel modelPathCourses;
    private final DefaultTableModel modelMyEnrollments;
    private final JTable tableAllPaths;
    private final JTable tablePathCourses;
    private final JTable tableMyEnrollments;

    public StudentGUI(User user) {
        this.student = user;

        setTitle(Config.PROJECT_TITLE + " - Student");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel welcomeRow = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome " + student.getName());
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            new LoginGUI();
            dispose();
        });
        welcomeRow.add(welcome, BorderLayout.WEST);
        welcomeRow.add(logout, BorderLayout.EAST);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton buttonAllPaths = new JButton("All Learning Paths");
        JButton buttonMyEnrollments = new JButton("My Enrollments");
        nav.add(buttonAllPaths);
        nav.add(buttonMyEnrollments);

        top.add(welcomeRow);
        top.add(Box.createVerticalStrut(4));
        top.add(nav);

        JPanel cards = new JPanel(new CardLayout());

        modelAllPaths = new DefaultTableModel(new Object[]{"ID", "Learning Path"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableAllPaths = new JTable(modelAllPaths);
        tableAllPaths.getTableHeader().setReorderingAllowed(false);

        modelPathCourses = new DefaultTableModel(new Object[]{"ID", "Course Name"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablePathCourses = new JTable(modelPathCourses);
        tablePathCourses.getTableHeader().setReorderingAllowed(false);

        JPanel allPathsPanel = new JPanel(new BorderLayout(0, 8));
        JPanel enrollActions = new JPanel(new BorderLayout());
        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton buttonRefreshPaths = new JButton("Refresh");
        buttonRefreshPaths.addActionListener(e -> {
            loadAllLearningPaths();
            loadCoursesForSelectedPath();
        });
        leftActions.add(buttonRefreshPaths);

        JButton buttonEnroll = new JButton("Enroll");
        buttonEnroll.addActionListener(e -> enrollSelectedPath());
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.add(buttonEnroll);

        enrollActions.add(leftActions, BorderLayout.WEST);
        enrollActions.add(rightActions, BorderLayout.EAST);
        allPathsPanel.add(enrollActions, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tableAllPaths), new JScrollPane(tablePathCourses));
        splitPane.setResizeWeight(0.55);
        allPathsPanel.add(splitPane, BorderLayout.CENTER);
        cards.add(allPathsPanel, CARD_ALL_PATHS);

        modelMyEnrollments = new DefaultTableModel(new Object[]{"Enrollment ID", "Learning Path", "Enrolled At", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableMyEnrollments = new JTable(modelMyEnrollments);
        tableMyEnrollments.getTableHeader().setReorderingAllowed(false);

        JPanel myEnrollmentsPanel = new JPanel(new BorderLayout(0, 8));
        JPanel unenrollActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton buttonUnenroll = new JButton("Remove Selected");
        buttonUnenroll.addActionListener(e -> removeSelectedEnrollment());
        unenrollActions.add(buttonUnenroll);
        myEnrollmentsPanel.add(unenrollActions, BorderLayout.NORTH);
        myEnrollmentsPanel.add(new JScrollPane(tableMyEnrollments), BorderLayout.CENTER);
        cards.add(myEnrollmentsPanel, CARD_MY_ENROLLMENTS);

        CardLayout cardLayout = (CardLayout) cards.getLayout();
        buttonAllPaths.addActionListener(e -> cardLayout.show(cards, CARD_ALL_PATHS));
        buttonMyEnrollments.addActionListener(e -> {
            cardLayout.show(cards, CARD_MY_ENROLLMENTS);
            loadMyEnrollments();
        });

        tableAllPaths.getSelectionModel().addListSelectionListener(e -> loadCoursesForSelectedPath());

        root.add(top, BorderLayout.NORTH);
        root.add(cards, BorderLayout.CENTER);
        setContentPane(root);

        loadAllLearningPaths();
        loadCoursesForSelectedPath();
        loadMyEnrollments();
        cardLayout.show(cards, CARD_ALL_PATHS);

        setVisible(true);
    }

    private void loadAllLearningPaths() {
        modelAllPaths.setRowCount(0);
        for (LearningPath path : LearningPath.getList()) {
            modelAllPaths.addRow(new Object[]{path.getId(), path.getName()});
        }
    }

    private void loadMyEnrollments() {
        modelMyEnrollments.setRowCount(0);
        for (StudentEnrollment enrollment : StudentEnrollment.getByStudent(student.getId())) {
            modelMyEnrollments.addRow(new Object[]{
                    enrollment.getId(),
                    enrollment.getLearningPathName(),
                    enrollment.getEnrolledAt(),
                    "Enrolled"
            });
        }
    }

    private void loadCoursesForSelectedPath() {
        modelPathCourses.setRowCount(0);

        int selectedRow = tableAllPaths.getSelectedRow();
        if (selectedRow < 0 && tableAllPaths.getRowCount() > 0) {
            tableAllPaths.setRowSelectionInterval(0, 0);
            selectedRow = 0;
        }

        if (selectedRow < 0) {
            return;
        }

        int learningPathId = Integer.parseInt(tableAllPaths.getValueAt(selectedRow, 0).toString());
        for (Course course : Course.getListByLearningPathId(learningPathId)) {
            modelPathCourses.addRow(new Object[]{course.getId(), course.getName()});
        }
    }

    private void enrollSelectedPath() {
        int selectedRow = tableAllPaths.getSelectedRow();
        if (selectedRow < 0) {
            Helper.showMessage("Please select a learning path first.");
            return;
        }

        int learningPathId = Integer.parseInt(tableAllPaths.getValueAt(selectedRow, 0).toString());
        if (StudentEnrollment.enroll(student.getId(), learningPathId)) {
            Helper.showMessage("done");
            loadMyEnrollments();
            loadCoursesForSelectedPath();
        } else {
            Helper.showMessage("You are already enrolled in this learning path.");
        }
    }

    private void removeSelectedEnrollment() {
        int selectedRow = tableMyEnrollments.getSelectedRow();
        if (selectedRow < 0) {
            Helper.showMessage("Please select an enrolled learning path first.");
            return;
        }

        int enrollmentId = Integer.parseInt(tableMyEnrollments.getValueAt(selectedRow, 0).toString());
        if (StudentEnrollment.unenroll(enrollmentId, student.getId())) {
            Helper.showMessage("done");
            loadMyEnrollments();
            loadCoursesForSelectedPath();
        } else {
            Helper.showMessage("error");
        }
    }
}

