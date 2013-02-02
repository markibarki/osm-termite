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
import intransix.osm.termite.app.viewregion.LocalCoordinateListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 *
 * @author sutter
 */
public class RenderLayer extends MapLayer implements MapDataListener, 
		LocalCoordinateListener, FilterListener {
	
	public final static int DEFAULT_ZLEVEL = 0;

	static int piggybackIndexRender;
	static int piggybackIndexFeature;
	static {
		piggybackIndexFeature = OsmObject.registerPiggybackUser();
		piggybackIndexRender = OsmObject.registerPiggybackUser();
	}
	
	private MapDataManager mapDataManager;
	private Theme theme;
	
	//feature info
	private FeatureTypeManager featureTypeManager;
	//This list holds the object according to there presentation order as determined
	//by the feature info map
	private java.util.List<OsmObject> orderedFeatures = new ArrayList<OsmObject>();
	private FeatureLayerComparator flc = new FeatureLayerComparator();
	
	public RenderLayer(MapDataManager mapDataManager) {
		this.mapDataManager = mapDataManager;
		this.setName("Render Layer");
		this.setOrder(MapLayer.ORDER_EDIT_MAP);
	}
	
	@Override
	public void onMapData(boolean dataPresent) {
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
		for(OsmNode node:osmData.getOsmNodes()) {
			processFeature(node);
			orderedFeatures.add(node);
		}
		for(OsmWay way:osmData.getOsmWays()) {
			processFeature(way);
			orderedFeatures.add(way);
		}
		Collections.sort(orderedFeatures,flc);
		
		this.notifyContentChange();
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
	
	@Override
	public void render(Graphics2D g2) {
		
		OsmData localData = mapDataManager.getOsmData();
		Theme localTheme = theme;
		
		AffineTransform localToPixels = getViewRegionManager().getLocalToPixels();
		AffineTransform mercatorToLocal = getViewRegionManager().getMercatorToLocal();
		double zoomScalePixelsPerLocal = getViewRegionManager().getZoomScalePixelsPerLocal();
//System.out.println(zoomScalePixelsPerLocal);
		if((localData == null)||(localTheme == null)) return;
		
		g2.transform(localToPixels);		
			
		PathFeature pathFeature;
		PointFeature pointFeature;
		for(OsmObject mapObject:orderedFeatures) {
			if(mapObject instanceof OsmWay) {
				pathFeature = (PathFeature)mapObject.getPiggybackData(piggybackIndexRender);
				if(pathFeature == null) {
					pathFeature = new PathFeature((OsmWay)mapObject);
					mapObject.setPiggybackData(piggybackIndexRender,pathFeature);
				}
				pathFeature.render(g2,mercatorToLocal,zoomScalePixelsPerLocal,localTheme);
			}
			else if(mapObject instanceof OsmNode) {
				pointFeature = (PointFeature)mapObject.getPiggybackData(piggybackIndexRender);
				if(pointFeature == null) {
					pointFeature = new PointFeature((OsmNode)mapObject);
					mapObject.setPiggybackData(piggybackIndexRender,pointFeature);
				}
				pointFeature.render(g2,mercatorToLocal,zoomScalePixelsPerLocal,localTheme);
			}
		}
	}
	
	@Override
	public void onLocalCoordinateChange(ViewRegionManager viewRegionManager, AffineTransform oldLocalToNewLocal) {
		OsmData mapData = mapDataManager.getOsmData();
		if(mapData != null) {
			Feature feature;
			//transform the cached data
			for(OsmObject mapObject:orderedFeatures) {
				feature = (Feature)mapObject.getPiggybackData(piggybackIndexRender);
				if(feature != null) {
					feature.transform(oldLocalToNewLocal);
				}
			}
		}
	}
	
	//=================================
	// Private Methods
	//=================================
	
	
	/** This method updates the feature info for the given object. */
	private void processFeature(OsmObject osmObject) {

		FeatureData featureData = (FeatureData)osmObject.getPiggybackData(piggybackIndexFeature);
		if(featureData == null) {
			featureData = new FeatureData();
			osmObject.setPiggybackData(piggybackIndexFeature, featureData);
		}
		
		if(!featureData.isUpToDate(osmObject)) {
			//feature info
			FeatureInfo featureInfo = featureTypeManager.getFeatureInfo(osmObject);
			featureData.setFeatureInfo(featureInfo);
			
			featureData.markAsUpToDate(osmObject);
		}
	}
	
	//========================
	// Classes
	//========================
	
	private class FeatureLayerComparator implements Comparator<OsmObject> {
		public int compare(OsmObject o1, OsmObject o2) {
			//remove sign bit, making negative nubmers larger than positives (with small enough negatives)
			FeatureData fd;
			FeatureInfo fi;
			
			fd = (FeatureData)o1.getPiggybackData(piggybackIndexFeature);
			fi = fd.getFeatureInfo();
			int ord1 = (fi != null) ? fi.getZorder() : FeatureInfo.DEFAULT_ZORDER;
			
			fd = (FeatureData)o2.getPiggybackData(piggybackIndexFeature);
			fi = fd.getFeatureInfo();
			int ord2 = (fi != null) ? fi.getZorder() : FeatureInfo.DEFAULT_ZORDER;
			return ord1 - ord2;
		}
	}

}
