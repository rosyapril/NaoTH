/*
 */

package naoscp;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import naoscp.components.NetwokPanel;
import naoscp.tools.BarProgressMonitor;
import naoscp.tools.FileUtils;
import naoscp.tools.NaoSCPException;
import naoscp.tools.Scp;
import naoscp.tools.SwingTools;

/**
 *
 * @author Henrich Mellmann
 */
public class NaoSCP extends javax.swing.JFrame {

    private final String utilsPath = "./../Utils";
    private final String deployStickScriptPath = utilsPath + "/DeployStick/startBrainwashing.sh";
    
    private static final String configlocation = System.getProperty("user.home")
        + "/.naoth/naoscp/";
    private final File configPath = new File(configlocation, "config");
    
    private final Properties config = new Properties();
    
    /**
     * Creates new form NaoSCP
     */
    public NaoSCP() {
        initComponents();
        
        Logger.getGlobal().addHandler(logTextPanel.getLogHandler());
        Logger.getGlobal().setLevel(Level.ALL);
        
        try {
            config.load(new FileReader(configPath));
            naoTHPanel.setProperties(config);
        } catch(IOException ex) {
            Logger.getGlobal().log(Level.INFO, 
                "Could not open the config file. It will be created after the first execution.");
        }
    }
    
    public void setEnabledAll(boolean v) {
        SwingTools.setEnabled(this, v);
    }
    
