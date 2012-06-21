/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.TermiteGui;
import javax.swing.JToolBar;

/**
 *
 * @author sutter
 */
public class WayEditorMode {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Way Tool";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/selectMode.png";
	
	private TermiteGui termiteGui;
	
	//====================
	// Public Methods
	//====================
	
	public WayEditorMode(TermiteGui termiteGui) {
		this.termiteGui = termiteGui;
	}
	
	/** This method returns the name of the editor mode. 
	 * 
	 * @return		The name of the editor mode 
	 */
	public String getName() {
		return MODE_NAME;
	}
	
	/** This method returns the name for image for the edit mode button on the
	 * edit mode toolbar. 
	 * 
	 * @return		The name of the icon image for this mode. 
	 */
	public String getIconImageName() {
		return ICON_NAME;
	}
	
	/** This method returns the submode toolbar that will be active when this mode is
	 * active.
	 * 
	 * @return		The submode toolbar 
	 */
	public JToolBar getSubmodeToolbar() {
		return null;
	}
	
	/** This method is called when the editor mode is turned on. 
	 */
	public void turnOn() {
		
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	public void turnOff() {
		
	}	
}
