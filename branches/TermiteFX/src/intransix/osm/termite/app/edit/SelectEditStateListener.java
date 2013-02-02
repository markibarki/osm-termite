package intransix.osm.termite.app.edit;

/**
 * This interface is used for notifications of select edit sub mode changes.
 * 
 * @author sutter
 */
public interface SelectEditStateListener {

	/** This method is called when the edit state changes. */
	void editStateChanged(boolean inMove);
}
