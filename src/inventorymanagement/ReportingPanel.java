package inventorymanagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReportingPanel extends JPanel {
    private JButton generateReportButton;
    private JTextArea reportArea;

    public ReportingPanel() {
        setLayout(new BorderLayout());

        generateReportButton = new JButton("Generate Report");
        reportArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(reportArea);

        generateReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateReport();
            }
        });

        add(generateReportButton, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    private void generateReport() {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT * FROM products";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            StringBuilder report = new StringBuilder();
            report.append("Inventory Report:\n\n");

            while (rs.next()) {
                report.append("Product ID: ").append(rs.getInt("id")).append("\n");
                report.append("Name: ").append(rs.getString("name")).append("\n");
                report.append("Type: ").append(rs.getString("type")).append("\n");
                report.append("Supplier ID: ").append(rs.getInt("supplier_id")).append("\n");
                report.append("Status: ").append(rs.getString("status")).append("\n");
                report.append("Stock: ").append(rs.getInt("stock")).append("\n");
                report.append("-----\n");
            }

            reportArea.setText(report.toString());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
