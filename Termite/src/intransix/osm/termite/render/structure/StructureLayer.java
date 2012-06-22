package intransix.osm.termite.render.structure;

import intransix.osm.termite.map.model.TermiteWay;
import intransix.osm.termite.map.model.TermiteLevel;
import intransix.osm.termite.map.model.TermiteNode;
import intransix.osm.termite.map.model.TermiteObject;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.map.theme.Theme;
import java.awt.*;
import java.awt.geom.*;

/**
 *
 * @author sutter
 */
public class StructureLayer implements MapLayer {
	
	public final static int DEFAULT_ZLEVEL = 0;
	
	private Theme theme;
	private TermiteLevel currentLevel;
	private MapPanel mapPanel;
	
	public void setMapPanel(MapPanel mapPanel) {
		this.mapPanel = mapPanel;
	}
	
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	
	public void setLevel(TermiteLevel level) {
		currentLevel = level;
	}
	
	public TermiteLevel getLevel() {
		return currentLevel;
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		TermiteLevel localLevel = currentLevel;
		Theme localTheme = theme;
		
		AffineTransform mapToPixels = mapPanel.getLocalToPixels();
		double zoomScale = mapPanel.getZoomScalePixelsPerMeter();
		
		if((localLevel == null)||(localTheme == null)) return;
		
		//make sure the level is sorted
		localLevel.checkFeatureSort();
		
		g2.transform(mapToPixels);		
			
		PathFeature pathFeature;
		for(TermiteWay way:localLevel.getWays()) {
			//render geometry
			pathFeature = (PathFeature)way.getRenderData();
			if(pathFeature == null) {
				pathFeature = new PathFeature(way);
				way.setRenderData(pathFeature);
			}
			pathFeature.render(g2,zoomScale,localTheme,localLevel);
		}
		PointFeature pointFeature;
		for(TermiteNode node:localLevel.getNodes()) {
			pointFeature = (PointFeature)node.getRenderData();
			if(pointFeature == null) {
				pointFeature = new PointFeature(node);
				node.setRenderData(pointFeature);
			}
			pointFeature.render(g2,zoomScale,localTheme,localLevel);
		}
	}
	
	
	//=================================
	// Private Methods
	//=================================

}
