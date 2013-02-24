/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.map;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.maplayer.MapLayerListener;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Affine;

/**
 * This class is a container for the map. It contains a world pane for which the
 * coordinates correspond to mercator coordinates (with a range 0 to 1). MapLayers
 * are placed on this pane. 
 * 
 * @author sutter
 */
public class MapPane extends Pane implements MapLayerListener, MapListener {
	
	//===================
	// Properties
	//===================
	
	private Pane worldPane;
	
	//===================
	// Public Methods
	//===================
	
	/** Constructor. */
	public MapPane() {
		
		worldPane = new Pane();
		worldPane.setPrefSize(1.0,1.0);
		worldPane.setMinSize(1.0,1.0);
		worldPane.setMaxSize(1.0,1.0);
		this.getChildren().add(worldPane);
		
		//request focus on mouse press
		this.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED,new EventHandler<javafx.scene.input.MouseEvent>() {
			@Override
			public void handle(javafx.scene.input.MouseEvent e) {
				requestFocus();
			}
		});
	}
	
	/** This method is called when the map layer list changes. */
	@Override
	public void layerListChanged(List<MapLayer> mapLayerList) {
		worldPane.getChildren().setAll(mapLayerList);
	}
	
	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onMapViewChange(ViewRegionManager viewRegionManager, boolean zoomChanged) {	
		Affine mercToPixelsFX = viewRegionManager.getMercatorToPixelsFX();
		worldPane.getTransforms().setAll(mercToPixelsFX);
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
