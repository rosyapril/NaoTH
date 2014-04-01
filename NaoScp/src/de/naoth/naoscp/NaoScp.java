package de.naoth.naoscp;

/*
 *
 * Warning - this is a "pre alpha version" and is largely untested
 *
 * Code and UI might need some cleanup, too (e.g. similar functions sometimes
 * expect String, sometimes File as parameter, which is ugly, but worksfornow [tm]
 *
 * Todo:
 *  * Nicer Error handling
 *  * if "default directory structure" on robot is missing, try to fix this instead of Exception
 *  * ssh-button?
 * 
 * @author Florian Holzhauer <fh-hu@fholzhauer.de> 2009
 * 
 */


import java.awt.Color;
import java.awt.Component;
import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.MaskFormatter;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;


public class NaoScp extends NaoScpMainFrame
{
  // if true, all ActionInfo is sent to log
  // @see actionInfo()
  private boolean logActionInfo = true;
//  private boolean debugVersion = true;
  
  private HashMap<String, JTextField> networkConfigTags = new HashMap<String, JTextField>();

  private HashMap<Integer, JTextField> naoNumberFields = new HashMap<Integer, JTextField>();
  private HashMap<Integer, Boolean> scriptDone = new HashMap<Integer, Boolean>();
  private HashMap<Integer, Boolean> copyDone = new HashMap<Integer, Boolean>();
  private HashMap<Integer, Boolean> hadCopyErrors = new HashMap<Integer, Boolean>();
  private HashMap<Integer, Boolean> hadScriptErrors = new HashMap<Integer, Boolean>();
  private HashMap<Integer, Boolean> hadErrors = new HashMap<Integer, Boolean>();
  private HashMap<Integer, Integer> iNaoBytes = new HashMap<Integer, Integer>();
  private HashMap<Integer, String> sNaoLanIps = new HashMap<Integer, String>();
  private HashMap<Integer, String> sNaoWLanIps = new HashMap<Integer, String>();

  private ArrayList<JmDNS> jmdnsList = new ArrayList<JmDNS>();
//  private ArrayList<JmdnsServiceListener> jmdnsServiceListenerList = new ArrayList<JmdnsServiceListener>();
  private HashMap<String, ArrayList<InetAddress>> hostAdresses = new HashMap<String, ArrayList<InetAddress>>();
  private Map<String, NaoSshWrapper> services;
  private final DefaultListModel naoModel;
  private final Map<String,Integer> bodyIdToPlayerNumber = new HashMap<String, Integer>();
  
  private boolean showCopyDoneMsg = false;
  private boolean showScriptDoneMsg = false;
  private boolean showDoneMsg = true;
  
  private String setupPlayerNo;
  private String lastBashColorOption;
  
  private NaoScpConfig config; 
  
  @SuppressWarnings("unchecked")
  public NaoScp()
  {
    String laf = "javax.swing.plaf.metal.MetalLookAndFeel";
    try 
    {
        UIManager.setLookAndFeel(laf);
    }
    catch (Exception e) 
    {
        e.printStackTrace(System.err);
        System.exit(1);
    }

    initComponents();

    wlanBtnGroup.add(radioWPA);
    wlanBtnGroup.add(radioWEP);

    lastBashColorOption = "bash_0";
    logTextPane.setEditorKit(new WrapEditorKit());
    StyledDocument doc = logTextPane.getStyledDocument();
    addStylesToDocument(doc);

    config = new NaoScpConfig();
    config.debugVersion = true;

    updateConfig();

    this.cbNoBackup.setVisible(config.debugVersion);
    this.cbNoBackup.setEnabled(config.debugVersion);

    this.naoNumberFields.put(0, new JTextField());
    this.scriptDone.put(0, false);
    this.copyDone.put(0, false);
    this.hadCopyErrors.put(0, false);
    this.hadScriptErrors.put(0, false);
    this.hadErrors.put(0, false);
    this.sNaoLanIps.put(0, "");
    this.sNaoWLanIps.put(0, "");
    this.iNaoBytes.put(0, -1);    
    
    Component[] allComps = scpPanel.getComponents();
    for(int c = 0; c < allComps.length; c++)
    {
      String name = allComps[c].getName();
      if(name != null && allComps[c].getClass() == JTextField.class)
      {
        Integer naoNo = Integer.parseInt(name.replaceAll("[a-zA-Z]+", ""));
        this.naoNumberFields.put(naoNo, (JTextField) allComps[c]);
        this.scriptDone.put(naoNo, false);
        this.copyDone.put(naoNo, false);
        this.hadCopyErrors.put(naoNo, false);
        this.hadScriptErrors.put(naoNo, false);
        this.hadErrors.put(naoNo, false);
        this.sNaoLanIps.put(naoNo, "");
        this.sNaoWLanIps.put(naoNo, "");
        this.iNaoBytes.put(naoNo, -1);
      }
    }
    
    this.networkConfigTags.put("LAN_IP", subnetFieldLAN);
    this.networkConfigTags.put("LAN_BRD", broadcastFieldLAN);
    this.networkConfigTags.put("LAN_MASK", netmaskFieldLAN);
    this.networkConfigTags.put("WLAN_IP", subnetFieldWLAN);
    this.networkConfigTags.put("WLAN_BRD", broadcastFieldWLAN);
    this.networkConfigTags.put("WLAN_MASK", netmaskFieldWLAN);

    naoModel = new DefaultListModel();
    lstNaos.setModel(naoModel);
        
    services = new HashMap<String, NaoSshWrapper>();
    
//    try
//    {      
//      //get own interface ip addresses
//      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) 
//      {
//        NetworkInterface intf = en.nextElement();
//        if(!intf.isLoopback() && intf.isUp())
//        {
//          ArrayList<InetAddress> interfaceAdresses = new ArrayList<InetAddress>();
//          for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) 
//          {
//            InetAddress addr = enumIpAddr.nextElement();
//            {
//              interfaceAdresses.add(addr);
//            }
//          }
//          hostAdresses.put(intf.getName(), interfaceAdresses);
//        }
//      }
//     
//      //hook jmdns service for each address
//      for(String intf : hostAdresses.keySet())
//      {
//        if(hostAdresses.get(intf).size() > 0)
//        {
//          JmDNS j = JmDNS.create(hostAdresses.get(intf).get(0), intf);
//          jmdnsList.add(j);
//          int idx = jmdnsList.indexOf(j);
//          JmdnsServiceListener listener = new JmdnsServiceListener(idx);
//          jmdnsList.get(idx).addServiceListener("_nao._tcp.local.", listener);
////          jmdnsServiceListenerList.add(listener);
//        }
//      }
//    }
//    catch (IOException ex)
//    {
//    }
       
    String value = System.getenv("NAOTH_BZR");
    
    if(value != null)
    {
      value = value + "/NaoTHSoccer";
      setDirectory(value);
    }
    else
    {
      try
      {
        String ResourceName="de/naoth/naoscp/NaoScp.class";
        String programPath = URLDecoder.decode(this.getClass().getClassLoader().getResource(ResourceName).getPath(), "UTF-8");
        programPath = programPath.replace("file:", "");
        //path replacement if NaoScp is being started from console directly
        programPath = programPath.replace("/NaoScp/dist/NaoScp.jar!/de/naoth/naoscp/NaoScp.class", "");
        //path replacement if NaoScp is started from IDE (Netbeans)
        programPath = programPath.replace("/NaoScp/build/classes/de/naoth/naoscp/NaoScp.class", "") + "/NaoTHSoccer";
        File ProgramDir = new File(programPath);
        if(ProgramDir.exists())
        {
          setDirectory(ProgramDir.getAbsolutePath());
        }
      }
      catch(UnsupportedEncodingException ueEx)
      {
        actionInfo("could not determine current work directory\n" + ueEx.getMessage());
      }
    }

    if(config.fhIsTesting)
    {
      config.remotePrefix = "/Users/robotest/deploy";
      subnetFieldLAN.setText("127.0.0");
      sshUser.setText("robotest");
      sshPassword.setText("robotest");
    }
    setActionBtnLabel();
  }

  public void addJmdnsListenerService(final ServiceEvent event, final int idx)
  {   
    SwingUtilities.invokeLater
    (
      new Runnable() 
      {
        public void run() 
        {
          jmdnsList.get(idx).requestServiceInfo(event.getType(), event.getName(), 1);
        }
      }
    );
  }
  
  public void resolveJmdnsListenerService(ServiceEvent event, int idx)
  {
    services.put(jmdnsList.get(idx).getName() + "_nao", new NaoSshWrapper(event.getInfo(), jmdnsList.get(idx).getName() + "_nao"));
    updateList();
  }
  
  public void removeJmdnsListenerService(ServiceEvent event, int idx)
  {
    services.remove(jmdnsList.get(idx).getName() + "_nao");
    updateList();
  }
  
  public void updateList()
  {
    synchronized (naoModel)
    {
      naoModel.clear();
      for(Map.Entry<String, NaoSshWrapper> entry : services.entrySet())
      {
        if (entry.getValue().isValid())
        {
          naoModel.addElement(entry.getValue());
        }
      }

    }
  }

