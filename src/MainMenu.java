import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


public class MainMenu extends javax.swing.JFrame {
    public MainMenu() {
        isFileSelected = false;
        initComponents();
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);
        filePathTextField.setText(SELECT_FILE_MSG_TEXT);
        setResizable(false);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectFileButton = new javax.swing.JButton();
        viewButton = new javax.swing.JButton();
        filePathTextField = new javax.swing.JTextField();
        copyrightLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        titleLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        selectFileButton.setText("Select file");
        selectFileButton.setToolTipText("");
        selectFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFileButtonActionPerformed(evt);
            }
        });

        viewButton.setText("View");
        viewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewButtonActionPerformed(evt);
            }
        });

        filePathTextField.setEditable(false);
        filePathTextField.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        filePathTextField.setToolTipText("");

        copyrightLabel.setFont(new java.awt.Font("Monospaced", 1, 12)); // NOI18N
        copyrightLabel.setText("©");
        copyrightLabel.setToolTipText("");

        versionLabel.setFont(new java.awt.Font("Monospaced", 1, 12)); // NOI18N
        versionLabel.setText("1.0");
        versionLabel.setToolTipText("");

        titleLabel.setFont(new java.awt.Font("Ink Free", 1, 18)); // NOI18N
        titleLabel.setText("BMP Viewer");
        titleLabel.setToolTipText("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(filePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectFileButton)
                        .addGap(10, 10, 10)
                        .addComponent(viewButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(titleLabel)
                        .addGap(2, 2, 2)
                        .addComponent(copyrightLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 9, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(versionLabel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(copyrightLabel)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(titleLabel)
                        .addComponent(versionLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(filePathTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(selectFileButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(viewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(15, 15, 15))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectFileButtonActionPerformed
        JFileChooser chooser = new JFileChooser(".");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setApproveButtonText("Select");
        chooser.setDialogTitle("Select a file");
        chooser.setApproveButtonToolTipText("Select the file");
        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    String filename = f.getName().toLowerCase();
                    return filename.endsWith(".bmp");
                }
            }

            @Override
            public String getDescription() {
                return "BMP Files (*.bmp)";
            }
        });
        chooser.setAcceptAllFileFilterUsed(false);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            setFilePath(chooser.getSelectedFile().getAbsolutePath());
        } else {
            resetFilePath();
        }
    }//GEN-LAST:event_selectFileButtonActionPerformed

    private void viewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewButtonActionPerformed
        if (!isFileSelected) {
            JOptionPane.showMessageDialog(this, "No file has been selected!");
            resetFilePath();
            return;
        }
        try {
            File f = new File(filePathTextField.getText());
            BufferedImage bi = ImageIO.read(f);
            if (bi == null) {
                JOptionPane.showMessageDialog(this, "Not an image file!");
                resetFilePath();
                return;
            } else if (f.isDirectory()) {
                JOptionPane.showMessageDialog(this, "Directory is selected!");
                resetFilePath();
                return;
            }
            byte[] fileBytes = Files.readAllBytes(Paths.get(filePathTextField.getText()));
            if (getShort(fileBytes[1], fileBytes[0]) != 0x4d42) {
                JOptionPane.showMessageDialog(this, "Not a BMP image file!");
                resetFilePath();
                return;
            }
            if (getShort(fileBytes[29], fileBytes[28]) != 24) {
                JOptionPane.showMessageDialog(this, "Only 24 bits BMP files are supported!");
                resetFilePath();
                return;
            }
            BMPViewer bmpViewer = new BMPViewer(this, true, fileBytes, filePathTextField.getText());
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            bmpViewer.setSize(screenSize.width, screenSize.height);
            bmpViewer.drawPanel.setSize(screenSize.width * 2, screenSize.height * 2);
            bmpViewer.setLocation(0, 0);
            bmpViewer.setVisible(true);
            resetFilePath();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "File does not exist or it's a directory!");
            resetFilePath();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Not a sound file!");
            resetFilePath();
        }


    }//GEN-LAST:event_viewButtonActionPerformed

    private short getShort(byte b1, byte b2) {
        return (short) ((b1 << 8) + b2);
    }

    private void resetFilePath() {
        filePathTextField.setText(SELECT_FILE_MSG_TEXT);
        isFileSelected = false;
    }

    private void setFilePath(String path) {
        filePathTextField.setText(path);
        isFileSelected = true;
    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MainMenu().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel copyrightLabel;
    private javax.swing.JTextField filePathTextField;
    private javax.swing.JButton selectFileButton;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JButton viewButton;
    // End of variables declaration//GEN-END:variables
    private boolean isFileSelected;
    private final String SELECT_FILE_MSG_TEXT = "Select a BMP file...";
}
