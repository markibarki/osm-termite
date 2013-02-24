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
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.maplayer.PaneLayer;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
//import java.awt.*;
//import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

/**
 *
 * @author sutter
 */
public class RenderLayer extends PaneLayer implements MapDataListener, 
		FilterListener, MapListener {
	
	public final static int DEFAULT_ZLEVEL = 0;
	
	/** this arbitrary number is used to set the scale for local coordinates. */
	private final static double LOCAL_COORDINATE_AREA = 1000000.0;

	static int piggybackIndexRender;
	static int piggybackIndexFeature;
	static {
		piggybackIndexFeature = OsmObject.registerPiggybackUser();
		piggybackIndexRender = OsmObject.registerPiggybackUser();
	}
	
	private MapDataManager mapDataManager;
	private Theme theme;
	private double pixelsToLocalScale = 1.0;
	
	//feature info
	private FeatureTypeManager featureTypeManager;
	//This list holds the object according to there presentation order as determined
	//by the feature info map
	private java.util.List<Shape> orderedFeatures = new ArrayList<>();
	private FeatureLayerComparator flc = new FeatureLayerComparator();
	
	//local coordinate definitions
	private AffineTransform mercToLocal;
	private Affine localToMercFX;
	private double localToMercScaleFactor = 1.0;
	
	public void connect(MapLayerManager mapLayerManager){
	}
	
	public void disconnect(MapLayerManager mapLayerManager){
	}
	
	public RenderLayer() {
		this.setName("Render Layer");
		this.setOrder(MapLayer.ORDER_EDIT_MAP);
	}
	
//@TODO Fix setting logic
	public void setMapDataManager(MapDataManager mapDataManager) {
		this.mapDataManager = mapDataManager;
	}
	
	@Override
	public void onMapData(boolean dataPresent) {
		
		if(dataPresent) {
			initializeLocalCoordinates();
		}
		
		//should be active whenever there is map data
		this.setActiveState(dataPresent);
		
		
	}
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	@Override
	public void osmDataChanged(int editNumber) {
		
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
		
		this.notifyContentChange();
	}
	
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
	
	/** This returns the priority for this object as a map data listener. */
	@Override
	public int getMapDataListenerPriority() {
		return PRIORITY_DATA_CONSUME;
	}
	
	@Override
	public void onFilterChanged() {
		//manually update the render layer with the new filter
		this.notifyContentChange();
	}	
	
	public void setFeatureTypeManager(FeatureTypeManager featureTypeManager) {
		this.featureTypeManager = featureTypeManager;
	}
	
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
	
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	
//	@Override
//	public void render(Graphics2D g2) {
//		
//		OsmData localData = mapDataManager.getOsmData();
//		Theme localTheme = theme;
//		
//		AffineTransform localToPixels = getViewRegionManager().getLocalToPixels();
//		AffineTransform mercatorToLocal = getViewRegionManager().getMercatorToLocal();
//		double zoomScalePixelsPerLocal = getViewRegionManager().getZoomScalePixelsPerLocal();
////System.out.println(zoomScalePixelsPerLocal);
//		if((localData == null)||(localTheme == null)) return;
//		
//		g2.transform(localToPixels);		
//			
//		PathFeature pathFeature;
//		PointFeature pointFeature;
//		for(OsmObject mapObject:orderedFeatures) {
//			if(mapObject instanceof OsmWay) {
//				pathFeature = (PathFeature)mapObject.getPiggybackData(piggybackIndexRender);
//				if(pathFeature == null) {
//					pathFeature = new PathFeature((OsmWay)mapObject);
//					mapObject.setPiggybackData(piggybackIndexRender,pathFeature);
//				}
//				pathFeature.render(g2,mercatorToLocal,zoomScalePixelsPerLocal,localTheme);
//			}
//			else if(mapObject instanceof OsmNode) {
//				pointFeature = (PointFeature)mapObject.getPiggybackData(piggybackIndexRender);
//				if(pointFeature == null) {
//					pointFeature = new PointFeature((OsmNode)mapObject);
//					mapObject.setPiggybackData(piggybackIndexRender,pointFeature);
//				}
//				pointFeature.render(g2,mercatorToLocal,zoomScalePixelsPerLocal,localTheme);
//			}
//		}
//	}
//	
//	@Override
//	public void onLocalCoordinateChange(ViewRegionManager viewRegionManager, AffineTransform oldLocalToNewLocal) {
//		OsmData mapData = mapDataManager.getOsmData();
//		if(mapData != null) {
//			Feature feature;
//			//transform the cached data
//			for(OsmObject mapObject:orderedFeatures) {
//				feature = (Feature)mapObject.getPiggybackData(piggybackIndexRender);
//				if(feature != null) {
//					feature.transform(oldLocalToNewLocal);
//				}
//			}
//		}
//	}
	
	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onMapViewChange(ViewRegionManager viewRegionManager, boolean zoomChanged) {		
		//update the stroke values
		if(zoomChanged) {
			double pixelsToMercScale = viewRegionManager.getZoomScaleMercPerPixel();
			pixelsToLocalScale = pixelsToMercScale / localToMercScaleFactor;
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
	
	//=================================
	// Private Methods
	//=================================
	
	
	
//	/** This method updates the feature info for the given object. */
//	private void processFeature(OsmObject osmObject) {
//
//		FeatureData featureData = (FeatureData)osmObject.getPiggybackData(piggybackIndexFeature);
//		if(featureData == null) {
//			featureData = new FeatureData();
//			osmObject.setPiggybackData(piggybackIndexFeature, featureData);
//		}
//		
//		if(!featureData.isUpToDate(osmObject)) {
//			//feature info
//			FeatureInfo featureInfo = featureTypeManager.getFeatureInfo(osmObject);
//			featureData.setFeatureInfo(featureInfo);
//			
//			featureData.markAsUpToDate(osmObject);
//		}
//	}
	
	private void initializeLocalCoordinates() {
		//get the rectangle
		Rectangle2D downloadRectangle = mapDataManager.getDownloadBounds();
		//find the local scale
		double mercArea = downloadRectangle.getWidth()* downloadRectangle.getHeight();
		localToMercScaleFactor = 1;
		if(mercArea > 0) {
			localToMercScaleFactor = Math.sqrt(mercArea / LOCAL_COORDINATE_AREA);
		}
		
		//create transforms
		AffineTransform localToMerc = new AffineTransform(localToMercScaleFactor,0.0,
				0.0,localToMercScaleFactor,
				downloadRectangle.getMinX(),downloadRectangle.getMinY());
		try {
			mercToLocal = localToMerc.createInverse();
		}
		catch(Exception ex) {
			//this should not happen
			throw new RuntimeException("Failed transform inverse");
		}

		localToMercFX = Transform.affine(localToMercScaleFactor,0.0,
				0.0,localToMercScaleFactor,
				downloadRectangle.getMinX(),downloadRectangle.getMinY());
		
		
		this.getTransforms().setAll(localToMercFX);
	}
	
	//========================
	// Classes
	//========================
	
	private class FeatureLayerComparator implements Comparator<Shape> {
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
