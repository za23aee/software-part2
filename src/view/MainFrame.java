package view;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame for the Healthcare Management System.
 * Uses JTabbedPane for navigation between different views.
 */
public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;

    public MainFrame() {
        initializeFrame();
        createMenuBar();
        createTabbedPane();
        createStatusBar();
    }

    private void initializeFrame() {
        setTitle("Healthcare Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout());
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem loadDataItem = new JMenuItem("Load Data");
        JMenuItem exitItem = new JMenuItem("Exit");

        loadDataItem.addActionListener(e -> loadData());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(loadDataItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();

        // Placeholder panels - will be replaced with actual implementations
        tabbedPane.addTab("Patients", createPlaceholderPanel("Patients"));
        tabbedPane.addTab("Clinicians", createPlaceholderPanel("Clinicians"));
        tabbedPane.addTab("Appointments", createPlaceholderPanel("Appointments"));
        tabbedPane.addTab("Prescriptions", createPlaceholderPanel("Prescriptions"));
        tabbedPane.addTab("Referrals", createPlaceholderPanel("Referrals"));

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createPlaceholderPanel(String name) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(name + " Panel - Coming Soon", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private void createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        JLabel statusLabel = new JLabel("Ready");
        statusBar.add(statusLabel);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void loadData() {
        JOptionPane.showMessageDialog(this,
                "Data loading will be implemented in the next phase.",
                "Load Data",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Healthcare Management System\n" +
                        "Version 1.0\n\n" +
                        "A Java Swing application implementing:\n" +
                        "- MVC Architecture Pattern\n" +
                        "- Singleton Design Pattern\n\n" +
                        "Part 2 Assignment",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
