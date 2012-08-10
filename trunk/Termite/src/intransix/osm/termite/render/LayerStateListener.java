package intransix.osm.termite.render;

import java.util.List;

/**
 * This is used to listener for changes to the layer state.
 * 
 * @author sutter
 */
public interface LayerStateListener {
	/** This method notifies when a layer changes state.
	 * 
	 * @param layer		The layer that changed.
	 * @param layers	The list of all layers.
	 */
	void layerStateChanged(MapLayer layer, List<MapLayer> layers);
}
