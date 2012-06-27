package intransix.osm.termite.render.structure;

import intransix.osm.termite.map.model.*;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.map.theme.Theme;
import intransix.osm.termite.gui.MapDataListener;
import java.awt.*;
import java.awt.geom.*;

/**
 *
 * @author sutter
 */
public class RenderLayer extends MapLayer implements MapDataListener {
	
	public final static int DEFAULT_ZLEVEL = 0;
	
	private TermiteData mapData;
	private Theme theme;
	
	public void onMapData(TermiteData mapData) {
		this.mapData = mapData;
	}
	
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		TermiteData localData = mapData;
		Theme localTheme = theme;
		
		AffineTransform mapToPixels = getMapPanel().getLocalToPixels();
		double zoomScale = getMapPanel().getZoomScalePixelsPerMeter();
		
		if((localData == null)||(localTheme == null)) return;
		
		//make sure the level is sorted
		GraduatedList<TermiteObject> objectList = mapData.getOrderedList();
		
		g2.transform(mapToPixels);		
			
		PathFeature pathFeature;
		PointFeature pointFeature;
		for(java.util.List<TermiteObject> subList:objectList.getLists()) {
			for(TermiteObject mapObject:subList) {
				if(mapObject instanceof TermiteWay) {
					pathFeature = (PathFeature)mapObject.getRenderData();
					if(pathFeature == null) {
						pathFeature = new PathFeature((TermiteWay)mapObject);
						mapObject.setRenderData(pathFeature);
					}
					pathFeature.render(g2,zoomScale,localTheme);
				}
				else if(mapObject instanceof TermiteNode) {
					pointFeature = (PointFeature)mapObject.getRenderData();
					if(pointFeature == null) {
						pointFeature = new PointFeature((TermiteNode)mapObject);
						mapObject.setRenderData(pointFeature);
					}
					pointFeature.render(g2,zoomScale,localTheme);
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
	
	
	//=================================
	// Private Methods
	//=================================

}
