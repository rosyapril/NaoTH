/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.naoth.rc.dialogs.panels;

import java.beans.PropertyChangeListener;

/**
 *
 * @author claas
 */
public class ColorPixelOnOffControl extends javax.swing.JPanel 
{
  private String coloredObjectName;

  /**
   * Creates new form ColorPixelOnOffControl
   */
  public ColorPixelOnOffControl(String coloredObjectName, PropertyChangeListener listener, boolean showPixels) 
  {
    initComponents();
    this.coloredObjectName = coloredObjectName;
    this.jRadioButton.setText("Show " + coloredObjectName + " Pixels");
    this.jRadioButton.setSelected(showPixels);
    this.addPropertyChangeListener(listener);
  }
  
  public boolean showColoredPixels()
  {
    return this.jRadioButton.isSelected();
  }
 
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jRadioButton = new javax.swing.JRadioButton();

        setMinimumSize(new java.awt.Dimension(40, 40));

        jRadioButton.setText("jRadioButton1");
        jRadioButton.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 147, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jRadioButton)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

  private void jRadioButtonItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonItemStateChanged
    firePropertyChange("ColorCalibrationTool:" + coloredObjectName + ":switched", null, this.jRadioButton.isSelected());
  }//GEN-LAST:event_jRadioButtonItemStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton jRadioButton;
    // End of variables declaration//GEN-END:variables
}