  public static void main(String args[])
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        new NaoScp().setVisible(true);
      }
    });
  }
  
  private void readNetworkConfig() throws FileNotFoundException, IOException
  {
    FileReader fReader = new FileReader(config.localSetupScriptPath() + "/network.conf");
    BufferedReader bReader = new BufferedReader(fReader);

    actionInfo("reading NaoConfigFiles/network.conf");
    String line = bReader.readLine();
    while(line != null)
    {
      String[] param = line.split("=");
      if(param.length == 2)
      {
        String key = param[0];
        String val = param[1];
        if (this.networkConfigTags.containsKey(key))
        {
          String[] value = val.split("\\.");
          if(value.length > 2)
          {
            this.networkConfigTags.get(key).setText(val);
          }
        }
      }          
      line = bReader.readLine();          
    }
    fReader.close();
  }
  
  private void writeNetworkConfig() throws IOException
  {
    File file = new File(config.localSetupScriptPath() + "/network.conf");
    if(!file.isFile())
    {
      file.createNewFile();
    }

    FileWriter fWriter = new FileWriter(config.localSetupScriptPath() + "/network.conf");

    if(subnetFieldLAN.getText().endsWith("."))
    {
      fWriter.write("LAN_IP=" + subnetFieldLAN.getText() + "\n");
    }
    else
    {
      fWriter.write("LAN_IP=" + subnetFieldLAN.getText() + ".\n");
    }
    fWriter.write("LAN_BRD=" + broadcastFieldLAN.getText() + "\n");
    fWriter.write("LAN_MASK=" + netmaskFieldLAN.getText() + "\n");

    if(subnetFieldWLAN.getText().endsWith("."))
    {
      fWriter.write("WLAN_IP=" + subnetFieldWLAN.getText() + "\n");
    }
    else
    {
      fWriter.write("WLAN_IP=" + subnetFieldWLAN.getText() + ".\n");
    }
    fWriter.write("WLAN_BRD=" + broadcastFieldWLAN.getText() + "\n");
    fWriter.write("WLAN_MASK=" + netmaskFieldWLAN.getText() + "\n");

    fWriter.close();
  }
  
  private void initNetworkConfig()
  {
    try
    {    
      try
      {      
        readNetworkConfig();
      }
      catch(FileNotFoundException fnfEx)
      {
        writeNetworkConfig();
      }
    }
    catch(IOException ioEx)
    {
      actionInfo("network config file not read and writeable");
    }
    
  }
  
  
  // fills the iNaoBytes, sNaoWLanIps, sNaoLanIps,
  private boolean checkNaoIps(Boolean init)
  {
    int naoIpCount = 0;
    for(Integer naoNo : this.iNaoBytes.keySet())
    {
      if(naoNo > 0)
      {
        iNaoBytes.put(naoNo, Integer.parseInt(naoNumberFields.get(naoNo).getText()));
      }
      else
      {
        if(!init && naoNo == 0)
        {
          iNaoBytes.put(naoNo, -1);
        }
      }
    }
    
    for(Integer naoNo : this.sNaoLanIps.keySet())
    {
      if(naoNo > 0 )
      {
        if(iNaoBytes.get(naoNo) < 256 && iNaoBytes.get(naoNo) > -1 )
        {
          if(subnetFieldLAN.getText().endsWith("."))
          {
            sNaoLanIps.put(naoNo, subnetFieldLAN.getText() + iNaoBytes.get(naoNo));
          }
          else
          {
            sNaoLanIps.put(naoNo, subnetFieldLAN.getText() + "." + iNaoBytes.get(naoNo));
          }
          if(subnetFieldWLAN.getText().endsWith("."))
          {
            sNaoWLanIps.put(naoNo, subnetFieldWLAN.getText() + iNaoBytes.get(naoNo));
          }
          else
          {
            sNaoWLanIps.put(naoNo, subnetFieldWLAN.getText() + "." + iNaoBytes.get(naoNo));
          }
          naoIpCount++;
        }
        else
        {
          sNaoLanIps.put(naoNo, "");
          sNaoWLanIps.put(naoNo, "");
        }
      }
      else
      {
        sNaoLanIps.put(naoNo, "");
        sNaoWLanIps.put(naoNo, "");
      }
    }
    return naoIpCount > 0;
  }

  
  private NaoScpConfig createDeployConfig()
  {
    NaoScpConfig cfg = new NaoScpConfig(config);
    cfg.addresses.clear();
    cfg.copyConfig = cbCopyConfig.isSelected();
    cfg.copyLib = cbCopyLib.isSelected();
    cfg.copyExe = cbCopyExe.isSelected();
    cfg.copyLogs = cbCopyLogs.isSelected();
    cfg.restartNaoqi = cbRestartNaoqi.isSelected();
    cfg.restartNaoth = cbRestartNaoth.isSelected();
    cfg.noBackup = cbNoBackup.isSelected();
    cfg.forceBackup = cbForceBackup.isSelected();
    cfg.sTeamCommPort = jTeamCommPort.getText();
    cfg.scheme = jSchemeBox.getSelectedItem().toString();
    cfg.teamNumber = jTeamNumber.getText();
    cfg.teamColor = jColorBox.getSelectedItem().toString();
    cfg.comment = jCommentTextArea.getText();
    
    if(cfg.backupIsSelected)
    {
      cfg.boxSelected = jBackupBox.getSelectedItem().toString();
      cfg.selectedBackup = cfg.backups.get(jBackupBox.getSelectedItem()).toString();
    }
    else
    {
      cfg.boxSelected = "";
      cfg.selectedBackup = "";
    }

    return cfg;
  }
  
  
  private void copyFiles2Nao()
  {
    clearLog();
    setFormEnabled(false);
    
    NaoScpConfig cfg = createDeployConfig();

    if(cfg.copyConfig || cfg.copyExe || cfg.copyLib || cfg.copyLogs || cfg.forceBackup || cfg.noBackup)
    {
      showCopyDoneMsg = true;
    }

    if(cfg.restartNaoth || cfg.restartNaoqi)
    {
      if(showCopyDoneMsg)
      {
        showCopyDoneMsg = false;
        showScriptDoneMsg = false;
        showDoneMsg = true;
      }
      else
      {
        showScriptDoneMsg = true;
        showDoneMsg = false;
      }
    }
    if(cfg.debugVersion && cfg.noBackup)
    {
      logTextPane.setBackground(Color.PINK);
    }
    else
    {
      logTextPane.setBackground(Color.WHITE);
    }

    if(!checkNaoIps(false))
    {
      actionInfo("[0;31mNo Nao has a valid ip address specified\n[0m");
      setFormEnabled(true);
      return;
    }
    
    cfg.backupTimestamp = String.valueOf(System.currentTimeMillis());
    
    prepareDeploy(cfg, false);
   
    for(Integer naoNo : copyDone.keySet())
    {
      if(naoNo > 0)
      {
        scriptDone.put(naoNo, false);
        copyDone.put(naoNo, false);
      }
      hadCopyErrors.put(naoNo, false);
      hadScriptErrors.put(naoNo, false);
      hadErrors.put(naoNo, false);
    }
    copyDone.put(0, true);
    scriptDone.put(0, true);

    for(Integer naoNo : sNaoLanIps.keySet())
    {
      if(!sNaoLanIps.get(naoNo).equals(""))
      {
        NaoScpConfig naoCfg = new NaoScpConfig(cfg);        
        naoCfg.addresses.add(sNaoLanIps.get(naoNo));
        naoCfg.addresses.add(sNaoWLanIps.get(naoNo));

        if(!showCopyDoneMsg && showScriptDoneMsg)
        {
          if (naoCfg.restartNaoqi)
          {
            remoteScriptRunner scriptRunner = new remoteScriptRunner(naoCfg, String.valueOf(naoNo), String.valueOf(iNaoBytes.get(naoNo)), "restartNaoqi", false);
            scriptRunner.execute();
          }
          else if(naoCfg.restartNaoth)
          {
            remoteScriptRunner scriptRunner = new remoteScriptRunner(naoCfg, String.valueOf(naoNo), String.valueOf(iNaoBytes.get(naoNo)), "restartNaoTH", false);
            scriptRunner.execute();
          }
          else
          {
            allIsDone(naoNo);
          }
        }
        else
        {
          remoteCopier copier = new remoteCopier(naoCfg, String.valueOf(naoNo), String.valueOf(iNaoBytes.get(naoNo)));
          copier.execute();
        }
      }
      else
      {
        allIsDone(naoNo);
      }
    }
  }
  
  // todo: take this apart
  private boolean prepareDeploy(NaoScpConfig cfg, boolean init)
  {
    this.actionInfo("preparing deploy dir");
    
    boolean result = true;
    checkNaoIps(init);
    
    for(Integer playerNumber : iNaoBytes.keySet())
    {
      if((!init && playerNumber > 0) || (init && playerNumber == 0))
      {
        // last byte of the player ip adress
        int playerIpByte = iNaoBytes.get(playerNumber);
        String sNaoByte = String.valueOf(playerIpByte);
        if(playerIpByte < 256 && playerIpByte > -1)
        {
          String playerNumberString = String.valueOf(playerNumber);
          
          String myConfigPathIn = cfg.localDeployInPath(playerNumberString, sNaoByte) + "/Config";
          File myConfigDirIn = new File(myConfigPathIn);
          myConfigDirIn.mkdirs();
    
          String currentDeployPath = cfg.localDeployOutPath(playerNumberString);
          result = DeployUtils.prepareDeploy(this, cfg, playerNumber.intValue(), currentDeployPath) && result;
        }
      }
    }
    
    this.actionInfo("finished preparing deploy dir");
    return result;
  }  
  

  private boolean checkDirPath(boolean verbose)
  {
    if(config == null || config.jDirPathLabel == null)
    {
      if(verbose)
      {
        JOptionPane.showMessageDialog(null, "Main Directory not set");
      }
      return false;
    }
    return true;
  }

  public void haveError(String sNaoNo, String error)
  {
     haveError(Integer.parseInt(sNaoNo), error);
  }
  
  public void haveError(int naoNo, String error)
  {
    hadErrors.put(naoNo, true);
    actionInfo(error);
  }
  
  public void haveCopyError(String sNaoNo, String error)
  {
     haveCopyError(Integer.parseInt(sNaoNo), error);
  }
  
  public void haveCopyError(int naoNo, String error)
  {
    hadCopyErrors.put(naoNo, true);
    haveError(naoNo, "[041mcopy error: " + error + "\n[0m");
  }

  public void haveScriptError(String sNaoNo, String error)
  {
     haveScriptError(Integer.parseInt(sNaoNo), error);
  }
  
  public void haveScriptError(int naoNo, String error)
  {
    hadScriptErrors.put(naoNo, true);
    haveError(naoNo, "[041mscript error:[0m " + error + "\n[0m");
  }

  public void allIsDone(String sNaoNo)
  {
     allIsDone(Integer.parseInt(sNaoNo));
  }
  
  public void allIsDone(int naoNo)
  {
    copyDone.put(naoNo, true);
    scriptDone.put(naoNo, true);
    checkIfFinished();
  }
    
  public void scriptIsDone(String sNaoNo)
  {
     scriptIsDone(Integer.parseInt(sNaoNo));
  }
  
  public void scriptIsDone(int naoNo)
  {
    scriptDone.put(naoNo, true);
    checkIfFinished();
  }  
  
  public void copyIsDone(String sNaoNo)
  {
     copyIsDone(Integer.parseInt(sNaoNo));
  }
  
  public void copyIsDone(int naoNo)
  {
    copyDone.put(naoNo, true);
    checkIfFinished();
  }

  public void checkIfFinished()
  {
    boolean done = true;
    boolean hadError = false;
    boolean hadCopyError = false;
    boolean hadScriptError = false;
    
    String add = "";
    
    for(Integer naoNo_ : this.iNaoBytes.keySet())
    {
      hadError = hadError || hadErrors.get(naoNo_);
      hadCopyError = hadCopyError || hadCopyErrors.get(naoNo_);
      hadScriptError = hadScriptError || hadScriptErrors.get(naoNo_);
      if(showDoneMsg)
      {
        done = done && copyDone.get(naoNo_) && scriptDone.get(naoNo_);
      }
      else
      {
        if(showCopyDoneMsg)
        {
          done = done && copyDone.get(naoNo_);
        }
        if(showScriptDoneMsg)
        {
          done = done && scriptDone.get(naoNo_);
        }
      }
    }
    if(done)
    {    
      if(showDoneMsg)
      {
        if(hadError)
        {
          add = " - error with Nao ";
          for(Integer naoNo_ : this.hadErrors.keySet())
          {
            if(hadErrors.get(naoNo_))
            {
              add += " " + naoNo_ + " ";
            }
          }
          add += " - please check Logs.";
        }
        actionInfo("[1;32mCopy + Scripts done\n[0m");
        JOptionPane.showMessageDialog(this, "Copy + Scripts done" + add);
      }
      else
      {
        if(showCopyDoneMsg)
        {
          if(hadCopyError)
          {
            add = " - copy error with Nao ";
            for(Integer naoNo_ : this.hadCopyErrors.keySet())
            {
              if(hadCopyErrors.get(naoNo_))
              {
                add += " " + naoNo_ + " ";
              }
            }
            add += " - please check Logs.";
          }
          actionInfo("[1;32mCopy done\n[0m");
          JOptionPane.showMessageDialog(this, "Copy done" + add);
        }
        if(showScriptDoneMsg)
        {
          if(hadCopyError)
          {
            add = " - script error with Nao ";
            for(Integer naoNo_ : this.hadScriptErrors.keySet())
            {
              if(hadScriptErrors.get(naoNo_))
              {
                add += " " + naoNo_ + " ";
              }
            }
            add += " - please check Logs.";
          }
          actionInfo("[1;32mScripts done\n[0m");
          JOptionPane.showMessageDialog(this, "Scripts done" + add);
        }
      }
      resetBackupList();    
      setFormEnabled(true);
    }
  }  
  
  private void setFormEnabled(boolean enable)
  {
    copyButton.setEnabled(enable);
    cbCopyConfig.setEnabled(enable);
    cbCopyLib.setEnabled(enable);
    cbCopyExe.setEnabled(enable);
    cbCopyLogs.setEnabled(enable);

    cbRestartNaoth.setEnabled(enable);
    cbRestartNaoqi.setEnabled(enable);
    cbRebootSystem.setEnabled(enable);
    if(config.debugVersion)
    {
      cbNoBackup.setEnabled(enable);
    }
    cbForceBackup.setEnabled(enable);

    if(jBackupBox.getItemCount() > 1 || !enable)
    {
      jBackupBox.setEnabled(enable);
    }

    jColorBox.setEnabled(enable);
    jCommentTextArea.setEnabled(enable);
    jDirChooser.setEnabled(enable);
    jSchemeBox.setEnabled(enable);

    jTeamCommPort.setEnabled(enable);
    jTeamNumber.setEnabled(enable);

    naoByte1.setEnabled(enable);
    naoByte2.setEnabled(enable);
    naoByte3.setEnabled(enable);
    naoByte4.setEnabled(enable);

    sshUser.setEnabled(enable);
    sshRootUser.setEnabled(enable);
    sshPassword.setEnabled(enable);
    sshRootPassword.setEnabled(enable);

    subnetFieldLAN.setEnabled(enable);
    netmaskFieldLAN.setEnabled(enable);
    broadcastFieldLAN.setEnabled(enable);

    subnetFieldWLAN.setEnabled(enable);
    netmaskFieldWLAN.setEnabled(enable);
    broadcastFieldWLAN.setEnabled(enable);

    radioWPA.setEnabled(enable);
    radioWEP.setEnabled(enable);
    wlanSSID.setEnabled(enable);
    wlanKey.setEnabled(enable);

    lstNaos.setEnabled(enable);
            
    jButtonRefreshData.setEnabled(enable);
    jButtonSaveNetworkConfig.setEnabled(enable);
    jButtonInitRobotSystem.setEnabled(enable);
    jButtonSetRobotNetwork.setEnabled(enable);
    jButtonRemoteKernelVideoReload.setEnabled(enable);
    
    btWriteToStick.setEnabled(enable);
  }

