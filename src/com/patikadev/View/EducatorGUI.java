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

public class EducatorGUI extends JFrame {
    private static final String CARD_LEARNING_PATHS = "card_learning_paths";
    private static final String CARD_COURSES = "card_courses";
    private static final String CARD_STUDENTS = "card_students";
    private static final String CARD_PATH_ENROLLMENTS = "card_path_enrollments";

    private final User educator;
    private final DefaultTableModel modelLearningPaths;
    private final DefaultTableModel modelCourses;
    private final DefaultTableModel modelStudents;
    private final DefaultTableModel modelPathEnrollments;
    private final JTable tableLearningPaths;
    private final JTable tableCourses;
    private final JTable tableStudents;
    private final JTable tablePathEnrollments;
    private final JTextField fieldCourseId;
    private final JTextField fieldCourseName;
    private final JComboBox<String> comboCourseLearningPath;
    private final JLabel labelSelectedPath;

    public EducatorGUI(User user) {
        this.educator = user;

        setTitle(Config.PROJECT_TITLE + " - Educator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel welcomeRow = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome " + educator.getName());
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            new LoginGUI();
            dispose();
        });
        welcomeRow.add(welcome, BorderLayout.WEST);
        welcomeRow.add(logout, BorderLayout.EAST);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton buttonLearningPaths = new JButton("Learning Paths");
        JButton buttonCourses = new JButton("Courses");
        JButton buttonStudents = new JButton("All Students");
        JButton buttonPathEnrollments = new JButton("Path Enrollments");
        nav.add(buttonLearningPaths);
        nav.add(buttonCourses);
        nav.add(buttonStudents);
        nav.add(buttonPathEnrollments);

        top.add(welcomeRow);
        top.add(Box.createVerticalStrut(4));
        top.add(nav);

        JPanel cards = new JPanel(new CardLayout());

        modelLearningPaths = new DefaultTableModel(new Object[]{"ID", "Learning Path"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableLearningPaths = new JTable(modelLearningPaths);
        tableLearningPaths.getTableHeader().setReorderingAllowed(false);
        cards.add(new JScrollPane(tableLearningPaths), CARD_LEARNING_PATHS);

        modelCourses = new DefaultTableModel(new Object[]{"ID", "Course Name", "Learning Path"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableCourses = new JTable(modelCourses);
        tableCourses.getTableHeader().setReorderingAllowed(false);

        JPanel coursesPanel = new JPanel(new BorderLayout(0, 8));
        JPanel courseActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        fieldCourseId = new JTextField();
        fieldCourseId.setPreferredSize(new Dimension(50, 28));
        fieldCourseId.setEnabled(false);
        fieldCourseName = new JTextField();
        fieldCourseName.setPreferredSize(new Dimension(170, 28));
        comboCourseLearningPath = new JComboBox<>();
        comboCourseLearningPath.setPreferredSize(new Dimension(210, 28));

        JButton addCourse = new JButton("Add");
        JButton editCourse = new JButton("Edit");
        JButton deleteCourse = new JButton("Delete");

        addCourse.addActionListener(e -> addCourse());
        editCourse.addActionListener(e -> editCourse());
        deleteCourse.addActionListener(e -> deleteCourse());

        courseActions.add(new JLabel("ID"));
        courseActions.add(fieldCourseId);
        courseActions.add(new JLabel("Course Name"));
        courseActions.add(fieldCourseName);
        courseActions.add(new JLabel("Learning Path"));
        courseActions.add(comboCourseLearningPath);
        courseActions.add(addCourse);
        courseActions.add(editCourse);
        courseActions.add(deleteCourse);
        coursesPanel.add(courseActions, BorderLayout.NORTH);
        coursesPanel.add(new JScrollPane(tableCourses), BorderLayout.CENTER);
        cards.add(coursesPanel, CARD_COURSES);

        modelStudents = new DefaultTableModel(new Object[]{"ID", "Full Name", "Username"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableStudents = new JTable(modelStudents);
        tableStudents.getTableHeader().setReorderingAllowed(false);
        cards.add(new JScrollPane(tableStudents), CARD_STUDENTS);

        modelPathEnrollments = new DefaultTableModel(new Object[]{"Enrollment ID", "Student ID", "Full Name", "Username", "Enrolled At"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablePathEnrollments = new JTable(modelPathEnrollments);
        tablePathEnrollments.getTableHeader().setReorderingAllowed(false);
        JPanel pathEnrollmentsPanel = new JPanel(new BorderLayout(0, 8));
        JPanel pathEnrollmentsActions = new JPanel(new BorderLayout());
        labelSelectedPath = new JLabel("Selected Path: -");
        JButton buttonReloadPathEnrollments = new JButton("Reload Selected Path");
        buttonReloadPathEnrollments.addActionListener(e -> loadPathEnrollmentsBySelection());
        pathEnrollmentsActions.add(labelSelectedPath, BorderLayout.WEST);
        pathEnrollmentsActions.add(buttonReloadPathEnrollments, BorderLayout.EAST);
        pathEnrollmentsPanel.add(pathEnrollmentsActions, BorderLayout.NORTH);
        pathEnrollmentsPanel.add(new JScrollPane(tablePathEnrollments), BorderLayout.CENTER);
        cards.add(pathEnrollmentsPanel, CARD_PATH_ENROLLMENTS);

        CardLayout cardLayout = (CardLayout) cards.getLayout();
        buttonLearningPaths.addActionListener(e -> cardLayout.show(cards, CARD_LEARNING_PATHS));
        buttonCourses.addActionListener(e -> {
            cardLayout.show(cards, CARD_COURSES);
            loadCourses();
        });
        buttonStudents.addActionListener(e -> {
            cardLayout.show(cards, CARD_STUDENTS);
            loadStudents();
        });
        buttonPathEnrollments.addActionListener(e -> {
            cardLayout.show(cards, CARD_PATH_ENROLLMENTS);
            loadPathEnrollmentsBySelection();
        });

        tableCourses.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = tableCourses.getSelectedRow();
            if (selectedRow < 0) {
                return;
            }

            fieldCourseId.setText(tableCourses.getValueAt(selectedRow, 0).toString());
            fieldCourseName.setText(tableCourses.getValueAt(selectedRow, 1).toString());
            comboCourseLearningPath.setSelectedItem(tableCourses.getValueAt(selectedRow, 2).toString());
        });

        root.add(top, BorderLayout.NORTH);
        root.add(cards, BorderLayout.CENTER);
        setContentPane(root);

        loadLearningPaths();
        loadCourseLearningPathOptions();
        loadCourses();
        loadStudents();
        loadPathEnrollmentsBySelection();
        cardLayout.show(cards, CARD_LEARNING_PATHS);

        setVisible(true);
    }

    private void loadLearningPaths() {
        modelLearningPaths.setRowCount(0);
        for (LearningPath path : LearningPath.getList()) {
            modelLearningPaths.addRow(new Object[]{path.getId(), path.getName()});
        }
    }

    private void loadCourses() {
        modelCourses.setRowCount(0);
        for (Course course : Course.getListByEducator(educator.getId())) {
            modelCourses.addRow(new Object[]{
                    course.getId(),
                    course.getName(),
                    course.getLearningPathName()
            });
        }
    }

    private void loadCourseLearningPathOptions() {
        comboCourseLearningPath.removeAllItems();
        for (LearningPath path : LearningPath.getList()) {
            comboCourseLearningPath.addItem(path.getName());
        }
    }

    private int getSelectedLearningPathId() {
        Object selectedPathName = comboCourseLearningPath.getSelectedItem();
        if (selectedPathName == null) {
            return -1;
        }

        for (LearningPath path : LearningPath.getList()) {
            if (path.getName().equals(selectedPathName.toString())) {
                return path.getId();
            }
        }

        return -1;
    }

    private void addCourse() {
        if (Helper.isFieldEmpty(fieldCourseName)) {
            Helper.showMessage("Please enter a course name.");
            return;
        }

        int learningPathId = getSelectedLearningPathId();
        if (learningPathId < 0) {
            Helper.showMessage("Please select a learning path.");
            return;
        }

        if (Course.add(fieldCourseName.getText().trim(), learningPathId, educator.getId())) {
            Helper.showMessage("done");
            clearCourseFields();
            loadCourses();
        } else {
            Helper.showMessage("error");
        }
    }

    private void editCourse() {
        if (Helper.isFieldEmpty(fieldCourseId) || Helper.isFieldEmpty(fieldCourseName)) {
            Helper.showMessage("Please select a course to edit.");
            return;
        }

        int learningPathId = getSelectedLearningPathId();
        if (learningPathId < 0) {
            Helper.showMessage("Please select a learning path.");
            return;
        }

        int courseId = Integer.parseInt(fieldCourseId.getText());

        if (Course.update(courseId, fieldCourseName.getText().trim(), learningPathId, educator.getId())) {
            Helper.showMessage("done");
            clearCourseFields();
            loadCourses();
        } else {
            Helper.showMessage("error");
        }
    }

    private void deleteCourse() {
        if (Helper.isFieldEmpty(fieldCourseId)) {
            Helper.showMessage("Please select a course to delete.");
            return;
        }

        int courseId = Integer.parseInt(fieldCourseId.getText());
        if (Course.delete(courseId, educator.getId())) {
            Helper.showMessage("done");
            clearCourseFields();
            loadCourses();
        } else {
            Helper.showMessage("error");
        }
    }

    private void clearCourseFields() {
        fieldCourseId.setText("");
        fieldCourseName.setText("");
        tableCourses.clearSelection();
    }

    private void loadStudents() {
        modelStudents.setRowCount(0);
        for (User student : User.getList("", "", "student")) {
            modelStudents.addRow(new Object[]{student.getId(), student.getName(), student.getUserName()});
        }
    }

    private void loadPathEnrollmentsBySelection() {
        modelPathEnrollments.setRowCount(0);

        int selectedRow = tableLearningPaths.getSelectedRow();
        if (selectedRow < 0 && tableLearningPaths.getRowCount() > 0) {
            tableLearningPaths.setRowSelectionInterval(0, 0);
            selectedRow = 0;
        }

        if (selectedRow < 0) {
            labelSelectedPath.setText("Selected Path: -");
            return;
        }

        int learningPathId = Integer.parseInt(tableLearningPaths.getValueAt(selectedRow, 0).toString());
        String learningPathName = tableLearningPaths.getValueAt(selectedRow, 1).toString();
        labelSelectedPath.setText("Selected Path: " + learningPathName);

        for (StudentEnrollment enrollment : StudentEnrollment.getByLearningPath(learningPathId)) {
            modelPathEnrollments.addRow(new Object[]{
                    enrollment.getId(),
                    enrollment.getStudentId(),
                    enrollment.getStudentName(),
                    enrollment.getStudentUserName(),
                    enrollment.getEnrolledAt()
            });
        }
    }
}

