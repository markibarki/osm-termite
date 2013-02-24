/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.map;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.maplayer.MapLayerListener;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.gui.TermiteFXGui;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Affine;

/**
 *
 * @author sutter
 */
public class MapPane extends Pane implements MapLayerListener, MapListener {
	
	private Pane worldPane;
	private ArrayList<Node> workingList = new ArrayList<>();
	
	public void init(ViewRegionManager viewRegionManager) {
		
		worldPane = new Pane();
		worldPane.setPrefSize(1.0,1.0);
		worldPane.setMinSize(1.0,1.0);
		worldPane.setMaxSize(1.0,1.0);
		this.getChildren().add(worldPane);
		
		//add the view region manager
		this.onMapViewChange(viewRegionManager, true);
		viewRegionManager.addMapListener(this);
		
		//request focus on mouse press
		this.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED,new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override
			public void handle(javafx.scene.input.MouseEvent e) {
				requestFocus();
			}
		});
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
		worldPane.getChildren().setAll(workingList);
		workingList.clear();
	}
	
	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onMapViewChange(ViewRegionManager viewRegionManager, boolean zoomChanged) {	
		Affine mercToPixelsFX = viewRegionManager.getMercatorToPixelsFX();
		this.getTransforms().setAll(mercToPixelsFX);
	}
	
	@Override
	public void onPanStart(ViewRegionManager vrm) {}
	
	@Override
	public void onPanStep(ViewRegionManager vrm) {}
	
	@Override
	public void onPanEnd(ViewRegionManager vrm) {}
	
	//=================================
	// Private Methods
	//=================================
	
	
}
