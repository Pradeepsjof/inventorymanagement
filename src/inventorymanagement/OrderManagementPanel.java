package inventorymanagement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class OrderManagementPanel extends JPanel {
    private JTable orderTable;
    private DefaultTableModel orderTableModel;
    private JTextField orderIdField, productIdField, quantityField, statusField;
    private JButton addUpdateButton, deleteButton, searchButton;
    private JTextField searchField;

    public OrderManagementPanel() {
        setLayout(new BorderLayout());

        // Create the order table
        orderTableModel = new DefaultTableModel(new Object[]{"Order ID", "Product ID", "Quantity", "Status"}, 0);
        orderTable = new JTable(orderTableModel);
        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);

        // Create the form panel
        JPanel formPanel = new JPanel(new GridLayout(2, 1));

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(new JLabel("Order ID:"));
        orderIdField = new JTextField();
        inputPanel.add(orderIdField);

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

        // Load orders into the table
        loadOrders();

        // Add action listeners
        addUpdateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addOrUpdateOrder();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteOrder();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchOrders();
            }
        });

        orderTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && orderTable.getSelectedRow() != -1) {
                loadSelectedOrder();
            }
        });
    }

    private void loadOrders() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM orders")) {

            orderTableModel.setRowCount(0); // Clear existing data

            while (rs.next()) {
                orderTableModel.addRow(new Object[]{
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addOrUpdateOrder() {
        int selectedRow = orderTable.getSelectedRow();
//        int orderId = orderIdField.getText().isEmpty() ? 0 : Integer.parseInt(orderIdField.getText());
        int productId = Integer.parseInt(productIdField.getText());
        int quantity = Integer.parseInt(quantityField.getText());
        String status = statusField.getText();

        if (selectedRow == -1) {
            // Add order
            try (Connection conn =  DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("INSERT INTO orders (product_id, quantity, status) VALUES (?, ?, ?)")) {

                pstmt.setInt(1, productId);
                pstmt.setInt(2, quantity);
                pstmt.setString(3, status);
                pstmt.executeUpdate();

                loadOrders();
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            // Update order
            int orderId = (int) orderTableModel.getValueAt(selectedRow, 0);

        	try (Connection conn = DBConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE orders SET product_id = ?, quantity = ?, status = ? WHERE order_id = ?")) {

                pstmt.setInt(1, productId);
                pstmt.setInt(2, quantity);
                pstmt.setString(3, status);
                pstmt.setInt(4, orderId);
                pstmt.executeUpdate();

                loadOrders();
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void deleteOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an order to delete.");
            return;
        }

        int orderId = (int) orderTableModel.getValueAt(selectedRow, 0);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM orders WHERE order_id = ?")) {

            pstmt.setInt(1, orderId);
            pstmt.executeUpdate();

            loadOrders();
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void searchOrders() {
        String searchQuery = searchField.getText();

        try (Connection conn =  DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM orders WHERE status LIKE ?")) {

            pstmt.setString(1, "%" + searchQuery + "%");
            ResultSet rs = pstmt.executeQuery();

            orderTableModel.setRowCount(0); // Clear existing data

            while (rs.next()) {
                orderTableModel.addRow(new Object[]{
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity"),
                        rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        orderIdField.setText(orderTableModel.getValueAt(selectedRow, 0).toString());
        productIdField.setText(orderTableModel.getValueAt(selectedRow, 1).toString());
        quantityField.setText(orderTableModel.getValueAt(selectedRow, 2).toString());
        statusField.setText(orderTableModel.getValueAt(selectedRow, 3).toString());
    }

    private void clearFields() {
        orderIdField.setText("");
        productIdField.setText("");
        quantityField.setText("");
        statusField.setText("");
    }
}
