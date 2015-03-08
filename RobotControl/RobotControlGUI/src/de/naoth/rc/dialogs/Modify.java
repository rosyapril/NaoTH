/*
 * Modify.java
 *
 * Created on 28.06.2009, 22:00:10
 */

package de.naoth.rc.dialogs;


import de.naoth.rc.RobotControl;
import de.naoth.rc.components.treetable.ModifyDataModel;
import de.naoth.rc.components.treetable.ModifyDataModel.ModifyDataNode;
import de.naoth.rc.components.treetable.TreeTable;
import de.naoth.rc.core.dialog.AbstractDialog;
import de.naoth.rc.core.dialog.DialogPlugin;
import de.naoth.rc.core.manager.ObjectListener;
import de.naoth.rc.core.manager.SwingCommandExecutor;
import de.naoth.rc.manager.GenericManagerFactory;

import de.naoth.rc.server.Command;
import java.awt.Color;
import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

/**
 *
 * @author admin
 */
public class Modify extends AbstractDialog
{
    private static final Command commandCognitionModifyList = new Command("Cognition:modify:list");
    private static final Command commandMotionModifyList = new Command("Motion:modify:list");
    
    Command commandToExecute = null;

    @PluginImplementation
    public static class Plugin extends DialogPlugin<Modify>
    {
        @InjectPlugin
        public static RobotControl parent;
        @InjectPlugin
        public static SwingCommandExecutor commandExecutor;
        @InjectPlugin
        public static GenericManagerFactory genericManagerFactory;
    }

    private ModifyDataModel treeTableModel = new ModifyDataModel();
    private TreeTable myTreeTable = new TreeTable(treeTableModel);
    
    ModifyUpdater modifyUpdaterCognition = new ModifyUpdater("Cognition");
    ModifyUpdater modifyUpdaterMotion = new ModifyUpdater("Motion");
    
    /** Creates new form Modify */
    public Modify() {
      initComponents();
      
      jScrollPane2.setViewportView(myTreeTable);

      TableColumn modifyColumn = myTreeTable.getColumn("Modify");
      modifyColumn.setMaxWidth(50);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        btRefresh = new javax.swing.JToggleButton();
        jScrollPane2 = new javax.swing.JScrollPane();

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        btRefresh.setText("Refresh");
        btRefresh.setFocusable(false);
        btRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRefreshActionPerformed(evt);
            }
        });
        jToolBar1.add(btRefresh);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRefreshActionPerformed
  
      if(btRefresh.isSelected())
      {
        if(Plugin.parent.checkConnected())
        {
          // HACK: recreate the whole table
          treeTableModel = new ModifyDataModel();
          myTreeTable = new TreeTable(treeTableModel);
          myTreeTable.getColumn("Modify").setMaxWidth(50);
          //myTreeTable.getTree().setRootVisible(false);
          jScrollPane2.setViewportView(myTreeTable);
      
          Plugin.genericManagerFactory.getManager(commandCognitionModifyList).addListener(modifyUpdaterCognition);
          Plugin.genericManagerFactory.getManager(commandMotionModifyList).addListener(modifyUpdaterMotion);
        } else {
          btRefresh.setSelected(false);
        }
      }
      else
      {
        Plugin.genericManagerFactory.getManager(commandCognitionModifyList).removeListener(modifyUpdaterCognition);
        Plugin.genericManagerFactory.getManager(commandMotionModifyList).removeListener(modifyUpdaterMotion);
      }
    }//GEN-LAST:event_btRefreshActionPerformed


  private class FlagModifiedListener implements ModifyDataModel.ValueChangedListener
  {
      private final String name;
      private final String prefix;
      
      FlagModifiedListener(String name, String prefix)
      {
          this.name = name;
          this.prefix = prefix;
      }
      
    @Override
    public void valueChanged(boolean enabled, double value)
    {
        if(!enabled)
        {
            Command command = new Command(prefix + ":modify:release").addArg(name, "");
            Plugin.commandExecutor.executeCommand(new PrintObjectListener(), command);
        }
        else
        {
            Command command = new Command(prefix + ":modify:set").addArg(name, ""+value);
            Plugin.commandExecutor.executeCommand(new PrintObjectListener(), command);
        }
    }
  }//end class FlagModifiedListener


  class ModifyUpdater implements ObjectListener<byte[]>
  {
    String rootName;

    public ModifyUpdater(String rootName) {
        this.rootName = rootName;
    }

       @Override
      public void newObjectReceived(byte[] object) {
          String str = new String(object);
          final String[] modifies = str.split("(\n|\t| |\r)+");
          
          SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                  try {
                      for (String msg : modifies) {
                          String[] s = msg.split("(( |\t)*=( |\t)*)|;");

                          ModifyDataNode node = treeTableModel.insertPath("[" + rootName + "]:" + s[1], ':');
                          
                          node.enabled = Integer.parseInt(s[0]) > 0;
                          node.value = Double.parseDouble(s[2]);

                          if (node.enabledListener == null) {
                              node.enabledListener = new FlagModifiedListener(s[1], rootName);
                          }
                      }//end for
                  } catch (NumberFormatException e) {
                      JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
                      dispose();
                  }
                  
                  //myTreeTable.getTree().expandPath(new TreePath(myTreeTable.getTree().getModel().getRoot()));
                  myTreeTable.revalidate();
                  myTreeTable.repaint();
              }
          });
      }

        @Override
        public void errorOccured(String cause)
        {
          btRefresh.setSelected(false);
          dispose();
        }
  }
  
  class PrintObjectListener implements ObjectListener<byte[]>
  {
        @Override
        public void newObjectReceived(byte[] object) {
            System.out.println(new String(object));
        }

        @Override
        public void errorOccured(String cause) {
            System.err.println(cause);
        }
  }
  

  class ColorRenderer extends DefaultTableCellRenderer
  {
    public ColorRenderer() {
      setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,boolean isSelected, boolean hasFocus, int row, int column)
    {
      Component comp = super.getTableCellRendererComponent(
                      table,  value, isSelected, hasFocus, row, column);

      boolean columnValue = ((Boolean)table.getValueAt(row, table.getColumnModel().getColumnIndex("Modify"))).booleanValue();

      if (columnValue) {
        setBackground(new Color(1.0f,0.8f,1.0f));
      } else {
        setBackground(null);
      }
      return comp;
    }//end getTableCellRendererComponent
  }//end ColorRenderer

  @Override
  public void dispose()
  {
    Plugin.genericManagerFactory.getManager(commandCognitionModifyList).removeListener(modifyUpdaterCognition);
    Plugin.genericManagerFactory.getManager(commandMotionModifyList).removeListener(modifyUpdaterMotion);
  }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btRefresh;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables

}