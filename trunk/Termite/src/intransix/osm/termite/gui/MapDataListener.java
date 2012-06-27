package intransix.osm.termite.gui;

import intransix.osm.termite.map.model.TermiteData;

/**
 * This interface is used to receive notification when map data is loaded or cleared. 
 * 
 * @author sutter
 */
public interface MapDataListener {

	/** This method is called when the map data is set of cleared. It will be called 
	 * with the value null when the data is cleared. 
	 * 
	 * @param feature	The selected map feature
	 */
	void onMapData(TermiteData mapData);
}
