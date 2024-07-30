package inventorymanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ProductManagementPanel extends JPanel {
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private JTextField nameField, typeField, stockField;
    private JComboBox<String> supplierComboBox, statusComboBox;
    private JButton addUpdateButton, deleteButton, searchButton;
    private JTextField searchField;

    public ProductManagementPanel() {
        setLayout(new BorderLayout());

        // Create the product table
        productTableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Type", "Supplier", "Status", "Stock"}, 0);
        productTable = new JTable(productTableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create the form panel
        JPanel formPanel = new JPanel(new GridLayout(2, 1));

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Type:"));
        typeField = new JTextField();
        inputPanel.add(typeField);

        inputPanel.add(new JLabel("Supplier:"));
        supplierComboBox = new JComboBox<>();
        loadSuppliers();
        inputPanel.add(supplierComboBox);

        inputPanel.add(new JLabel("Status:"));
        statusComboBox = new JComboBox<>(new String[]{"available", "out of stock", "discontinued"});
        inputPanel.add(statusComboBox);

        inputPanel.add(new JLabel("Stock:"));
        stockField = new JTextField();
        inputPanel.add(stockField);

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

        // Load products into the table
        loadProducts();

        // Add action listeners
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addOrUpdateProduct();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteProduct();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchProducts();
            }
        });

        productTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && productTable.getSelectedRow() != -1) {
                loadSelectedProduct();
            }
        });
    }

    private void loadSuppliers() {
        try (Connection conn =  DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name FROM suppliers")) {

            while (rs.next()) {
                supplierComboBox.addItem(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProducts() {
        try (Connection conn =  DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM products")) {

            productTableModel.setRowCount(0); // Clear existing data

            while (rs.next()) {
                productTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("supplier_id"),
                        rs.getString("status"),
                        rs.getInt("stock")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addOrUpdateProduct() {
        int selectedRow = productTable.getSelectedRow();
        String name = nameField.getText();
        String type = typeField.getText();
        String supplier = (String) supplierComboBox.getSelectedItem();
        String status = (String) statusComboBox.getSelectedItem();
        int stock = Integer.parseInt(stockField.getText());

        if (selectedRow == -1) {
            // Add product
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO products (name, type, supplier_id, status, stock) VALUES (?, ?, (SELECT id FROM suppliers WHERE name = ?), ?, ?)")) {

                pstmt.setString(1, name);
                pstmt.setString(2, type);
                pstmt.setString(3, supplier);
                pstmt.setString(4, status);
                pstmt.setInt(5, stock);
                pstmt.executeUpdate();

                loadProducts();
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Update product
            int productId = (int) productTableModel.getValueAt(selectedRow, 0);

            try (Connection conn =  DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE products SET name = ?, type = ?, supplier_id = (SELECT id FROM suppliers WHERE name = ?), status = ?, stock = ? WHERE id = ?")) {

                pstmt.setString(1, name);
                pstmt.setString(2, type);
                pstmt.setString(3, supplier);
                pstmt.setString(4, status);
                pstmt.setInt(5, stock);
                pstmt.setInt(6, productId);
                pstmt.executeUpdate();

                loadProducts();
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            return;
        }

        int productId = (int) productTableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM products WHERE id = ?")) {

            pstmt.setInt(1, productId);
            pstmt.executeUpdate();

            loadProducts();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchProducts() {
        String searchQuery = searchField.getText();

        try (Connection conn =  DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM products WHERE name LIKE ?")) {

            pstmt.setString(1, "%" + searchQuery + "%");
            ResultSet rs = pstmt.executeQuery();

            productTableModel.setRowCount(0); // Clear existing data

            while (rs.next()) {
                productTableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getInt("supplier_id"),
                        rs.getString("status"),
                        rs.getInt("stock")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        nameField.setText(productTableModel.getValueAt(selectedRow, 1).toString());
        typeField.setText(productTableModel.getValueAt(selectedRow, 2).toString());
        supplierComboBox.setSelectedItem(getSupplierName((int) productTableModel.getValueAt(selectedRow, 3)));
        statusComboBox.setSelectedItem(productTableModel.getValueAt(selectedRow, 4).toString());
        stockField.setText(productTableModel.getValueAt(selectedRow, 5).toString());
    }

    private String getSupplierName(int supplierId) {
        try (Connection conn =  DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT name FROM suppliers WHERE id = ?")) {

            pstmt.setInt(1, supplierId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void clearFields() {
        nameField.setText("");
        typeField.setText("");
        supplierComboBox.setSelectedIndex(0);
        statusComboBox.setSelectedIndex(0);
        stockField.setText("");
    }
}