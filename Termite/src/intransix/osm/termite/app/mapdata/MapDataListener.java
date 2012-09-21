package intransix.osm.termite.app.mapdata;

import intransix.osm.termite.map.data.OsmData;

/**
 * This interface is used to receive notification when map data is loaded or cleared. 
 * 
 * @author sutter
 */
public interface MapDataListener {

	/** This method is called when the map data is set of cleared. It will be called 
	 * with the value null when the data is cleared. 
	 * 
	 * @param mapData	The map data object
	 */
	void onMapData(OsmData mapData);
}
