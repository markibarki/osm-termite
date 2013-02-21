/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.map;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.maplayer.MapLayerListener;
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

/**
 *
 * @author sutter
 */
public class MapPane extends Pane implements MapLayerListener {
	
public TermiteFXGui gui;
	
	private ViewRegionManager viewRegionManager;
	private ArrayList<Node> workingList = new ArrayList<>();
	
	public void init(ViewRegionManager viewRegionManager) {
		this.viewRegionManager = viewRegionManager;
		
		//key handlers for navigation
		gui.addEventFilter(KeyEvent.KEY_PRESSED,new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				EventTarget target = e.getTarget();
				System.out.println("Target: " + e.getTarget());
				if(target instanceof Control) {
					System.out.println("Event passed through");
				}
				else {
					System.out.println(e.getCode());
					keyPressed(e);
					e.consume();
				}
			}
		});
		
//		this.addEventHandler(KeyEvent.KEY_PRESSED,new EventHandler<KeyEvent>() {
//			@Override
//			public void handle(KeyEvent e) {
//System.out.println(e.getCode());
//				keyPressed(e);
//			}
//		});

		
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
		getChildren().setAll(workingList);
		workingList.clear();
	}
	
	private final double PAN_PIX = 30;
	private final double ZOOM_FACTOR = 1.3;
	
	private void keyPressed(KeyEvent e) {
		if(viewRegionManager == null) return;
		
		switch(e.getCode()) {
			case UP:
				viewRegionManager.translate(0,PAN_PIX);
				break;
				
			case DOWN:
				viewRegionManager.translate(0,-PAN_PIX);
				break;
				
			case RIGHT:
				viewRegionManager.translate(-PAN_PIX,0);
				break;
				
			case LEFT:
				viewRegionManager.translate(PAN_PIX,0);
				break;
				
			case PAGE_UP:
				viewRegionManager.zoom(ZOOM_FACTOR); 
				break;
			
			case PAGE_DOWN:
				viewRegionManager.zoom(1/ZOOM_FACTOR);
				break;
				
		}
	}
}
