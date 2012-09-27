package intransix.osm.termite.app.geocode.action;

import intransix.osm.termite.app.geocode.*;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.render.source.GeocodeLayer;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;


/**
 *
 * @author sutter
 */
public class SelectAction implements GeocodeMouseAction {
	
	private GeocodeManager geocodeManager;
	private ViewRegionManager viewRegionManager;
	private GeocodeLayer geocodeLayer;
	
	public SelectAction(GeocodeManager geocodeManager, ViewRegionManager viewRegionManager) {
		this.geocodeManager = geocodeManager;
		this.viewRegionManager = viewRegionManager;
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
	
	@Override
	public void mouseMoved(Point2D mouseMerc, MouseEvent e) {}
	
	@Override
	public void mousePressed(Point2D mouseMerc, MouseEvent e) {
		AnchorPoint[] anchorPoints = geocodeManager.getAnchorPoints();
		double mercPerPixelsScale = 1.0 / viewRegionManager.getZoomScalePixelsPerMerc();
		int cnt = anchorPoints.length;
		for(int i = 0; i < cnt; i++) {
			AnchorPoint anchorPoint = anchorPoints[i];
			if(anchorPoint.hitCheck(mouseMerc, mercPerPixelsScale)) {
				geocodeManager.setSelection(i);
				geocodeLayer.notifyContentChange();
				return;
			}
		}
		//if we are still here, nothing was selected
		geocodeManager.setSelection(GeocodeManager.INVALID_SELECTION);
		geocodeLayer.notifyContentChange();
	}
	
}
