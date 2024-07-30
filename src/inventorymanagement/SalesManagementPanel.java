package inventorymanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class SalesManagementPanel extends JPanel {
    private JTable salesTable;
    private DefaultTableModel salesTableModel;
    private JTextField salesIdField, productIdField, quantityField, statusField;
    private JButton addUpdateButton, deleteButton, searchButton;
    private JTextField searchField;

    public SalesManagementPanel() {
        setLayout(new BorderLayout());

        // Create the sales table
        salesTableModel = new DefaultTableModel(new Object[]{"Sales ID", "Product ID", "Quantity", "Status"}, 0);
        salesTable = new JTable(salesTableModel);
        JScrollPane scrollPane = new JScrollPane(salesTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create the form panel
        JPanel formPanel = new JPanel(new GridLayout(2, 1));

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Sales ID:"));
        salesIdField = new JTextField();
        inputPanel.add(salesIdField);

        inputPanel.add(new JLabel("Product ID:"));
        productIdField = new JTextField();
        inputPanel.add(productIdField);

        inputPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        inputPanel.add(quantityField);

        inputPanel.add(new JLabel("Status:"));
        statusField = new JTextField();
        inputPanel.add(statusField);

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

        // Load sales into the table
        loadSales();

        // Add action listeners
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addOrUpdateSale();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSale();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchSales();
            }
        });

        salesTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && salesTable.getSelectedRow() != -1) {
                loadSelectedSale();
            }
        });
    }

    private void loadSales() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM sales")) {

            salesTableModel.setRowCount(0); // Clear existing data

            while (rs.next()) {
                salesTableModel.addRow(new Object[]{
                        rs.getInt("sales_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addOrUpdateSale() {
        int selectedRow = salesTable.getSelectedRow();
//        int salesId = salesIdField.getText().isEmpty() ? 0 : Integer.parseInt(salesIdField.getText());
        int productId = Integer.parseInt(productIdField.getText());
        int quantity = Integer.parseInt(quantityField.getText());
        String status = statusField.getText();

        if (selectedRow == -1) {
            // Add sale
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO sales (product_id, quantity, status) VALUES (?, ?, ?)")) {

                pstmt.setInt(1, productId);
                pstmt.setInt(2, quantity);
                pstmt.setString(3, status);
                pstmt.executeUpdate();

                loadSales();
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Update sale
            int salesId = (int) salesTableModel.getValueAt(selectedRow, 0);

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE sales SET product_id = ?, quantity = ?, status = ? WHERE sales_id = ?")) {

                pstmt.setInt(1, productId);
                pstmt.setInt(2, quantity);
                pstmt.setString(3, status);
                pstmt.setInt(4, salesId);
                pstmt.executeUpdate();

                loadSales();
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteSale() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a sale to delete.");
            return;
        }

        int salesId = (int) salesTableModel.getValueAt(selectedRow, 0);

        try (Connection conn =  DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM sales WHERE sales_id = ?")) {

            pstmt.setInt(1, salesId);
            pstmt.executeUpdate();

            loadSales();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchSales() {
        String searchQuery = searchField.getText();

        try (Connection conn =  DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM sales WHERE status LIKE ?")) {

            pstmt.setString(1, "%" + searchQuery + "%");
            ResultSet rs = pstmt.executeQuery();

            salesTableModel.setRowCount(0); // Clear existing data

            while (rs.next()) {
                salesTableModel.addRow(new Object[]{
                        rs.getInt("sales_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSelectedSale() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        salesIdField.setText(salesTableModel.getValueAt(selectedRow, 0).toString());
        productIdField.setText(salesTableModel.getValueAt(selectedRow, 1).toString());
        quantityField.setText(salesTableModel.getValueAt(selectedRow, 2).toString());
        statusField.setText(salesTableModel.getValueAt(selectedRow, 3).toString());
    }

    private void clearFields() {
        salesIdField.setText("");
        productIdField.setText("");
        quantityField.setText("");
        statusField.setText("");
    }
}