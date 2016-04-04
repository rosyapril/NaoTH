/*
 * RobotStatus.java
 *
 * Created on 09.11.2010, 23:10:57
 */

package de.naoth.rc.components;

import de.naoth.rc.dataformats.SPLMessage;
import de.naoth.rc.server.MessageServer;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Heinrich Mellmann
 */
public class RobotStatus extends javax.swing.JPanel {

    private class RingBuffer extends ArrayList<Long> {
        private final int size;
        public RingBuffer(int size) {
            this.size = size;
        }
        
        @Override
        public boolean add(Long v) {
            boolean r = super.add(v);
            
            if(size() > size) {
                remove(0);
            }
            
            return r;
        }        
    }
    
    private final MessageServer messageServer;
    private String ipAddress = "";
    
    public final static long MAX_TIME_BEFORE_DEAD = 5000; //ms
    private final RingBuffer timestamps = new RingBuffer(5);
    
    private final Color darkOrange = new Color(255, 130, 0);
    
    
    /** Creates new form RobotStatus */
    public RobotStatus(MessageServer messageServer, String ipAddress) {
        initComponents();

        this.messageServer = messageServer;
        this.ipAddress = ipAddress;
        
        this.jlAddress.setText(this.ipAddress);
    }
    
    public RobotStatus(MessageServer messageServer, String ipAddress, Color backgroundColor) {
        this(messageServer, ipAddress);
        this.setBackground(backgroundColor);
    }

    public void setStatus(long timestamp, SPLMessage msg)
    {
      this.jlPlayerNumber.setText("" + msg.playerNum);
      
      long currentTime = System.currentTimeMillis();
      
      //this.jlTimestamp.setText("" + timeDelta);
      
      // don't add the timestamp if it did not change compared to the last time
      long lastSeen = Long.MIN_VALUE;
      if(!timestamps.isEmpty()) 
      {
        lastSeen = timestamps.get(timestamps.size()-1);
      }
      if(lastSeen < timestamp)
      {
        timestamps.add(timestamp);
        lastSeen = timestamp;
      }
      double msgPerSecond = calculateMsgPerSecond();
      if((currentTime - lastSeen) > MAX_TIME_BEFORE_DEAD || msgPerSecond <= 0.0)
      {
        this.jlTimestamp.setText("DEAD");
        this.jlTimestamp.setForeground(Color.red);
      }
      else
      {
        this.jlTimestamp.setText(String.format("%4.2f msg/s", msgPerSecond));
        this.jlTimestamp.setForeground(Color.black);
      }
      
      this.jlFallenTime.setText(msg.fallen == 1 ? "FALLEN" : "NOT FALLEN");
      this.jlBallAge.setText("" + msg.ballAge + "s");
      
      jlTemperature.setForeground(Color.black);
      jlBatteryCharge.setForeground(Color.black);
      
      if(msg.user != null)
      {
        //Representations.BUUserTeamMessage user = Representations.BUUserTeamMessage.parseFrom(msg.data);
        jlTemperature.setText(String.format(" %3.1f °C", msg.user.getTemperature()));
        jlBatteryCharge.setText(String.format("%3.1f%%", msg.user.getBatteryCharge()*100.0f));
        
        if(msg.user.getTemperature() >= 60.0f)
        {
          jlTemperature.setForeground(darkOrange);
        }
        if(msg.user.getTemperature() >= 75.0f)
        {
          jlTemperature.setForeground(Color.red);
        }
        
        if(msg.user.getBatteryCharge() <= 0.3f)
        {
          jlBatteryCharge.setForeground(darkOrange);
        }
        if(msg.user.getBatteryCharge() <= 0.1f)
        {
          jlBatteryCharge.setForeground(Color.red);
        }

        this.jlTeamNumber.setText("" + msg.teamNum);
      }
      else
      {
        jlTemperature.setText("TEMP ??");
        jlBatteryCharge.setText("??");
      }
      
      this.connectButton.setEnabled(!this.messageServer.isConnected());
    }
    
