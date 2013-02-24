package intransix.osm.termite.app.maplayer;

import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.gui.TermiteFXGui;
import java.util.*;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.layout.Pane;


/**
 *
 * @author sutter
 */
public class MapLayerManager {
	
	//======================
	// Properties
	//======================
	
	private List<MapLayer> mapLayers = new ArrayList<>();
	
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
	
	/** This method adds a map layer. */
	public void addLayer(MapLayer layer) {
		if(!mapLayers.contains(layer)) {
			mapLayers.add(layer);
			layer.connect(this);
			
			//sort collection and update layers
			Collections.sort(mapLayers, comp);
			notifyListChange();
		}
	}
	
	/** This method removes a map layer. */
	public void removeLayer(MapLayer layer) {
		mapLayers.remove(layer);
		layer.disconnect(this);
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