/**
   * Staging - prepares "deploy out dir for setting up nao system" with the files to be copied to the robots
   * copies files, modifies nao system cfgs according to UI settings
   * @return
   */
  private boolean prepareSetupDeploy(NaoScpConfig cfg, String sNaoByte)
  {
    actionInfo("preparing deploy dir");

    File deployOutDir = new File(cfg.localDeployRootPath());

    if( ! deployOutDir.isDirectory())
    {
      if( ! deployOutDir.mkdirs())
      {
        actionInfo("Error: Could not create deploy out directory");
        return false;
      }
    }

    if( ! deployOutDir.canWrite())
    {
      actionInfo("Error: Cant write to deploy out dir");
      return false;
    }

    checkNaoIps(false);
    int naoNo = 0;
    int iNaoByte = Integer.parseInt(sNaoByte);
    
    if(iNaoByte < 256 && iNaoByte > -1)
    {
      String sNaoNo = String.valueOf(naoNo);

      String mySetupScriptPath = cfg.localDeployOutPath(sNaoNo) + cfg.setupScriptPath();
      File mySetupScriptDir = new File(mySetupScriptPath);
      if(mySetupScriptDir.isDirectory())
      {
        DeployUtils.deleteDir(mySetupScriptDir);
      }
      mySetupScriptDir.mkdirs();

      File mySetupScriptCheckRC = new File(mySetupScriptPath +"/checkRC.sh");        
      DeployUtils.copyFiles(this, new File(cfg.localSetupScriptPath() + "/checkRC.sh"), mySetupScriptCheckRC);
      File mySetupScriptInitEnv = new File(mySetupScriptPath +"/init_env.sh");        
      DeployUtils.copyFiles(this, new File(cfg.localSetupScriptPath() + "/init_env.sh"), mySetupScriptInitEnv);
      File mySetupScriptInitNet = new File(mySetupScriptPath +"/init_net.sh");        
      DeployUtils.copyFiles(this, new File(cfg.localSetupScriptPath() + "/init_net.sh"), mySetupScriptInitNet);
      File myAutoloadIni = new File(mySetupScriptPath +"/autoload.ini");        
      DeployUtils.copyFiles(this, new File(cfg.localSetupScriptPath() + "/autoload.ini"), myAutoloadIni);
      File myNaothScript = new File(mySetupScriptPath +"/naoth");        
      DeployUtils.copyFiles(this, new File(cfg.localSetupScriptPath() + "/naoth"), myNaothScript);
            
      File libRT = new File(cfg.stagingLibDir +"/usr/lib/librt.so");
      if(libRT.exists())
      {
        libRT.delete();
      }
      File mySetupDirEtc = new File(mySetupScriptPath + "/etc");
      mySetupDirEtc.mkdirs();
      DeployUtils.copyFiles(this, new File(cfg.localSetupScriptPath() + "/etc"), mySetupDirEtc);
      
      File mySetupDirBin = new File(mySetupScriptPath + "/usr/bin");
      mySetupDirBin.mkdirs();
      DeployUtils.copyFiles(this, new File(cfg.localSetupScriptPath() + "/bin"), mySetupDirBin);
      
      
      setConfdNet(cfg, sNaoByte);
      setHostname(cfg, sNaoByte);
      saveWpaSupplicant(cfg, cfg.localDeployOutPath("0") + cfg.setupScriptPath());
    }
        
    actionInfo("finished preparing deploy dir");

    return true;
  }//end prepareSetupDeploy

  private void setConfdNet(NaoScpConfig cfg, String sNaoByte)
  {
      try
      {
        BufferedReader br = new BufferedReader(new FileReader(cfg.localSetupScriptPath() + "/etc/conf.d/net"));

        String line = "";
        String fileContent = "";
        while(line != null)
        {
          line = br.readLine();
          if(line != null)
          {
            fileContent += line + "\n";
          }
        }
        br.close();
        if(subnetFieldLAN.getText().endsWith("."))
        {
          fileContent = fileContent.replace("ETH_ADDR", subnetFieldLAN.getText() + sNaoByte);
        }
        else
        {
          fileContent = fileContent.replace("ETH_ADDR", subnetFieldLAN.getText() + "."+ sNaoByte);
        }
        if(subnetFieldWLAN.getText().endsWith("."))
        {
          fileContent = fileContent.replace("WLAN_ADDR", subnetFieldWLAN.getText() + sNaoByte);
        }
        else
        {
          fileContent = fileContent.replace("WLAN_ADDR", subnetFieldWLAN.getText() + "."+ sNaoByte);
        }
        fileContent = fileContent.replace("ETH_NETMASK", netmaskFieldLAN.getText());
        fileContent = fileContent.replace("WLAN_NETMASK", netmaskFieldWLAN.getText());
        fileContent = fileContent.replace("ETH_BRD", broadcastFieldLAN.getText());
        fileContent = fileContent.replace("WLAN_BRD", broadcastFieldWLAN.getText());
        //fileContent.trim();

        BufferedWriter bw = new BufferedWriter(new FileWriter(cfg.localDeployOutPath("0") + cfg.setupScriptPath() + "/etc/conf.d/net"));
        bw.write(fileContent);
        bw.close();
      }
      catch(Exception e)
      {
        actionInfo(e.toString());
      }
  }//end setConfdNet

  private void loadWpaSupplicant()
  {
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(config.localSetupScriptPath() + "/etc/wpa_supplicant/wpa_supplicant.conf"));
      String line = "";
      String fileContent = "";
      while(line != null)
      {
        line = br.readLine();
        if(line != null)
        {
          if(line.contains("ssid=\""))
          {
            String ssid = line.trim().replace("ssid=", "").replace("\"", "");
            wlanSSID.setText(ssid);
          }
          if(line.contains("wep_key0="))
          {
            String ssid = line.trim().replace("wep_key0=", "");
            wlanKey.setText(ssid);
          }
          if(line.contains("psk="))
          {
            String ssid = line.trim().replace("psk=", "").replace("\"", "");
            wlanKey.setText(ssid);
          }
          fileContent += line + "\n";
        }
      }
      br.close();
      if(fileContent.contains("WPA-PSK"))
      {
        radioWPA.setSelected(true);
        radioWEP.setSelected(false);
      }
      else
      {
        radioWPA.setSelected(false);
        radioWEP.setSelected(true);
      }
    }
    catch(Exception e)
    {}

  }//end loadWpaSupplicant

  private void saveWpaSupplicant(NaoScpConfig cfg, String dstDir)
  {
    try
    {
      String wpaConfigFileName = cfg.localSetupScriptPath() + "/wpa_supplicant.wpa";
      if(radioWEP.isSelected())
      {
        wpaConfigFileName = cfg.localSetupScriptPath() + "/wpa_supplicant.wep";
      }
      BufferedReader br = new BufferedReader(new FileReader(wpaConfigFileName));

      String line = "";
      String fileContent = "";
      while(line != null)
      {
        line = br.readLine();
        if(line != null)
        {
          fileContent += line + "\n";
        }
      }
      br.close();
      fileContent = fileContent.replace("WLAN_SSID", wlanSSID.getText());
      fileContent = fileContent.replace("WLAN_KEY", wlanKey.getText());
      //fileContent.trim();

      File wpaConfig = new File(dstDir + "/etc/wpa_supplicant/wpa_supplicant.conf");
      if(wpaConfig.exists())
      {
        wpaConfig.delete();
      }
      BufferedWriter bw = new BufferedWriter(new FileWriter(dstDir + "/etc/wpa_supplicant/wpa_supplicant.conf"));
      bw.write(fileContent);
      bw.close();
    }
    catch(Exception e)
    {
      actionInfo(e.toString());
    }
  }//end setWpaSupplicant
  
  private void setHostname(NaoScpConfig cfg, String sNaoByte)
  {
    try
    {
      BufferedReader br = new BufferedReader(new FileReader(cfg.localSetupScriptPath() + "/etc/hostname"));

      String line = "";
      String fileContent = "";
      while(line != null)
      {
        line = br.readLine();
        if(line != null)
        {
          fileContent += line + "\n";
        }
      }
      br.close();
      fileContent = fileContent.replace("NAONR", sNaoByte);

      BufferedWriter bw = new BufferedWriter(new FileWriter(cfg.localDeployOutPath("0") + cfg.setupScriptPath() + "/etc/hostname"));
      bw.write(fileContent);
      bw.close();


      br = new BufferedReader(new FileReader(cfg.localSetupScriptPath() + "/etc/conf.d/hostname"));

      line = "";
      fileContent = "";
      while(line != null)
      {
        line = br.readLine();
        if(line != null)
        {
          fileContent += line + "\n";
        }
      }
      br.close();

      fileContent = fileContent.replace("NAONR", sNaoByte);
      bw = new BufferedWriter(new FileWriter(cfg.localDeployOutPath("0") + cfg.setupScriptPath() + "/etc/conf.d/hostname"));
      bw.write(fileContent);
      bw.close();
    }
    catch(Exception e)
    {
      actionInfo(e.toString());
    }
  }//end setHostname
  
  
  private String readFile(String fileName)
  {
    StringBuilder content = new StringBuilder();

    try 
    {
      //use buffering, reading one line at a time
      //FileReader always assumes default encoding is OK!
      BufferedReader input =  new BufferedReader(new FileReader(fileName));
      try {
        String line; //not declared within while loop
        /*
        * readLine is a bit quirky :
        * it returns the content of a line MINUS the newline.
        * it returns null only for the END of the stream.
        * it returns an empty String if two newlines appear in a row.
        */
        while (( line = input.readLine()) != null){
          content.append(line);
          content.append("\n");
        }
      }
      finally {
        input.close();
      }
    }
    catch (IOException ex)
    {
      actionInfo("I/O Error in readFile- " + fileName + "\n" + ex.toString());
    }

    return content.toString();
  }//end readFile

  private void setDirectory(String directoryName)
  {
    jDirPathLabel.setText(directoryName);
    config.jDirPathLabel = directoryName;
    setSchemes();
    copyButton.setEnabled(true);
    jButtonSaveNetworkConfig.setEnabled(true);
    jButtonSetRobotNetwork.setEnabled(true);
    jButtonInitRobotSystem.setEnabled(true);
    setFormData();
    setRobots();
  }

  private void setCommentText()
  {
    String comment = "";
    if(jBackupBox.getItemCount() > 1)
    {
      File file = new File(config.localDeployRootPath() + "/in/" + config.backups.get(jBackupBox.getSelectedItem()) + "/comment.cfg");
      if(file.exists() && file.isFile())
      {
        comment = readFile(file.getPath());
      }
    }
    jCommentTextArea.setText(comment);
  }

  private void resetBackupList()
  {
    String deployInPath = config.localDeployRootPath() + "/in";
    File deployInDir = new File(deployInPath);
    jBackupBox.removeAllItems();
    jBackupBox.addItem("no backups to copy");
    if(deployInDir.exists())
    {
      FileFilter filter;
      filter = new FileFilter()
                {
                  public boolean accept(File f)
                  {
                      return f.isDirectory();
                  }
                  public String getDescription()
                  {
                      return "Directory";
                  }
                };

      File[] backupFiles = deployInDir.listFiles(filter);
      Arrays.sort(backupFiles);

      String[] backupItems = new String[backupFiles.length];
      
      for(int i = backupFiles.length - 1; i >= 0; i--)
      {
        String entry = backupFiles[i].getName();
        Boolean entryAvaliable = false;
        String entryTag = "";
        Boolean isMinimal = false;

        File minConfigDir = new File(config.localDeployRootPath() + "/in/" + entry + "/MinimalConfig/");
        if(minConfigDir.exists() && minConfigDir.isDirectory())
        {
          entryAvaliable = true;
          isMinimal = true;
        }

        File configDir = new File(config.localDeployRootPath() + "/in/" + entry + "/Config/");
        if(configDir.exists() && configDir.isDirectory())
        {
          entryAvaliable = true;
          entryTag += "C";
        }

        File libFile = new File(config.localDeployRootPath() + "/in/" + entry + "/libnaosmal.so");
        if(libFile.exists() && libFile.isFile())
        {
          entryAvaliable = true;
          entryTag += "L";
        }

        File exeFile = new File(config.localDeployRootPath() + "/in/" + entry + "/naoth");
        if(exeFile.exists() && exeFile.isFile())
        {
          entryAvaliable = true;
          entryTag += "E";
        }

        if(isMinimal)
        {
          if(entryTag.equals(""))
          {
            entryTag = "M" + entryTag;
          }
          else
          {
            entryTag = "M [" + entryTag + "]";
          }
          
        }

        boolean error = false;

        if(entryAvaliable)
        {
          String[] split = entry.split("-");
          Long time = Long.parseLong(split[0]);
          Date date = new Date(time);
          Format formatter;
          formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
          if(split.length > 2)
          {
            backupItems[i] = "Nao " + split[2] + " (" + split[1] + ") (" + entryTag + "): " + formatter.format(date);
          }
          else if(split.length > 1)
          {
            backupItems[i] = "Nao (" + split[1] + ") (" + entryTag + "): " + formatter.format(date);
          }
          else
          {
            error = true;
          }
          if(!error)
          {
            config.backups.put(backupItems[i], entry);
            jBackupBox.addItem(backupItems[i]);
          }
        }
      }
      if(jBackupBox.getItemCount() > 1)
      {
        jBackupBox.setEnabled(true);
      }
      else
      {
        jBackupBox.setEnabled(false);
      }
    }
  }

  private void setFormData()
  {
    this.initNetworkConfig();
    resetBackupList();
    
    File f = new File(config.localConfigPath() + "/general/player.cfg");
    if(!f.isFile())
    {
      DeployUtils.writePlayerCfg(this, f, setupPlayerNo, jTeamNumber.getText(), jColorBox.getSelectedItem().toString());
    }
    else
    {
      readPlayerCfg(f);
    }

    loadWpaSupplicant();

    f = new File(config.localConfigPath() + "/general/teamcomm.cfg");
    if(!f.isFile())
    {
      DeployUtils.writeTeamcommCfg(this, jTeamCommPort.getText(), f);
    }
    else
    {
      readTeamCfg(f);
    }
  }


  private boolean setRobots()
  {
    File configDir = new File(config.localConfigPath());
    if( !configDir.isDirectory()) {
      return false;
    }

    File robotsDir = new File(config.localConfigPath() + "/robots");
    if( !robotsDir.isDirectory()) {
      return false;
    }
    
    File files[] = robotsDir.listFiles();
    Arrays.sort(files);
    
    try {
        for(int i = 0, n = files.length; i < n; i ++)
        {
          if(files[i].isDirectory()) {
            final String name = files[i].getName();
            int number = 3;
            bodyIdToPlayerNumber.put(name, number);
            
            final MaskFormatter formatter = new MaskFormatter(name+": *");
            formatter.setValidCharacters("12345");
            formatter.setPlaceholderCharacter(Character.forDigit(number, 10));
            
            final JFormattedTextField input = new JFormattedTextField(formatter);
            input.setInputVerifier( new InputVerifier() {
                public boolean verify(JComponent input) {
                    JFormattedTextField ftf = (JFormattedTextField)input;
                    try {
                        String s = (String)formatter.stringToValue(ftf.getText());
                        // capture the player number from the input and parse it
                        final Matcher matcher = Pattern.compile( name+":[ \\\\t]*(\\d)" ).matcher( ftf.getText() );
                        if(matcher.find() && matcher.groupCount() == 1) {
                            int number = Integer.parseInt(matcher.group(1));
                            bodyIdToPlayerNumber.put(name, number);
                        }
                    } catch (Exception ex) {}
                    return true;
                }
            });
            
            this.playerNumbersPanel.add(input);
          }
        }

        return true;
    }catch(ParseException ex) {
        return false;
    }
  }

  /**
   * populates the Schemes-Dropdown with the Schemes found in the schemedir
   *
   * @return
   */
  private boolean setSchemes()
  {
    File configPath = new File(config.localConfigPath());
    if( ! configPath.isDirectory())
    {
      return false;
    }

    File schemePath = new File(config.localConfigPath() + "/scheme");
    if( ! schemePath.isDirectory())
    {
      return false;
    }

    File files[] = schemePath.listFiles();
    Arrays.sort(files);
    jSchemeBox.removeAllItems();
    jSchemeBox.addItem("n/a");
    for(int i = 0, n = files.length; i < n; i ++)
    {
      if(files[i].isDirectory())
      {
        jSchemeBox.addItem(files[i].getName());
      }
    }
    jSchemeBox.setSelectedIndex(0);
    return true;
  }//end setSchemes
    
  private void logBashColored(String logtext)
  {
    ArrayList<String> unformatedParts = new ArrayList<String>();
    ArrayList<String> partFormats = new ArrayList<String>();
    partFormats.add(lastBashColorOption);
    
    String bashFormatIdentifier = "["; 
    int posLeft = 0;     
    
    while (posLeft < logtext.length())
    {
      int pos = logtext.indexOf(bashFormatIdentifier, posLeft);
      if(pos != -1)
      {
        int posFormatEnd = logtext.indexOf("m", pos + 1);
        if(posFormatEnd != -1)
        {
          String format = logtext.substring(pos + 1, posFormatEnd);
          String toBeFormated = logtext.substring(posLeft, pos);
          partFormats.add("bash_" + format.replace(";", "_"));
          lastBashColorOption = "bash_" + format.replace(";", "_");
          unformatedParts.add(toBeFormated);
          pos = posFormatEnd;
        }
        posLeft = pos;
      }     
      posLeft++;
    }
    unformatedParts.add("");
    StyledDocument doc = logTextPane.getStyledDocument();

    try 
    {
      if(unformatedParts.size() > 1)
      {
        String unformated = "";
        for (int i = 0; i < unformatedParts.size(); i++) 
        {
          unformated += unformatedParts.get(i);
          if(logActionInfo)
          {
            Style s = doc.getStyle(partFormats.get(i));
            doc.insertString(doc.getLength(), unformatedParts.get(i), s);
          }
        }
        jCopyStatus.setText(unformated);
        System.out.println(unformated);
      }
      else
      {
        jCopyStatus.setText(logtext);
        System.out.println(logtext + "\n");
        if(logActionInfo)
        {
          doc.insertString(doc.getLength(), logtext + "\n", doc.getStyle(lastBashColorOption));
        }
      }
    } 
    catch (BadLocationException ble) 
    {
//        System.err.println("Couldn't insert initial text into text pane.");
    }
  }  
  
  
  public final void actionInfo(String logtext)
  {
    logBashColored(logtext);
  }//end log
  
  public final void actionError(String logtext)
  {
    logBashColored("[0;31m" + logtext + "\n[0m");
  }
  
  public final void actionFinish(String logtext)
  {
    logBashColored("[0;32m" + logtext + "\n[0m");
  }

  private void clearLog()
  {
    logTextPane.setText("");
  }//end log

  
  private void readPlayerCfg(File configFile)
  {
    try
    {
      BufferedReader reader = new BufferedReader(new FileReader(configFile));
      String line = reader.readLine();
      while(line != null && !line.contains("[player]"))
      {
        line = reader.readLine();
      }
      line = reader.readLine();
      while(line != null)
      {
        String value;
        if(line.contains("TeamColor="))
        {
          value = line.replace("TeamColor=", "").trim();
          for(int idx = 0; idx < jColorBox.getItemCount(); idx++)
          {
            if(value.equalsIgnoreCase(jColorBox.getItemAt(idx).toString()) )
            {
              jColorBox.setSelectedIndex(idx);
              break;
            }
          }
        }
        else if(line.contains("TeamNumber="))
        {
          value = line.replace("TeamNumber=", "").trim();          
          jTeamNumber.setText(value);
        }        
        line = reader.readLine();
      }
    }
    catch(Exception e)
    {
      actionInfo(e.toString());
    }
  }
  
  
  private void readTeamCfg(File configFile)
  {
    try
    {
      BufferedReader reader = new BufferedReader(new FileReader(configFile));
      String line = reader.readLine();
      while(line != null && !line.contains("[teamcomm]"))
      {
        line = reader.readLine();
      }
      line = reader.readLine();
      while(line != null)
      {
        String value;
        if(line.contains("port="))
        {
          value = line.replace("port=", "").trim();          
          jTeamCommPort.setText(value);
        }        
        line = reader.readLine();
      }
    }
    catch(Exception e)
    {
      actionInfo(e.toString());
    }
  }
 
  
  public boolean isIPV6Address(String hostAddress)
  {
    if(hostAddress != null && hostAddress.contains(":"))
    {
      return true;
    }
    return false;
  }

  public boolean isIPV4Address(String hostAddress)
  {
    if(hostAddress.contains(":") || !hostAddress.contains(".") || hostAddress.length() < 8)
    {
      return false;
    }
    String[] IPv4Parts = hostAddress.trim().split("\\.");
    if(IPv4Parts.length != 4)
    {
      return false;
    }
    for(int p = 0; p < 4; p++)
    {
      int part;
      try
      {
        part = Integer.parseInt(IPv4Parts[p]);
      }
      catch(NumberFormatException ex)
      {
        return false;
      }
      if(part < 0 || part > 255)
      {
        return false;
      }
    }    
    return true;
  }

  
  private class robotConfigPreparator
  {
    private ArrayList<String> addresses;
    String address;
    robotConfigPreparator(NaoScpConfig naoScpConfig)
    {
      synchronized (naoModel)
      {
        clearLog();
        naoScpConfig.copyConfig = true;
        naoScpConfig.copyLib = true;
        naoScpConfig.copyExe = true;
        naoScpConfig.copyLogs = false;
        naoScpConfig.restartNaoth = false;
        naoScpConfig.noBackup = true;

        setFormEnabled(false);

        showCopyDoneMsg = false;
        showScriptDoneMsg = false;
        showDoneMsg = true;

        for(Integer naoNo : copyDone.keySet())
        {
          if(naoNo > 0)
          {
            copyDone.put(naoNo, true);
            scriptDone.put(naoNo, true);
          }
          hadCopyErrors.put(naoNo, false);
          hadScriptErrors.put(naoNo, false);
          hadErrors.put(naoNo, false);
        }
        copyDone.put(0, false);
        scriptDone.put(0, false);
        naoScpConfig.backupTimestamp = String.valueOf(System.currentTimeMillis());

        addresses = new ArrayList<String>();      
        NaoSshWrapper w;
        int i = lstNaos.getSelectedIndex();
        lstNaos.clearSelection();
        if (i >= 0)
        {
          w = (NaoSshWrapper) naoModel.get(i);

          if(w.getAddresses() != null && w.getAddresses().length > 0)
          {
            for(InetAddress addr : w.getAddresses())
            {
              if(addr instanceof Inet4Address)
              {
                addresses.add(addr.getHostAddress());
              }
            }
            address = w.getAddresses()[0].getHostAddress();
          }
          else
          {
            address = w.getDefaultHostAddress();
          }

          if(isIPV6Address(address))
          {
            if(address == null)
            {
              address = JOptionPane.showInputDialog("IPv6 addresses are not supported. Enter appropriate IPv4 address", "");             
              while(address != null && !isIPV4Address(address))
              {
                address = JOptionPane.showInputDialog("Not a valid IPv4 address. Enter a valid one", "");             
              }
            }
          }
        }
        else
        {        
          address = JOptionPane.showInputDialog("No IP selected. Enter an IPv4 address", "");
          while(address != null && !isIPV4Address(address))
          {
            if(isIPV6Address(address))
            {
              address = JOptionPane.showInputDialog("IPv6 addresses are not supported. Enter appropriate IPv4 address", ""); 
            }
            else
            {
              address = JOptionPane.showInputDialog("Not a valid IPv4 address. Enter a valid one", "");             
            }
          }
          addresses.clear();
          addresses.add(address);
        }
      }
    }
    
    public String getDefaultAddress()
    {
      sNaoLanIps.put(0, address);
      sNaoWLanIps.put(0, address);
      return address;
    }
    
    public String askForNaoNumber()
    {
      String sNaoByte = null;
      int iNaoByte = -1;
      while(iNaoByte > 255 || iNaoByte < 0)
      {
        sNaoByte = JOptionPane.showInputDialog("Enter Nao Number (0-255)", "");
        if(sNaoByte == null)
        {
          break;
        }
        else
        {
          try
          {
            iNaoByte = Integer.parseInt(sNaoByte);
          }
          catch(Exception e)
          {
            iNaoByte = -1;
          }
        }
      }
      iNaoBytes.put(0, iNaoByte);          
      return sNaoByte;
    }
    
    public String askForPlayerNumber()
    {
      String sPlayerNo = null;
      int iPlayerNo = 0;
      while(iPlayerNo >= iNaoBytes.size() || iPlayerNo < 1)
      {
        sPlayerNo = JOptionPane.showInputDialog("Enter Player Number (1 - " + (iNaoBytes.size() - 1) + ")", "");
        if(sPlayerNo == null)
        {
          break;
        }
        else
        {
          iPlayerNo = Integer.parseInt(sPlayerNo);
        }
      }
      return sPlayerNo;
    }
    
    public ArrayList<String> getAdressList()
    {
      return this.addresses;
    }
  }

  private void remoteReloadKernelVideoModule()
  {
    clearLog();
    NaoScpConfig cfg = new NaoScpConfig(config);
    robotConfigPreparator preparator = new robotConfigPreparator(cfg);
    String address = preparator.getDefaultAddress();
    cfg.addresses = preparator.getAdressList();

    //show only Script done Message @ the end
    showCopyDoneMsg = false;
    showScriptDoneMsg = true;
    showDoneMsg = false;
    
    //nothing to copy so its done @ begin
    copyDone.put(0, true);

    if(address == null)
    {
      actionInfo("reloading kernel video module aborted");
      setFormEnabled(true);
      return;
    }
    remoteScriptRunner scriptRunner = new remoteScriptRunner(cfg, "0", "0", "reloadKernelVideoModule", false);
    scriptRunner.execute();
  }
  
  private void updateConfig()
  {
    config.sshUser = this.sshUser.getText();
    config.sshPassword = this.sshPassword.getText();
    config.sshRootPassword = this.sshRootPassword.getText();
    config.progressBar = this.progressBar;
    config.comment = jCommentTextArea.getText();
    config.scheme = jSchemeBox.getSelectedItem().toString();
  }
  
  private void initializeRobot()
  {
    updateConfig();
    
    config.comment = "Initialized Robot on " + new Date().toString();
    
    clearLog();
    NaoScpConfig cfg = new NaoScpConfig(config);
    cfg.stagingLibDir = null;
    File stdCtcDir = new File("/opt/aldebaran/info/crosscompile/staging");

    if(stdCtcDir.isDirectory())
    {
      stdCtcDir = new File("/opt/aldebaran/info/crosscompile/staging");
    }
    else
    {
      stdCtcDir = new File("./../../");
    }      

    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(stdCtcDir);
    chooser.setDialogTitle("Select toolchain \"extern\" Directory");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    int ret = chooser.showOpenDialog(this);

    while(ret != JFileChooser.CANCEL_OPTION && cfg.stagingLibDir == null)
    {
      if(ret == JFileChooser.APPROVE_OPTION)
      {
        cfg.stagingLibDir = chooser.getSelectedFile().getPath();
      }

      if(cfg.stagingLibDir != null)
      {
        cfg.stagingLibDir += "/lib";
        File gioFile = new File(cfg.stagingLibDir + "/libgio-2.0.so");
        File glibDir = new File(cfg.stagingLibDir + "/glib-2.0");
        if(!gioFile.isFile() || !glibDir.isDirectory())
        {
          cfg.stagingLibDir = null;
          chooser.setDialogTitle("toolchain \"extern\" Directory seems to be wrong. Try again");
          ret = chooser.showOpenDialog(this);
        }
      }
    }     
    if(cfg.stagingLibDir == null)
    {
      actionInfo("no valid toolchain \"extern\" Directory selected");
      setFormEnabled(true);
      return;
    }
    
    robotConfigPreparator preparator = new robotConfigPreparator(cfg);
    cfg.copySysLibs = true;
    String address = preparator.getDefaultAddress();
    cfg.addresses = preparator.getAdressList();
    String sNaoByte = null;
    setupPlayerNo = null;
    if(address != null )
    {
      sNaoByte = preparator.askForNaoNumber();
      if(sNaoByte != null )
      {
        setupPlayerNo = preparator.askForPlayerNumber();
      }
    }    
    iNaoBytes = iNaoBytes;
    if(address == null || sNaoByte == null || setupPlayerNo == null || !prepareDeploy(cfg, true) || !prepareSetupDeploy(cfg, sNaoByte))
    {
      actionInfo("robot initialization aborted");
      setFormEnabled(true);
      return;
    }
    remoteSetupCopier setupCopier = new remoteSetupCopier(cfg, sNaoByte, "full");
    setupCopier.execute();
  }

  
  private void setRobotNetwork()
  {
    clearLog();
    NaoScpConfig cfg = new NaoScpConfig(config);
    robotConfigPreparator preparator = new robotConfigPreparator(cfg);
    cfg.copySysLibs = false;
    final String address = preparator.getDefaultAddress();
    cfg.addresses = preparator.getAdressList();
      
    String sNaoByte = null;
    if(address != null )
    {
      sNaoByte = preparator.askForNaoNumber();
    }
    if(address == null || sNaoByte == null || !prepareSetupDeploy(cfg, sNaoByte))
    {
      actionInfo("robot network configuration aborted");
      setFormEnabled(true);
      return;
    }
    remoteSetupCopier setupCopier = new remoteSetupCopier(cfg, sNaoByte, "network");
    setupCopier.execute();
  }
    
  private void setActionBtnLabel()
  {
    boolean copyActionSelected = cbCopyConfig.isSelected()
                                  || cbCopyExe.isSelected()
                                  || cbCopyLib.isSelected()
                                  || cbCopyLogs.isSelected();
    boolean scriptActionSelected = cbRestartNaoth.isSelected()
                                  || cbRestartNaoqi.isSelected();

    copyButton.setEnabled(checkDirPath(false));

    if(copyActionSelected && scriptActionSelected)
    {
      copyButton.setText("Copy files and run script");
    }
    else if(copyActionSelected)
    {
      copyButton.setText("Copy files");
    }
    else if(scriptActionSelected)
    {
      if(cbForceBackup.isSelected())
      {
        copyButton.setText("Full backup and run Script");
      }
      else if(cbNoBackup.isSelected())
      {
        copyButton.setText("Minimal backup and run Script");
      }
      else
      {
        copyButton.setText("Run Script");
      }
    }
    else if(cbForceBackup.isSelected())
    {
      copyButton.setText("Full backup");
    }
    else if(cbNoBackup.isSelected())
    {
      copyButton.setText("Minimal backup");
    }
    else
    {
      copyButton.setText("Nothing to do");
      copyButton.setEnabled(false);
    }
  }


  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    jDialog1 = new javax.swing.JDialog();
    wlanBtnGroup = new javax.swing.ButtonGroup();
    fileChooserStick = new javax.swing.JFileChooser();
    jLabel12 = new javax.swing.JLabel();
    jSplitPane1 = new javax.swing.JSplitPane();
    mainTab = new javax.swing.JTabbedPane();
    panelCopy = new javax.swing.JPanel();
    actionsTab = new javax.swing.JTabbedPane();
    scpPanel = new javax.swing.JPanel();
    jLabel6 = new javax.swing.JLabel();
    naoByte1 = new javax.swing.JTextField();
    jLabel7 = new javax.swing.JLabel();
    naoByte2 = new javax.swing.JTextField();
    naoByte3 = new javax.swing.JTextField();
    jLabel8 = new javax.swing.JLabel();
    copyButton = new javax.swing.JButton();
    jLabel15 = new javax.swing.JLabel();
    jBackupBox = new javax.swing.JComboBox();
    naoByte4 = new javax.swing.JTextField();
    jLabel17 = new javax.swing.JLabel();
    stickPanel = new javax.swing.JPanel();
    jLabel10 = new javax.swing.JLabel();
    btWriteToStick = new javax.swing.JButton();
    playerNumbersPanel = new javax.swing.JPanel();
    jSettingsPanel = new javax.swing.JPanel();
    jLabel3 = new javax.swing.JLabel();
    jColorBox = new javax.swing.JComboBox();
    jLabel2 = new javax.swing.JLabel();
    jTeamNumber = new javax.swing.JTextField();
    jLabel4 = new javax.swing.JLabel();
    jSchemeBox = new javax.swing.JComboBox();
    jButtonRefreshData = new javax.swing.JButton();
    jDirPathLabel = new javax.swing.JLabel();
    jDirChooser = new javax.swing.JButton();
    cbCopyLib = new javax.swing.JCheckBox();
    cbRestartNaoth = new javax.swing.JCheckBox();
    cbCopyConfig = new javax.swing.JCheckBox();
    cbNoBackup = new javax.swing.JCheckBox();
    jScrollPane2 = new javax.swing.JScrollPane();
    jCommentTextArea = new javax.swing.JTextArea();
    cbCopyLogs = new javax.swing.JCheckBox();
    jLabel16 = new javax.swing.JLabel();
    cbCopyExe = new javax.swing.JCheckBox();
    cbRestartNaoqi = new javax.swing.JCheckBox();
    cbForceBackup = new javax.swing.JCheckBox();
    panelConfig = new javax.swing.JPanel();
    jSettingsPanel1 = new javax.swing.JPanel();
    jLabel13 = new javax.swing.JLabel();
    jLabel1 = new javax.swing.JLabel();
    jLabel20 = new javax.swing.JLabel();
    jLabel22 = new javax.swing.JLabel();
    subnetFieldLAN = new javax.swing.JTextField();
    netmaskFieldLAN = new javax.swing.JTextField();
    broadcastFieldLAN = new javax.swing.JTextField();
    jLabel14 = new javax.swing.JLabel();
    jLabel19 = new javax.swing.JLabel();
    jLabel21 = new javax.swing.JLabel();
    jLabel23 = new javax.swing.JLabel();
    subnetFieldWLAN = new javax.swing.JTextField();
    netmaskFieldWLAN = new javax.swing.JTextField();
    broadcastFieldWLAN = new javax.swing.JTextField();
    radioWPA = new javax.swing.JRadioButton();
    radioWEP = new javax.swing.JRadioButton();
    jLabel5 = new javax.swing.JLabel();
    wlanSSID = new javax.swing.JTextField();
    jLabel9 = new javax.swing.JLabel();
    wlanKey = new javax.swing.JPasswordField();
    jSettingsPanel2 = new javax.swing.JPanel();
    jLabel24 = new javax.swing.JLabel();
    jTeamCommPort = new javax.swing.JTextField();
    jLabel27 = new javax.swing.JLabel();
    sshUser = new javax.swing.JTextField();
    jLabel28 = new javax.swing.JLabel();
    sshPassword = new javax.swing.JPasswordField();
    jLabel29 = new javax.swing.JLabel();
    sshRootUser = new javax.swing.JTextField();
    jLabel30 = new javax.swing.JLabel();
    sshRootPassword = new javax.swing.JPasswordField();
    jButtonSetRobotNetwork = new javax.swing.JButton();
    jButtonInitRobotSystem = new javax.swing.JButton();
    jButtonSaveNetworkConfig = new javax.swing.JButton();
    jScrollPane3 = new javax.swing.JScrollPane();
    lstNaos = new javax.swing.JList();
    cbRebootSystem = new javax.swing.JCheckBox();
    jButtonRemoteKernelVideoReload = new javax.swing.JButton();
    jPanel3 = new javax.swing.JPanel();
    jScrollPane5 = new javax.swing.JScrollPane();
    logTextPane = new javax.swing.JTextPane();
    jCopyStatus = new javax.swing.JLabel();
    progressBar = new javax.swing.JProgressBar();

    javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
    jDialog1.getContentPane().setLayout(jDialog1Layout);
    jDialog1Layout.setHorizontalGroup(
      jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 400, Short.MAX_VALUE)
    );
    jDialog1Layout.setVerticalGroup(
      jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 300, Short.MAX_VALUE)
    );

    fileChooserStick.setAcceptAllFileFilterUsed(false);
    fileChooserStick.setDialogTitle("Select the USB stick path");
    fileChooserStick.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("NaoSCP");
    addWindowListener(new java.awt.event.WindowAdapter()
    {
      public void windowClosing(java.awt.event.WindowEvent evt)
      {
        formWindowClosing(evt);
      }
    });

    jLabel12.setFont(new java.awt.Font("Lucida Grande", 0, 8)); // NOI18N
    jLabel12.setText("v0.5");

    jLabel6.setText("Nao 1:");

    naoByte1.setText("-1");
    naoByte1.setMaximumSize(new java.awt.Dimension(10, 31));
    naoByte1.setName("naoByte1"); // NOI18N
    naoByte1.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        naoByte1ActionPerformed(evt);
      }
    });

    jLabel7.setText("Nao 2:");

    naoByte2.setText("-1");
    naoByte2.setMaximumSize(new java.awt.Dimension(10, 31));
    naoByte2.setName("naoByte2"); // NOI18N
    naoByte2.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        naoByte2ActionPerformed(evt);
      }
    });

    naoByte3.setText("-1");
    naoByte3.setMaximumSize(new java.awt.Dimension(10, 31));
    naoByte3.setName("naoByte3"); // NOI18N
    naoByte3.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        naoByte3ActionPerformed(evt);
      }
    });

    jLabel8.setText("Nao 3:");

    copyButton.setEnabled(false);
    copyButton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        copyButtonActionPerformed(evt);
      }
    });

    jLabel15.setText("Version to Upload:");

    jBackupBox.setEnabled(false);
    jBackupBox.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        jBackupBoxItemStateChanged(evt);
      }
    });

    naoByte4.setText("-1");
    naoByte4.setMaximumSize(new java.awt.Dimension(10, 31));
    naoByte4.setName("naoByte4"); // NOI18N
    naoByte4.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        naoByte4ActionPerformed(evt);
      }
    });

    jLabel17.setText("Nao 4:");

    javax.swing.GroupLayout scpPanelLayout = new javax.swing.GroupLayout(scpPanel);
    scpPanel.setLayout(scpPanelLayout);
    scpPanelLayout.setHorizontalGroup(
      scpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(scpPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(scpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(scpPanelLayout.createSequentialGroup()
            .addComponent(jLabel15)
            .addGap(18, 18, 18)
            .addComponent(jBackupBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(scpPanelLayout.createSequentialGroup()
            .addComponent(jLabel6)
            .addGap(18, 18, 18)
            .addComponent(naoByte1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(6, 6, 6)
            .addComponent(jLabel7)
            .addGap(18, 18, 18)
            .addComponent(naoByte2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(6, 6, 6)
            .addComponent(jLabel8)
            .addGap(18, 18, 18)
            .addComponent(naoByte3, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(jLabel17)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(naoByte4, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(18, 18, 18)
            .addComponent(copyButton, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)))
        .addContainerGap())
    );
    scpPanelLayout.setVerticalGroup(
      scpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(scpPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(scpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel7)
          .addComponent(jLabel8)
          .addComponent(jLabel6)
          .addComponent(naoByte2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(naoByte1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(naoByte3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(naoByte4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(copyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel17))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(scpPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel15)
          .addComponent(jBackupBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(90, Short.MAX_VALUE))
    );

    actionsTab.addTab("SCP", scpPanel);

    jLabel10.setText("Player Numbers:");

    btWriteToStick.setText("Write to Stick");
    btWriteToStick.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        btWriteToStickActionPerformed(evt);
      }
    });

    playerNumbersPanel.setMinimumSize(new java.awt.Dimension(0, 23));
    playerNumbersPanel.setLayout(new javax.swing.BoxLayout(playerNumbersPanel, javax.swing.BoxLayout.X_AXIS));

    javax.swing.GroupLayout stickPanelLayout = new javax.swing.GroupLayout(stickPanel);
    stickPanel.setLayout(stickPanelLayout);
    stickPanelLayout.setHorizontalGroup(
      stickPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, stickPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(stickPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(playerNumbersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(btWriteToStick, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 556, Short.MAX_VALUE)
          .addGroup(javax.swing.GroupLayout.Alignment.LEADING, stickPanelLayout.createSequentialGroup()
            .addComponent(jLabel10)
            .addGap(0, 0, Short.MAX_VALUE)))
        .addContainerGap())
    );
    stickPanelLayout.setVerticalGroup(
      stickPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(stickPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jLabel10)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(playerNumbersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btWriteToStick)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    actionsTab.addTab("Stick", stickPanel);

    jSettingsPanel.setBackground(new java.awt.Color(204, 204, 255));

    jLabel3.setText("Color:");

    jColorBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "red", "blue" }));
    jColorBox.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jColorBoxActionPerformed(evt);
      }
    });

    jLabel2.setText("TeamNr:");

    jTeamNumber.setText("2");

    jLabel4.setText("Scheme:");

    jSchemeBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "n/a" }));
    jSchemeBox.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        jSchemeBoxItemStateChanged(evt);
      }
    });

    jButtonRefreshData.setText("Refresh");
    jButtonRefreshData.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButtonRefreshDataActionPerformed(evt);
      }
    });

    jDirPathLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
    jDirPathLabel.setText("Set directory to {RepDir}/NaoTHSoccer");
    jDirPathLabel.setToolTipText("NaoController project directory (e.g., \"D:\\u005cu005cNaoTH-2009\\u005cu005cProjects\\u005cu005cNaoController\")");

    jDirChooser.setText("...");
    jDirChooser.setActionCommand("jDirChoose");
    jDirChooser.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jDirChooserPerformed(evt);
      }
    });

    javax.swing.GroupLayout jSettingsPanelLayout = new javax.swing.GroupLayout(jSettingsPanel);
    jSettingsPanel.setLayout(jSettingsPanelLayout);
    jSettingsPanelLayout.setHorizontalGroup(
      jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jSettingsPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jSettingsPanelLayout.createSequentialGroup()
            .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel4)
              .addComponent(jLabel3))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jSettingsPanelLayout.createSequentialGroup()
                .addComponent(jColorBox, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTeamNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButtonRefreshData, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE))
              .addComponent(jSchemeBox, 0, 423, Short.MAX_VALUE)))
          .addGroup(jSettingsPanelLayout.createSequentialGroup()
            .addComponent(jDirChooser)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jDirPathLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)))
        .addContainerGap())
    );
    jSettingsPanelLayout.setVerticalGroup(
      jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jSettingsPanelLayout.createSequentialGroup()
        .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jDirChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jDirPathLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jSchemeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jColorBox)
          .addComponent(jTeamNumber)
          .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jButtonRefreshData)
          .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)))
    );

    cbCopyLib.setText("copyLibNaoSMAL");
    cbCopyLib.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        cbCopyLibItemStateChanged(evt);
      }
    });

    cbRestartNaoth.setSelected(true);
    cbRestartNaoth.setText("restartNaoTH");
    cbRestartNaoth.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        cbRestartNaothItemStateChanged(evt);
      }
    });
    cbRestartNaoth.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbRestartNaothActionPerformed(evt);
      }
    });

    cbCopyConfig.setText("copyConfig");
    cbCopyConfig.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        cbCopyConfigItemStateChanged(evt);
      }
    });

    cbNoBackup.setText("ONLY MINIMAL BACKUP");
    cbNoBackup.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        cbNoBackupItemStateChanged(evt);
      }
    });
    cbNoBackup.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbNoBackupActionPerformed(evt);
      }
    });

    jCommentTextArea.setColumns(15);
    jCommentTextArea.setRows(4);
    jScrollPane2.setViewportView(jCommentTextArea);

    cbCopyLogs.setText("copyLogs");
    cbCopyLogs.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        cbCopyLogsItemStateChanged(evt);
      }
    });
    cbCopyLogs.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbCopyLogsActionPerformed(evt);
      }
    });

    jLabel16.setText("Comment:");

    cbCopyExe.setSelected(true);
    cbCopyExe.setText("copyNaoTH(exe)");
    cbCopyExe.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        cbCopyExeItemStateChanged(evt);
      }
    });

    cbRestartNaoqi.setText("restartNaoqi");
    cbRestartNaoqi.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        cbRestartNaoqiItemStateChanged(evt);
      }
    });
    cbRestartNaoqi.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbRestartNaoqiActionPerformed(evt);
      }
    });

    cbForceBackup.setText("force full Backup");
    cbForceBackup.addItemListener(new java.awt.event.ItemListener()
    {
      public void itemStateChanged(java.awt.event.ItemEvent evt)
      {
        cbForceBackupItemStateChanged(evt);
      }
    });
    cbForceBackup.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cbForceBackupActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout panelCopyLayout = new javax.swing.GroupLayout(panelCopy);
    panelCopy.setLayout(panelCopyLayout);
    panelCopyLayout.setHorizontalGroup(
      panelCopyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(panelCopyLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(panelCopyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(panelCopyLayout.createSequentialGroup()
            .addGroup(panelCopyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(cbCopyExe)
              .addComponent(cbCopyLib))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(panelCopyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(cbCopyConfig)
              .addComponent(cbCopyLogs))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(panelCopyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(panelCopyLayout.createSequentialGroup()
                .addComponent(cbRestartNaoth)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbForceBackup))
              .addGroup(panelCopyLayout.createSequentialGroup()
                .addComponent(cbRestartNaoqi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbNoBackup)))))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(panelCopyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(panelCopyLayout.createSequentialGroup()
            .addComponent(jLabel16)
            .addGap(0, 0, Short.MAX_VALUE))
          .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        .addContainerGap())
      .addComponent(actionsTab)
    );
    panelCopyLayout.setVerticalGroup(
      panelCopyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(panelCopyLayout.createSequentialGroup()
        .addComponent(actionsTab, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(20, 20, 20)
        .addGroup(panelCopyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(panelCopyLayout.createSequentialGroup()
            .addComponent(jLabel16)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane2))
          .addComponent(jSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
        .addGroup(panelCopyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(panelCopyLayout.createSequentialGroup()
            .addGroup(panelCopyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(cbCopyConfig)
              .addComponent(cbRestartNaoth)
              .addComponent(cbForceBackup, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(8, 8, 8)
            .addGroup(panelCopyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(cbCopyLogs)
              .addComponent(cbRestartNaoqi)
              .addComponent(cbNoBackup, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
          .addGroup(panelCopyLayout.createSequentialGroup()
            .addComponent(cbCopyExe)
            .addGap(8, 8, 8)
            .addComponent(cbCopyLib)))
        .addContainerGap())
    );

    mainTab.addTab("Copy & Run", panelCopy);

    panelConfig.setPreferredSize(new java.awt.Dimension(456, 462));
    panelConfig.setVerifyInputWhenFocusTarget(false);

    jSettingsPanel1.setBackground(new java.awt.Color(204, 204, 255));

    jLabel13.setText("LAN:");

    jLabel1.setText("SubNet");

    jLabel20.setText("Netmask");

    jLabel22.setText("Broadcast");

    subnetFieldLAN.setText("10.0.0");
    subnetFieldLAN.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        subnetFieldLANActionPerformed(evt);
      }
    });

    netmaskFieldLAN.setText("255.255.255.0");
    netmaskFieldLAN.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        netmaskFieldLANActionPerformed(evt);
      }
    });

    broadcastFieldLAN.setText("10.0.0.255");
    broadcastFieldLAN.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        broadcastFieldLANActionPerformed(evt);
      }
    });
    broadcastFieldLAN.addInputMethodListener(new java.awt.event.InputMethodListener()
    {
      public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt)
      {
        broadcastFieldLANInputMethodTextChanged(evt);
      }
      public void caretPositionChanged(java.awt.event.InputMethodEvent evt)
      {
      }
    });
    broadcastFieldLAN.addKeyListener(new java.awt.event.KeyAdapter()
    {
      public void keyPressed(java.awt.event.KeyEvent evt)
      {
        broadcastFieldLANKeyPressed(evt);
      }
    });

    jLabel14.setText("WLAN:");

    jLabel19.setText("SubNet");

    jLabel21.setText("Netmask");

    jLabel23.setText("Broadcast");

    subnetFieldWLAN.setText("192.168.4");
    subnetFieldWLAN.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        subnetFieldWLANActionPerformed(evt);
      }
    });

    netmaskFieldWLAN.setText("255.255.255.0");
    netmaskFieldWLAN.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        netmaskFieldWLANActionPerformed(evt);
      }
    });

    broadcastFieldWLAN.setText("192.168.4.255");
    broadcastFieldWLAN.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        broadcastFieldWLANActionPerformed(evt);
      }
    });
    broadcastFieldWLAN.addInputMethodListener(new java.awt.event.InputMethodListener()
    {
      public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt)
      {
        broadcastFieldWLANInputMethodTextChanged(evt);
      }
      public void caretPositionChanged(java.awt.event.InputMethodEvent evt)
      {
        broadcastFieldWLANCaretPositionChanged(evt);
      }
    });
    broadcastFieldWLAN.addPropertyChangeListener(new java.beans.PropertyChangeListener()
    {
      public void propertyChange(java.beans.PropertyChangeEvent evt)
      {
        broadcastFieldWLANPropertyChange(evt);
      }
    });
    broadcastFieldWLAN.addKeyListener(new java.awt.event.KeyAdapter()
    {
      public void keyTyped(java.awt.event.KeyEvent evt)
      {
        broadcastFieldWLANKeyTyped(evt);
      }
      public void keyPressed(java.awt.event.KeyEvent evt)
      {
        broadcastFieldWLANKeyPressed(evt);
      }
    });
    broadcastFieldWLAN.addVetoableChangeListener(new java.beans.VetoableChangeListener()
    {
      public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException
      {
        broadcastFieldWLANVetoableChange(evt);
      }
    });

    radioWPA.setBackground(new java.awt.Color(204, 204, 255));
    radioWPA.setSelected(true);
    radioWPA.setText("WPA PSK");

    radioWEP.setBackground(new java.awt.Color(204, 204, 255));
    radioWEP.setText("WEP");

    jLabel5.setText("SSID");

    wlanSSID.setText("SPL_Field_B");
    wlanSSID.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        wlanSSIDActionPerformed(evt);
      }
    });

    jLabel9.setText("WLAN Key");

    wlanKey.setText("SPLRC2012");
    wlanKey.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        wlanKeyActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout jSettingsPanel1Layout = new javax.swing.GroupLayout(jSettingsPanel1);
    jSettingsPanel1.setLayout(jSettingsPanel1Layout);
    jSettingsPanel1Layout.setHorizontalGroup(
      jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jSettingsPanel1Layout.createSequentialGroup()
        .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSettingsPanel1Layout.createSequentialGroup()
            .addGap(12, 12, 12)
            .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(jSettingsPanel1Layout.createSequentialGroup()
                .addComponent(jLabel13)
                .addGap(217, 217, 217))
              .addGroup(jSettingsPanel1Layout.createSequentialGroup()
                .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(jLabel1)
                  .addComponent(jLabel22))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                  .addComponent(subnetFieldLAN, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                  .addComponent(netmaskFieldLAN, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                  .addComponent(broadcastFieldLAN, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(jLabel14)
                  .addGroup(jSettingsPanel1Layout.createSequentialGroup()
                    .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                      .addGroup(jSettingsPanel1Layout.createSequentialGroup()
                        .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                          .addComponent(jLabel21)
                          .addComponent(jLabel19))
                        .addGap(28, 28, 28))
                      .addGroup(jSettingsPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel23)
                        .addGap(18, 18, 18)))
                    .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                      .addComponent(broadcastFieldWLAN, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                      .addComponent(subnetFieldWLAN)
                      .addComponent(netmaskFieldWLAN, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                      .addComponent(radioWPA, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
          .addGroup(jSettingsPanel1Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSettingsPanel1Layout.createSequentialGroup()
                .addGap(84, 84, 84)
                .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(wlanKey, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                  .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jSettingsPanel1Layout.createSequentialGroup()
                    .addComponent(wlanSSID, javax.swing.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(radioWEP)
                    .addGap(168, 168, 168))))
              .addComponent(jLabel5)
              .addComponent(jLabel9))))
        .addGap(13, 13, 13))
    );
    jSettingsPanel1Layout.setVerticalGroup(
      jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jSettingsPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(jSettingsPanel1Layout.createSequentialGroup()
            .addComponent(jLabel14)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel19)
              .addComponent(subnetFieldWLAN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel21)
              .addComponent(netmaskFieldWLAN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(broadcastFieldWLAN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(jSettingsPanel1Layout.createSequentialGroup()
            .addComponent(jLabel13)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(subnetFieldLAN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jLabel1))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel20)
              .addComponent(netmaskFieldLAN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel22)
              .addComponent(broadcastFieldLAN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(jLabel23))))
        .addGap(18, 18, 18)
        .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel5)
          .addComponent(wlanSSID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(radioWEP)
          .addComponent(radioWPA))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jSettingsPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel9)
          .addComponent(wlanKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jSettingsPanel2.setBackground(new java.awt.Color(204, 204, 255));

    jLabel24.setText("TeamComm:");

    jTeamCommPort.setText("10400");

    jLabel27.setText("ssh:");

    sshUser.setText("nao");
    sshUser.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        sshUserActionPerformed(evt);
      }
    });

    jLabel28.setText(":");

    sshPassword.setText("nao");

    jLabel29.setText("ssh:");

    sshRootUser.setEditable(false);
    sshRootUser.setText("root");
    sshRootUser.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        sshRootUserActionPerformed(evt);
      }
    });

    jLabel30.setText(":");

    sshRootPassword.setText("root");

    javax.swing.GroupLayout jSettingsPanel2Layout = new javax.swing.GroupLayout(jSettingsPanel2);
    jSettingsPanel2.setLayout(jSettingsPanel2Layout);
    jSettingsPanel2Layout.setHorizontalGroup(
      jSettingsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jSettingsPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jSettingsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jSettingsPanel2Layout.createSequentialGroup()
            .addComponent(jLabel24)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jTeamCommPort))
          .addGroup(jSettingsPanel2Layout.createSequentialGroup()
            .addGroup(jSettingsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
              .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jSettingsPanel2Layout.createSequentialGroup()
                .addComponent(jLabel29)
                .addGap(18, 18, 18)
                .addComponent(sshRootUser, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
              .addGroup(jSettingsPanel2Layout.createSequentialGroup()
                .addComponent(jLabel27)
                .addGap(18, 18, 18)
                .addComponent(sshUser, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel28)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jSettingsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(sshPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
              .addComponent(sshRootPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE))))
        .addContainerGap())
    );
    jSettingsPanel2Layout.setVerticalGroup(
      jSettingsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jSettingsPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jSettingsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jTeamCommPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(jSettingsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel27)
          .addComponent(sshUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(sshPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(jSettingsPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(sshRootUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel29)
          .addComponent(sshRootPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    jButtonSetRobotNetwork.setText("Set Network to Robot");
    jButtonSetRobotNetwork.setEnabled(false);
    jButtonSetRobotNetwork.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButtonSetRobotNetworkActionPerformed(evt);
      }
    });

    jButtonInitRobotSystem.setText("Initialize Robot System");
    jButtonInitRobotSystem.setEnabled(false);
    jButtonInitRobotSystem.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButtonInitRobotSystemActionPerformed(evt);
      }
    });

    jButtonSaveNetworkConfig.setText("Save As Default Config");
    jButtonSaveNetworkConfig.setEnabled(false);
    jButtonSaveNetworkConfig.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButtonSaveNetworkConfigActionPerformed(evt);
      }
    });

    lstNaos.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    lstNaos.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mouseClicked(java.awt.event.MouseEvent evt)
      {
        lstNaosMouseClicked(evt);
      }
    });
    jScrollPane3.setViewportView(lstNaos);

    cbRebootSystem.setText("reboot OS");

    jButtonRemoteKernelVideoReload.setText("Reload kernel video module");
    jButtonRemoteKernelVideoReload.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        jButtonRemoteKernelVideoReloadActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout panelConfigLayout = new javax.swing.GroupLayout(panelConfig);
    panelConfig.setLayout(panelConfigLayout);
    panelConfigLayout.setHorizontalGroup(
      panelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(panelConfigLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(panelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jSettingsPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jSettingsPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(panelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(cbRebootSystem, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jButtonSetRobotNetwork, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jButtonInitRobotSystem, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 196, Short.MAX_VALUE)
          .addComponent(jButtonSaveNetworkConfig, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
          .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
          .addComponent(jButtonRemoteKernelVideoReload, javax.swing.GroupLayout.PREFERRED_SIZE, 196, Short.MAX_VALUE))
        .addContainerGap())
    );
    panelConfigLayout.setVerticalGroup(
      panelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelConfigLayout.createSequentialGroup()
        .addGroup(panelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addGroup(panelConfigLayout.createSequentialGroup()
            .addContainerGap()
            .addComponent(cbRebootSystem)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane3))
          .addGroup(panelConfigLayout.createSequentialGroup()
            .addGap(14, 14, 14)
            .addComponent(jSettingsPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(panelConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jSettingsPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelConfigLayout.createSequentialGroup()
            .addComponent(jButtonRemoteKernelVideoReload)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jButtonSaveNetworkConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButtonSetRobotNetwork, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jButtonInitRobotSystem, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap())
    );

    mainTab.addTab("Network Configuration / Utilities", panelConfig);

    jSplitPane1.setLeftComponent(mainTab);

    logTextPane.setEditable(false);
    jScrollPane5.setViewportView(logTextPane);

    jCopyStatus.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
    jCopyStatus.setText("idle...");

    progressBar.setEnabled(false);

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jCopyStatus, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
      .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
      .addComponent(jScrollPane5)
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jCopyStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    jSplitPane1.setRightComponent(jPanel3);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addComponent(jLabel12)
            .addGap(0, 0, Short.MAX_VALUE))
          .addComponent(jSplitPane1))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jLabel12)
        .addGap(2, 2, 2)
        .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents
       
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
//      for(int i = 0; i < jmdnsList.size() && i < jmdnsServiceListenerList.size(); i++)
//      {
//        JmDNS jmdns = jmdnsList.get(i);
//        ServiceListener svcListener = jmdnsServiceListenerList.get(i);
//        try
//        {
//          jmdns.removeServiceListener("_nao._tcp.local.", svcListener);
//          jmdns.close();
//        }
//        catch(Exception e){}
//      }
    }//GEN-LAST:event_formWindowClosing

  private void jButtonRemoteKernelVideoReloadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonRemoteKernelVideoReloadActionPerformed
  {//GEN-HEADEREND:event_jButtonRemoteKernelVideoReloadActionPerformed
    remoteReloadKernelVideoModule();
  }//GEN-LAST:event_jButtonRemoteKernelVideoReloadActionPerformed

  private void lstNaosMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_lstNaosMouseClicked
  {//GEN-HEADEREND:event_lstNaosMouseClicked

    if (evt.getClickCount() == 2 && checkDirPath(true))
    {
      Object[] options={ "initialize Robot", "set network config" };
      int pressedBtnId = JOptionPane.showOptionDialog
      (
        null, "Choose or loose!",
        "Demand", JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null, options, options[0]
      );
      if(pressedBtnId == 0)
      {
        initializeRobot();
      }
      else if(pressedBtnId == 1)
      {
        setRobotNetwork();
      }
    }
  }//GEN-LAST:event_lstNaosMouseClicked

  private void jButtonSaveNetworkConfigActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSaveNetworkConfigActionPerformed
  {//GEN-HEADEREND:event_jButtonSaveNetworkConfigActionPerformed
    try
    {
      actionInfo("saving network configuration");
      writeNetworkConfig();
      saveWpaSupplicant(config, config.localSetupScriptPath());
      DeployUtils.writePlayerCfg(this, 
        new File(config.localConfigPath() + "/general/player.cfg"), 
        setupPlayerNo, jTeamNumber.getText(), jColorBox.getSelectedItem().toString());
      DeployUtils.writeTeamcommCfg(this, jTeamCommPort.getText(), new File(config.localConfigPath() + "/general/teamcomm.cfg"));
      //todo: why writing scheme here?
      DeployUtils.writeScheme(this, jSchemeBox.getSelectedItem().toString(), new File(config.localConfigPath() + "/scheme.cfg"));
    }
    catch(IOException ex)
    {
      actionInfo("Could'nt write network config file\n"  + ex.getMessage());
    }
  }//GEN-LAST:event_jButtonSaveNetworkConfigActionPerformed

  private void jButtonInitRobotSystemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonInitRobotSystemActionPerformed
  {//GEN-HEADEREND:event_jButtonInitRobotSystemActionPerformed
    if(checkDirPath(true))
    {
      initializeRobot();
    }
  }//GEN-LAST:event_jButtonInitRobotSystemActionPerformed

  private void jButtonSetRobotNetworkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonSetRobotNetworkActionPerformed
  {//GEN-HEADEREND:event_jButtonSetRobotNetworkActionPerformed
    if(checkDirPath(true))
    {
      setRobotNetwork();
    }
  }//GEN-LAST:event_jButtonSetRobotNetworkActionPerformed

  private void wlanKeyActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_wlanKeyActionPerformed
  {//GEN-HEADEREND:event_wlanKeyActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_wlanKeyActionPerformed

  private void wlanSSIDActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_wlanSSIDActionPerformed
  {//GEN-HEADEREND:event_wlanSSIDActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_wlanSSIDActionPerformed

  private void broadcastFieldWLANVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException//GEN-FIRST:event_broadcastFieldWLANVetoableChange
  {//GEN-HEADEREND:event_broadcastFieldWLANVetoableChange

  }//GEN-LAST:event_broadcastFieldWLANVetoableChange

  private void broadcastFieldWLANKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_broadcastFieldWLANKeyPressed
  {//GEN-HEADEREND:event_broadcastFieldWLANKeyPressed
    
  }//GEN-LAST:event_broadcastFieldWLANKeyPressed

  private void broadcastFieldWLANKeyTyped(java.awt.event.KeyEvent evt)//GEN-FIRST:event_broadcastFieldWLANKeyTyped
  {//GEN-HEADEREND:event_broadcastFieldWLANKeyTyped

  }//GEN-LAST:event_broadcastFieldWLANKeyTyped

  private void broadcastFieldWLANPropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_broadcastFieldWLANPropertyChange
  {//GEN-HEADEREND:event_broadcastFieldWLANPropertyChange

  }//GEN-LAST:event_broadcastFieldWLANPropertyChange

  private void broadcastFieldWLANCaretPositionChanged(java.awt.event.InputMethodEvent evt)//GEN-FIRST:event_broadcastFieldWLANCaretPositionChanged
  {//GEN-HEADEREND:event_broadcastFieldWLANCaretPositionChanged

  }//GEN-LAST:event_broadcastFieldWLANCaretPositionChanged

  private void broadcastFieldWLANInputMethodTextChanged(java.awt.event.InputMethodEvent evt)//GEN-FIRST:event_broadcastFieldWLANInputMethodTextChanged
  {//GEN-HEADEREND:event_broadcastFieldWLANInputMethodTextChanged

  }//GEN-LAST:event_broadcastFieldWLANInputMethodTextChanged

  private void broadcastFieldWLANActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_broadcastFieldWLANActionPerformed
  {//GEN-HEADEREND:event_broadcastFieldWLANActionPerformed
   
  }//GEN-LAST:event_broadcastFieldWLANActionPerformed

  private void netmaskFieldWLANActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_netmaskFieldWLANActionPerformed
  {//GEN-HEADEREND:event_netmaskFieldWLANActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_netmaskFieldWLANActionPerformed

  private void subnetFieldWLANActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_subnetFieldWLANActionPerformed
  {//GEN-HEADEREND:event_subnetFieldWLANActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_subnetFieldWLANActionPerformed

  private void broadcastFieldLANKeyPressed(java.awt.event.KeyEvent evt)//GEN-FIRST:event_broadcastFieldLANKeyPressed
  {//GEN-HEADEREND:event_broadcastFieldLANKeyPressed
    
  }//GEN-LAST:event_broadcastFieldLANKeyPressed

  private void broadcastFieldLANInputMethodTextChanged(java.awt.event.InputMethodEvent evt)//GEN-FIRST:event_broadcastFieldLANInputMethodTextChanged
  {//GEN-HEADEREND:event_broadcastFieldLANInputMethodTextChanged

  }//GEN-LAST:event_broadcastFieldLANInputMethodTextChanged

  private void broadcastFieldLANActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_broadcastFieldLANActionPerformed
  {//GEN-HEADEREND:event_broadcastFieldLANActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_broadcastFieldLANActionPerformed

  private void netmaskFieldLANActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_netmaskFieldLANActionPerformed
  {//GEN-HEADEREND:event_netmaskFieldLANActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_netmaskFieldLANActionPerformed

  private void subnetFieldLANActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_subnetFieldLANActionPerformed
  {//GEN-HEADEREND:event_subnetFieldLANActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_subnetFieldLANActionPerformed

  private void cbForceBackupActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbForceBackupActionPerformed
  {//GEN-HEADEREND:event_cbForceBackupActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_cbForceBackupActionPerformed

  private void cbForceBackupItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbForceBackupItemStateChanged
  {//GEN-HEADEREND:event_cbForceBackupItemStateChanged
    setActionBtnLabel();
  }//GEN-LAST:event_cbForceBackupItemStateChanged

  private void cbRestartNaoqiActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbRestartNaoqiActionPerformed
  {//GEN-HEADEREND:event_cbRestartNaoqiActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_cbRestartNaoqiActionPerformed

  private void cbRestartNaoqiItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbRestartNaoqiItemStateChanged
  {//GEN-HEADEREND:event_cbRestartNaoqiItemStateChanged
    setActionBtnLabel();
  }//GEN-LAST:event_cbRestartNaoqiItemStateChanged

  private void cbCopyExeItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbCopyExeItemStateChanged
  {//GEN-HEADEREND:event_cbCopyExeItemStateChanged
    setActionBtnLabel();
  }//GEN-LAST:event_cbCopyExeItemStateChanged

  private void cbCopyLogsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbCopyLogsActionPerformed
  {//GEN-HEADEREND:event_cbCopyLogsActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_cbCopyLogsActionPerformed

  private void cbCopyLogsItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbCopyLogsItemStateChanged
  {//GEN-HEADEREND:event_cbCopyLogsItemStateChanged
    setActionBtnLabel();
  }//GEN-LAST:event_cbCopyLogsItemStateChanged

  private void cbNoBackupActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbNoBackupActionPerformed
  {//GEN-HEADEREND:event_cbNoBackupActionPerformed

  }//GEN-LAST:event_cbNoBackupActionPerformed

  private void cbNoBackupItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbNoBackupItemStateChanged
  {//GEN-HEADEREND:event_cbNoBackupItemStateChanged
    setActionBtnLabel();
  }//GEN-LAST:event_cbNoBackupItemStateChanged

  private void jButtonRefreshDataActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButtonRefreshDataActionPerformed
  {//GEN-HEADEREND:event_jButtonRefreshDataActionPerformed
    setFormData();
  }//GEN-LAST:event_jButtonRefreshDataActionPerformed

  private void jSchemeBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_jSchemeBoxItemStateChanged
  {//GEN-HEADEREND:event_jSchemeBoxItemStateChanged

  }//GEN-LAST:event_jSchemeBoxItemStateChanged

  private void jColorBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jColorBoxActionPerformed
  {//GEN-HEADEREND:event_jColorBoxActionPerformed

  }//GEN-LAST:event_jColorBoxActionPerformed

  private void cbCopyConfigItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbCopyConfigItemStateChanged
  {//GEN-HEADEREND:event_cbCopyConfigItemStateChanged
    setActionBtnLabel();
  }//GEN-LAST:event_cbCopyConfigItemStateChanged

  private void cbRestartNaothActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbRestartNaothActionPerformed
  {//GEN-HEADEREND:event_cbRestartNaothActionPerformed

  }//GEN-LAST:event_cbRestartNaothActionPerformed

  private void cbRestartNaothItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbRestartNaothItemStateChanged
  {//GEN-HEADEREND:event_cbRestartNaothItemStateChanged
    setActionBtnLabel();
  }//GEN-LAST:event_cbRestartNaothItemStateChanged

  private void cbCopyLibItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_cbCopyLibItemStateChanged
  {//GEN-HEADEREND:event_cbCopyLibItemStateChanged
    setActionBtnLabel();
  }//GEN-LAST:event_cbCopyLibItemStateChanged

  /**
   * selects the NaoController directory, populates schemes Dir
   * @param evt
   */
  private void jDirChooserPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jDirChooserPerformed
  {//GEN-HEADEREND:event_jDirChooserPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setCurrentDirectory(new java.io.File("."));
    chooser.setDialogTitle("Select NaoController Directory");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setAcceptAllFileFilterUsed(false);
    if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
    {
      setDirectory(String.valueOf(chooser.getSelectedFile()));
    }
  }//GEN-LAST:event_jDirChooserPerformed

  private void naoByte4ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_naoByte4ActionPerformed
  {//GEN-HEADEREND:event_naoByte4ActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_naoByte4ActionPerformed

  private void jBackupBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_jBackupBoxItemStateChanged
  {//GEN-HEADEREND:event_jBackupBoxItemStateChanged

    String itemValue = evt.getItem().toString();

    if(jBackupBox.getItemCount() > 0 && jBackupBox.getSelectedItem().equals(itemValue) && !itemValue.equals(jBackupBox.getItemAt(0)))
    {
      config.backupIsSelected = true;
      config.boxSelected = jBackupBox.getSelectedItem().toString();
      config.selectedBackup = config.backups.get(jBackupBox.getSelectedItem()).toString();
    }
    else
    {
      config.boxSelected = "";
      config.selectedBackup = "";
      config.backupIsSelected = false;
    }
    setCommentText();
  }//GEN-LAST:event_jBackupBoxItemStateChanged

  private void copyButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_copyButtonActionPerformed
  {//GEN-HEADEREND:event_copyButtonActionPerformed
    if(checkDirPath(true))
    {
      copyFiles2Nao();
      resetBackupList();
    }
  }//GEN-LAST:event_copyButtonActionPerformed

  private void naoByte3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_naoByte3ActionPerformed
  {//GEN-HEADEREND:event_naoByte3ActionPerformed

  }//GEN-LAST:event_naoByte3ActionPerformed

  private void naoByte2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_naoByte2ActionPerformed
  {//GEN-HEADEREND:event_naoByte2ActionPerformed

  }//GEN-LAST:event_naoByte2ActionPerformed

  private void naoByte1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_naoByte1ActionPerformed
  {//GEN-HEADEREND:event_naoByte1ActionPerformed

  }//GEN-LAST:event_naoByte1ActionPerformed

    private void btWriteToStickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btWriteToStickActionPerformed

    if(checkDirPath(true) &&
       fileChooserStick.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
    {
        setFormEnabled(false);
        clearLog();
        new Thread(new Runnable() { public void run() 
        { 
            String usbStickPath = String.valueOf(fileChooserStick.getSelectedFile());

            NaoScpConfig cfg = createDeployConfig();
            actionInfo("Starting to copy files");
            if(DeployUtils.assembleDeployDir(NaoScp.this, cfg, usbStickPath + "/deploy"))
            {
              if(cfg.copyConfig) 
              {
                actionInfo("Configuring files on stick");
                DeployUtils.configureUSBDeployDir(NaoScp.this, cfg, bodyIdToPlayerNumber, usbStickPath + "/deploy");
              }

              File scriptFile = new File(cfg.localSetupStickPath() + "/startBrainwashing.sh");
              if(scriptFile.isFile())
              {          
                actionInfo("Copying brain wash script");
                DeployUtils.copyFiles(NaoScp.this, scriptFile, new File(usbStickPath + "/startBrainwashing.sh"));
              }
              else
              {
                actionError("No brainwashing script found (should be " + scriptFile.getAbsolutePath() + ")");
              }

              actionFinish("Finished");
              setFormEnabled(true);
            }//end if
        }//end run
      }).start();
        }
    }//GEN-LAST:event_btWriteToStickActionPerformed

  private void sshRootUserActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sshRootUserActionPerformed
  {//GEN-HEADEREND:event_sshRootUserActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_sshRootUserActionPerformed

  private void sshUserActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_sshUserActionPerformed
  {//GEN-HEADEREND:event_sshUserActionPerformed
    // TODO add your handling code here:
  }//GEN-LAST:event_sshUserActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTabbedPane actionsTab;
  private javax.swing.JTextField broadcastFieldLAN;
  private javax.swing.JTextField broadcastFieldWLAN;
  private javax.swing.JButton btWriteToStick;
  private javax.swing.JCheckBox cbCopyConfig;
  private javax.swing.JCheckBox cbCopyExe;
  private javax.swing.JCheckBox cbCopyLib;
  private javax.swing.JCheckBox cbCopyLogs;
  private javax.swing.JCheckBox cbForceBackup;
  private javax.swing.JCheckBox cbNoBackup;
  private javax.swing.JCheckBox cbRebootSystem;
  private javax.swing.JCheckBox cbRestartNaoqi;
  private javax.swing.JCheckBox cbRestartNaoth;
  private javax.swing.JButton copyButton;
  private javax.swing.JFileChooser fileChooserStick;
  private javax.swing.JComboBox jBackupBox;
  private javax.swing.JButton jButtonInitRobotSystem;
  private javax.swing.JButton jButtonRefreshData;
  private javax.swing.JButton jButtonRemoteKernelVideoReload;
  private javax.swing.JButton jButtonSaveNetworkConfig;
  private javax.swing.JButton jButtonSetRobotNetwork;
  private javax.swing.JComboBox jColorBox;
  private javax.swing.JTextArea jCommentTextArea;
  private javax.swing.JLabel jCopyStatus;
  private javax.swing.JDialog jDialog1;
  private javax.swing.JButton jDirChooser;
  private javax.swing.JLabel jDirPathLabel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel10;
  private javax.swing.JLabel jLabel12;
  private javax.swing.JLabel jLabel13;
  private javax.swing.JLabel jLabel14;
  private javax.swing.JLabel jLabel15;
  private javax.swing.JLabel jLabel16;
  private javax.swing.JLabel jLabel17;
  private javax.swing.JLabel jLabel19;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel20;
  private javax.swing.JLabel jLabel21;
  private javax.swing.JLabel jLabel22;
  private javax.swing.JLabel jLabel23;
  private javax.swing.JLabel jLabel24;
  private javax.swing.JLabel jLabel27;
  private javax.swing.JLabel jLabel28;
  private javax.swing.JLabel jLabel29;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel30;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JLabel jLabel9;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JComboBox jSchemeBox;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JScrollPane jScrollPane5;
  private javax.swing.JPanel jSettingsPanel;
  private javax.swing.JPanel jSettingsPanel1;
  private javax.swing.JPanel jSettingsPanel2;
  private javax.swing.JSplitPane jSplitPane1;
  private javax.swing.JTextField jTeamCommPort;
  private javax.swing.JTextField jTeamNumber;
  private javax.swing.JTextPane logTextPane;
  private javax.swing.JList lstNaos;
  private javax.swing.JTabbedPane mainTab;
  private javax.swing.JTextField naoByte1;
  private javax.swing.JTextField naoByte2;
  private javax.swing.JTextField naoByte3;
  private javax.swing.JTextField naoByte4;
  private javax.swing.JTextField netmaskFieldLAN;
  private javax.swing.JTextField netmaskFieldWLAN;
  private javax.swing.JPanel panelConfig;
  private javax.swing.JPanel panelCopy;
  private javax.swing.JPanel playerNumbersPanel;
  private javax.swing.JProgressBar progressBar;
  private javax.swing.JRadioButton radioWEP;
  private javax.swing.JRadioButton radioWPA;
  private javax.swing.JPanel scpPanel;
  private javax.swing.JPasswordField sshPassword;
  private javax.swing.JPasswordField sshRootPassword;
  private javax.swing.JTextField sshRootUser;
  private javax.swing.JTextField sshUser;
  private javax.swing.JPanel stickPanel;
  private javax.swing.JTextField subnetFieldLAN;
  private javax.swing.JTextField subnetFieldWLAN;
  private javax.swing.ButtonGroup wlanBtnGroup;
  private javax.swing.JPasswordField wlanKey;
  private javax.swing.JTextField wlanSSID;
  // End of variables declaration//GEN-END:variables
};


