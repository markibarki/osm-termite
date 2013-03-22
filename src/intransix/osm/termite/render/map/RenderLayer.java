package intransix.osm.termite.render.map;

import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.app.feature.FeatureData;
import intransix.osm.termite.app.filter.FilterListener;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.map.theme.Theme;
import intransix.osm.termite.app.mapdata.MapDataListener;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

/**
 * This class renders the map data.
 * 
 * @author sutter
 */
public class RenderLayer extends MapLayer implements MapDataListener, 
		FilterListener, MapListener {
	
	//====================
	// Properties
	//====================

	static int piggybackIndexRender;
	static int piggybackIndexFeature;
	static {
		piggybackIndexFeature = OsmObject.registerPiggybackUser();
		piggybackIndexRender = OsmObject.registerPiggybackUser();
	}

	private Theme theme;
	
	//feature info
	private FeatureTypeManager featureTypeManager;
	//This list holds the object according to there presentation order as determined
	//by the feature info map
	private java.util.List<Shape> orderedFeatures = new ArrayList<>();
	private FeatureLayerComparator flc = new FeatureLayerComparator();
	
	//local coordinate definitions
	private AffineTransform mercToLocal;
	private double pixelsToLocalScale = 1.0;
	
	//====================
	// Public Methods
	//====================
	
	/** Constructor */
	public RenderLayer() {
		this.setName("Render Layer");
		this.setOrder(MapLayer.ORDER_EDIT_MAP);
	}
	
	@Override
	public void onMapData(MapDataManager mapDataManager, boolean dataPresent) {
		
		//should be active whenever there is map data
		this.setActiveState(dataPresent);
	}
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	@Override
	public void osmDataChanged(MapDataManager mapDataManager, int editNumber) {
		
		//update the feature list
		//update feature info
		OsmData osmData = mapDataManager.getOsmData();
		
		orderedFeatures.clear();
		Shape shapeFeature;
		for(OsmNode node:osmData.getOsmNodes()) {
			checkFeatureInfo(node);
			shapeFeature = extractPointFeature(node);
			orderedFeatures.add(shapeFeature);
		}
		for(OsmWay way:osmData.getOsmWays()) {
			checkFeatureInfo(way);
			shapeFeature = extractWayFeature(way);
			orderedFeatures.add(shapeFeature);
		}
		Collections.sort(orderedFeatures,flc);
		
		this.getChildren().setAll(orderedFeatures);
		orderedFeatures.clear();
	}
	
	/** This returns the priority for this object as a map data listener. */
	@Override
	public int getMapDataListenerPriority() {
		return PRIORITY_DATA_CONSUME;
	}
	
	/** This method is called when the filter changes. */
	@Override
	public void onFilterChanged() {
		throw new RuntimeException("Implement filter change action in rener layer!");
	}	
	
	/** This method sets the feature manager. */
	public void setFeatureTypeManager(FeatureTypeManager featureTypeManager) {
		this.featureTypeManager = featureTypeManager;
	}
	
	/** This method gets the feature manager. */
	public FeatureTypeManager getFeatureTypeManager() {
		return featureTypeManager;
	}
	
	/** Convenience methods for accessing feature data. */
	public static FeatureInfo getObjectFeatureInfo(OsmObject osmObject) {
		FeatureData fd = (FeatureData)osmObject.getPiggybackData(piggybackIndexFeature);
		if(fd != null) {
			return fd.getFeatureInfo();
		}
		else {
			return null;
		}
	}
	
	/** This method sets the theme. IT does not update existing data at the time
	 * of calling. It only apples to data loaded after the call to this function. */
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	
	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onMapViewChange(ViewRegionManager viewRegionManager, boolean zoomChanged) {		
		//update the stroke values
		if(zoomChanged) {
			pixelsToLocalScale = viewRegionManager.getZoomScaleLocalPerPixel();
			for(Node node:getChildren()) {
				if(node instanceof Feature) {
					((Feature)node).setPixelsToLocal(pixelsToLocalScale);
				}
			}
		}
	}
	
	@Override
	public void onPanStart(ViewRegionManager vrm) {}
	
	@Override
	public void onPanStep(ViewRegionManager vrm) {}
	
	@Override
	public void onPanEnd(ViewRegionManager vrm) {}
	
	@Override
	public void onLocalCoordinatesSet(ViewRegionManager vrm) {
		this.mercToLocal = vrm.getMercatorToLocal();
		this.pixelsToLocalScale = vrm.getZoomScaleLocalPerPixel();
		Affine localToMercFX = vrm.getLocalToMercatorFX();
		this.getTransforms().setAll(localToMercFX);
	}
	
	//=================================
	// Private Methods
	//=================================
	
	/** This method makes sure the piggyback feature info is up to date. */
	private void checkFeatureInfo(OsmObject osmObject) {
		FeatureData fd = (FeatureData)osmObject.getPiggybackData(piggybackIndexFeature);
		if(fd == null) {
			fd = new FeatureData();
			osmObject.setPiggybackData(RenderLayer.piggybackIndexFeature,fd);
		}
		if(!fd.isUpToDate(osmObject)) {
			FeatureInfo fi = featureTypeManager.getFeatureInfo(osmObject);
			fd.setFeatureInfo(fi);
		}
	}
	
	/** This method gets the shape object for a node, creating it if needed. */
	private Shape extractPointFeature(OsmNode node) {
		PointFeature feature = null;
		
		ShapePiggybackData data = (ShapePiggybackData)node.getPiggybackData(piggybackIndexRender);
		if(data == null) {
			feature = new PointFeature(node);
			data = new ShapePiggybackData();
			data.setShape(feature);
			node.setPiggybackData(piggybackIndexRender,data);
			feature.initStyle(theme);
			feature.setPixelsToLocal(pixelsToLocalScale);
		}
		
		if(!data.isUpToDate(node)) {
			feature.updateData(mercToLocal);
		}
		
		return data.getShape();
	}
	
	/** This method gets the shape object for a way, creating it if needed. */
	private Shape extractWayFeature(OsmWay way) {
		PathFeature feature = null;
		
		ShapePiggybackData data = (ShapePiggybackData)way.getPiggybackData(piggybackIndexRender);
		if(data == null) {
			feature = new PathFeature(way);
			data = new ShapePiggybackData();
			data.setShape(feature);
			way.setPiggybackData(piggybackIndexRender,data);
			feature.initStyle(theme);
			feature.setPixelsToLocal(pixelsToLocalScale);
		}
		
		if(!data.isUpToDate(way)) {
			feature.updateData(mercToLocal);
		}
		
		return feature;
	}
	
	
	//========================
	// Classes
	//========================
	
	/** This method orders the features for display. */
	private class FeatureLayerComparator implements Comparator<Shape> {
		@Override
		public int compare(Shape o1, Shape o2) {
			//remove sign bit, making negative nubmers larger than positives (with small enough negatives)
			FeatureData fd;
			FeatureInfo fi;
			
			fi = ((Feature)o1).getFeatureInfo();
			int ord1 = (fi != null) ? fi.getZorder() : FeatureInfo.DEFAULT_ZORDER;
			
			fi = ((Feature)o2).getFeatureInfo();
			int ord2 = (fi != null) ? fi.getZorder() : FeatureInfo.DEFAULT_ZORDER;
			return ord1 - ord2;
		}
	}

}
