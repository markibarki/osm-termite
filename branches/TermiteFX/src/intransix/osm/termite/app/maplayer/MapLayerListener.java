package intransix.osm.termite.app.maplayer;

import java.util.List;

/**
 * This class is used to notify when a map layer changes.
 * 
 * @author sutter
 */
public interface MapLayerListener {
	
	/** This method is called when the map layer state changes, including enable,
	 * visible and opacity. */
	void layerStateChanged(MapLayer mapLayer);
	
	/** This method is called when the content of a layer changes. */
	void layerContentChanged(MapLayer mapLayer);
	
	/** This method is called when the map layer list changes. */
	void layerListChanged(List<MapLayer> mapLayerList);
}
