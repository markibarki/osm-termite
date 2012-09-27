package intransix.osm.termite.app.geocode.action;

import intransix.osm.termite.app.geocode.*;
import intransix.osm.termite.render.source.GeocodeLayer;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

/**
 *
 * @author sutter
 */
public class PlaceAction implements GeocodeMouseAction {
	
	private GeocodeManager geocodeManager;
	private GeocodeEditorMode geocodeEditorMode;
	private GeocodeLayer geocodeLayer;
	
	public PlaceAction(GeocodeManager geocodeManager, 
			GeocodeEditorMode geocodeEditorMode) {
		this.geocodeManager = geocodeManager;
		this.geocodeEditorMode = geocodeEditorMode;
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
		int pointIndex = geocodeEditorMode.getPlacementPointIndex();
		
		AnchorPoint anchorPoint = geocodeManager.getAnchorPoints()[pointIndex];
		anchorPoint.mercPoint = mouseMerc;
		anchorPoint.imagePoint = new Point2D.Double();
		
		AffineTransform mercToImage = geocodeManager.getMercToImage();
		mercToImage.transform(mouseMerc,anchorPoint.imagePoint);
		
		geocodeLayer.notifyContentChange();
	}
	
}
