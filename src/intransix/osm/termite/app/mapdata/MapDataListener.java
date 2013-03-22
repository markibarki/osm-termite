package intransix.osm.termite.app.mapdata;

import java.awt.geom.Rectangle2D;

/**
 * This interface is used to receive notification when map data is loaded or cleared. 
 * 
 * @author sutter
 */
public interface MapDataListener {
	
	public final static int PRIORITY_DATA_MODIFY_1 = 1;
	public final static int PRIORITY_DATA_MODIFY_2 = 2;
	public final static int PRIORITY_DATA_MODIFY_3 = 3;
	public final static int PRIORITY_DATA_CONSUME = 4;

	/** This method is called when the map data is set or cleared. It will be called 
	 * with the value true when the data is set and false when the data is cleared. The
	 * method osmDataChanged is also called when the data is set.
	 * 
	 * @param dataPresent	Set to true if data is present, false if data is cleared.
	 */
	void onMapData(MapDataManager mapDataManager, boolean dataPresent);
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	void osmDataChanged(MapDataManager mapDataManager, int editNumber);
	
	/** This method returns the priority for the listener. It is used to determine
	 * the order the listeners get called.
	 * 
	 * @return				The priority of the map listener
	 */
	int getMapDataListenerPriority();
	
}
