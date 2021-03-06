/*
 */

package naoscp;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.*;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import naoscp.components.DeployDialog;
import naoscp.components.NetwokPanel;
import naoscp.tools.*;

/**
 *
 * @author Henrich Mellmann
 */
public class NaoSCP extends javax.swing.JPanel {

    public static final String VERSION = "1.1";
    
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    
    private final String projectPath = getBasePath();
    private final String utilsPath = projectPath + "/Utils";

    private final String deployStickScriptPath = utilsPath + "/DeployStick/startBrainwashing.sh";

    private static final String configlocation = System.getProperty("user.home") + "/.naoth/naoscp/";
    private final File configPath = new File(configlocation, "config");

    private final Properties config = new Properties();
    private Frame parentFrame = null;

    /**
     * Creates new form NaoSCP
     */
    public NaoSCP() {
      boolean configLoaded = false;
      try {
        config.load(new FileReader(configPath));
        config.setProperty("naoscp.naothsoccerpath", projectPath + "/NaoTHSoccer");
        configLoaded = true;
      } catch (IOException ex) {
        Logger.getGlobal().log(Level.INFO,
                "Could not open the config file. It will be created after the first execution.");
      }
        
      try {
        final String fontSizeProp = config.getProperty("naoscp.font-size");
        if (fontSizeProp != null) {
          UIManager.setLookAndFeel(new NimbusLookAndFeel() {
            @Override
            public UIDefaults getDefaults() {
              UIDefaults defaults = super.getDefaults();
              defaults.put("defaultFont", new Font(Font.SANS_SERIF, Font.PLAIN, Integer.parseInt(fontSizeProp)));
              return defaults;
            }

          });
        }
      } catch (UnsupportedLookAndFeelException ex) {
        Logger.getGlobal().log(Level.INFO,
                "Could not set look and feel/font size.");
      }
      
      initComponents();

      Logger.getGlobal().addHandler(logTextPanel.getLogHandler());
      Logger.getGlobal().setLevel(Level.ALL);

      if (configLoaded) {
        naoTHPanel.setProperties(config);
      }
    }

    public String getBasePath() {
        String path = "./..";
        try {
            String ResourceName = "naoscp/NaoSCP.class";
            String programPath = URLDecoder.decode(this.getClass().getClassLoader().getResource(ResourceName).getPath(), "UTF-8");
            programPath = programPath.replace("file:", "");
            //path replacement if NaoScp is being started from console directly
            programPath = programPath.replace("/NaoSCP/dist/NaoSCP.jar!/naoscp/NaoSCP.class", "");
            //path replacement if NaoScp is started from IDE (Netbeans)
            programPath = programPath.replace("/NaoSCP/build/classes/naoscp/NaoSCP.class", "");
            File ProgramDir = new File(programPath);
            if (ProgramDir.exists()) {
                path = ProgramDir.getAbsolutePath();
            }
        } catch (UnsupportedEncodingException ueEx) {
        }
        return path;
    }

    public void setEnabledAll(boolean v) {
        SwingTools.setEnabled(this, v);
    }

    private void setupNetwork(File setupDir, int robotNumber) throws IOException {
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
        tmp.save(new File(setupDir, "/etc/wpa_supplicant/wpa_supplicant.conf"));

        tmp = new TemplateFile(new File(utilsPath + "/NaoConfigFiles/etc/conf.d/net"));
        tmp.set("ETH_ADDR", cfg.getLan().subnet + "." + robotNumber);
        tmp.set("ETH_NETMASK", cfg.getLan().mask);
        tmp.set("ETH_BRD", cfg.getLan().broadcast);

        tmp.set("WLAN_ADDR", cfg.getWlan().subnet + "." + robotNumber);
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
        statusBarPanel = new javax.swing.JPanel();
        txtRobotNumber = new javax.swing.JFormattedTextField();
        btDeploy = new javax.swing.JButton();
        txtDeployTag = new javax.swing.JTextField();
        btWriteToStick = new javax.swing.JButton();
        btSetNetwork = new javax.swing.JButton();
        btInintRobot = new javax.swing.JButton();
        logPanel = new javax.swing.JPanel();
        logTextPanel = new naoscp.components.LogTextPanel();
        jProgressBar = new javax.swing.JProgressBar();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        setLayout(new java.awt.GridBagLayout());

        netwokPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Network"));
        netwokPanel.setMinimumSize(new java.awt.Dimension(410, 245));
        netwokPanel.setName(""); // NOI18N
        netwokPanel.setPreferredSize(new java.awt.Dimension(410, 245));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(netwokPanel, gridBagConstraints);

        naoTHPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("NaoTH"));
        naoTHPanel.setMaximumSize(new java.awt.Dimension(32777, 32777));
        naoTHPanel.setMinimumSize(new java.awt.Dimension(539, 270));
        naoTHPanel.setPreferredSize(new java.awt.Dimension(539, 270));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(naoTHPanel, gridBagConstraints);

