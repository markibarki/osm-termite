package intransix.osm.termite.map.data;

/**
 * This is a listener for data edits of the OsmData.
 * 
 * @author sutter
 */
public interface OsmDataChangedListener {
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	void osmDataChanged(int editNumber);
	
}
