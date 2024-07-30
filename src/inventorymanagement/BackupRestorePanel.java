package inventorymanagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class BackupRestorePanel extends JPanel {
    private JButton backupButton, restoreButton;
    private JTextField backupPathField, restorePathField;

    public BackupRestorePanel() {
        setLayout(new GridLayout(4, 2));

        JLabel backupPathLabel = new JLabel("Backup Path:");
        backupPathField = new JTextField();
        JLabel restorePathLabel = new JLabel("Restore Path:");
        restorePathField = new JTextField();

        backupButton = new JButton("Backup");
        restoreButton = new JButton("Restore");

        backupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    backupDatabase();
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });

        restoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    restoreDatabase();
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });

        add(backupPathLabel);
        add(backupPathField);
        add(restorePathLabel);
        add(restorePathField);
        add(backupButton);
        add(restoreButton);
    }

    private void backupDatabase() throws IOException, InterruptedException {
        String backupPath = backupPathField.getText();
        String executeCmd = "mysqldump -u username -p password inventory_db -r " + backupPath;
        Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
        int processComplete = runtimeProcess.waitFor();

        if (processComplete == 0) {
            JOptionPane.showMessageDialog(this, "Backup completed successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Backup failed.");
        }
    }

    private void restoreDatabase() throws IOException, InterruptedException {
        String restorePath = restorePathField.getText();
        String[] executeCmd = new String[]{"mysql", "-u", "username", "-p", "password", "inventory_db", "-e", " source " + restorePath};
        Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
        int processComplete = runtimeProcess.waitFor();

        if (processComplete == 0) {
            JOptionPane.showMessageDialog(this, "Restore completed successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Restore failed.");
        }
    }
}