        statusBarPanel.setMinimumSize(new java.awt.Dimension(550, 24));
        statusBarPanel.setPreferredSize(new java.awt.Dimension(550, 24));
        statusBarPanel.setLayout(new javax.swing.BoxLayout(statusBarPanel, javax.swing.BoxLayout.X_AXIS));

        txtRobotNumber.setColumns(3);
        txtRobotNumber.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        txtRobotNumber.setToolTipText("Last octet of the robots ip address.");
        statusBarPanel.add(txtRobotNumber);

        btDeploy.setText("Send toRobot");
        btDeploy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDeployActionPerformed(evt);
            }
        });
        statusBarPanel.add(btDeploy);

        txtDeployTag.setColumns(10);
        txtDeployTag.setToolTipText("Small description of the deploying binary / test case.");
        statusBarPanel.add(txtDeployTag);

        btWriteToStick.setText("Write to Stick");
        btWriteToStick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btWriteToStickActionPerformed(evt);
            }
        });
        statusBarPanel.add(btWriteToStick);

        btSetNetwork.setText("Set Network");
        btSetNetwork.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSetNetworkActionPerformed(evt);
            }
        });
        statusBarPanel.add(btSetNetwork);

        btInintRobot.setText("Initialize Robot");
        btInintRobot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btInintRobotActionPerformed(evt);
            }
        });
        statusBarPanel.add(btInintRobot);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        add(statusBarPanel, gridBagConstraints);

        logPanel.setMinimumSize(new java.awt.Dimension(22, 0));
        logPanel.setPreferredSize(new java.awt.Dimension(550, 46));
        logPanel.setLayout(new java.awt.BorderLayout());

        logTextPanel.setPreferredSize(new java.awt.Dimension(400, 22));
        logPanel.add(logTextPanel, java.awt.BorderLayout.CENTER);

        jProgressBar.setMinimumSize(new java.awt.Dimension(10, 24));
        jProgressBar.setPreferredSize(new java.awt.Dimension(10, 24));
        logPanel.add(jProgressBar, java.awt.BorderLayout.SOUTH);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(logPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void btDeployActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDeployActionPerformed
        this.logTextPanel.clear();

        if(txtRobotNumber.getText().trim().isEmpty()) {
            Logger.getGlobal().log(Level.WARNING, "Missing robot number!");
            return;
        }

        // create deploy directory in systems 'temp' directory
        final File targetDir = createTemporaryDirectory("nao_scp_deploy_");
        if(targetDir == null) {return;}

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // STEP 1: create the deploy directory for the playerNumber
                    File deployDir = new File(targetDir, "deploy");

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

                        Scp.CommandStream shell = scp.getShell();
                        // HACK: always stop naoth before proceeding
                        shell.run("naoth stop", "killing naoth cognition processes");
                        shell.run("su", "Password:");
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

    private String getIpAddress() throws NaoSCPException, UnknownHostException, IOException {
        NetwokPanel.NetworkConfig cfg = netwokPanel.getNetworkConfig();

        String lan = cfg.getLan().subnet + "." + txtRobotNumber.getText();
        
        Logger.getGlobal().log(Level.INFO, "check " + lan);
        
        try{
            InetAddress iAddr = InetAddress.getByName(lan);
            
            if (!iAddr.isReachable(2500)) {
                Logger.getGlobal().log(Level.WARNING, lan + " not reachable");
            } else {
                return lan;
            }
        } catch (UnknownHostException uh) {
            Logger.getGlobal().log(Level.WARNING, "Unknown host: " + lan);
        }

        String wlan = cfg.getWlan().subnet + "." + txtRobotNumber.getText();
        Logger.getGlobal().log(Level.INFO, "check " + wlan);
        
        try{        
            InetAddress iAddr2 = InetAddress.getByName(wlan);
            if (!iAddr2.isReachable(2500)) {
                Logger.getGlobal().log(Level.WARNING, wlan + " not reachable");
            } else {
                return wlan;
            }
        } catch (UnknownHostException uh) {
            Logger.getGlobal().log(Level.WARNING, "Unknown host: " + wlan);
        }

        throw new NaoSCPException("Robot is not reachable.");
    }
    

    private void btWriteToStickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btWriteToStickActionPerformed
          
        this.logTextPanel.clear();
        
        final DeployDialog deployDialog = new DeployDialog(getParentFrame());

        if (deployDialog.showOpenDialog(this) == DeployDialog.OPTION.APPROVE) {
            
            final File targetDir = deployDialog.getSelectedFile();

            new Thread(new Runnable() {
                @Override
                public void run() {

                    setEnabledAll(false);
                    
                    Logger.getGlobal().log(Level.INFO, "----" + dateFormat.format(new Date()) + "---");
                    Logger.getGlobal().log(Level.INFO, "Write to USB: " + targetDir);
                    
                    try {
                        // STEP 1: create the deploy directory for the playerNumber
                        File deployDir = new File(targetDir, "deploy");

                        // delete the target directory if it's existing, 
                        // so we have a fresh new directory
                        if (deployDir.isDirectory()) {
                            // backup 
                            File commentFile = new File(deployDir, "comment.txt");
                            if (commentFile.exists()) {
                                String backup_name = FileUtils.readFile(commentFile);

                                File backup_dir = new File(targetDir, backup_name);
                                if(backup_dir.exists()) {
                                    Logger.getGlobal().log(Level.WARNING, String.format("Could not back up the deploy directory, file already exists: %s", backup_dir.getAbsolutePath()));
                                } else if (deployDir.renameTo(backup_dir)) {
                                    deployDir = new File(targetDir, "deploy");
                                } else {
                                    Logger.getGlobal().log(Level.WARNING, String.format("Could not back up the deploy directory %s to %s", deployDir.getAbsolutePath(), backup_dir.getAbsolutePath()));
                                }
                            } else {
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
                        if (tag != null && !tag.isEmpty()) {
                            backup_tag += "-" + tag;
                        }

                        FileUtils.writeToFile(backup_tag, new File(deployDir, "comment.txt"));
                        
                        // unmount usb storage device if selected
                        deployDialog.closeUSBStorageDevice();

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

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // senity check
            File libDir = chooser.getSelectedFile();
            File gioFile = new File(libDir, "libgio-2.0.so");
            File glibDir = new File(libDir, "glib-2.0");
            if (!gioFile.isFile() || !glibDir.isDirectory()) {
                chooser.setDialogTitle("Toolchain \"extern/lib\" Directory seems to be wrong. Try again.");
                JOptionPane.showMessageDialog(this,
                        "Toolchain \"extern/lib\" Directory seems to be wrong. Cannot find 'libgio-2.0.so' or 'glib-2.0'.",
                        "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }
            config.setProperty("naoscp.libpath", libDir.getAbsolutePath());
            
            final File tmpDir = createTemporaryDirectory("nao_scp_init_");
            if(tmpDir == null) {return;}

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File setupDir = new File(tmpDir, "setup");

                        if (setupDir.isDirectory()) {
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

                            String robotNumberRaw = JOptionPane.showInputDialog(NaoSCP.this, "Robot number");
                            int robotNr = 100;
                            try {
                                robotNr = Integer.parseInt(robotNumberRaw.trim());
                            } catch (NullPointerException | NumberFormatException ex) {
                                JOptionPane.showMessageDialog(NaoSCP.this, "Could not parse robot number, defaulting to 100");
                            }

                            // copy libs
                            File libDir = chooser.getSelectedFile();
                            FileUtils.copyFiles(libDir, new File(setupDir, "/home/nao/lib"));
                            try {
                                setupNetwork(setupDir, robotNr);
                            } catch (IOException ex) {
                                Logger.getGlobal().log(Level.SEVERE, ex.getMessage());
                            }

                            // set hostname
                            FileUtils.writeToFile("nao"+robotNr, new File(setupDir,"/etc/hostname"));
                            FileUtils.writeToFile("hostname=\"nao"+robotNr+"\"", new File(setupDir,"/etc/conf.d/hostname"));
                            
                            // copy to robot
                            String ip = JOptionPane.showInputDialog(NaoSCP.this, "Robot ip address");
                            Scp scp = new Scp(ip, "nao", "nao");
                            scp.setProgressMonitor(new BarProgressMonitor(jProgressBar));

                            scp.mkdir("/home/nao/tmp");
                            scp.cleardir("/home/nao/tmp");
                            scp.put(setupDir, "/home/nao/tmp");

                            scp.chmod(755, "/home/nao/tmp/init_env.sh");

                            //scp.runStream("su\nroot\ncd /home/nao/tmp\n./init_env.sh");
                            //scp.run("/home/nao/tmp", "./init_env.sh");
                            //Scp.CommandStream shell = scp.getShell();
                            //shell.run("ls");
                            //shell.close();
                            Scp.CommandStream shell = scp.getShell();
                            shell.run("su", "Password:");
                            shell.run("root");
                            shell.run("cd /home/nao/tmp/");
                            shell.run("./init_env.sh", "DONE");

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

    class TemplateFile {

        private String text;
        private final File template;

        TemplateFile(File file) throws IOException {
            this.template = file;
            this.text = FileUtils.readFile(file);
        }

        public void set(String arg, String value) {
            text = text.replace(arg, value);
        }

        // save to a different file
        public void save(File file) throws IOException {
            FileUtils.writeToFile(text, file);
        }

        // overwrite th template file
        public void save() throws IOException {
            FileUtils.writeToFile(text, this.template);
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

        String robotNumberRaw = JOptionPane.showInputDialog(NaoSCP.this, "Robot number");
        // dialog canceled ...
        if(robotNumberRaw == null || robotNumberRaw.trim().isEmpty()) {
            Logger.getGlobal().log(Level.INFO, "Canceled.");
            return;
        }
        // default number
        int robotNr = 100;
        try {
            robotNr = Integer.parseInt(robotNumberRaw.trim());
        } catch (NullPointerException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(NaoSCP.this, "Could not parse robot number, defaulting to 100");
        }
        final int robotNrFinal = robotNr;
        final File tmpDir = createTemporaryDirectory("nao_scp_setup_");
        if(tmpDir == null){return;}

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File setupDir = new File(tmpDir, "setup");

                    if (setupDir.isDirectory()) {
                        //Logger.getGlobal().log(Level.SEVERE, "Could not clean the setup directory: " + setupDir.getAbsolutePath());
                        FileUtils.deleteDir(setupDir);
                    }

                    if (!setupDir.mkdirs()) {
                        Logger.getGlobal().log(Level.SEVERE, "Could not create setup directory: " + setupDir.getAbsolutePath());
                    } else {

                        setupNetwork(setupDir, robotNrFinal);

                        FileUtils.copyFiles(new File(utilsPath, "/NaoConfigFiles/init_net.sh"), setupDir);

                        // copy to robot
                        String ip = JOptionPane.showInputDialog(NaoSCP.this, "Robot ip address");
                        if(ip == null || ip.trim().isEmpty()) {
                            Logger.getGlobal().log(Level.INFO, "Canceled.");
                            return;
                        }
                        Scp scp = new Scp(ip, "nao", "nao");
                        scp.setProgressMonitor(new BarProgressMonitor(jProgressBar));

                        scp.mkdir("/home/nao/tmp");
                        scp.cleardir("/home/nao/tmp");
                        scp.put(setupDir, "/home/nao/tmp");

                        scp.chmod(755, "/home/nao/tmp/init_net.sh");

                        Scp.CommandStream shell = scp.getShell();
                        shell.run("su", "Password:");
                        shell.run("root");
                        shell.run("cd /home/nao/tmp/");
                        shell.run("./init_net.sh", "DONE");

                        scp.disconnect();

                        Logger.getGlobal().log(Level.INFO, "DONE");
                    }
                } catch (IOException | NaoSCPException | JSchException | SftpException ex) {
                    Logger.getGlobal().log(Level.SEVERE, ex.getMessage());
                }
            }
        }).start();

    }//GEN-LAST:event_btSetNetworkActionPerformed

    private boolean isExtended = true;
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        // switch layout between different widths and prevent multiple 're-layouts'
        if(isExtended && getWidth() < 750) {
            isExtended = false;
            GridBagLayout l = (GridBagLayout)getLayout();
            GridBagConstraints c = l.getConstraints(logPanel);
            c.gridx = 0;
            c.gridy = 2;
            c.gridwidth  = 1;
            c.gridheight = 1;
            l.setConstraints(logPanel, c);
            revalidate();
            repaint();
        } else if(!isExtended && getWidth() >= 750) {
            isExtended = true;
            GridBagLayout l = (GridBagLayout)getLayout();
            GridBagConstraints c = l.getConstraints(logPanel);
            c.gridx = 1;
            c.gridy = 0;
            c.gridwidth  = GridBagConstraints.REMAINDER;
            c.gridheight = GridBagConstraints.REMAINDER;
            l.setConstraints(logPanel, c);
            revalidate();
            repaint();
        }
    }//GEN-LAST:event_formComponentResized

    public void formWindowClosing() {
        try {
            // save configuration to file
            new File(configlocation).mkdirs();
            config.store(new FileWriter(configPath), "");
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.SEVERE, "Could not write config file.", ex);
        }
    }
    
    /**
     * Returns the Frame this panel belongs to.
     * If the parent container isn't a Frame and there wasn't set any parent frame, then 'null' is returned.
     * @return the parent frame or null
     */
    public Frame getParentFrame() {
        return parentFrame==null?((getParent() instanceof Frame)?(Frame)getParent():null):parentFrame;
    }
    
    /**
     * Sets the parent frame of this panel.
     * @param f the frame this panel should belong to
     */
    public void setParentFrame(Frame f) {
        parentFrame = f;
    }
    
    /**
     * Creates a temporary directory in systems 'temp' folder with the given prefix.
     * @param prefix the temporary directory should prepend with.
     * @return a File object to the temporary directory
     */
    private File createTemporaryDirectory(String prefix) {
        try {
            return Files.createTempDirectory(prefix).toFile();
        } catch (IOException ex) {
            Logger.getGlobal().log(Level.SEVERE, "Can not create temporary directory in systems temp directory.");
        }
        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btDeploy;
    private javax.swing.JButton btInintRobot;
    private javax.swing.JButton btSetNetwork;
    private javax.swing.JButton btWriteToStick;
    private javax.swing.JProgressBar jProgressBar;
    private javax.swing.JPanel logPanel;
    private naoscp.components.LogTextPanel logTextPanel;
    private naoscp.components.NaoTHPanel naoTHPanel;
    private naoscp.components.NetwokPanel netwokPanel;
    private javax.swing.JPanel statusBarPanel;
    private javax.swing.JTextField txtDeployTag;
    private javax.swing.JFormattedTextField txtRobotNumber;
    // End of variables declaration//GEN-END:variables
}
