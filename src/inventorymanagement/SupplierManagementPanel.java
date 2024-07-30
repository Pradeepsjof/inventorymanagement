package inventorymanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SupplierManagementPanel extends JPanel {
    private JTable supplierTable;
    private DefaultTableModel supplierTableModel;
    private JTextField nameField, contactField;
    private JButton addUpdateButton, deleteButton, searchButton;
    private JTextField searchField;

    public SupplierManagementPanel() {
        setLayout(new BorderLayout());

        // Create the supplier table
        supplierTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Contact"}, 0);
        supplierTable = new JTable(supplierTableModel);
        JScrollPane scrollPane = new JScrollPane(supplierTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create the form panel
        JPanel formPanel = new JPanel(new GridLayout(2, 1));

        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Contact:"));
        contactField = new JTextField();
        inputPanel.add(contactField);

        formPanel.add(inputPanel);

        JPanel buttonPanel = new JPanel();
        addUpdateButton = new JButton("Add/Update");
        deleteButton = new JButton("Delete");
        searchButton = new JButton("Search");
        searchField = new JTextField(10);

        buttonPanel.add(new JLabel("Search:"));
        buttonPanel.add(searchField);
        buttonPanel.add(searchButton);
        buttonPanel.add(addUpdateButton);
        buttonPanel.add(deleteButton);

        formPanel.add(buttonPanel);

        add(formPanel, BorderLayout.NORTH);

        // Load suppliers into the table
        loadSuppliers();

        // Add action listeners
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addOrUpdateSupplier();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSupplier();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchSuppliers();
            }
        });

        supplierTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && supplierTable.getSelectedRow() != -1) {
                loadSelectedSupplier();
            }
        });
    }

    private void loadSuppliers() {
        try (Connection conn =  DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM suppliers")) {

            supplierTableModel.setRowCount(0); // Clear existing data

            while (rs.next()) {
                supplierTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addOrUpdateSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        String name = nameField.getText();
        String contact = contactField.getText();

        if (selectedRow == -1) {
            // Add supplier
            try (Connection conn =  DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO suppliers (name, contact) VALUES (?, ?)")) {

                pstmt.setString(1, name);
                pstmt.setString(2, contact);
                pstmt.executeUpdate();

                loadSuppliers();
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Update supplier
            int supplierId = (int) supplierTableModel.getValueAt(selectedRow, 0);

            try (Connection conn =  DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE suppliers SET name = ?, contact = ? WHERE id = ?")) {

                pstmt.setString(1, name);
                pstmt.setString(2, contact);
                pstmt.setInt(3, supplierId);
                pstmt.executeUpdate();

                loadSuppliers();
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete.");
            return;
        }

        int supplierId = (int) supplierTableModel.getValueAt(selectedRow, 0);

        try (Connection conn =  DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM suppliers WHERE id = ?")) {

            pstmt.setInt(1, supplierId);
            pstmt.executeUpdate();

            loadSuppliers();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchSuppliers() {
        String searchQuery = searchField.getText();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM suppliers WHERE name LIKE ?")) {

            pstmt.setString(1, "%" + searchQuery + "%");
            ResultSet rs = pstmt.executeQuery();

            supplierTableModel.setRowCount(0); // Clear existing data

            while (rs.next()) {
                supplierTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSelectedSupplier() {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        nameField.setText(supplierTableModel.getValueAt(selectedRow, 1).toString());
        contactField.setText(supplierTableModel.getValueAt(selectedRow, 2).toString());
    }

    private void clearFields() {
        nameField.setText("");
        contactField.setText("");
    }
}