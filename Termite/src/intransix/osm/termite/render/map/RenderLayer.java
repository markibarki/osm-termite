package intransix.osm.termite.render.map;

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
import java.awt.*;
import java.awt.geom.*;

/**
 *
 * @author sutter
 */
public class RenderLayer extends MapLayer implements MapDataListener, 
		LocalCoordinateListener {
	
	public final static int DEFAULT_ZLEVEL = 0;

	static int piggybackIndex;
	static {
		piggybackIndex = OsmObject.registerPiggybackUser();
	}
	
	private MapDataManager mapDataManager;
	private Theme theme;
	
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
		this.notifyContentChange();
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
		
		//make sure the level is sorted
		java.util.List<OsmObject> objectList = mapDataManager.getFeatureList();
		
		g2.transform(localToPixels);		
			
		PathFeature pathFeature;
		PointFeature pointFeature;
		for(OsmObject mapObject:objectList) {
			if(mapObject instanceof OsmWay) {
				pathFeature = (PathFeature)mapObject.getPiggybackData(piggybackIndex);
				if(pathFeature == null) {
					pathFeature = new PathFeature((OsmWay)mapObject);
					mapObject.setPiggybackData(piggybackIndex,pathFeature);
				}
				pathFeature.render(g2,mercatorToLocal,zoomScalePixelsPerLocal,localTheme);
			}
			else if(mapObject instanceof OsmNode) {
				pointFeature = (PointFeature)mapObject.getPiggybackData(piggybackIndex);
				if(pointFeature == null) {
					pointFeature = new PointFeature((OsmNode)mapObject);
					mapObject.setPiggybackData(piggybackIndex,pointFeature);
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
			java.util.List<OsmObject> objectList = mapDataManager.getFeatureList();
			//transform the cached data
			for(OsmObject mapObject:objectList) {
				feature = (Feature)mapObject.getPiggybackData(piggybackIndex);
				if(feature != null) {
					feature.transform(oldLocalToNewLocal);
				}
			}
		}
	}
	
	//=================================
	// Private Methods
	//=================================

}
