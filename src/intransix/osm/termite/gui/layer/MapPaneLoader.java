/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.layer;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.maplayer.MapLayerListener;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * This class loads the active map layers into the map pane.
 * 
 * @author sutter
 */
public class MapPaneLoader implements MapLayerListener {
	
	private Pane mapPane;
	private ArrayList<Node> workingList = new ArrayList<>();
	
	public MapPaneLoader(Pane mapPane) {
		this.mapPane = mapPane;
	}
	
	/** This method is called when the map layer state changes, including enable,
	 * visible and opacity. */
	@Override
	public void layerStateChanged(MapLayer mapLayer) {
	}
	
	/** This method is called when the content of a layer changes. */
	@Override
	public void layerContentChanged(MapLayer mapLayer) {
		
	}
	
	/** This method is called when the map layer list changes. */
	@Override
	public void layerListChanged(List<MapLayer> mapLayerList) {
		workingList.clear();
		for(MapLayer layer:mapLayerList) {
			workingList.add((Node)layer);
		}
		mapPane.getChildren().setAll(workingList);
		workingList.clear();
	}
}
