package intransix.osm.termite.app.gui;

import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

/**
 *
 * @author sutter
 */
public class MapPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener  {
	
	private final static double ROTATION_SCALE_FACTOR = 1.1;
	
	private AffineTransform mapToPixels = new AffineTransform();
	private AffineTransform pixelsToMap = new AffineTransform();
	private ArrayList<Layer> layers = new ArrayList<Layer>();
	private ArrayList<MapListener> mapListeners = new ArrayList<MapListener>();
	private double zoomScale = 1.0;
	
	private boolean panOn = false;
	private double lastX;
	private double lastY;
	
	public MapPanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
    }
	
	public final AffineTransform getMapToPixels() {
		return mapToPixels;
	}
	
	public final AffineTransform getPixelsToMap() {
		return pixelsToMap;
	}
	
	public final double getZoomScale() {
		 return zoomScale;
	}
	
	public void addLayer(Layer layer) {
		this.layers.add(layer);
		layer.setMapPanel(this);
	}
	
	public void removeLayer(Layer layer) {
		this.layers.remove(layer);
	}
	
	public void addMapListener(MapListener listener) {
		this.mapListeners.add(listener);
	}
	
	public void removeMapListener(MapListener listener) {
		this.mapListeners.remove(listener);
	}
	
	public void setBounds(Rectangle2D bounds) {
		Dimension dim = this.getPreferredSize();
		double xScale = dim.width / bounds.getWidth();
		double yScale = dim.height / bounds.getHeight();
		double scale = (xScale > yScale) ? yScale : xScale;

		double xOffset = bounds.getMinX();
		double yOffset = bounds.getMinY();
		mapToPixels.scale(scale, scale);
		mapToPixels.translate(-xOffset,-yOffset);
		updateTransforms();
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
			
		AffineTransform originalTransform = g2.getTransform();
		for(Layer layer:layers) {
			layer.render(g2);
			g2.setTransform(originalTransform);
		}
	}
	
	//-------------------------
	// Mouse Events
	//-------------------------
	
	public void mouseClicked(MouseEvent e) {
//		e.getButton();
//		e.getClickCount();
//		e.getLocationOnScreen();
//		e.getPoint();
//		e.isPopupTrigger();
		System.out.println(e.paramString());
	}
	
	public void mouseDragged(MouseEvent e) {
//		System.out.println(e.paramString());
		if(panOn) {
			panStep(e.getX(),e.getY());
		}
	}
	
	public void mouseEntered(MouseEvent e) {
		System.out.println(e.paramString());
	}
	
	public void mouseExited(MouseEvent e) {
		System.out.println(e.paramString());
		if(panOn) {
			endPan(e.getX(),e.getY());
		}
	}
	
	public void mouseMoved(MouseEvent e) {
//		System.out.println(e.paramString());
		
	}
	
	public void mousePressed(MouseEvent e) {
		System.out.println(e.paramString());
		if(e.getButton() == MouseEvent.BUTTON2) {
			startPan(e.getX(),e.getY());
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		System.out.println(e.paramString());
		if(panOn) {
			endPan(e.getX(),e.getY());
		}
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		System.out.println(e.paramString());
		
		int rotation = e.getWheelRotation();
		double scaleFactor = Math.pow(ROTATION_SCALE_FACTOR,rotation);
		double x = e.getX();
		double y = e.getY();
		
		zoom(scaleFactor,x,y);
	}
	
	public void zoom(double zoomFactor, double x, double y) {
		AffineTransform zt = new AffineTransform();
		zt.translate((1-zoomFactor)*x, (1-zoomFactor)*y);
		zt.scale(zoomFactor, zoomFactor);
		mapToPixels.preConcatenate(zt);
		updateTransforms();
		for(MapListener mapListener:mapListeners) {
			mapListener.onZoom(zoomScale);
		}
		this.repaint();
	}
	
	public void startPan(double x, double y) {
		lastX = x;
		lastY = y;
		panOn = true;
		for(MapListener mapListener:mapListeners) {
			mapListener.onPanStart();
		}
	}
	
	public void endPan(double x, double y) {
		panOn = false;
		for(MapListener mapListener:mapListeners) {
			mapListener.onPanEnd();
		}
	}
	
	public void panStep(double x, double y) {
		translate(x-lastX,y-lastY);
		lastX = x;
		lastY = y;
	}
	
	public void translate(double dx, double dy) {
		AffineTransform zt = new AffineTransform();
		zt.translate(dx,dy);
		mapToPixels.preConcatenate(zt);
		updateTransforms();
		this.repaint();
	}
	
	//=================================
	// Private Methods
	//=================================
	
	private void updateTransforms() {
		zoomScale = Math.sqrt(mapToPixels.getDeterminant());
		try {
			pixelsToMap = mapToPixels.createInverse();
		}
		catch(Exception ex) {
			//should not fail
		}
	}

}
