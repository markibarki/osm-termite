package intransix.osm.termite.app.maplayer;

import java.util.*;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.render.MapPanel;


/**
 *
 * @author sutter
 */
public class MapLayerManager {
	
	//======================
	// Properties
	//======================
	
	private ViewRegionManager viewRegionManager;
	
	private List<MapLayer> mapLayers = new ArrayList<MapLayer>();
	
	
	private Comparator<MapLayer> comp = new Comparator<MapLayer>() {
		public int compare(MapLayer ml1, MapLayer ml2) {
			return Double.compare(ml1.getOrder(),ml2.getOrder());
		}
	};
	
	private List<MapLayerListener> layerListeners = new ArrayList<MapLayerListener>();
	
	//////////////////////////////////////////////////
	
	private MapPanel mapPanel;
	
	//======================
	// Public Methods
	//======================
	
	public MapLayerManager(ViewRegionManager viewRegionManager, MapPanel mapPanel) {
		this.viewRegionManager = viewRegionManager;
		this.mapPanel = mapPanel;
	}
	
	public List<MapLayer> getMapLayers() {
		return mapLayers;
	}
	
	public void addLayer(MapLayer layer) {
		if(!mapLayers.contains(layer)) {
			mapLayers.add(layer);
			layer.connect(this,viewRegionManager,mapPanel);
			
			//sort collection
			Collections.sort(mapLayers, comp);
			notifyListChange();
		}
	}
	
	public void removeLayer(MapLayer layer) {
		mapLayers.remove(layer);
		notifyListChange();
	}
	
	/** This method adds a mode listener. */
	public void addLayerListener(MapLayerListener layerListener) {
		if(!layerListeners.contains(layerListener)) {
			layerListeners.add(layerListener);
		}
	}
	
	/** This method removes a mode listener. */
	public void removeLayerListener(MapLayerListener layerListener) {
		layerListeners.remove(layerListener);
	}
	
	//========================
	// Package Methods
	//========================
	
	void notifyContentChange(MapLayer layer) {
		for(int i = 0; i < layerListeners.size(); i++) {
			MapLayerListener layerListener = layerListeners.get(i);
			layerListener.layerContentChanged(layer);
		}
	}
	
	void notifyStateChange(MapLayer layer) {
		for(int i = 0; i < layerListeners.size(); i++) {
			MapLayerListener layerListener = layerListeners.get(i);
			layerListener.layerStateChanged(layer);
		}
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
