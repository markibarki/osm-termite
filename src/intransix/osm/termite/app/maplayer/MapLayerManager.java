package intransix.osm.termite.app.maplayer;

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
	
	private Pane mapPane;
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
	
	public MapLayerManager(Pane mapPane) {
		this.mapPane = mapPane;
	}
	
	/** This method adds a mouse event handler to the proper parent. */
	public <T extends Event> void addMouseEventHandler(EventType<T> type,EventHandler <? super T> handler) {
		mapPane.addEventHandler(type,handler);
	}
	
	/** This method removes a mouse event handler from the proper parent. */
	public <T extends Event> void removeMouseEventHandler(EventType<T> type,EventHandler <? super T> handler) {
		mapPane.removeEventHandler(type,handler);
	}
	
	/** This method adds a key event handler to the proper parent. */
	public <T extends Event> void addKeyEventHandler(EventType<T> type,EventHandler <? super T> handler) {
		TermiteFXGui.getStage().addEventHandler(type,handler);
	}
	
	/** This method removes a key event handler from the proper parent. */
	public <T extends Event> void removeKeyEventHandler(EventType<T> type,EventHandler <? super T> handler) {
		TermiteFXGui.getStage().addEventHandler(type,handler);
	}
	
	public List<MapLayer> getMapLayers() {
		return mapLayers;
	}
	
	public void addLayer(MapLayer layer) {
		if(!mapLayers.contains(layer)) {
			mapLayers.add(layer);
			layer.connect(this);
			
			//sort collection and update layers
			Collections.sort(mapLayers, comp);
			notifyListChange();
		}
	}
	
	public void removeLayer(MapLayer layer) {
		mapLayers.remove(layer);
		layer.disconnect(this);
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