    private double calculateMsgPerSecond()
    {
      long sumOfDiffs = 0;
      int numberOfEntries = 0;
      Iterator<Long> it = timestamps.iterator();
      if(it.hasNext())
      {
        long t1 = it.next();
        while(it.hasNext())
        {
          long t2 = it.next();
          long diff = t2-t1;
          if(diff > 0)
          {
            sumOfDiffs += (t2-t1);
            numberOfEntries++;
          }
          t1 = t2;
        }
      }
      if(numberOfEntries > 0)
      {
        return 1000.0 / ((double) sumOfDiffs / (double) numberOfEntries);
      }
      else
      {
        return 0.0;
      }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel5 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jlPlayerNumber = new javax.swing.JLabel();
        jlTimestamp = new javax.swing.JLabel();
        jlTeamNumber = new javax.swing.JLabel();
        jlFallenTime = new javax.swing.JLabel();
        jlBallAge = new javax.swing.JLabel();
        jlBatteryCharge = new javax.swing.JLabel();
        jlTemperature = new javax.swing.JLabel();
        jlAddress = new javax.swing.JLabel();
        connectButton = new javax.swing.JButton();
        cbShowRobot = new javax.swing.JCheckBox();

        jLabel5.setText("jLabel5");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        setMaximumSize(new java.awt.Dimension(150, 200));
        setMinimumSize(new java.awt.Dimension(150, 100));
        setPreferredSize(new java.awt.Dimension(150, 100));
        setLayout(new java.awt.GridLayout(5, 2));

        jlPlayerNumber.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/rc/res/user-info.png"))); // NOI18N
        jlPlayerNumber.setText("PN");
        jlPlayerNumber.setToolTipText("Player Number");
        add(jlPlayerNumber);

        jlTimestamp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/rc/res/appointment-new.png"))); // NOI18N
        jlTimestamp.setText("DEAD");
        add(jlTimestamp);

        jlTeamNumber.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/rc/res/system-users.png"))); // NOI18N
        jlTeamNumber.setText("TN");
        jlTeamNumber.setToolTipText("Team Number");
        add(jlTeamNumber);

        jlFallenTime.setText("FT");
        add(jlFallenTime);

        jlBallAge.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/rc/res/media-record.png"))); // NOI18N
        jlBallAge.setText("BA");
        jlBallAge.setToolTipText("Ball age");
        add(jlBallAge);

        jlBatteryCharge.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/rc/res/battery.png"))); // NOI18N
        jlBatteryCharge.setText("BATTERY 100%");
        jlBatteryCharge.setToolTipText("Battery charge");
        add(jlBatteryCharge);

        jlTemperature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/rc/res/thermometer.png"))); // NOI18N
        jlTemperature.setText("TEMP 000°C");
        jlTemperature.setToolTipText("Temperature");
        add(jlTemperature);

        jlAddress.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/naoth/rc/res/network-idle.png"))); // NOI18N
        jlAddress.setText("-");
        add(jlAddress);

        connectButton.setText("Connect");
        connectButton.setMaximumSize(new java.awt.Dimension(50, 23));
        connectButton.setMinimumSize(new java.awt.Dimension(50, 23));
        connectButton.setPreferredSize(new java.awt.Dimension(50, 23));
        connectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectButtonActionPerformed(evt);
            }
        });
        add(connectButton);

        cbShowRobot.setSelected(true);
        cbShowRobot.setText("Show robot");
        cbShowRobot.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cbShowRobot.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        add(cbShowRobot);
    }// </editor-fold>//GEN-END:initComponents

    private void connectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectButtonActionPerformed
      if(!this.messageServer.isConnected())
      {
        try
        {
          // TODO: fix port 5401
          this.messageServer.connect(this.ipAddress, 5401);
        }catch(IOException ex)
        {
          Logger.getLogger(RobotStatus.class.getName()).log(Level.SEVERE, "Coult not connect.", ex);
        }
      }
    }//GEN-LAST:event_connectButtonActionPerformed

    public boolean showRobot() {
        return cbShowRobot.isSelected();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbShowRobot;
    private javax.swing.JButton connectButton;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel jlAddress;
    private javax.swing.JLabel jlBallAge;
    private javax.swing.JLabel jlBatteryCharge;
    private javax.swing.JLabel jlFallenTime;
    private javax.swing.JLabel jlPlayerNumber;
    private javax.swing.JLabel jlTeamNumber;
    private javax.swing.JLabel jlTemperature;
    private javax.swing.JLabel jlTimestamp;
    // End of variables declaration//GEN-END:variables

}
