package intransix.osm.termite.app.edit;

/**
 * This interface is used for notifications of edit state changes.
 * 
 * @author sutter
 */
public interface EditStateListener {

	void editStateChanged(boolean inMove);
}
