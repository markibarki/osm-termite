package intransix.osm.termite.app.gui;

import intransix.osm.termite.map.geom.TermiteLevel;
import intransix.osm.termite.map.geom.TermiteFeature;
import intransix.osm.termite.map.geom.TermiteStructure;
import intransix.osm.termite.map.geom.TermiteData;
import intransix.osm.termite.map.geom.FeatureLevelGeom;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

import intransix.osm.termite.theme.*;

/**
 *
 * @author sutter
 */
public class MapPanel extends JPanel {
	
	public final static int DEFAULT_ZLEVEL = 0;
	
	private Theme theme;
	private TermiteData map;
	private TermiteStructure currentStructure;
	private TermiteLevel currentLevel;
	
	private AffineTransform mapToPixels = new AffineTransform();
	private AffineTransform pixelsToMap = new AffineTransform();
	
	public MapPanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
    }
	
Rectangle2D bounds = null;
public void setBounds(Rectangle2D bounds) {
this.bounds = bounds;
	Dimension dim = this.getPreferredSize();
	double xScale = dim.width / bounds.getWidth();
	double yScale = dim.height / bounds.getHeight();
	double scale = (xScale > yScale) ? yScale : xScale;
	
	double xOffset = bounds.getMinX();
	double yOffset = bounds.getMinY();
double[] mat = new double[6];
mapToPixels.getMatrix(mat);
	mapToPixels.scale(scale, scale);
mapToPixels.getMatrix(mat);
	mapToPixels.translate(-xOffset,-yOffset);
mapToPixels.getMatrix(mat);
	try {
		pixelsToMap = mapToPixels.createInverse();
	}
	catch(Exception ex) {
		//shouldn't happen
	}
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
	
	public void setLevel(int zlevel) {
		if(currentStructure == null) return;
		currentLevel = currentStructure.lookupLevel(zlevel);
	}
	
//	@Override
//	public Dimension getPreferredSize() {
//        return new Dimension(250,200);
//    }
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		//make sure antialiasing is on
		RenderingHints rh = g2.getRenderingHints();
		if((!rh.containsValue(RenderingHints.KEY_ANTIALIASING))||(rh.get(RenderingHints.KEY_ANTIALIASING) != RenderingHints.VALUE_ANTIALIAS_ON)) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		if((currentLevel == null)||(theme == null)) return;
		
float zoomScale = (float)Math.sqrt(mapToPixels.getDeterminant());
		
		g2.transform(mapToPixels);		
		
		if(bounds != null) {
			g2.setColor(Color.cyan);
			g2.fill(bounds);
		}
			
		for(FeatureLevelGeom geom:currentLevel.getLevelGeom()) {
			//render geometry
			geom.render(g2,zoomScale,theme);
		}
		

		
		g2.transform(pixelsToMap);
	}
	
}
