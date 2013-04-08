package intransix.osm.termite.app.geocode;

import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.gui.mode.source.GeocodeEditorMode;
import intransix.osm.termite.render.source.GeocodeLayer;
import intransix.osm.termite.render.source.SourceLayer;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
//import java.awt.geom.AffineTransform;


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
	
	private SourceLayer activeSourceLayer;
	
	private AnchorPoint[] anchorPoints;
	private AnchorPoint p0 = new AnchorPoint();
	private AnchorPoint p1 = new AnchorPoint();
	private AnchorPoint p2 = new AnchorPoint();
	
	private AffineTransform imageToMerc;
	private AffineTransform mercToImage;
	private AffineTransform moveImageToMerc = new AffineTransform();
	
	//source layer management
	private List<SourceLayer> sourceLayers = new ArrayList<>();
	
	private List<AnchorPointListener> anchorPointListeners = new ArrayList<>();


	//==================
	// Public Methods
	//==================
	
	/** This method initializes the geocode layer. */
	public GeocodeManager() {
		anchorPoints = new AnchorPoint[3];
		anchorPoints[0] = p0;
		anchorPoints[1] = p1;
		anchorPoints[2] = p2;
	}
	
	/** This call should be used to maintain the list of all available source layers. */
	public void addSourceLayer(SourceLayer sourceLayer) {
		this.sourceLayers.add(sourceLayer);
	}
	
	/** This call should be used to maintain the list of all available source layers. */
	public void removeSourceLayer(SourceLayer sourceLayer) {
		this.sourceLayers.remove(sourceLayer);
	}
	
	/** This call returns the list of all available source layers. */
	public List<SourceLayer> getSourceLayers() {
		return sourceLayers;
	}
	
	public void setMode(GeocodeEditorMode geocodeEditorMode) {
		this.geocodeEditorMode = geocodeEditorMode;
	}
	
	public void setGeocodeLayer(GeocodeLayer geocodeLayer) {
		this.geocodeLayer = geocodeLayer;
		this.addAnchorPointListener(geocodeLayer);
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
	public void setActiveSourceLayer(SourceLayer sourceLayer) {

//test caching geocode
if(this.activeSourceLayer != null) {
	this.activeSourceLayer.storeChanges();
}

		this.activeSourceLayer = sourceLayer;
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
		if(activeSourceLayer != null) {
//			sourceLayer.notifyContentChange();
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
		if(this.activeSourceLayer != null) {
			this.setActiveSourceLayer(activeSourceLayer);
		}
	}
	
	/** This method should be called when the geocode layer is made inactive. */
	public void layerInactive() {
		selection = INVALID_SELECTION;

//test caching geocode
if(activeSourceLayer != null) {
	activeSourceLayer.storeChanges();
}
		
		for(AnchorPoint ap:anchorPoints) {
			ap.reset();
		}
		this.anchorPointsUpdated();
	

		imageToMerc = null;
		mercToImage = null;
	}
	
	/** This method initializes a move operation. */
	public void initMove() {
		if(activeSourceLayer == null) return;
		
		moveImageToMerc.setTransform(imageToMerc);
		activeSourceLayer.setMove(true, moveImageToMerc);
	}
	
	/** This method should be called to end a move operation. */
	public void exitMove() {
		if(activeSourceLayer == null) return;
		
		activeSourceLayer.setMove(false,null);
	}
	
	/** This method should be called to update the move preview. */
	public void updateMove() {
		if(activeSourceLayer == null) return;
		
		activeSourceLayer.setMove(true,moveImageToMerc);
	}
	
	/** This method executes a move on the source image. */
	public void executeMove() {
		if(activeSourceLayer == null) return;
		
		//copy transform
		imageToMerc.setTransform(moveImageToMerc);
		updateInverseTransform();
		for(AnchorPoint ap:anchorPoints) {
			if(ap.mercPoint != null) {
				imageToMerc.transform(ap.imagePoint, ap.mercPoint);
			}
		}
		activeSourceLayer.setMove(false,null);
		activeSourceLayer.setImageToMerc(imageToMerc);
	}
	
	/** This adds an anchor point listener. */
	public void addAnchorPointListener(AnchorPointListener listener) {
		anchorPointListeners.add(listener);
	}
	
	/** This removes an anchor point listener. */
	public void removeAnchorPointListener(AnchorPointListener listener) {
		anchorPointListeners.remove(listener);
	}
	
	/** This method should be called if the anchor points are updated. */
	public void anchorPointsUpdated() {
		for(AnchorPointListener listener:anchorPointListeners) {
			listener.anchorPointsChanged(anchorPoints);
		}
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
