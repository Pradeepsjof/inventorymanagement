package inventorymanagement;


import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private LoginFrame loginFrame;

    public MainFrame() {
        // Initialize login frame
        loginFrame = new LoginFrame(this);

        // Set title and size of the main frame
        setTitle("Inventory Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Check if the user is authenticated
        if (!loginFrame.isAuthenticated()) {
            // Show login frame
            loginFrame.setVisible(true);
        } else {
            // If already authenticated, show the main frame
            showMainPanel();
        }
    }

    public void showMainPanel() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Product Management", new ProductManagementPanel());
        tabbedPane.addTab("Supplier Management", new SupplierManagementPanel());
        tabbedPane.addTab("Inventory Tracking", new InventoryTrackingPanel());
        tabbedPane.addTab("Order Management", new OrderManagementPanel());
        tabbedPane.addTab("Sales Management", new SalesManagementPanel());
        tabbedPane.addTab("Reporting", new ReportingPanel());
        tabbedPane.addTab("Backup & Restore", new BackupRestorePanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Ensure the main frame is visible
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
        });
    }
}