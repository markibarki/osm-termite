package intransix.osm.termite.app.geocode.action;

import intransix.osm.termite.gui.mode.source.GeocodeEditorMode;
import intransix.osm.termite.render.source.AnchorPoint;
import intransix.osm.termite.app.geocode.*;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.render.source.GeocodeLayer;
//import java.awt.event.MouseEvent;
//import java.awt.geom.Point2D;


/**
 * This mouse action is used to select an anchor point in geocoding. 
 * @author sutter
 */
public class SelectAction implements GeocodeMouseAction {
	
	//=====================
	// Properties
	//=====================
	
	private GeocodeManager geocodeManager;
//	private ViewRegionManager viewRegionManager;
	private GeocodeLayer geocodeLayer;
	
	//=====================
	// Public Methods
	//=====================
	
	public SelectAction(GeocodeManager geocodeManager) {
		this.geocodeManager = geocodeManager;
	}
	
	@Override
	public void init(GeocodeLayer geocodeLayer) {
		this.geocodeLayer = geocodeLayer;
	}
	
	/** This should return false if these if no move action. */
	@Override
	public boolean doMove() {
		return false;
	}
	
//	@Override
//	public void mouseMoved(Point2D mouseMerc, MouseEvent e) {}
//	
//	@Override
//	public void mousePressed(Point2D mouseMerc, MouseEvent e) {
//		AnchorPoint[] anchorPoints = geocodeManager.getAnchorPoints();
//		double mercPerPixelsScale = viewRegionManager.getZoomScaleMercPerPixels();
//		int cnt = anchorPoints.length;
//		boolean objectSelected = false;
//		for(int i = 0; i < cnt; i++) {
//			AnchorPoint anchorPoint = anchorPoints[i];
//			if(anchorPoint.hitCheck(mouseMerc, mercPerPixelsScale)) {
//				geocodeManager.setSelection(i);
//				objectSelected = true;
//			}
//		}
//		if(!objectSelected) {
//			//if we are still here, nothing was selected
//			geocodeManager.setSelection(GeocodeManager.INVALID_SELECTION);
//		}
//		
//		//exit the move after a click
//		geocodeManager.getGeocodeEditorMode().setLayerState(GeocodeEditorMode.LayerState.SELECT);
//			
//		geocodeLayer.notifyContentChange();
//	}
	
}
