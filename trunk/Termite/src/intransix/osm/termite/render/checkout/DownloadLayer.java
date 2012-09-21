package intransix.osm.termite.render.checkout;

import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.app.maplayer.MapLayer;
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
public class DownloadLayer extends MapLayer implements MouseListener, MouseMotionListener {
	
	private final static Color FILL_COLOR = new Color(0,0,255,64);
	private final static Color STROKE_COLOR = new Color(0,0,255,196);
	private final static float STROKE_WIDTH = 2;

	private Stroke stroke = new BasicStroke(STROKE_WIDTH);
	
	private Rectangle2D selection = null;
	private Point2D startPoint = null;
	
	public Rectangle2D getSelectionMercator() {
		return selection;
	}
	
	public DownloadLayer() {
		this.setName("Checkout Search Layer");
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
	
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform mercatorToPixels = getViewRegionManager().getMercatorToPixels();
		if(selection != null) {	
			Shape shape = mercatorToPixels.createTransformedShape(selection);
			
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
		if((e.getModifiers() & (MouseEvent.BUTTON1_MASK | MouseEvent.BUTTON1_DOWN_MASK)) != 0) {
			Point2D point = getMercatorPoint(e.getX(),e.getY());
			updateSelection(point);
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
			notifyContentChange();
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			Point2D point = getMercatorPoint(e.getX(),e.getY());
			if(selection != null) {
				updateSelection(point);
			}
		}
	}
	
	private Point2D getMercatorPoint(int pixX, int pixY) {
		Point2D point = new Point2D.Double(pixX,pixY);
		AffineTransform pixelsToMercator = getViewRegionManager().getPixelsToMercator();
		pixelsToMercator.transform(point, point);
		return point;
	}
	
	/** This method updates the selection for a new mouse point. */
	private void updateSelection(Point2D mercPoint) {
		if(selection == null) {
			startPoint = mercPoint;
			selection = new Rectangle2D.Double(mercPoint.getX(),mercPoint.getY(),0,0);
		} 
		else {
			double x,y,w,h;
			if(mercPoint.getX() < startPoint.getX()) {
				x = mercPoint.getX();
				w = startPoint.getX() - x;
			}
			else {
				x = startPoint.getX();
				w = mercPoint.getX() - x;
			}
			if(mercPoint.getY() < startPoint.getY()) {
				y = mercPoint.getY();
				h = startPoint.getY() - y;
			}
			else {
				y = startPoint.getY();
				h = mercPoint.getY() - y;
			}
			selection.setRect(x, y, w, h);
		}
		notifyContentChange();
	}
}
