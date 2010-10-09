/*
 * RobotControlGUI.java
 *
 * Created on 08.10.2010, 16:31:30
 */
package de.hu_berlin.informatik.ki.nao;

import de.hu_berlin.informatik.ki.nao.interfaces.MessageServerProvider;
import de.hu_berlin.informatik.ki.nao.server.IMessageServerParent;
import de.hu_berlin.informatik.ki.nao.server.MessageServer;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import org.flexdock.view.Viewport;

/**
 *
 * @author thomas
 */
@PluginImplementation
public class RobotControlGUI extends javax.swing.JFrame implements MessageServerProvider,
  IMessageServerParent, Plugin
{

  private Viewport dock;
  private MessageServer messageServer;

  private DialogRegistry dialogRegistry;

  // Propertes
  private File fConfig;
  private Properties config;

  /** Creates new form RobotControlGUI */
  public RobotControlGUI()
  {
    initComponents();
    messageServer = new MessageServer(this);
    dock = new Viewport();

    dialogRegistry = new DialogRegistry(dialogsMenu, dock);

    // icon
    Image icon = Toolkit.getDefaultToolkit().getImage(
      this.getClass().getResource("res/RobotControlLogo128.png"));
    setIconImage(icon);

    this.getContentPane().add(dock, BorderLayout.CENTER);

  }

  @PluginLoaded
  public void registerDialog(final Dialog dialog)
  {

    dialogRegistry.registerDialog(dialog);
  }

  public boolean checkConnected()
  {
    return messageServer.isConnected();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    menuBar = new javax.swing.JMenuBar();
    mainControlMenu = new javax.swing.JMenu();
    connectMenuItem = new javax.swing.JMenuItem();
    disconnectMenuItem = new javax.swing.JMenuItem();
    jSeparator1 = new javax.swing.JSeparator();
    resetLayoutMenuItem = new javax.swing.JMenuItem();
    exitMenuItem = new javax.swing.JMenuItem();
    dialogsMenu = new javax.swing.JMenu();
    helpMenu = new javax.swing.JMenu();
    aboutMenuItem = new javax.swing.JMenuItem();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("RobotControl for Nao");

    mainControlMenu.setMnemonic('R');
    mainControlMenu.setText("Main");

    connectMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
    connectMenuItem.setMnemonic('c');
    connectMenuItem.setText("Connect");
    connectMenuItem.setToolTipText("Connect to robot");
    connectMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        connectMenuItemActionPerformed(evt);
      }
    });
    mainControlMenu.add(connectMenuItem);

    disconnectMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
    disconnectMenuItem.setMnemonic('d');
    disconnectMenuItem.setText("Disconnect");
    disconnectMenuItem.setToolTipText("Disconnect from robot");
    disconnectMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        disconnectMenuItemActionPerformed(evt);
      }
    });
    mainControlMenu.add(disconnectMenuItem);
    mainControlMenu.add(jSeparator1);

    resetLayoutMenuItem.setMnemonic('l');
    resetLayoutMenuItem.setText("Reset Layout");
    resetLayoutMenuItem.setToolTipText("Resets all layout information and closes all dialogs");
    resetLayoutMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        resetLayoutMenuItemActionPerformed(evt);
      }
    });
    mainControlMenu.add(resetLayoutMenuItem);

    exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
    exitMenuItem.setMnemonic('e');
    exitMenuItem.setText("Exit");
    exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exitMenuItemActionPerformed(evt);
      }
    });
    mainControlMenu.add(exitMenuItem);

    menuBar.add(mainControlMenu);

    dialogsMenu.setMnemonic('d');
    dialogsMenu.setText("Dialogs");
    menuBar.add(dialogsMenu);

    helpMenu.setMnemonic('h');
    helpMenu.setText("Help");

    aboutMenuItem.setMnemonic('a');
    aboutMenuItem.setText("About");
    aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        aboutMenuItemActionPerformed(evt);
      }
    });
    helpMenu.add(aboutMenuItem);

    menuBar.add(helpMenu);

    setJMenuBar(menuBar);

    java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    setBounds((screenSize.width-650)/2, (screenSize.height-514)/2, 650, 514);
  }// </editor-fold>//GEN-END:initComponents

    private void connectMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_connectMenuItemActionPerformed
    {//GEN-HEADEREND:event_connectMenuItemActionPerformed
}//GEN-LAST:event_connectMenuItemActionPerformed

    private void disconnectMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_disconnectMenuItemActionPerformed
    {//GEN-HEADEREND:event_disconnectMenuItemActionPerformed
    }//GEN-LAST:event_disconnectMenuItemActionPerformed

    private void resetLayoutMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_resetLayoutMenuItemActionPerformed
    {//GEN-HEADEREND:event_resetLayoutMenuItemActionPerformed
    }//GEN-LAST:event_resetLayoutMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitMenuItemActionPerformed
    {//GEN-HEADEREND:event_exitMenuItemActionPerformed
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_aboutMenuItemActionPerformed
    {//GEN-HEADEREND:event_aboutMenuItemActionPerformed
    }//GEN-LAST:event_aboutMenuItemActionPerformed

  public MessageServer getMessageServer()
  {
    return messageServer;
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {

      public void run()
      {
        PluginManager pluginManager = PluginManagerFactory.createPluginManager();
        try
        {
          pluginManager.addPluginsFrom(new URI("classpath://*"));

          pluginManager.getPlugin(RobotControlGUI.class).setVisible(true);
        }
        catch (URISyntaxException ex)
        {
          Logger.getLogger(RobotControlGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    });
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JMenuItem aboutMenuItem;
  private javax.swing.JMenuItem connectMenuItem;
  private javax.swing.JMenu dialogsMenu;
  private javax.swing.JMenuItem disconnectMenuItem;
  private javax.swing.JMenuItem exitMenuItem;
  private javax.swing.JMenu helpMenu;
  private javax.swing.JSeparator jSeparator1;
  private javax.swing.JMenu mainControlMenu;
  private javax.swing.JMenuBar menuBar;
  private javax.swing.JMenuItem resetLayoutMenuItem;
  // End of variables declaration//GEN-END:variables


  public void showConnected(boolean isConnected)
  {
    // TODO
  }

  public Properties getConfig()
  {
    if (fConfig == null || config == null)
    {
      fConfig = new File(System.getProperty("user.home") + "/.robotcontrol");
      config = new Properties();
      try
      {
        config.load(new FileReader(fConfig));
      }
      catch (IOException ex)
      {
        // ignore
      }
    }//end if

    return config;
  }
}
