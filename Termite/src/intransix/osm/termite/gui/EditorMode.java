package intransix.osm.termite.gui;

import intransix.osm.termite.render.MapLayerManager;

/**
 * This is the base class for an editor mode. The editor mode controls what the 
 * editor does.
 * 
 * @author sutter
 */
public abstract class EditorMode {
	
	private boolean modeEnabled;
	private boolean dataPresentEnabled = true;
	private boolean dataMissingEnabled = false;
	
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
	
	/** This method can be overwritten to set any needed map layers. */
	public void setLayers(MapLayerManager mapLayerManager) {
	}
	
	/** This method returns true if the mode is enabled. */
	public boolean getModeEnabled() {
		return modeEnabled;
	}
	
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
	
	/** This method is called when the map data present state changes. */
	public void setModeState(boolean mapDataPresent) {
		boolean enabled = mapDataPresent ? dataPresentEnabled : dataMissingEnabled;
		setEnabled(enabled);
	}
	
	protected void setDataEnabledStates(boolean dataPresentEnabled, boolean dataMissingEnabled) {
		this.dataPresentEnabled = dataPresentEnabled;
		this.dataMissingEnabled = dataMissingEnabled;
	}
	
	protected void setEnabled(boolean enabled) {
		modeEnabled = enabled;
		if(uiButton != null) {
			uiButton.setEnabled(modeEnabled);
		}
	}
}
