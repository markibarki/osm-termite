package intransix.osm.termite.gui.mode;

import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.render.MapPanel;
import javax.swing.JToolBar;

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
	private EditorModeManager editorModeManager;
	
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
	 * 
	 * @param mapPanel	The UI component for the map 
	 */
	public abstract void turnOn();
	
	/** This method is called when the editor mode is turned off.
	 * 
	 * @param mapPanel The UI component for the map 
	 */
	public abstract void turnOff();
	
	/** This method returns true if the mode is enabled. */
	public boolean getModeEnabled() {
		return modeEnabled;
	}
	
	/** This method returns true if the mode is active. */
	public boolean getModeActive() {
		return (editorModeManager.getActiveMode() == this);
	}
	
	/** This method returns the submode toolbar for the editor mode. */
	public JToolBar getToolBar() {
		return null;
	}
	//---------------------
	// Management fucntions
	//---------------------
	
	public void setEditorModeManager(EditorModeManager editorModeManager) {
		this.editorModeManager = editorModeManager;
	}
	
	/** This method is called when the map data present state changes. */
	public boolean getEnableStateForDataState(boolean mapDataPresent) {
		return mapDataPresent ? dataPresentEnabled : dataMissingEnabled;
	}
	
	protected void setDataEnabledStates(boolean dataPresentEnabled, boolean dataMissingEnabled) {
		this.dataPresentEnabled = dataPresentEnabled;
		this.dataMissingEnabled = dataMissingEnabled;
	}
	
	public void setEnabled(boolean enabled) {
		modeEnabled = enabled;
		editorModeManager.notifyEnableChange(this);
	}
}
