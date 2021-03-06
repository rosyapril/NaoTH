/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.naoth.rc.core.dialog;

import javax.swing.JPanel;
import net.xeoh.plugins.base.Plugin;

/**
 * Interface for all dialogs
 * @author thomas
 */
public interface Dialog extends Plugin
{
  
  /**
   * Returns this as panel.<br><br>
   * 
   * Mostly this just should return <code>this</code>
   * @return
   */
  public JPanel getPanel();

  /**
   * Get the name that should be used e.g. for the menu entries.
   * @return
   */
  public String getDisplayName();

  /**
   * Get the category of the dialog, this is used to sort the dialogs in submenus
   * @return
   */
  public String getCategory();
  
  /**
   * This method is called when the dialog is closed.
   * Here the all the registered listener should be unregistered.
   */
  public void dispose();
  
  
  public void destroy();

}
