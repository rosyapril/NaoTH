/*
 * DynamicCanvasPanel.java
 *
 * Created on 8. August 2008, 18:43
 */
package de.naoth.rc.components;

import de.naoth.rc.drawings.Arrow;
import de.naoth.rc.drawings.Drawable;
import de.naoth.rc.drawings.BoundingBox;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author  Heinrich Mellmann
 */
public class DynamicCanvasPanel extends javax.swing.JPanel 
                                implements MouseMotionListener, 
                                           MouseListener, 
                                           MouseWheelListener,
                                           KeyListener
{

  private double offsetX;
  private double offsetY;
  private double scale;
  private double rotation;
  private boolean mirrorXAxis = true;
  private boolean showCoordinates = true; 
  private double dragOffsetX;
  private double dragOffsetY;
  private boolean antializing;
  private boolean fitToViewport = false;
  
  private Drawable backgroundDrawing = null;
  private final List<Drawable> drawingList = Collections.synchronizedList(new ArrayList<>());

  public DynamicCanvasPanel()
  {
    this(true);
  }

  /** Creates new form DynamicCanvasPanel */
  public DynamicCanvasPanel(boolean interactive)
  {
    initComponents();

    if (interactive)
    {
      this.addMouseMotionListener(this);
      this.addMouseListener(this);
      this.addMouseWheelListener(this);
      this.addKeyListener(this);
    }
    this.setOpaque(true);

    this.offsetX = 0.0;
    this.offsetY = 0.0;
    this.scale = 1.0;

    this.antializing = false;
    this.rotation = 0.0;
  }

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

  int oldWidth = 0;
  int oldHeight = 0;
  private void formComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_formComponentResized
  {//GEN-HEADEREND:event_formComponentResized
      if(this.fitToViewport) {
          fitToViewport();
      } else {
        if(oldWidth != 0 && oldHeight != 0)
        {
            double sw = ((double)this.getWidth()) / ((double)oldWidth);
            double sh = ((double)this.getHeight()) / ((double)oldHeight);

            double s = (Math.abs(sh-1.0) < Math.abs(sw-1.0))? sw: sh;

            this.scale *= s;
            this.offsetX = (int)( ((double)this.offsetX) * s +0.5);
            this.offsetY = (int)( ((double)this.offsetY) * s +0.5);
        }

        oldWidth = this.getWidth();
        oldHeight = this.getHeight();
      }
      this.repaint();
  }//GEN-LAST:event_formComponentResized

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
  public void addDrawing(Drawable drawing)
  {
    this.drawingList.add(drawing);
  }

  public void removeDrawing(Drawable drawing)
  {
    this.drawingList.remove(drawing);
  }

  @Override
  public String getToolTipText(MouseEvent e)
  {
      Point.Double p = canvasCoordinatesToInternal(new Point.Double(e.getX(), e.getY()));
      String s = String.format("%.0f;%.0f", p.x, p.y);
      return s;
  }

  @Override
  public Point getToolTipLocation(MouseEvent e)
  {
    Point p = e.getPoint();
    p.y += 15;
    return p;
  }
  
  @Override
  protected void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    paintDrawings(g2d, offsetX, offsetY, rotation, scale);
  }
  
    public void fitToViewport() {
        BoundingBox bb = new BoundingBox();
        paintDrawings(bb, offsetX, offsetY, rotation, scale, true);
        
        if(bb.getWidth() == 0 && bb.getHeight() == 0) {
            return;
        }
        
        // "add" a 10px margin and calculate scale
        double scale_x = ((double) this.getWidth() - 10) / (bb.getWidth());
        double scale_y = ((double) this.getHeight() - 10) / (bb.getHeight());
        setScale(scale_x < scale_y ? scale_x : scale_y);
        // center drawings
        setOffsetX(getWidth() / 2.);
        setOffsetY(getHeight() / 2.);
        // repaint everything
        repaint();
    }
  
  public void paintDrawings(Graphics2D g2d, double x, double y, double r, double s) {
      paintDrawings(g2d, x, y, r, s, false);
  }
    
  private void paintDrawings(Graphics2D g2d, double x, double y, double r, double s, boolean onlyBackground) 
  {
    if (this.antializing)
    {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    // transform the coordinate system to the mathematical one ;)
    g2d.translate(x, y);
    if(mirrorXAxis) {
      g2d.transform(new AffineTransform(1,0,0,-1,0,0));
    }
    g2d.rotate(r);
    g2d.scale(s, s);

    if(backgroundDrawing != null) {
        backgroundDrawing.draw(g2d);
    }
    
    if(!onlyBackground) {
        synchronized(drawingList) {
            drawingList.forEach(d -> { if(d!=null){d.draw(g2d);} });
        }
    }
    
    // transform the drawing-pane back (nessesary to draw the other components corect)
    g2d.scale(1.0/s, 1.0/s);
    g2d.rotate(-r);
    if(mirrorXAxis) {
      g2d.transform(new AffineTransform(1,0,0,-1,0,0));
    }
    g2d.translate(-x, -y);
    
    if(this.showCoordinates && !onlyBackground) {
        drawCoordinateSystem(g2d, this.getSize().width-30, this.getSize().height-30);
    }
  }
  
  
  void drawCoordinateSystem(Graphics2D g2d, int x, int y)
  {
    AffineTransform tx = new AffineTransform();
    tx.translate(x, y);
    if(mirrorXAxis) {
        tx.scale(1.0f, -1.0f);
    }
    tx.rotate(rotation);
    
    Point2D p0 = tx.transform(new Point.Double(-20, -20), null);
    Point2D p1 = tx.transform(new Point.Double(0, -20), null);
    Point2D p2 = tx.transform(new Point.Double(-20, 0), null);
    Point2D p3 = tx.transform(new Point.Double(10, -20), null);
    Point2D p4 = tx.transform(new Point.Double(-20, 10), null);
    
    g2d.setStroke(new BasicStroke(2.0f));
    g2d.setColor(Color.red);
    g2d.drawString("X", (int)p3.getX(), (int)p3.getY());
    new Arrow((int)p0.getX(), (int)p0.getY(), (int)p1.getX(), (int)p1.getY()).draw(g2d);
    
    g2d.setColor(Color.black);
    g2d.drawString("Y", (int)p4.getX(), (int)p4.getY());
    new Arrow((int)p0.getX(), (int)p0.getY(), (int)p2.getX(), (int)p2.getY()).draw(g2d);
  }
  
  public Point.Double canvasCoordinatesToInternal(Point.Double p)
  {
    AffineTransform tx = new AffineTransform();
    tx.rotate(-rotation);
    tx.scale(1.0/scale, 1.0/scale);
    if(mirrorXAxis) {
        tx.scale(1.0f, -1.0f);
    }
    tx.translate(-offsetX, -offsetY);

    Point.Double result = new Point.Double();
    tx.transform(p, result);
    return result;
  }

  // <editor-fold defaultstate="collapsed" desc="Handling Mouse Events">
  @Override
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    double newScale = scale - scale * e.getWheelRotation() * 0.1;
    if (newScale < 15)
    {
      double newOffsetX = (offsetX - e.getX()) * newScale / scale + e.getX();
      double newOffsetY = (offsetY - e.getY()) * newScale / scale + e.getY();

      dragOffsetX += newOffsetX - offsetX;
      dragOffsetY += newOffsetY - offsetY;

      offsetX = newOffsetX;
      offsetY = newOffsetY;
      scale = newScale;
      repaint();
    }//end if
  }//end mouseWheelMoved

  @Override
  public void mouseDragged(MouseEvent e)
  {
    this.offsetX = this.dragOffsetX + e.getX();
    this.offsetY = this.dragOffsetY + e.getY();
    this.repaint();
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    this.dragOffsetX = offsetX - e.getX();
    this.dragOffsetY = offsetY - e.getY();
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
  }

  @Override
  public void mouseClicked(MouseEvent e)
  {
  }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        int k = ke.getKeyCode();
        if(ke.getKeyCode() == KeyEvent.VK_PLUS) {
            scale += scale * 0.1;
            repaint();
        } else if(ke.getKeyCode() == KeyEvent.VK_MINUS) {
            scale -= scale * 0.1;
            repaint();
        } else if(ke.getKeyCode() == KeyEvent.VK_A) {
            this.offsetX -= 50;
            repaint();
        } else if(ke.getKeyCode() == KeyEvent.VK_D) {
            this.offsetX += 50;
            repaint();
        } else if(ke.getKeyCode() == KeyEvent.VK_S) {
            this.offsetY += 50;
            repaint();
        } else if(ke.getKeyCode() == KeyEvent.VK_W) {
            this.offsetY -= 50;
            repaint();
        } else if(ke.getKeyCode() == KeyEvent.VK_E) {
            this.rotation += Math.PI*0.5;
            repaint();
        } else if(ke.getKeyCode() == KeyEvent.VK_Q) {
            this.rotation -= Math.PI*0.5;
            repaint();
        } 
    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }
  
  

  // </editor-fold>
  // <editor-fold defaultstate="collapsed" desc="Getter and Setter">
  public void setOffsetX(double offsetX)
  {
    this.offsetX = offsetX;
  }

  public void setOffsetY(double offsetY)
  {
    this.offsetY = offsetY;
  }

  public void setScale(double scale)
  {
    this.scale = scale;
  }

  public List<Drawable> getDrawingList()
  {
    return drawingList;
  }

  public double getOffsetX()
  {
    return offsetX;
  }

  public double getOffsetY()
  {
    return offsetY;
  }

  public double getScale()
  {
    return scale;
  }

  public double getRotation()
  {
    return rotation;
  }

  public void setRotation(double rotation)
  {
    this.rotation = rotation;
  }

  public boolean isAntializing()
  {
    return antializing;
  }

  public void setAntializing(boolean antializing)
  {
    this.antializing = antializing;
  }

  public boolean isMirrorXAxis()
  {
    return mirrorXAxis;
  }

  public void setMirrorXAxis(boolean mirrorXAxis)
  {
    this.mirrorXAxis = mirrorXAxis;
  }
  
  public boolean isShowCoordinates()
  {
    return showCoordinates;
  }

  public void setShowCoordinates(boolean showCoordinates)
  {
    this.showCoordinates = showCoordinates;
  }

    public Drawable getBackgroundDrawing() {
        return backgroundDrawing;
    }

    public void setBackgroundDrawing(Drawable background) {
        this.backgroundDrawing = background;
    }
  
  
  
    
  // </editor-fold>

    public boolean isFitToViewport() {
        return fitToViewport;
    }

    public void setFitToViewport(boolean fitToViewport) {
        this.fitToViewport = fitToViewport;
        fitToViewport();
    }
}//end DynamicCanvasPanel

