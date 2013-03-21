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
import javafx.scene.input.MouseEvent;
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
	private ViewRegionManager viewRegionManager;
	
	private boolean panOn;
	private double panStartPixelX;
	private double panStartPixelY;
	private double panLastPixelX;
	private double panLastPixelY;
	
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
		this.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_ENTERED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(javafx.scene.input.MouseEvent e) {
				requestFocus();
			}
		});
		
		//handle mouse drags - and absordb all clicks associated with a drag
		//allow true clicks to flow through to children
		this.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(javafx.scene.input.MouseEvent e) {
				panStartPixelX = e.getSceneX();
				panStartPixelY = e.getSceneY();
			}
		});
		this.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(javafx.scene.input.MouseEvent e) {
				if(panOn) {
					panOn = false;
				}
			}
		});
		this.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(javafx.scene.input.MouseEvent e) {
				if(!e.isStillSincePress()) {
					double mousePixelX = e.getSceneX();
					double mousePixelY = e.getSceneY();
					if(!panOn) {
						viewRegionManager.startPan(panStartPixelX, panStartPixelY);
						panOn = true;
					}
					viewRegionManager.panStep(mousePixelX, mousePixelY);
				}
			}
		});
		
		//consume any mouse click that resulted from a drag, allow true clicks to pass
		this.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED,new EventHandler<MouseEvent>() {
			@Override
			public void handle(javafx.scene.input.MouseEvent e) {
				if(!e.isStillSincePress()) {
					e.consume();
				}
			}
		});
		
		//consume any mouse click that resulted from a drag, allow true clicks to pass
//		this.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_EXITED,new EventHandler<MouseEvent>() {
//			@Override
//			public void handle(javafx.scene.input.MouseEvent e) {
//				System.out.println("Mouse exited: " + e.isStillSincePress());
//				if(panOn) {
//					panOn = false;
//				}
//			}
//		});
		
// mouse event types
//		MouseEvent.MOUSE_CLICKED;
//		MouseEvent.MOUSE_DRAGGED;
//		MouseEvent.MOUSE_ENTERED;
//		MouseEvent.MOUSE_ENTERED_TARGET;
//		MouseEvent.MOUSE_EXITED;
//		MouseEvent.MOUSE_EXITED_TARGET;
//		MouseEvent.MOUSE_MOVED;
//		MouseEvent.MOUSE_PRESSED;
//		MouseEvent.MOUSE_RELEASED;
	}
	
	public void setViewRegionManager(ViewRegionManager viewRegionManager) {
		this.viewRegionManager = viewRegionManager;
		this.onMapViewChange(viewRegionManager, true);
		viewRegionManager.addMapListener(this);
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
