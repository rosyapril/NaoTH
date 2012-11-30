/*
 * FieldViewer.java
 *
 * Created on 1. Mai 2008, 00:02
 */
package de.naoth.rc.dialogs;

import de.naoth.rc.AbstractDialog;
import de.naoth.rc.Dialog;
import de.naoth.rc.DialogPlugin;
import de.naoth.rc.RobotControl;
import de.naoth.rc.dataformats.JanusImage;
import de.naoth.rc.dialogs.Tools.PNGExportFileType;
import de.naoth.rc.dialogs.Tools.PlainPDFExportFileType;
import de.naoth.rc.dialogs.drawings.Drawable;
import de.naoth.rc.dialogs.drawings.DrawingCollection;
import de.naoth.rc.dialogs.drawings.DrawingOnField;
import de.naoth.rc.dialogs.drawings.DrawingsContainer;
import de.naoth.rc.dialogs.drawings.FieldDrawingS3D2011;
import de.naoth.rc.dialogs.drawings.FieldDrawingSPL2012;
import de.naoth.rc.dialogs.drawings.FieldDrawingSPL2013;
import de.naoth.rc.dialogs.drawings.LocalFieldDrawing;
import de.naoth.rc.dialogs.drawings.RadarDrawing;
import de.naoth.rc.dialogs.drawings.StrokePlot;
import de.naoth.rc.manager.DebugDrawingManager;
import de.naoth.rc.manager.ImageManager;
import de.naoth.rc.manager.ObjectListener;
import de.naoth.rc.manager.PlotDataManager;
import de.naoth.rc.manager.TeamCommDrawingManager;
import de.naoth.rc.math.Vector2D;
import de.naoth.rc.messages.Messages.PlotItem;
import de.naoth.rc.messages.Messages.Plots;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;
import org.freehep.graphicsio.emf.EMFExportFileType;
import org.freehep.graphicsio.java.JAVAExportFileType;
import org.freehep.graphicsio.ps.EPSExportFileType;
import org.freehep.graphicsio.svg.SVGExportFileType;
import org.freehep.util.export.ExportDialog;

/**
 *
 * @author  Heinrich Mellmann
 */
