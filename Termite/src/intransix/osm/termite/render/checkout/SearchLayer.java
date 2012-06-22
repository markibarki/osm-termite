package intransix.osm.termite.render.checkout;

import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.MapLayer;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.*;
import java.awt.geom.*;

/**
 *
 * @author sutter
 */
public class SearchLayer implements MapLayer, MouseListener, MouseMotionListener {
	
	private final static Color FILL_COLOR = new Color(0,0,255,64);
	private final static Color STROKE_COLOR = new Color(0,0,255,196);
	private final static float STROKE_WIDTH = 2;
	
	private MapPanel mapPanel;
	private Stroke stroke = new BasicStroke(STROKE_WIDTH);
	
	Rectangle2D selection = null;
	
	public Rectangle2D getSelection() {
		return selection;
	}
	
	@Override 
	public void setMapPanel(MapPanel mapPanel) {
		this.mapPanel = mapPanel;
		mapPanel.addMouseListener(this);
		mapPanel.addMouseMotionListener(this);
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform localToPixels = mapPanel.getLocalToPixels();
		if(selection != null) {
//			g2.transform(localToPixels);
//			
//			float zoomScale = (float)mapPanel.getZoomScalePixelsPerMeter();
//			if(strokeScale != zoomScale) {
//				stroke = new BasicStroke(STROKE_WIDTH/zoomScale);
//				strokeScale = zoomScale;
//			}
			Shape shape = localToPixels.createTransformedShape(selection);
			
			g2.setPaint(FILL_COLOR);
			g2.fill(shape);
			g2.setStroke(stroke);
			g2.setColor(STROKE_COLOR);
			g2.draw(shape);
		}
	}
	
	//-------------------------
	// Mouse Events
	//-------------------------
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mouseDragged(MouseEvent e) {
		Point2D point = getLocalPoint(e.getX(),e.getY());
		if(selection == null) {
			selection = new Rectangle2D.Double(point.getX(),point.getY(),0,0);
		}
		else {
			selection.add(point);
		}
		mapPanel.repaint();
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}
	
	public void mouseMoved(MouseEvent e) {
		
	}
	
	public void mousePressed(MouseEvent e) {
		selection = null;
		mapPanel.repaint();
	}
	
	public void mouseReleased(MouseEvent e) {
		Point2D point = getLocalPoint(e.getX(),e.getY());
		if(selection != null) {
			selection.add(point);
			mapPanel.repaint();
		}
	}
	
	private Point2D getLocalPoint(int pixX, int pixY) {
		Point2D point = new Point2D.Double(pixX,pixY);
		AffineTransform pixelsToMap = mapPanel.getPixelsToLocal();
		pixelsToMap.transform(point, point);
		return point;
	}
}
