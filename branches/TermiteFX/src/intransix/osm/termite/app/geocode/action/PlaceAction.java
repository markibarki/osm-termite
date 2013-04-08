package intransix.osm.termite.app.geocode.action;

import intransix.osm.termite.gui.mode.source.GeocodeEditorMode;
import intransix.osm.termite.app.geocode.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javafx.scene.input.MouseEvent;

/**
 * This method is used to place an anchor point in geocoding. 
 * 
 * @author sutter
 */
public class PlaceAction implements GeocodeMouseAction {
	
	//=====================
	// Properties
	//=====================
	
	private GeocodeManager geocodeManager;
	private GeocodeEditorMode geocodeEditorMode;
	
	//=====================
	// Public Methods
	//=====================
	
	public PlaceAction(GeocodeManager geocodeManager, 
			GeocodeEditorMode geocodeEditorMode) {
		this.geocodeManager = geocodeManager;
		this.geocodeEditorMode = geocodeEditorMode;
	}
	
	/** This should return false if these if no move action. */
	@Override
	public boolean doMove() {
		return false;
	}
	
	@Override
	public void mouseMoved(Point2D mouseMerc, double mercPerPixelsScale, MouseEvent e) {}
	
	@Override
	public void mousePressed(Point2D mouseMerc, double mercPerPixelsScale, MouseEvent e) {
		int pointIndex = geocodeEditorMode.getPlacementPointIndex();
		
		AnchorPoint[] anchorPoints = geocodeManager.getAnchorPoints();
		AnchorPoint anchorPoint = anchorPoints[pointIndex];
		anchorPoint.mercPoint = mouseMerc;
		anchorPoint.imagePoint = new Point2D.Double();
		
		AffineTransform mercToImage = geocodeManager.getMercToImage();
		mercToImage.transform(mouseMerc,anchorPoint.imagePoint);
		
		//in trhee point mode, update the point types if needed
		if(geocodeEditorMode.getGeocodeType() == GeocodeEditorMode.GeocodeType.THREE_POINT_ORTHO) {
			AnchorPoint.setScalePointTypes(anchorPoints[1], anchorPoints[2]);
		}
		
		//exit the move after a click
		geocodeManager.getGeocodeEditorMode().setLayerState(GeocodeEditorMode.LayerState.SELECT);
		
		anchorPoint.setIsActive(true);
		geocodeManager.anchorPointsUpdated();
//		geocodeLayer.notifyContentChange();
	}
	
}
