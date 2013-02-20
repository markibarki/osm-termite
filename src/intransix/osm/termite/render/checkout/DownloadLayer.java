package intransix.osm.termite.render.checkout;

import intransix.osm.termite.app.maplayer.CanvasLayer;
//import java.awt.Graphics2D;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.event.MouseMotionListener;
//import java.awt.geom.AffineTransform;
//import java.awt.*;
//import java.awt.event.*;
//import java.awt.geom.*;

/**
 *
 * @author sutter
 */
public class DownloadLayer extends CanvasLayer {
	
//	private final static Color FILL_COLOR = new Color(0,0,255,64);
//	private final static Color STROKE_COLOR = new Color(0,0,255,196);
//	private final static float STROKE_WIDTH = 2;
//
//	private Stroke stroke = new BasicStroke(STROKE_WIDTH);
//	
//	private Rectangle2D selection = null;
//	private Point2D startPoint = null;
//	private boolean selecting = false;
//	
//	public Rectangle2D getSelectionMercator() {
//		return selection;
//	}
//	
//	public DownloadLayer() {
//		this.setName("Checkout Search Layer");
//		this.setOrder(MapLayer.ORDER_EDIT_MARKINGS);
//	}
//	
//	/** This mode sets the edit layer active. */
//	@Override
//	public void setActiveState(boolean isActive) {
//		super.setActiveState(isActive);
////@TODO add event handlers
////		MapPanel mapPanel = this.getMapPanel();
////		if(mapPanel != null) {
////			if(isActive) {
////				mapPanel.addMouseListener(this);
////				mapPanel.addMouseMotionListener(this);
////				mapPanel.addKeyListener(this);
////			}
////			else {
////				mapPanel.removeMouseListener(this);
////				mapPanel.removeMouseMotionListener(this);
////				mapPanel.removeKeyListener(this);
////			}
////		}
//	}
//	
//	@Override
//	public void render(Graphics2D g2) {
//		
//		AffineTransform mercatorToPixels = getViewRegionManager().getMercatorToPixels();
//		if(selection != null) {	
//			Shape shape = mercatorToPixels.createTransformedShape(selection);
//			
//			g2.setPaint(FILL_COLOR);
//			g2.fill(shape);
//			g2.setStroke(stroke);
//			g2.setColor(STROKE_COLOR);
//			g2.draw(shape);
//		}
//	}
//	
//	/** This method clears the selection. */
//	public void clearSelection() {
//		startPoint = null;
//		selection = null;
//		selecting = false;
//		this.notifyContentChange();
//	}
//	
//	//-------------------------
//	// Mouse Events
//	//-------------------------
//	
//	public void mouseClicked(MouseEvent e) {
//		if(e.getButton() == MouseEvent.BUTTON1) {
//			Point2D point = getMercatorPoint(e.getX(),e.getY());
//			if(!selecting) {
//				startPoint = point;
//				selection = new Rectangle2D.Double(point.getX(),point.getY(),0,0);
//				selecting = true;
//			}
//			else if(selection != null) {
//				updateSelection(point);
//				selecting = false;
//			}
//		}
//		notifyContentChange();
//	}
//	
//	public void mouseDragged(MouseEvent e) {	
//	}
//	
//	public void mouseEntered(MouseEvent e) {
//	}
//	
//	public void mouseExited(MouseEvent e) {
//	}
//	
//	public void mouseMoved(MouseEvent e) {
//		if((selecting)&&(selection != null)) {
//			Point2D point = getMercatorPoint(e.getX(),e.getY());
//			updateSelection(point);
//			notifyContentChange();
//		}
//	}
//	
//	public void mousePressed(MouseEvent e) {
//		
//	}
//	
//	public void mouseReleased(MouseEvent e) {
//	}
//	
//		
//	// <editor-fold defaultstate="collapsed" desc="Key Listener">
//	
//	/** Handle the key typed event from the text field. */
//    @Override
//	public void keyTyped(KeyEvent e) {
//    }
//
//    /** Handle the key-pressed event from the text field. */
//	@Override
//    public void keyPressed(KeyEvent e) {
//		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//			clearSelection();
//		}
//    }
//
//    /** Handle the key-released event from the text field. */
//    @Override
//	public void keyReleased(KeyEvent e) {
//    }
//	
//	// </editor-fold>
//
//
//	
//	private Point2D getMercatorPoint(int pixX, int pixY) {
//		Point2D point = new Point2D.Double(pixX,pixY);
//		AffineTransform pixelsToMercator = getViewRegionManager().getPixelsToMercator();
//		pixelsToMercator.transform(point, point);
//		return point;
//	}
//	
//	/** This method updates the selection for a new mouse point. */
//	private void updateSelection(Point2D mercPoint) {
//		
//		double x,y,w,h;
//		if(mercPoint.getX() < startPoint.getX()) {
//			x = mercPoint.getX();
//			w = startPoint.getX() - x;
//		}
//		else {
//			x = startPoint.getX();
//			w = mercPoint.getX() - x;
//		}
//		if(mercPoint.getY() < startPoint.getY()) {
//			y = mercPoint.getY();
//			h = startPoint.getY() - y;
//		}
//		else {
//			y = startPoint.getY();
//			h = mercPoint.getY() - y;
//		}
//		selection.setRect(x, y, w, h);
//	}
}
