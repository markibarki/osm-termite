package intransix.osm.termite.render.edit;

/**
 * This interface is used for notifications of edit state changes.
 * 
 * @author sutter
 */
public interface EditStateListener {

	void editModeChanged(boolean inMove);
}
