package intransix.osm.termite.render.edit;

import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.map.model.TermiteLevel;
import intransix.osm.termite.map.model.TermiteNode;
import intransix.osm.termite.map.model.TermiteWay;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.structure.PathFeature;
import intransix.osm.termite.render.structure.PointFeature;
import intransix.osm.termite.map.theme.Theme;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.*;

/**
 *
 * @author sutter
 */
public class EditLayer implements MapLayer, MouseListener, MouseMotionListener {
	
	private final static double RADIUS_METERS = .5; 
	
	private final static Color SELECT_COLOR = Color.RED;
	private final static Color HIGHLIGHT_COLOR = Color.MAGENTA;
	private final static Color BACKGROUND_HIGHLIGHT_COLOR = Color.PINK;
	
	private TermiteLevel currentLevel;
	private MapPanel mapPanel;
	
	private OsmNode activeNode = null;
	
	public void setLevel(TermiteLevel level) {
		this.currentLevel = level;
	}
	
	@Override 
	public void setMapPanel(MapPanel mapPanel) {
		this.mapPanel = mapPanel;
		mapPanel.addMouseListener(this);
		mapPanel.addMouseMotionListener(this);
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform mapToPixels = mapPanel.getMapToPixels();
		g2.transform(mapToPixels);		
		
		OsmNode localNode = activeNode;
		if(localNode != null) {
			g2.setColor(HIGHLIGHT_COLOR);
			Shape rect = new Rectangle2D.Double(localNode.getX()-RADIUS_METERS,
					localNode.getY()-RADIUS_METERS,2*RADIUS_METERS,2*RADIUS_METERS);
			g2.fill(rect);
		}
	}
	
	//-------------------------
	// Mouse Events
	//-------------------------
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mouseDragged(MouseEvent e) {
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
		activeNode = null;
	}
	
	public void mouseMoved(MouseEvent e) {
		//read mouse location
		double pixX = e.getX();
		double pixY = e.getY();
		Point2D point = new Point2D.Double(pixX,pixY);
		AffineTransform pixelsToMap = mapPanel.getPixelsToMap();
		pixelsToMap.transform(point, point);
		
		//loook for a point
		for(TermiteNode tNode:currentLevel.getNodes()) {
			OsmNode oNode = tNode.getOsmObject();
			double d = point.distance(oNode.getX(),oNode.getY());
			if(d < RADIUS_METERS) {
				this.activeNode = oNode;
				mapPanel.repaint();
				return;
			}
		}
		if(activeNode != null) {
			activeNode = null;
			mapPanel.repaint();
		}
	}
	
	public void mousePressed(MouseEvent e) {
	}
	
	public void mouseReleased(MouseEvent e) {
	}
}
