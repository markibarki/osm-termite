package intransix.osm.termite.app.geocode;

import intransix.osm.termite.gui.mode.source.GeocodeEditorMode;
import intransix.osm.termite.render.source.AnchorPoint;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.render.source.GeocodeLayer;
import intransix.osm.termite.render.source.SourceLayer;
import java.awt.geom.AffineTransform;


/**
 * This object manages the geocoding state.
 * 
 * @author sutter
 */
public class GeocodeManager {
	
	//==================
	// Properties
	//==================
	
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
		

	//==================
	// Public Methods
	//==================
	
	/** This method initializes the geocode layer. */
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
	
	/** This method retrieves the geocode editor mode. */
	public GeocodeEditorMode getGeocodeEditorMode() {
		return geocodeEditorMode;
	}
	
	/** This method retrieves the geocode layer. */
	public GeocodeLayer getGeocodeLayer() {
		return geocodeLayer;
	}
	
	/** This method sets the source layer that will be geocoded. */
	public void setSourceLayer(SourceLayer sourceLayer) {

//test caching geocode
if(this.sourceLayer != null) {
	this.sourceLayer.storeChanges();
}

		this.sourceLayer = sourceLayer;
		if(sourceLayer != null) {
			imageToMerc = sourceLayer.getImageToMerc();
			if(imageToMerc != null) {
				updateInverseTransform();
			}
		}
	}

	/** This method retrieves the current list of anchor points. */
	public AnchorPoint[] getAnchorPoints() {
		return anchorPoints;
	}
	
	/** This method gets the selcted anchor point. */
	public int getSelection() {
		return selection;
	}
	
	/** This method sets the selected anchor point. */
	public void setSelection(int selection) {
		this.selection = selection;
	}
	
	/** This method gets the selected anchor point. */
	public AnchorPoint getSelectedAnchorPoint() {
		if(selection != INVALID_SELECTION) {
			return anchorPoints[selection];
		}
		else {
			return null;
		}
	}
	
	/** This method gets the AffineTransform relating the current moving image
	 * location to meractor coordinates. */
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
	
	/** This method gets the AffineTransform relating the image pixels to mercator coordinates
	 * for the source image. */
	public AffineTransform getImageToMerc() {
		return imageToMerc;
	}
	
	/** This method gets the AffineTransform relating mercator coordinates
	 * to image pixels for the source image. */
	public AffineTransform getMercToImage() {
		return mercToImage;
	}
	
	/** This method should be called when the geocode layer is made active. */
	public void layerActive() {
		//refresh the source layer transformation
		if(this.sourceLayer != null) {
			this.setSourceLayer(sourceLayer);
		}
	}
	
	/** This method should be called when the geocode layer is made inactive. */
	public void layerInactive() {
		selection = INVALID_SELECTION;

//test caching geocode
if(sourceLayer != null) {
	sourceLayer.storeChanges();
}
		
		for(AnchorPoint ap:anchorPoints) {
			ap.reset();
		}
	

		imageToMerc = null;
		mercToImage = null;
	}
	
	/** This method initializes a move operation. */
	public void initMove() {
		if(sourceLayer == null) return;
		
		moveImageToMerc.setTransform(imageToMerc);
		sourceLayer.setMove(true, moveImageToMerc);
	}
	
	/** This method should be called to end a move operation. */
	public void exitMove() {
		if(sourceLayer == null) return;
		
		sourceLayer.setMove(false,null);
	}
	
	/** This method executes a move on the source image. */
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
	
	//=====================
	// Private Methods
	//=====================
	
	/** This method updates the mercToImgae transform. It should be called whenever
	 * the imageToMerc transform is changed. 
	 */
	private void updateInverseTransform() {
		try {
			mercToImage = imageToMerc.createInverse();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
