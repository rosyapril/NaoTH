/*
 * Console.java
 *
 * Created on 28. Februar 2008, 23:33
 */
package de.hu_berlin.informatik.ki.nao.dialogs;

import de.hu_berlin.informatik.ki.nao.Dialog;
import de.hu_berlin.informatik.ki.nao.RobotControlGUI;
import de.hu_berlin.informatik.ki.nao.interfaces.MessageServerProvider;
import de.hu_berlin.informatik.ki.nao.manager.ObjectListener;
import de.hu_berlin.informatik.ki.nao.manager.UnrequestedOutputManager;
import de.hu_berlin.informatik.ki.nao.server.Command;
import de.hu_berlin.informatik.ki.nao.server.CommandSender;
import de.hu_berlin.informatik.ki.nao.server.MessageServer;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

/**
 *
 * @author  thomas
 */
@PluginImplementation
public class Console extends JPanel implements CommandSender,
  ObjectListener<String>, Dialog
{

  @InjectPlugin
  public RobotControlGUI parent;
  @InjectPlugin
  public UnrequestedOutputManager unrequestedOutputManager;
  @InjectPlugin
  public MessageServerProvider msgServer;

  /** Creates new form Console */
  public Console()
  {
    initComponents();
  }

  @Init
  public void init()
  {
    if(parent == null)
    {
      throw (new IllegalArgumentException("\"parent\" was null"));
    }

    if(cbUnrequestedMessages.isSelected())
    {
      unrequestedOutputManager.addListener(this);
    }

    cbInput.getEditor().getEditorComponent().addKeyListener(new KeyListener()
    {

      public void keyTyped(KeyEvent e)
      {
      }

      public void keyPressed(KeyEvent e)
      {

        if(e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          send();
        }
      }

      public void keyReleased(KeyEvent e)
      {
      }
    });

  }

  public JPanel getPanel()
  {
    return this;
  }

  private void send()
  {
    if(parent.checkConnected())
    {
      JTextField t = (JTextField) cbInput.getEditor().getEditorComponent();
      String cmd = t.getText().trim();

      // parse command line //

      String[] wsSplitted = cmd.split("[ \\t]+");

      Command parsedCommand = new Command();

      if(wsSplitted.length > 0)
      {
        parsedCommand.setName(wsSplitted[0]);
        
        for(int i=1; i < wsSplitted.length; i++)
        {
          // split name/value
          String[] argNameValue = wsSplitted[i].split("[ \\t]*=[ \\t]*");
          // should be exact 2, and yes, you can't pass a "="...

          if(argNameValue.length == 1)
          {
            parsedCommand.addArg(argNameValue[0]);
          }
          else if(argNameValue.length >= 2)
          {
            parsedCommand.addArg(argNameValue[0], argNameValue[1]);
          }
        }
        
      }
      
      msgServer.getMessageServer().executeSingleCommand(this, parsedCommand);
      cbInput.insertItemAt(cmd, 0);
      cbInput.setSelectedIndex(0);
    }
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btSend = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtOutput = new javax.swing.JTextArea();
        cbInput = new javax.swing.JComboBox();
        cbUnrequestedMessages = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jButtonClean = new javax.swing.JButton();

        btSend.setMnemonic('S');
        btSend.setText("Send");
        btSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSendActionPerformed(evt);
            }
        });

        txtOutput.setColumns(20);
        txtOutput.setLineWrap(true);
        txtOutput.setRows(5);
        jScrollPane1.setViewportView(txtOutput);

        cbInput.setEditable(true);

        cbUnrequestedMessages.setMnemonic('u');
        cbUnrequestedMessages.setSelected(true);
        cbUnrequestedMessages.setText("Show unrequested output messages");
        cbUnrequestedMessages.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                cbUnrequestedMessagesStateChanged(evt);
            }
        });

        jLabel1.setText("try \"help\" for available commands");

        jButtonClean.setText("Clean");
        jButtonClean.setPreferredSize(new java.awt.Dimension(59, 23));
        jButtonClean.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCleanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(cbUnrequestedMessages)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 210, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(cbInput, 0, 446, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btSend)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonClean, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbInput, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btSend, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonClean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(cbUnrequestedMessages)))
        );
    }// </editor-fold>//GEN-END:initComponents

  private void btSendActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btSendActionPerformed
  {//GEN-HEADEREND:event_btSendActionPerformed

    send();

  }//GEN-LAST:event_btSendActionPerformed

private void cbUnrequestedMessagesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbUnrequestedMessagesStateChanged

  if(cbUnrequestedMessages.isSelected())
  {
    unrequestedOutputManager.addListener(this);
  }
  else
  {
    unrequestedOutputManager.removeListener(this);
  }

}//GEN-LAST:event_cbUnrequestedMessagesStateChanged

private void jButtonCleanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCleanActionPerformed
  txtOutput.setText("");
  txtOutput.setCaretPosition(0);
}//GEN-LAST:event_jButtonCleanActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btSend;
    private javax.swing.JComboBox cbInput;
    private javax.swing.JCheckBox cbUnrequestedMessages;
    private javax.swing.JButton jButtonClean;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txtOutput;
    // End of variables declaration//GEN-END:variables
  public void handleResponse(byte[] result, Command originalCommand)
  {
    try
    {
      String string = new String(result, MessageServer.STRING_ENCODING);
      txtOutput.append(originalCommand.getName() + ":\n" + string + "\n\n");
    }
    catch(UnsupportedEncodingException ex)
    {
      Logger.getLogger(Console.class.getName()).log(Level.SEVERE, null, ex);
      txtOutput.append("ERROR: This Java-version is not able to decode " +
        MessageServer.STRING_ENCODING + "\n\n");
    }
    txtOutput.setCaretPosition(txtOutput.getText().length() - 1);

  }

  public void handleError(int code)
  {
    txtOutput.append("ERROR: Robot returned error code " + code + "\n\n");
    txtOutput.setCaretPosition(txtOutput.getText().length() - 1);
  }

  public Command getCurrentCommand()
  {
    return new Command("ping");
  }

  // text //
  public void newObjectReceived(String object)
  {
    if(!"".equals(object))
    {
      txtOutput.append(object);
      txtOutput.setCaretPosition(txtOutput.getText().length() - 1);
    }
  }

  public void errorOccured(String cause)
  {
    cbUnrequestedMessages.setSelected(false);
    unrequestedOutputManager.removeListener(this);
  }

  public void dispose()
  {
    unrequestedOutputManager.removeListener(this);
  }//end dispose
}
