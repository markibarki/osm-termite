package intransix.osm.termite.app.geocode;

import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.source.GeocodeLayer;
import intransix.osm.termite.render.source.SourceLayer;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author sutter
 */
public class GeocodeManager {
	
	public final static int INVALID_SELECTION = -1;
	
	private GeocodeEditorMode geocodeEditorMode;
	private GeocodeLayer geocodeLayer;	

	private int selection = INVALID_SELECTION;
	
	private SourceLayer sourceLayer;
	
	private AnchorPoint[] anchorPoints;
	private AnchorPoint p0 = new AnchorPoint();
	private AnchorPoint p1 = new AnchorPoint();
	private AnchorPoint p2 = new AnchorPoint();
	
	private AffineTransform imageToMerc;
	private AffineTransform mercToImage;
	private AffineTransform moveImageToMerc = new AffineTransform();
		
	//----------------
	// Mangement
	//----------------
	
	public void init(MapLayerManager mapLayerManager) {
		geocodeLayer = new GeocodeLayer(this);
		geocodeEditorMode = new GeocodeEditorMode(this);
		geocodeLayer.setGeocodeEditorMode(geocodeEditorMode);
		geocodeEditorMode.setGeocodeLayer(geocodeLayer);
		
		mapLayerManager.addLayerListener(geocodeEditorMode);
		
		anchorPoints = new AnchorPoint[3];
		anchorPoints[0] = p0;
		anchorPoints[1] = p1;
		anchorPoints[2] = p2;
	}
	
	public GeocodeEditorMode getGeocodeEditorMode() {
		return geocodeEditorMode;
	}
	
	public GeocodeLayer getGeocodeLayer() {
		return geocodeLayer;
	}
	
	//----------------
	// Geocoding
	//----------------
	
	public void setSourceLayer(SourceLayer sourceLayer) {	
		this.sourceLayer = sourceLayer;
		if(sourceLayer != null) {
			imageToMerc = sourceLayer.getImageToMerc();
			if(imageToMerc != null) {
				updateInverseTransform();
			}
		}
	}

	public AnchorPoint[] getAnchorPoints() {
		return anchorPoints;
	}
	public int getSelection() {
		return selection;
	}
	public void setSelection(int selection) {
		this.selection = selection;
	}
	public AnchorPoint getSelectedAnchorPoint() {
		if(selection != INVALID_SELECTION) {
			return anchorPoints[selection];
		}
		else {
			return null;
		}
	}
	public AffineTransform getMoveImageToMerc() {
		return moveImageToMerc;
	}
	
	/** This method should be called if a move is active and the move transform 
	 * is updated. */
	public void moveImageToMercUpdated() {
		if(sourceLayer != null) {
			sourceLayer.notifyContentChange();
		}
	}
	
	public AffineTransform getImageToMerc() {
		return imageToMerc;
	}
	
	public AffineTransform getMercToImage() {
		return mercToImage;
	}
	
	public void layerActive() {
		//refresh the source layer transformation
		if(this.sourceLayer != null) {
			this.setSourceLayer(sourceLayer);
		}
	}
	
	public void layerInactive() {
		selection = INVALID_SELECTION;
		
		for(AnchorPoint ap:anchorPoints) {
			ap.reset();
		}
	

		imageToMerc = null;
		mercToImage = null;
	}
		
	//=====================
	// Private Methods
	//=====================
	
	public void initMove() {
		if(sourceLayer == null) return;
		
		moveImageToMerc.setTransform(imageToMerc);
		sourceLayer.setMove(true, moveImageToMerc);
	}
	
	public void exitMove() {
		if(sourceLayer == null) return;
		
		sourceLayer.setMove(false,null);
	}
	
	public void executeMove() {
		if(sourceLayer == null) return;
		
		//copy transform
		imageToMerc.setTransform(moveImageToMerc);
		updateInverseTransform();
		for(AnchorPoint ap:anchorPoints) {
			if(ap.mercPoint != null) {
				imageToMerc.transform(ap.imagePoint, ap.mercPoint);
			}
		}
		sourceLayer.setMove(false,null);
		sourceLayer.setImageToMerc(imageToMerc);
	}
	
	private void updateInverseTransform() {
		try {
			mercToImage = imageToMerc.createInverse();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
