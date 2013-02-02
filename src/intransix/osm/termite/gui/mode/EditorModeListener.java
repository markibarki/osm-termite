package intransix.osm.termite.gui.mode;

/**
 *
 * @author sutter
 */
public interface EditorModeListener {
	
	/** This method is called when the mode changes. */
	void activeModeChanged(EditorMode activeMode);
	
	/** This is called is a mode goes from disabled to enabled. */
	void modeEnableChanged(EditorMode mode);
}
