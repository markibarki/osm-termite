package intransix.osm.termite.gui;

import intransix.osm.termite.map.data.OsmWay;
import intransix.osm.termite.map.data.OsmRelation;

/**
 * This interface is used to receive notification when a map level is selected. 
 * 
 * @author sutter
 */
public interface LevelSelectedListener {
	
	/** This method is called when a map level is selected. It may be called 
	 * with the value null for the level or the level and the structure. 
	 * 
	 * @param structure		The footprint in the outdoor map for the selected level
	 * @param level			The selected level
	 */
	void onLevelSelected(OsmWay structure, OsmRelation level);
}
