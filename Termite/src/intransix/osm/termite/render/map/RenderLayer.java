package intransix.osm.termite.render.map;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.theme.Theme;
import intransix.osm.termite.app.mapdata.MapDataListener;
import intransix.osm.termite.app.viewregion.LocalCoordinateListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import java.awt.*;
import java.awt.geom.*;

/**
 *
 * @author sutter
 */
public class RenderLayer extends MapLayer implements MapDataListener, 
		LocalCoordinateListener, OsmDataChangedListener {
	
	public final static int DEFAULT_ZLEVEL = 0;
	
	private OsmData mapData;
	private Theme theme;
	
	public RenderLayer() {
		this.setName("Render Layer");
	}
	
	@Override
	public void onMapData(OsmData mapData) {
		if(this.mapData != null) {
			mapData.removeDataChangedListener(this);
		}
		
		this.mapData = mapData;
		//should be active whenever there is map data
		this.setActiveState(mapData != null);
		
		if(this.mapData != null) {
			mapData.addDataChangedListener(this);
		}
		
		this.notifyContentChange();
	}
	
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		OsmData localData = mapData;
		Theme localTheme = theme;
		
		AffineTransform localToPixels = getViewRegionManager().getLocalToPixels();
		AffineTransform mercatorToLocal = getViewRegionManager().getMercatorToLocal();
		double zoomScalePixelsPerLocal = getViewRegionManager().getZoomScalePixelsPerLocal();
//System.out.println(zoomScalePixelsPerLocal);
		if((localData == null)||(localTheme == null)) return;
		
		//make sure the level is sorted
		java.util.List<OsmObject> objectList = mapData.getFeatureList();
		
		g2.transform(localToPixels);		
			
		PathFeature pathFeature;
		PointFeature pointFeature;
		for(OsmObject mapObject:objectList) {
			if(mapObject instanceof OsmWay) {
				pathFeature = (PathFeature)mapObject.getRenderData();
				if(pathFeature == null) {
					pathFeature = new PathFeature((OsmWay)mapObject);
					mapObject.setRenderData(pathFeature);
				}
				pathFeature.render(g2,mercatorToLocal,zoomScalePixelsPerLocal,localTheme);
			}
			else if(mapObject instanceof OsmNode) {
				pointFeature = (PointFeature)mapObject.getRenderData();
				if(pointFeature == null) {
					pointFeature = new PointFeature((OsmNode)mapObject);
					mapObject.setRenderData(pointFeature);
				}
				pointFeature.render(g2,mercatorToLocal,zoomScalePixelsPerLocal,localTheme);
			}
		}
	}
	
	@Override
	public void onLocalCoordinateChange(ViewRegionManager viewRegionManager, AffineTransform oldLocalToNewLocal) {
		if(mapData != null) {
			Feature feature;
			java.util.List<OsmObject> objectList = mapData.getFeatureList();
			//transform the cached data
			for(OsmObject mapObject:objectList) {
				feature = (Feature)mapObject.getRenderData();
				if(feature != null) {
					feature.transform(oldLocalToNewLocal);
				}
			}
		}
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
	
	/** This method returns the type of user this listener is. The type of listener
	 * determines the order in which the listener is called when data has changed. 
	 * 
	 * @return 
	 */
	@Override
	public int getListenerType() {
		return OsmDataChangedListener.LISTENER_CONSUMER;
	}
	
	
	//=================================
	// Private Methods
	//=================================

}
