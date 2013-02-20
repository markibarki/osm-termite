package intransix.osm.termite.gui.mode.source;

import intransix.osm.termite.app.geocode.GeocodeManager;
import intransix.osm.termite.app.geocode.GeocodeStateListener;
import intransix.osm.termite.render.source.GeocodeToolbar;
import intransix.osm.termite.render.source.AnchorPoint;
import intransix.osm.termite.gui.mode.EditorMode;
import intransix.osm.termite.app.geocode.action.*;
import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.maplayer.MapLayerListener;
import intransix.osm.termite.render.source.*;

import java.util.ArrayList;
import javax.swing.*;
import java.util.List;

/**
 * This is the editor mode for geocoding.
 * 
 * @author sutter
 */
public class GeocodeEditorMode extends EditorMode implements MapLayerListener  {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Geocode Mode";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/globe25.png";
	
	/** This is the submode of geocoding. */
	public enum LayerState {
		INACTIVE,
		SELECT,
		PLACE_P0,
		PLACE_P1,
		PLACE_P2,
		MOVE
	}
	
	/** This is the type of geocoding. */
	public enum GeocodeType {
		TWO_POINT,
		THREE_POINT_ORTHO,
		FREE_TRANSFORM
	}
	
	private GeocodeType geocodeType = GeocodeType.TWO_POINT;
	private LayerState layerState = LayerState.INACTIVE;
	private AnchorPoint.PointType placePointType;
	private int placePointIndex;
	
	private java.util.List<GeocodeStateListener> stateListeners = new ArrayList<GeocodeStateListener>();
	
	List<MapLayer> mapLayerList;
	private GeocodeLayer geocodeLayer;
	private GeocodeManager geocodeManager;
	
	private GeocodeToolbar toolBar = null;
	
	//====================
	// Public Methods
	//====================
	
	/** Constructor */
	public GeocodeEditorMode() {
		this.toolBar = new GeocodeToolbar(this);
		this.addGeocodeStateListener(this.toolBar);
		
		//make this automatically disabled - only enabled manually
		setDataEnabledStates(false,false);
	}
	
//@TODO fixc this!!!
	public void setGeocodeManager(GeocodeManager geocodeManager) {
		this.geocodeManager = geocodeManager;
	}
	
	/** This adds a listener for changes in the geocode state. */
	public void addGeocodeStateListener(GeocodeStateListener stateListener) {
		if(!stateListeners.contains(stateListener)) {
			stateListeners.add(stateListener);
		}
	}
	
	/** This removes a listener for changes in the geocode state. */
	public void removeGeocodeStateListener(GeocodeStateListener stateListener) {
		stateListeners.remove(stateListener);
	}
	
	
	/** This method gets the current geocode type. */
	public GeocodeType getGeocodeType() {
		return geocodeType;
	}
	
	/** This method sets the layer state, which controls which user action is done. */
	public LayerState getLayerState() {
		return layerState;
	}

	/** This method returns the index of the point to place. It is only valid in
	 * one of the anchor placement modes. */
	public int getPlacementPointIndex() {
		return placePointIndex;
	}
	
	/** This method sets the geocode type. */
	public void setGeocodeType(GeocodeType geocodeType) {
		this.geocodeType = geocodeType;
		
		//set the anchor point types
		AnchorPoint[] anchorPoints = geocodeManager.getAnchorPoints();
		switch(geocodeType) {
			case TWO_POINT:
				anchorPoints[0].pointType = AnchorPoint.PointType.TRANSLATE;
				anchorPoints[1].pointType = AnchorPoint.PointType.ROTATE_SCALE_XY;
				anchorPoints[2].reset();
				break;
			
			case THREE_POINT_ORTHO:
				anchorPoints[0].pointType = AnchorPoint.PointType.TRANSLATE;
				AnchorPoint.setScalePointTypes(anchorPoints[1],anchorPoints[2]);
				break;
				
			case FREE_TRANSFORM:
				anchorPoints[0].pointType = AnchorPoint.PointType.FREE_TRANSFORM;
				anchorPoints[1].pointType = AnchorPoint.PointType.FREE_TRANSFORM;
				anchorPoints[2].pointType = AnchorPoint.PointType.FREE_TRANSFORM;
				break;
		}
		
		for(GeocodeStateListener gsl:stateListeners) {
			gsl.geocodeTypeChanged(this.geocodeType);
		}
	}
	
