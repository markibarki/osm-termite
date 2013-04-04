/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.dialog;

/**
 * This is a callback used for notification for a TermiteDialog. 
 * 
 * @author sutter
 */
public interface DialogCallback {
	
	/** This is method is called as the callback for the OK or Cancel actions on a dialog. The return
	 * value of this method determines if the dialog closes (true) or not (false). */
	boolean handle(TermiteDialog dialog);
}
