package intransix.osm.termite.render.map;

import intransix.osm.termite.map.data.OsmObject;
import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.data.OsmWay;
import intransix.osm.termite.render.*;
import intransix.osm.termite.map.theme.Theme;
import intransix.osm.termite.gui.MapDataListener;
import intransix.osm.termite.render.LocalCoordinateListener;
import java.awt.*;
import java.awt.geom.*;

/**
 *
 * @author sutter
 */
public class RenderLayer extends MapLayer implements MapDataListener, LocalCoordinateListener {
	
	public final static int DEFAULT_ZLEVEL = 0;
	
	private OsmData mapData;
	private Theme theme;
	
	public RenderLayer(MapLayerManager mapLayerManager) {
		super(mapLayerManager);
		this.setName("Render Layer");
		
		MapPanel mapPanel = getMapPanel();
		mapPanel.addLocalCoordinateListener(this);
	}
	
	public void onMapData(OsmData mapData) {
		this.mapData = mapData;
	}
	
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		OsmData localData = mapData;
		Theme localTheme = theme;
		
		AffineTransform localToPixels = getMapPanel().getLocalToPixels();
		AffineTransform mercatorToLocal = getMapPanel().getMercatorToLocal();
		double zoomScalePixelsPerLocal = getMapPanel().getZoomScalePixelsPerLocal();
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
	public void onLocalCoordinateChange(MapPanel mapPanel, AffineTransform oldLocalToNewLocal) {
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
	
	
	//=================================
	// Private Methods
	//=================================

}
