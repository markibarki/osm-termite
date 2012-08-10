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
public class SearchLayer extends MapLayer implements MouseListener, MouseMotionListener {
	
	private final static Color FILL_COLOR = new Color(0,0,255,64);
	private final static Color STROKE_COLOR = new Color(0,0,255,196);
	private final static float STROKE_WIDTH = 2;

	private Stroke stroke = new BasicStroke(STROKE_WIDTH);
	
	private Rectangle2D selection = null;
	
	public Rectangle2D getSelectionMercator() {
		return selection;
	}
	
	public SearchLayer() {
		this.setName("Checkout Search Layer");
	}
	
	@Override 
	public void setMapPanel(MapPanel mapPanel) {
		super.setMapPanel(mapPanel);
		mapPanel.addMouseListener(this);
		mapPanel.addMouseMotionListener(this);
	}
	
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform mercatorToPixels = getMapPanel().getMercatorToPixels();
		if(selection != null) {	
			Shape shape = mercatorToPixels.createTransformedShape(selection);
			
			g2.setPaint(FILL_COLOR);
			g2.fill(shape);
			g2.setStroke(stroke);
			g2.setColor(STROKE_COLOR);
			g2.draw(shape);
		}
	}
	
	/** This mode sets the edit layer active. */
	@Override
	public void setActiveState(boolean isActive) {
		super.setActiveState(isActive);
		MapPanel mapPanel = this.getMapPanel();
		if(mapPanel != null) {
			if(isActive) {
				mapPanel.addMouseListener(this);
				mapPanel.addMouseMotionListener(this);
			}
			else {
				mapPanel.removeMouseListener(this);
				mapPanel.removeMouseMotionListener(this);
			}
		}
	}
	
	//-------------------------
	// Mouse Events
	//-------------------------
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mouseDragged(MouseEvent e) {	
		if((e.getModifiers() & (MouseEvent.BUTTON1_MASK | MouseEvent.BUTTON1_DOWN_MASK)) != 0) {
			Point2D point = getMercatorPoint(e.getX(),e.getY());
			if(selection == null) {
				selection = new Rectangle2D.Double(point.getX(),point.getY(),0,0);
			} 
			else {
				selection.add(point);
			}
			getMapPanel().repaint();
		}
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}
	
	public void mouseMoved(MouseEvent e) {
		
	}
	
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			selection = null;
			getMapPanel().repaint();
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			Point2D point = getMercatorPoint(e.getX(),e.getY());
			if(selection != null) {
				selection.add(point);
				getMapPanel().repaint();
			}
		}
	}
	
	private Point2D getMercatorPoint(int pixX, int pixY) {
		Point2D point = new Point2D.Double(pixX,pixY);
		AffineTransform pixelsToMercator = getMapPanel().getPixelsToMercator();
		pixelsToMercator.transform(point, point);
		return point;
	}
}
