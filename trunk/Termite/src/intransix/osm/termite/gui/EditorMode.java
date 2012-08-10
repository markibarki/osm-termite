package intransix.osm.termite.gui;

/**
 * This is the base class for an editor mode. The editor mode controls what the 
 * editor does.
 * 
 * @author sutter
 */
public abstract class EditorMode {
	
	/** This method returns the name of the editor mode. 
	 * 
	 * @return		The name of the editor mode 
	 */
	public abstract String getName();
	
	/** This method returns the name for image for the edit mode button on the
	 * edit mode toolbar. 
	 * 
	 * @return		The name of the icon image for this mode. 
	 */
	public abstract String getIconImageName();
	
	/** This method is called when the editor mode is turned on. 
	 */
	public abstract void turnOn();
	
	/** This method is called when the editor mode is turned off. 
	 */
	public abstract void turnOff();
	
	//---------------------
	// Management fucntions
	//---------------------
	
	private javax.swing.JToggleButton uiButton;
	private int shortcut;
	
	public void setUIButton(javax.swing.JToggleButton uiButton) {
		this.uiButton = uiButton;
	}
	
	public javax.swing.JToggleButton getUIButton() {
		return uiButton;
	}
	
	public void setUIShortcut(int shortcut) {
		this.shortcut = shortcut;
	}
	
	public int getUIShortcut() {
		return shortcut;
	}
}