    private void setupNetwork(File setupDir) throws IOException 
    {
        NetwokPanel.NetworkConfig cfg = netwokPanel.getNetworkConfig();
            
        TemplateFile tmp = null;
        if (cfg.getWlan_encryption().ecryption == NetwokPanel.NetworkConfig.WlanConfig.Encryption.WEP) {
            tmp = new TemplateFile(new File(utilsPath + "/NaoConfigFiles/wpa_supplicant.wep"));
        } else {
            tmp = new TemplateFile(new File(utilsPath + "/NaoConfigFiles/wpa_supplicant.wpa"));
        }

        tmp.set("WLAN_SSID", cfg.getWlan_encryption().ssid);
        tmp.set("WLAN_KEY", cfg.getWlan_encryption().key);
        
        File wpa_supplicant_dir = new File(setupDir, "/etc/wpa_supplicant/");
        wpa_supplicant_dir.mkdirs();
        tmp.save(new File(setupDir,"/etc/wpa_supplicant/wpa_supplicant.conf"));

        
        tmp = new TemplateFile(new File(utilsPath + "/NaoConfigFiles/etc/conf.d/net"));
        tmp.set("ETH_ADDR", cfg.getLan().subnet);
        tmp.set("ETH_NETMASK", cfg.getLan().mask);
        tmp.set("ETH_BRD", cfg.getLan().broadcast);

        tmp.set("WLAN_ADDR", cfg.getWlan().subnet);
        tmp.set("WLAN_NETMASK", cfg.getWlan().mask);
        tmp.set("WLAN_BRD", cfg.getWlan().broadcast);
        
        File conf_dir = new File(setupDir, "/etc/conf.d/");
        conf_dir.mkdirs();
        tmp.save(new File(setupDir, "/etc/conf.d/net"));
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
        txtRobotNumber = new javax.swing.JFormattedTextField();
        txtDeployTag = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("NaoSCP 1.0");
        setLocationByPlatform(true);
        setMaximumSize(new java.awt.Dimension(2147483647, 495));
        setMinimumSize(new java.awt.Dimension(0, 495));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
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
        gridBagConstraints.gridx = 1;
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

        txtRobotNumber.setColumns(3);
        txtRobotNumber.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txtRobotNumber.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        getContentPane().add(txtRobotNumber, gridBagConstraints);

        txtDeployTag.setColumns(10);
        txtDeployTag.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        getContentPane().add(txtDeployTag, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btDeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDeployActionPerformed
        this.logTextPanel.clear();
        
        final File targetDir = new File("./tmp");
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                try 
                {
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
                        //NaoSCP.this.setEnabledAll(false);
                        naoTHPanel.getAction().run(deployDir);

                        FileUtils.copyFiles(new File(deployStickScriptPath), targetDir);

                        // send stuff to robot
                        String robotIp = getIpAddress();
                        Scp scp = new Scp(robotIp, "nao", "nao");
                        
                        scp.setProgressMonitor(new BarProgressMonitor(jProgressBar));

                        scp.mkdir("/home/nao/tmp"); // just in case it doesn't exist
                        scp.cleardir("/home/nao/tmp");
                        scp.put(deployDir, "/home/nao/tmp/deploy");
                        scp.put(new File(deployStickScriptPath), "/home/nao/tmp/setup.sh");

                        //scp.channel.chown(WIDTH, utilsPath);
                        scp.chmod(755, "/home/nao/tmp/setup.sh");
                        //scp.run("/home/nao/tmp", "./setup.sh");
                        
                        Scp.CommandStream shell =  scp.getShell();
                        shell.run("su");
                        shell.run("root");
                        shell.run("cd /home/nao/tmp/");
                        shell.run("./setup.sh", "DONE");

                        scp.disconnect();

                        Logger.getGlobal().log(Level.INFO, "DONE");
                        //NaoSCP.this.setEnabledAll(true);
                    }
                } catch (JSchException | SftpException | IOException | NaoSCPException ex) {
                    Logger.getGlobal().log(Level.SEVERE, ex.getMessage());
                }
            }
        }).start();
    }//GEN-LAST:event_btDeployActionPerformed

    private String getIpAddress() throws NaoSCPException, UnknownHostException, IOException 
    {
        NetwokPanel.NetworkConfig cfg = netwokPanel.getNetworkConfig();
        
        String lan = cfg.getLan().subnet + "." + txtRobotNumber.getText();
        Logger.getGlobal().log(Level.INFO, "check " + lan);
        InetAddress iAddr = InetAddress.getByName(lan);
        if(!iAddr.isReachable(2500)) {
            Logger.getGlobal().log(Level.WARNING, lan + " not reachable");
        } else {
            return lan;
        }
        
        String wlan = cfg.getWlan().subnet + "." + txtRobotNumber.getText();
        Logger.getGlobal().log(Level.INFO, "check " + wlan);
        InetAddress iAddr2 = InetAddress.getByName(wlan);
        if(!iAddr2.isReachable(2500)) {
            Logger.getGlobal().log(Level.WARNING, wlan + " not reachable");
        } else {
            return wlan;
        }
        
        throw new NaoSCPException("Robot is not reachable.");
    }
    
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
                    
                    setEnabledAll(false);
                    
                    try 
                    {
                        // STEP 1: create the deploy directory for the playerNumber
                        File deployDir = new File(targetDir,"deploy");

                        // delete the target directory if it's existing, 
                        // so we have a fresh new directory
                        if (deployDir.isDirectory()) {
                            // backup 
                            File commentFile = new File(deployDir, "comment.txt");
                            if(commentFile.exists()) {
                                String backup_name = FileUtils.readFile(commentFile);
                                
                                if(deployDir.renameTo(new File(targetDir, backup_name))) {
                                    deployDir = new File(targetDir,"deploy");
                                } else {
                                    Logger.getGlobal().log(Level.WARNING, "Could not back up the deploy directory: " + deployDir.getAbsolutePath());
                                }
                            }
                            else 
                            {
                                FileUtils.deleteDir(deployDir);
                            }
                        }

                        if (!deployDir.mkdirs()) {
                            //Logger.getGlobal().log(Level.SEVERE, "Could not create deploy out directory");
                            throw new NaoSCPException("Could not create deploy out directory");
                        } 

                        //NaoSCP.this.setEnabledAll(false);
                        naoTHPanel.getAction().run(deployDir);
                        FileUtils.copyFiles(new File(deployStickScriptPath), targetDir);
                        //NaoSCP.this.setEnabledAll(true);
                        
                        
                        // get the current date and time
                        //String ISO_DATE_FORMAT = "yyyy-MM-dd";
                        String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd-HH-mm-ss";
                        SimpleDateFormat s = new SimpleDateFormat(ISO_DATE_TIME_FORMAT);
                        String backup_tag = s.format(new Date());
                        
                        // create a tag file
                        String tag = txtDeployTag.getText();
                        if(tag != null && !tag.isEmpty()) {
                            backup_tag += "-" + tag;
                        }
                        
                        FileUtils.writeToFile(backup_tag, new File(deployDir, "comment.txt"));
                        
                        Logger.getGlobal().log(Level.INFO, "DONE");
                    } catch (NaoSCPException | IOException ex) {
                        Logger.getGlobal().log(Level.SEVERE, ex.getMessage());
                    }
                    
                    setEnabledAll(true);
                }
            }).start();
        }
    }//GEN-LAST:event_btWriteToStickActionPerformed

    private void btInintRobotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btInintRobotActionPerformed
        final JFileChooser chooser = new JFileChooser();
        String libPath = config.getProperty("naoscp.libpath", ".");
        chooser.setCurrentDirectory(new File(libPath));
        chooser.setDialogTitle("Select toolchain \"extern/lib\" Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
        {
            // senity check
            File libDir = chooser.getSelectedFile();
            File gioFile = new File(libDir, "libgio-2.0.so");
            File glibDir = new File(libDir, "glib-2.0");
            if(!gioFile.isFile() || !glibDir.isDirectory())
            {
              chooser.setDialogTitle("Toolchain \"extern/lib\" Directory seems to be wrong. Try again.");
              JOptionPane.showMessageDialog(this, 
                      "Toolchain \"extern/lib\" Directory seems to be wrong. Cannot find 'libgio-2.0.so' or 'glib-2.0'.", 
                      "ERROR", JOptionPane.ERROR_MESSAGE);
              return;
            }
            config.setProperty("naoscp.libpath", libDir.getAbsolutePath());
            
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
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
                            FileUtils.copyFiles(new File(deployStickScriptPath), setupDir);

                            // copy scripts
                            FileUtils.copyFiles(new File(utilsPath + "/NaoConfigFiles"), setupDir);

                            // copy libs
                            File libDir = chooser.getSelectedFile();
                            FileUtils.copyFiles(libDir, new File(setupDir, "/home/nao/lib"));
                            try {
                                setupNetwork(setupDir);
                            } catch (IOException ex) {
                                Logger.getGlobal().log(Level.SEVERE, ex.getMessage());
                            }

                            
                            // copy to robot
                            String ip = JOptionPane.showInputDialog(this, "Robot ip address");
                            Scp scp = new Scp(ip, "nao", "nao");
                            scp.setProgressMonitor(new BarProgressMonitor(jProgressBar));

                            scp.mkdir("/home/nao/tmp");
                            scp.cleardir("/home/nao/tmp");
                            scp.put(setupDir, "/home/nao/tmp");

                            scp.chmod(755, "/home/nao/tmp/init_env.sh");
                            
                            //scp.runStream("su\nroot\ncd /home/nao/tmp\n./init_env.sh");
                            //scp.run("/home/nao/tmp", "./init_env.sh");
                            Scp.CommandStream shell = scp.getShell();
                            shell.run("ls");
                            shell.close();
                            

                            scp.disconnect();

                            Logger.getGlobal().log(Level.INFO, "DONE");
                        }
                    } catch (JSchException | SftpException | IOException | NaoSCPException ex) {
                        Logger.getGlobal().log(Level.SEVERE, ex.getMessage());
                    }
                }
            }).start();
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
/*
        try {
            File tmpDir = new File("./tmp");
            File setupDir = new File(tmpDir, "setup");
            
            setupNetwork(setupDir);
            
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        */
        new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File tmpDir = new File("./tmp");
                        File setupDir = new File(tmpDir, "setup");
                    
                        if(setupDir.isDirectory()) {
                            //Logger.getGlobal().log(Level.SEVERE, "Could not clean the setup directory: " + setupDir.getAbsolutePath());
                            FileUtils.deleteDir(setupDir);
                        }

                        if (!setupDir.mkdirs()) {
                            Logger.getGlobal().log(Level.SEVERE, "Could not create setup directory: " + setupDir.getAbsolutePath());
                        } else {
                            
                            setupNetwork(setupDir);
                            
                            FileUtils.copyFiles(new File(utilsPath,"/NaoConfigFiles/init_net.sh"), setupDir);
                            
                            // copy to robot
                            String ip = JOptionPane.showInputDialog(this, "Robot ip address");
                            Scp scp = new Scp(ip, "nao", "nao");
                            scp.setProgressMonitor(new BarProgressMonitor(jProgressBar));

                            scp.mkdir("/home/nao/tmp");
                            scp.cleardir("/home/nao/tmp");
                            scp.put(setupDir, "/home/nao/tmp");

                            scp.chmod(755, "/home/nao/tmp/init_net.sh");
                            
                            Scp.CommandStream shell =  scp.getShell();
                            shell.run("su");
                            shell.run("root");
                            shell.run("cd /home/nao/tmp/");
                            shell.run("./init_net.sh", "DONE");

                            scp.disconnect();
                            
                            Logger.getGlobal().log(Level.INFO, "DONE");
                        }
                    } catch (IOException | NaoSCPException |JSchException | SftpException ex) {
                        Logger.getGlobal().log(Level.SEVERE, ex.getMessage());
                    }
                }
            }).start();
        
    }//GEN-LAST:event_btSetNetworkActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            // save configuration to file
            new File(configlocation).mkdirs();
            config.store(new FileWriter(configPath), "");
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.SEVERE, "Could not write config file.", ex);
        }
    }//GEN-LAST:event_formWindowClosing

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
    private javax.swing.JButton btDeploy;
    private javax.swing.JButton btInintRobot;
    private javax.swing.JButton btSetNetwork;
    private javax.swing.JButton btWriteToStick;
    private javax.swing.JProgressBar jProgressBar;
    private naoscp.components.LogTextPanel logTextPanel;
    private naoscp.components.NaoTHPanel naoTHPanel;
    private naoscp.components.NetwokPanel netwokPanel;
    private javax.swing.JTextField txtDeployTag;
    private javax.swing.JFormattedTextField txtRobotNumber;
    // End of variables declaration//GEN-END:variables
}
