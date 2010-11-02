/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * DebugRequestPanel.java
 *
 * Created on 29.10.2010, 15:15:34
 */
package de.hu_berlin.informatik.ki.nao.dialogs;

import de.hu_berlin.informatik.ki.nao.Dialog;
import de.hu_berlin.informatik.ki.nao.checkboxtree.CheckboxTreeCellRenderer;
import de.hu_berlin.informatik.ki.nao.checkboxtree.SelectableTreeCellEditor;
import de.hu_berlin.informatik.ki.nao.checkboxtree.SelectableTreeNode;
import javax.swing.JPanel;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;

/**
 *
 * @author thomas
 */
@PluginImplementation
public class DebugRequestPanel extends javax.swing.JPanel implements Dialog
{

  private SelectableTreeNode rootNode;
  private DefaultTreeModel mainTreeModel;

  /** Creates new form DebugRequestPanel */
  public DebugRequestPanel()
  {
    initComponents();
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jScrollPane1 = new javax.swing.JScrollPane();
    mainTree = new javax.swing.JTree();

    jScrollPane1.setViewportView(mainTree);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
    );
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTree mainTree;
  // End of variables declaration//GEN-END:variables

  @Override
  @Init
  public void init()
  {
    rootNode = new SelectableTreeNode("root", "", false);
    rootNode.insert(new SelectableTreeNode("debug_request_1", "tooltip", true), 0);
    rootNode.insert(new SelectableTreeNode("debug_request_2", "tooltip", false), 1);

    mainTreeModel = new DefaultTreeModel(rootNode);
    mainTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    mainTree.setCellRenderer(new CheckboxTreeCellRenderer());
    mainTree.setModel(mainTreeModel);
    mainTree.setRootVisible(true);
    mainTree.setEditable(true);
    mainTree.setCellEditor(new SelectableTreeCellEditor());

  }

  @Override
  public JPanel getPanel()
  {
    return this;
  }

  @Override
  public void dispose()
  {
  }
}
