package intransix.osm.termite.app.maplayer;

import intransix.osm.termite.gui.map.MapPane;
import java.util.*;


/**
 *
 * @author sutter
 */
public class MapLayerManager {
	
	//======================
	// Properties
	//======================
	
	private List<MapLayer> mapLayers = new ArrayList<>();
	private MapPane mapPane;
	
	private Comparator<MapLayer> comp = new Comparator<MapLayer>() {
		@Override
		public int compare(MapLayer ml1, MapLayer ml2) {
			return Double.compare(ml1.getOrder(),ml2.getOrder());
		}
	};
	
	private List<MapLayerListener> layerListeners = new ArrayList<>();
	
	//======================
	// Public Methods
	//======================
	
	/** This method gets the list of map layers. */
	public List<MapLayer> getMapLayers() {
		return mapLayers;
	}
	
	/** This should be called to set the world pane into the layer manager. It
	 * is used by layers to provide mouse events. */
	public void setMapPane(MapPane mapPane) {
		this.mapPane = mapPane;
	}
	
	/** This method returns the pane the represents the world. The coordinates
	 * are the Mercator coordinates defined in this project. IT can be used to handle
	 * mouse events tied to a geographic coordinates. */
	public MapPane getMapPane() {
		return mapPane;
	}
	
	/** This method adds a map layer. */
	public void addLayer(MapLayer layer) {
		if(!mapLayers.contains(layer)) {
			mapLayers.add(layer);
			
			//sort collection and update layers
			Collections.sort(mapLayers, comp);
			notifyListChange();
		}
	}
	
	/** This method removes a map layer. */
	public void removeLayer(MapLayer layer) {
		mapLayers.remove(layer);
		notifyListChange();
	}
	
	/** This method adds a layer listener. */
	public void addLayerListener(MapLayerListener layerListener) {
		if(!layerListeners.contains(layerListener)) {
			layerListeners.add(layerListener);
		}
	}
	
	/** This method removes a layer listener. */
	public void removeLayerListener(MapLayerListener layerListener) {
		layerListeners.remove(layerListener);
	}
	
	//======================
	// Private Methods
	//======================
	
	/** This method notifies any listeners the a mode enabled changed. */
	private void notifyListChange() {
		for(MapLayerListener layerListener:layerListeners) {
			layerListener.layerListChanged(mapLayers);
		}
	}
}
