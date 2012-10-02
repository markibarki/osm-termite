package intransix.osm.termite.app.mapdata;

/**
 * This interface is used to receive notification when map data is loaded or cleared. 
 * 
 * @author sutter
 */
public interface MapDataListener {

	/** This method is called when the map data is set or cleared. It will be called 
	 * with the value true when the data is set and false when the data is cleared. The
	 * method osmDataChanged is also called when the data is set.
	 * 
	 * @param dataPresent	Set to true if data is present, false if data is cleared.
	 */
	void onMapData(boolean dataPresent);
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	void osmDataChanged(int editNumber);
}