	/** This method gets the layer state. */
	public void setLayerState(LayerState layerState) {
	
		//check if there is no change
		if(layerState == this.layerState) return;
		
		//if we are exiting a move, we must clean up
		if(this.layerState == LayerState.MOVE) {
			geocodeManager.exitMove();
		}
		
		this.layerState = layerState;
	
		//set the mouse actions for geocode layer
		switch(layerState) {
			case INACTIVE:
				geocodeLayer.setMouseAction(null);
				break;
				
			case SELECT:
				geocodeLayer.setMouseAction(new SelectAction(geocodeManager));
				break;
				
			case PLACE_P0:
				geocodeLayer.setMouseAction(new PlaceAction(geocodeManager,this));
				placePointIndex = 0;
				break;
				
			case PLACE_P1:
				geocodeLayer.setMouseAction(new PlaceAction(geocodeManager,this));
				placePointIndex = 1;
				break;
				
			case PLACE_P2:
				geocodeLayer.setMouseAction(new PlaceAction(geocodeManager,this));
				placePointIndex = 2;
				break;
				
			case MOVE:
				geocodeManager.initMove();
				
				MoveAction moveAction = new MoveAction(geocodeManager);
				geocodeLayer.setMouseAction(moveAction);
				moveAction.initMove();
				break;
		}

		for(GeocodeStateListener gsl:stateListeners) {
			gsl.geocodeModeChanged(this.layerState);
		}
	}
	
	/** This method updates the source layer. */
	public void setSourceLayer(SourceLayer sourceLayer) {
		//revert to select mode if we change layers
		this.setLayerState(LayerState.SELECT);
		
		geocodeManager.setSourceLayer(sourceLayer);
	}
	
	//---------------
	// MapLayer Methods
	//---------------
	
	/** This method returns the name of the editor mode. 
	 * 
	 * @return		The name of the editor mode 
	 */
	@Override
	public String getName() {
		return MODE_NAME;
	}
	
	/** This method returns the name for image for the edit mode button on the
	 * edit mode toolbar. 
	 * 
	 * @return		The name of the icon image for this mode. 
	 */
	@Override
	public String getIconImageName() {
		return ICON_NAME;
	}
	
	/** This method is called when the editor mode is turned on. 
	 */
	@Override
	public void turnOn() {
		if(geocodeLayer != null) {
//			geocodeLayer.setActiveState(true);
//			geocodeLayer.setVisible(true);
//			
//			setGeocodeType(GeocodeType.TWO_POINT);
//			setLayerState(LayerState.SELECT);
//			geocodeManager.layerActive();
		}
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	@Override
	public void turnOff() {
		if(geocodeLayer != null) {
//			setLayerState(LayerState.INACTIVE);
//			
//			geocodeLayer.setActiveState(false);
//			geocodeLayer.setVisible(false);
//			geocodeManager.layerInactive();
		}
	}
	
	//---------------
	// Map Layer listener interface
	//---------------
	
	/** This method is called when the map layer state changes, including enable,
	 * visible and opacity. */
	@Override
	public void layerStateChanged(MapLayer mapLayer) {
		//just reload layers
		toolBar.updateLayers(mapLayerList);
	}
	
	/** This method is called when the content of a layer changes. */
	@Override
	public void layerContentChanged(MapLayer mapLayer) {
		//no action
	}
	
	/** This method is called when the map layer list changes. */
	@Override
	public void layerListChanged(List<MapLayer> mapLayerList) {
		this.mapLayerList = mapLayerList;
		toolBar.updateLayers(mapLayerList);
	}
	
	@Override
	public JToolBar getToolBar() {
		return toolBar;
	}
	
	//=======================
	// Package Methods - UPDATE THIS!!!
	//=======================
	
	/** This sets the geocode map layer object. */
	public void setGeocodeLayer(GeocodeLayer geocodeLayer) {
		this.geocodeLayer = geocodeLayer;
	}
	
	//=======================
	// Private Methods
	//=======================
	
}
