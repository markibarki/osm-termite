package intransix.osm.termite.gui;

import intransix.osm.termite.map.osm.OsmObject;

/**
 * This interface is used to receive notification when a map feature is selected. 
 * 
 * @author sutter
 */
public interface FeatureSelectedListener {
	
	/** This method is called when a map feature is selected. It may be called 
	 * with the value null if a selection is cleared an no new selection is made. 
	 * 
	 * @param feature	The selected map feature
	 */
	void onFeatureSelected(OsmObject feature);
}
