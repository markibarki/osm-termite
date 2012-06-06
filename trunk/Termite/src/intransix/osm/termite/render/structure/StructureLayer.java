package intransix.osm.termite.render.structure;

import intransix.osm.termite.map.model.TermiteStructure;
import intransix.osm.termite.map.model.TermiteData;
import intransix.osm.termite.map.model.TermiteWay;
import intransix.osm.termite.map.model.TermiteLevel;
import intransix.osm.termite.map.model.TermiteNode;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.theme.Theme;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

/**
 *
 * @author sutter
 */
public class StructureLayer implements MapLayer {
	
	public final static int DEFAULT_ZLEVEL = 0;
	
	private Theme theme;
	private TermiteData map;
	private TermiteStructure currentStructure;
	private TermiteLevel currentLevel;
	private MapPanel mapPanel;
	
	public void setMapPanel(MapPanel mapPanel) {
		this.mapPanel = mapPanel;
	}
	
	public void setTheme(Theme theme) {
		this.theme = theme;
	}
	
	public void setMap(TermiteData data) {
		this.map = data;
	}
	
	public void setStructure(long id) {
		currentStructure = map.getStructure(id);
		setLevel(DEFAULT_ZLEVEL);
	}
	
	public TermiteStructure getCurrentStructure() {
		return currentStructure;
	}
	
	public void setLevel(int zlevel) {
		if(currentStructure == null) return;
		currentLevel = currentStructure.lookupLevel(zlevel);
	}
	
	public TermiteLevel getCurrentLevel() {
		return currentLevel;
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		TermiteLevel localLevel = currentLevel;
		Theme localTheme = theme;
		
		AffineTransform mapToPixels = mapPanel.getMapToPixels();
		double zoomScale = mapPanel.getZoomScale();
		
		if((localLevel == null)||(localTheme == null)) return;
		
		AffineTransform originalTransform = g2.getTransform();
		
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
		
		g2.setTransform(originalTransform);
	}
	
	
	//=================================
	// Private Methods
	//=================================

}