public class FieldViewer extends AbstractDialog
  implements ObjectListener<DrawingsContainer>,
  Dialog
{

  @PluginImplementation
  public static class Plugin extends DialogPlugin<FieldViewer>
  {
      @InjectPlugin
      public static RobotControl parent;
      @InjectPlugin
      public static DebugDrawingManager debugDrawingManager;
      @InjectPlugin
      public static PlotDataManager plotDataManager;
      @InjectPlugin
      public static ImageManager imageManager;
      @InjectPlugin
      public static TeamCommDrawingManager teamCommDrawingManager;
  }//end Plugin
  
  private Drawable backgroundDrawing;

  private ImageListener imageListener;
  private ImageDrawing imageDrawing;

  private PlotDataListener plotDataListener;

  private StrokePlot strokePlot;

  /** Creates new form FieldViewer */
  public FieldViewer()
  {
    initComponents();
    
    // 
    this.cbBackground.setModel(
            new javax.swing.DefaultComboBoxModel(
            new Drawable[] 
            { 
                new FieldDrawingSPL2012(),
                new FieldDrawingSPL2013(),
                new FieldDrawingS3D2011(),
                new LocalFieldDrawing(),
                new RadarDrawing()
            }
    ));
    
    this.backgroundDrawing = (Drawable)this.cbBackground.getSelectedItem();

    this.imageListener = new ImageListener();
    this.plotDataListener = new PlotDataListener();
  }

  @Init
  @Override
  public void init()
  {
    this.fieldCanvas.getDrawingList().add(0, this.backgroundDrawing);
    this.fieldCanvas.setAntializing(btAntializing.isSelected());
    this.fieldCanvas.repaint();

    this.strokePlot = new StrokePlot(300);
  }

  @Override
  public JPanel getPanel()
  {
    return this;
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu = new javax.swing.JPopupMenu();
        jMenuItemExport = new javax.swing.JMenuItem();
        jToolBar1 = new javax.swing.JToolBar();
        btReceiveDrawings = new javax.swing.JToggleButton();
        btReceiveTeamCommDrawings = new javax.swing.JToggleButton();
        btClean = new javax.swing.JButton();
        cbBackground = new javax.swing.JComboBox();
        btImageProjection = new javax.swing.JToggleButton();
        btRotate = new javax.swing.JToggleButton();
        btAntializing = new javax.swing.JCheckBox();
        btCollectDrawings = new javax.swing.JCheckBox();
        btTrace = new javax.swing.JCheckBox();
        jSlider1 = new javax.swing.JSlider();
        drawingPanel = new javax.swing.JPanel();
        fieldCanvas = new de.naoth.rc.dialogs.panels.DynamicCanvasPanel();

        jMenuItemExport.setText("Export");
        jMenuItemExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportActionPerformed(evt);
            }
        });
        jPopupMenu.add(jMenuItemExport);

        jToolBar1.setRollover(true);

        btReceiveDrawings.setText("Receive");
        btReceiveDrawings.setFocusable(false);
        btReceiveDrawings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btReceiveDrawings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btReceiveDrawings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btReceiveDrawingsActionPerformed(evt);
            }
        });
        jToolBar1.add(btReceiveDrawings);

        btReceiveTeamCommDrawings.setText("TeamComm");
        btReceiveTeamCommDrawings.setFocusable(false);
        btReceiveTeamCommDrawings.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btReceiveTeamCommDrawings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btReceiveTeamCommDrawings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btReceiveTeamCommDrawingsActionPerformed(evt);
            }
        });
        jToolBar1.add(btReceiveTeamCommDrawings);

        btClean.setText("Clean");
        btClean.setFocusable(false);
        btClean.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btClean.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btClean.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCleanActionPerformed(evt);
            }
        });
        jToolBar1.add(btClean);

        cbBackground.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SPL2012", "SPL2013", "S3D2011", "RADAR", "LOCAL" }));
        cbBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbBackgroundActionPerformed(evt);
            }
        });
        jToolBar1.add(cbBackground);

        btImageProjection.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/rc/res/view_icon.png"))); // NOI18N
        btImageProjection.setToolTipText("Image Projection");
        btImageProjection.setFocusable(false);
        btImageProjection.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btImageProjection.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btImageProjection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btImageProjectionActionPerformed(evt);
            }
        });
        jToolBar1.add(btImageProjection);

        btRotate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/rc/res/rotate_ccw.png"))); // NOI18N
        btRotate.setFocusable(false);
        btRotate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btRotate.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/rc/res/rotate_cw.png"))); // NOI18N
        btRotate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btRotate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRotateActionPerformed(evt);
            }
        });
        jToolBar1.add(btRotate);

        btAntializing.setText("Antialiazing");
        btAntializing.setFocusable(false);
        btAntializing.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btAntializing.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btAntializing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btAntializingActionPerformed(evt);
            }
        });
        jToolBar1.add(btAntializing);

        btCollectDrawings.setText("Collect");
        btCollectDrawings.setFocusable(false);
        btCollectDrawings.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btCollectDrawings.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btCollectDrawings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCollectDrawingsActionPerformed(evt);
            }
        });
        jToolBar1.add(btCollectDrawings);

        btTrace.setText("Trace");
        btTrace.setFocusable(false);
        btTrace.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btTrace.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btTrace);

        jSlider1.setMaximum(255);
        jSlider1.setValue(247);
        jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSlider1StateChanged(evt);
            }
        });
        jToolBar1.add(jSlider1);

        drawingPanel.setBackground(new java.awt.Color(247, 247, 247));
        drawingPanel.setLayout(new java.awt.BorderLayout());

        fieldCanvas.setBackground(null);
        fieldCanvas.setComponentPopupMenu(jPopupMenu);
        fieldCanvas.setOffsetX(350.0);
        fieldCanvas.setOffsetY(200.0);
        fieldCanvas.setOpaque(false);
        fieldCanvas.setScale(0.07);

        javax.swing.GroupLayout fieldCanvasLayout = new javax.swing.GroupLayout(fieldCanvas);
        fieldCanvas.setLayout(fieldCanvasLayout);
        fieldCanvasLayout.setHorizontalGroup(
            fieldCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 674, Short.MAX_VALUE)
        );
        fieldCanvasLayout.setVerticalGroup(
            fieldCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 363, Short.MAX_VALUE)
        );

        drawingPanel.add(fieldCanvas, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(drawingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(drawingPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btReceiveDrawingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btReceiveDrawingsActionPerformed
      if(btReceiveDrawings.isSelected())
      {
        if(Plugin.parent.checkConnected())
        {
          Plugin.debugDrawingManager.addListener(this);
          Plugin.plotDataManager.addListener(plotDataListener);
        }
        else
        {
          btReceiveDrawings.setSelected(false);
        }
      }
      else
      {
        Plugin.debugDrawingManager.removeListener(this);
        Plugin.plotDataManager.removeListener(plotDataListener);
      }
    }//GEN-LAST:event_btReceiveDrawingsActionPerformed

private void jMenuItemExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportActionPerformed
  
  ExportDialog export = new ExportDialog("FieldViewer", false);
  
  
  // add the image types for export
  export.addExportFileType(new SVGExportFileType());
  export.addExportFileType(new PlainPDFExportFileType());
  export.addExportFileType(new EPSExportFileType());
  export.addExportFileType(new EMFExportFileType());
  export.addExportFileType(new JAVAExportFileType());
  export.addExportFileType(new PNGExportFileType());
  
  export.showExportDialog(this, "Export view as ...", this.fieldCanvas, "export");
}//GEN-LAST:event_jMenuItemExportActionPerformed



private void btImageProjectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btImageProjectionActionPerformed
    if(btImageProjection.isSelected())
      {
        if(Plugin.parent.checkConnected())
        {
          Plugin.imageManager.addListener(imageListener);
        }
        else
        {
          btReceiveDrawings.setSelected(false);
        }
      }
      else
      {
        Plugin.imageManager.removeListener(imageListener);
      }
}//GEN-LAST:event_btImageProjectionActionPerformed

