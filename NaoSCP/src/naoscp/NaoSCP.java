/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package naoscp;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import naoscp.components.NetwokPanel;
import naoscp.tools.BarProgressMonitor;
import naoscp.tools.FileUtils;
import naoscp.tools.Scp;

/**
 *
 * @author Henrich Mellmann
 */
public class NaoSCP extends javax.swing.JFrame {

    private final String utilsPath = "./../Utils";
    private final String deployStickScriptPath = utilsPath + "/DeployStick/startBrainwashing.sh";
    
    /**
     * Creates new form NaoSCP
     */
    public NaoSCP() {
        initComponents();
        
        Logger.getGlobal().addHandler(logTextPanel.getLogHandler());
        Logger.getGlobal().setLevel(Level.FINE);
    }

    private static void setEnabled(Component component, boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setEnabled(child, enabled);
            }
        }
    }
    
    public void setEnabledAll(boolean v) {
        setEnabled(this, v);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        netwokPanel = new naoscp.components.NetwokPanel();
        naoTHPanel = new naoscp.components.NaoTHPanel();
        btDeploy = new javax.swing.JButton();
        logTextPanel = new naoscp.components.LogTextPanel();
        btWriteToStick = new javax.swing.JButton();
        jProgressBar = new javax.swing.JProgressBar();
        btSetNetwork = new javax.swing.JButton();
        btInintRobot = new javax.swing.JButton();
        btAdvancedSimle = new javax.swing.JToggleButton();
        txt = new javax.swing.JFormattedTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("NaoSCP 1.0");
        setLocationByPlatform(true);
        setMaximumSize(new java.awt.Dimension(2147483647, 495));
        setMinimumSize(new java.awt.Dimension(0, 495));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        netwokPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Network"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(netwokPanel, gridBagConstraints);

        naoTHPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("NaoTH"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(naoTHPanel, gridBagConstraints);

        btDeploy.setText("Send toRobot");
        btDeploy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDeployActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        getContentPane().add(btDeploy, gridBagConstraints);

        logTextPanel.setPreferredSize(new java.awt.Dimension(400, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(logTextPanel, gridBagConstraints);

        btWriteToStick.setText("Write to Stick");
        btWriteToStick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btWriteToStickActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(btWriteToStick, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(jProgressBar, gridBagConstraints);

        btSetNetwork.setText("Set Network");
        btSetNetwork.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSetNetworkActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(btSetNetwork, gridBagConstraints);

        btInintRobot.setText("Initialize Robot");
        btInintRobot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btInintRobotActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(btInintRobot, gridBagConstraints);

        btAdvancedSimle.setText("Advanced");
        btAdvancedSimle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAdvancedSimleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        getContentPane().add(btAdvancedSimle, gridBagConstraints);

        txt.setColumns(3);
        txt.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txt.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        getContentPane().add(txt, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btDeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDeployActionPerformed
        this.logTextPanel.clear();
        
        final File targetDir = new File("./tmp");
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                // STEP 1: create the deploy directory for the playerNumber
                File deployDir = new File(targetDir,"deploy");

                // delete the target directory if it's existing, 
                // so we have a fresh new directory
                if (deployDir.isDirectory()) {
                    FileUtils.deleteDir(deployDir);
                }

                if (!deployDir.mkdirs()) {
                    Logger.getGlobal().log(Level.SEVERE, "Could not create deploy out directory");
                } else {
                    NaoSCP.this.setEnabledAll(false);
                    naoTHPanel.getAction().run(deployDir);
                    FileUtils.copyFiles(new File(deployStickScriptPath), targetDir);
                    
                    // send stuff to robot
                    try {
                        Scp scp = new Scp("192.168.56.101", "nao", "nao");
                        scp.setProgressMonitor(new BarProgressMonitor(jProgressBar));
                        
                        scp.cleardir("/home/nao/tmp");
                        scp.put(deployDir, "/home/nao/tmp");
                        scp.put(new File(deployStickScriptPath), "/home/nao/tmp/setup.sh");
                        
                        //scp.channel.chown(WIDTH, utilsPath);
                        scp.chmod(755, "/home/nao/tmp/setup.sh");
                        scp.run("/home/nao/tmp", "./setup.sh");
                        
                        scp.disconnect();
                    } catch (JSchException | SftpException | IOException ex) {
                        Logger.getGlobal().log(Level.SEVERE, ex.getMessage());
                    }
                    
                    Logger.getGlobal().log(Level.INFO, "DONE");
                    NaoSCP.this.setEnabledAll(true);
                }
            }
        }).start();
    }//GEN-LAST:event_btDeployActionPerformed

    private void btWriteToStickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btWriteToStickActionPerformed
        this.logTextPanel.clear();

        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setDialogTitle("Select NaoController Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            final File targetDir = chooser.getSelectedFile();
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // STEP 1: create the deploy directory for the playerNumber
                    File deployDir = new File(targetDir,"deploy");
                    
                    // delete the target directory if it's existing, 
                    // so we have a fresh new directory
                    if (deployDir.isDirectory()) {
                        // backup 
                        //FileUtils.deleteDir(deployDir);
                        if(deployDir.renameTo(new File(targetDir, "bak"))) {
                            deployDir = new File(targetDir,"deploy");
                        } else {
                            Logger.getGlobal().log(Level.WARNING, "Could not back up the deploy directory: " + deployDir.getAbsolutePath());
                        }
                    }

                    if (!deployDir.mkdirs()) {
                        Logger.getGlobal().log(Level.SEVERE, "Could not create deploy out directory");
                    } else {
                        NaoSCP.this.setEnabledAll(false);
                        naoTHPanel.getAction().run(new File(targetDir,"deploy"));
                        FileUtils.copyFiles(new File(deployStickScriptPath), targetDir);
                        NaoSCP.this.setEnabledAll(true);
                    }
                }
            }).start();
        }
    }//GEN-LAST:event_btWriteToStickActionPerformed

    private void btInintRobotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btInintRobotActionPerformed
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));
        chooser.setDialogTitle("Select toolchain \"extern/lib\" Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            File tmpDir = new File("./tmp");
            File setupDir = new File(tmpDir, "setup");
            
            if(setupDir.isDirectory()) {
                //Logger.getGlobal().log(Level.SEVERE, "Could not clean the setup directory: " + setupDir.getAbsolutePath());
                FileUtils.deleteDir(setupDir);
            }
            
            if (!setupDir.mkdirs()) {
                Logger.getGlobal().log(Level.SEVERE, "Could not create setup directory: " + setupDir.getAbsolutePath());
            } else {
                // copy deploy stuff
                naoTHPanel.getAction().run(setupDir);
                
                // copy scripts
                FileUtils.copyFiles(new File(utilsPath + "/NaoConfigFiles"), new File(setupDir, "home/nao/naoqi/naothSetup"));
                
                // copy libs
                File libDir = chooser.getSelectedFile();
                FileUtils.copyFiles(libDir, new File(setupDir, "lib"));
            }
        }
        
    }//GEN-LAST:event_btInintRobotActionPerformed

    class TemplateFile
    {
        private String text;
        TemplateFile(File file) throws IOException {
            this.text = FileUtils.readFile(file);
        }
        
        public void set(String arg, String value) {
            text = text.replace(arg, value);
        }
        
        public void save(File file) throws IOException {
            FileUtils.writeToFile(text, file);
        }
    }
    
    private void btSetNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSetNetworkActionPerformed
        try {
            File tmpDir = new File("./tmp");
            File setupDir = new File(tmpDir, "setup");
            
            NetwokPanel.NetworkConfig cfg = netwokPanel.getNetworkConfig();
            
            TemplateFile tmp = new TemplateFile(new File(utilsPath + "/NaoConfigFiles/wpa_supplicant.wep"));
            tmp.set("WLAN_SSID", cfg.getWlan_encryption().ssid);
            tmp.set("WLAN_KEY", cfg.getWlan_encryption().key);
            tmp.save(new File(setupDir,"wpa_supplicant"));
            
            tmp = new TemplateFile(new File(utilsPath + "/NaoConfigFiles/etc/conf.d/net"));
            tmp.set("ETH_ADDR", cfg.getLan().subnet);
            tmp.set("ETH_NETMASK", cfg.getLan().mask);
            tmp.set("ETH_BRD", cfg.getLan().broadcast);
            
            tmp.set("WLAN_ADDR", cfg.getWlan().subnet);
            tmp.set("ETH_NETMASK", cfg.getWlan().mask);
            tmp.set("ETH_BRD", cfg.getWlan().broadcast);
            tmp.save(new File(setupDir,"net"));
            
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btSetNetworkActionPerformed

    private void btAdvancedSimleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAdvancedSimleActionPerformed
        this.netwokPanel.setVisible(this.btAdvancedSimle.isSelected());
    }//GEN-LAST:event_btAdvancedSimleActionPerformed

    /**
     * @param args the command line arguments
     */
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NaoSCP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NaoSCP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NaoSCP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NaoSCP.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new NaoSCP().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btAdvancedSimle;
    private javax.swing.JButton btDeploy;
    private javax.swing.JButton btInintRobot;
    private javax.swing.JButton btSetNetwork;
    private javax.swing.JButton btWriteToStick;
    private javax.swing.JProgressBar jProgressBar;
    private naoscp.components.LogTextPanel logTextPanel;
    private naoscp.components.NaoTHPanel naoTHPanel;
    private naoscp.components.NetwokPanel netwokPanel;
    private javax.swing.JFormattedTextField txt;
    // End of variables declaration//GEN-END:variables
}
