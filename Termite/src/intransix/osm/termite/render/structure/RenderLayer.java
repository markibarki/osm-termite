package intransix.osm.termite.render.structure;

import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.MapPanel;
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
		
		if((localData == null)||(localTheme == null)) return;
		
		//make sure the level is sorted
		GraduatedList<OsmObject> objectList = mapData.getOrderedList();
		
		g2.transform(localToPixels);		
			
		PathFeature pathFeature;
		PointFeature pointFeature;
		for(java.util.List<OsmObject> subList:objectList.getLists()) {
			for(OsmObject mapObject:subList) {
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
//		for(TermiteWay way:localLevel.getWays()) {
//			//render geometry
//			pathFeature = (PathFeature)way.getRenderData();
//			if(pathFeature == null) {
//				pathFeature = new PathFeature(way);
//				way.setRenderData(pathFeature);
//			}
//			pathFeature.render(g2,zoomScale,localTheme,localLevel);
//		}
//		PointFeature pointFeature;
//		for(TermiteNode node:localLevel.getNodes()) {
//			pointFeature = (PointFeature)node.getRenderData();
//			if(pointFeature == null) {
//				pointFeature = new PointFeature(node);
//				node.setRenderData(pointFeature);
//			}
//			pointFeature.render(g2,zoomScale,localTheme,localLevel);
//		}
	}
	
	@Override
	public void onLocalCoordinateChange(MapPanel mapPanel, AffineTransform oldLocalToNewLocal) {
		if(mapData != null) {
			Feature feature;
			GraduatedList<OsmObject> objectList = mapData.getOrderedList();
			//transform the cached data
			for(java.util.List<OsmObject> subList:objectList.getLists()) {
				for(OsmObject mapObject:subList) {
					feature = (Feature)mapObject.getRenderData();
					if(feature != null) {
						feature.transform(oldLocalToNewLocal);
					}
				}
			}
		}
	}
	
	
	//=================================
	// Private Methods
	//=================================

}
