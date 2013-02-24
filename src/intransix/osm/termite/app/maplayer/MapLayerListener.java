package intransix.osm.termite.app.maplayer;

import java.util.List;

/**
 * This class is used to notify when a map layer changes.
 * 
 * @author sutter
 */
public interface MapLayerListener {
	
	/** This method is called when the map layer list changes. */
	void layerListChanged(List<MapLayer> mapLayerList);
}