private void btAntializingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btAntializingActionPerformed
  this.fieldCanvas.setAntializing(btAntializing.isSelected());
  this.fieldCanvas.repaint();
}//GEN-LAST:event_btAntializingActionPerformed

private void btCollectDrawingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCollectDrawingsActionPerformed
  // TODO add your handling code here:
}//GEN-LAST:event_btCollectDrawingsActionPerformed

private void btReceiveTeamCommDrawingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btReceiveTeamCommDrawingsActionPerformed
  if(btReceiveTeamCommDrawings.isSelected())
  {
    Plugin.teamCommDrawingManager.addListener(this);
  }
  else
  {
    Plugin.teamCommDrawingManager.removeListener(this);
  }
}//GEN-LAST:event_btReceiveTeamCommDrawingsActionPerformed

private void jSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider1StateChanged
  int v = this.jSlider1.getValue();
  this.drawingPanel.setBackground(new Color(v,v,v));
  this.drawingPanel.repaint();
}//GEN-LAST:event_jSlider1StateChanged

    private void btCleanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCleanActionPerformed
        this.strokePlot.clear();
        resetView();
        this.fieldCanvas.repaint();
    }//GEN-LAST:event_btCleanActionPerformed

    private void cbBackgroundActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cbBackgroundActionPerformed
    {//GEN-HEADEREND:event_cbBackgroundActionPerformed
        this.backgroundDrawing = (Drawable)this.cbBackground.getSelectedItem();
        this.fieldCanvas.getDrawingList().set(0, this.backgroundDrawing);
        this.fieldCanvas.repaint();
    }//GEN-LAST:event_cbBackgroundActionPerformed

    private void btRotateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btRotateActionPerformed
    {//GEN-HEADEREND:event_btRotateActionPerformed
        if(this.btRotate.isSelected())
        {
            this.fieldCanvas.setRotation(-Math.PI*0.5);
        }
        else
        {
            this.fieldCanvas.setRotation(0);
        }
        this.fieldCanvas.repaint();
    }//GEN-LAST:event_btRotateActionPerformed

  @Override
  public void errorOccured(String cause)
  {
    btReceiveDrawings.setSelected(false);
    Plugin.debugDrawingManager.removeListener(this);
  }
  
  void resetView()
  {
    this.fieldCanvas.getDrawingList().clear();
    this.fieldCanvas.getDrawingList().add(0, this.backgroundDrawing);
    if(btTrace.isSelected())
        this.fieldCanvas.getDrawingList().add(this.strokePlot);
    if(imageDrawing != null)
        this.fieldCanvas.getDrawingList().add(imageDrawing);
  }//end clearView

  
  @Override
  public void newObjectReceived(DrawingsContainer objectList)
  {
    if(objectList != null)
    {
      if(!this.btCollectDrawings.isSelected())
      {
        resetView();
      }
      DrawingCollection drawingCollection = objectList.get(DrawingOnField.class);
      if(drawingCollection != null)
        this.fieldCanvas.getDrawingList().add(drawingCollection);

      repaint();
    }//end if
  }//end newObjectReceived


  class PlotDataListener implements ObjectListener<Plots>
  {
    @Override
    public void errorOccured(String cause)
    {
      btReceiveDrawings.setSelected(false);
      Plugin.plotDataManager.removeListener(this);
    }//end errorOccured

    @Override
    public void newObjectReceived(Plots data)
    {
      if (data == null) return;

      for(PlotItem item : data.getPlotsList())
      {
        if(item.getType() == PlotItem.PlotType.Plot2D
          && item.hasX() && item.hasY())
        {
          strokePlot.addStroke(item.getName(), Color.blue);
          strokePlot.setEnabled(item.getName(), true);
          strokePlot.plot(item.getName(), new Vector2D(item.getX(), item.getY()));
        }
        else if(item.getType() == PlotItem.PlotType.Origin2D
          && item.hasX() && item.hasY() && item.hasRotation())
        {
          strokePlot.setRotation(item.getRotation());
        }
      } //end for
    }//end newObjectReceived
  }//end class PlotDataListener

  class ImageListener implements ObjectListener<JanusImage>
  {
      @Override
      public void newObjectReceived(JanusImage object)
      {
        if(imageDrawing == null)
        {
          imageDrawing = new ImageDrawing(object.getRgb());
          fieldCanvas.getDrawingList().add(imageDrawing);
        }else
        {
          imageDrawing.setImage(object.getRgb());
        }
      }//end newObjectReceived

      @Override
      public void errorOccured(String cause)
      {
        btImageProjection.setSelected(false);
        Plugin.imageManager.removeListener(this);
      }//end errorOccured
  }//end ImageListener


    private class ImageDrawing implements Drawable
    {
      protected BufferedImage image;
      
      public ImageDrawing(BufferedImage image)
      {
          this.image = image;
      }

      @Override
      public void draw(Graphics2D g2d)
      {
        if(image != null)
        {
          g2d.rotate(Math.PI*0.5);
          g2d.drawImage(image, new AffineTransform(1, 0, 0, 1, -image.getWidth()/2, -image.getHeight()/2), null);
          g2d.rotate(-Math.PI*0.5);
        }
      }//end draw

      public void setImage(BufferedImage image)
      {
          this.image = image;
      }
    }//end class ImageDrawing

  @Override
  public void dispose()
  {
    // remove all the registered listeners
    Plugin.debugDrawingManager.removeListener(this);
    Plugin.plotDataManager.removeListener(plotDataListener);
    Plugin.imageManager.removeListener(imageListener);
    Plugin.teamCommDrawingManager.removeListener(this);
  }//end dispose
  
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox btAntializing;
    private javax.swing.JButton btClean;
    private javax.swing.JCheckBox btCollectDrawings;
    private javax.swing.JToggleButton btImageProjection;
    private javax.swing.JToggleButton btReceiveDrawings;
    private javax.swing.JToggleButton btReceiveTeamCommDrawings;
    private javax.swing.JToggleButton btRotate;
    private javax.swing.JCheckBox btTrace;
    private javax.swing.JComboBox cbBackground;
    private javax.swing.JPanel drawingPanel;
    private de.naoth.rc.dialogs.panels.DynamicCanvasPanel fieldCanvas;
    private javax.swing.JMenuItem jMenuItemExport;
    private javax.swing.JPopupMenu jPopupMenu;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}
