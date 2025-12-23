import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import view.MainFrame;

/**
 * Main entry point for the Healthcare Management System application.
 *
 * This application implements:
 * - MVC (Model-View-Controller) architectural pattern
 * - Singleton design pattern for ReferralManager
 * - Java Swing GUI
 * - CSV file-based data persistence
 */
public class Main {

    public static void main(String[] args) {
        // Set system look and feel for better native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default look and feel
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        // Launch the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}
