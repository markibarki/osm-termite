package intransix.osm.termite.app.gui;

import intransix.osm.termite.map.geom.TermiteLevel;
import intransix.osm.termite.map.geom.TermiteFeature;
import intransix.osm.termite.map.geom.TermiteStructure;
import intransix.osm.termite.map.geom.TermiteData;
import intransix.osm.termite.map.geom.FeatureLevelGeom;
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
public class StructureLayer implements Layer {
	
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
		currentStructure = map.getTermiteStructure(id,false);
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
	
		
	public void onZoom(double zoomScale) {}
	public void onPanStart() {}
	public void onPanEnd() {}
	
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform mapToPixels = mapPanel.getMapToPixels();
		double zoomScale = mapPanel.getZoomScale();
		
		if((currentLevel == null)||(theme == null)) return;
		
		AffineTransform originalTransform = g2.getTransform();
		
		g2.transform(mapToPixels);		
			
		for(FeatureLevelGeom geom:currentLevel.getLevelGeom()) {
			//render geometry
			geom.render(g2,zoomScale,theme);
		}
		
		g2.setTransform(originalTransform);
	}
	
	
	//=================================
	// Private Methods
	//=================================

}
