package inventorymanagement;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class InventoryTrackingPanel extends JPanel {
    private JTable inventoryTable;

    public InventoryTrackingPanel() {
        setLayout(new BorderLayout());

        inventoryTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(inventoryTable);

        loadInventoryData();

        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadInventoryData() {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT * FROM products";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            // Set ResultSet to JTable
            inventoryTable.setModel(Dbutils.resultSetToTableModel(rs));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}