/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import javax.swing.JToolBar;

/**
 *
 * @author sutter
 */
public class GeocodeEditorMode implements EditorMode {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Geocode";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/globe25.png";
	
	private TermiteGui termiteGui;
	
	//====================
	// Public Methods
	//====================
	
	public GeocodeEditorMode(TermiteGui termiteGui) {
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
	
	/** This method is called when the editor mode is turned on. 
	 */
	public void turnOn() {
		
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	public void turnOff() {
		
	}
}
