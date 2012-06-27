package intransix.osm.termite.render;

import intransix.osm.termite.util.LocalCoordinates;
import intransix.osm.termite.gui.maplayer.MapLayerManagerPane;
import java.util.*;
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
	
	private AffineTransform localToPixels = new AffineTransform();
	private AffineTransform pixelsToLocal = new AffineTransform();
	private java.util.List<MapLayer> layers = new ArrayList<MapLayer>();
	private java.util.List<MapListener> mapListeners = new ArrayList<MapListener>();
	private double zoomScalePixelsPerMeter = 1.0;
	
	private boolean panOn = false;
	private double lastX;
	private double lastY;
	
	
	private MapLayerManagerPane mapLayerManager;
	
	public MapPanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
    }
	
	public void setMapLayerManager(MapLayerManagerPane mapLayerTab) {
		mapLayerManager = mapLayerTab;
	}
	
	public java.util.List<MapLayer> getMapLayers() {
		return layers;
	}
	
	public final AffineTransform getLocalToPixels() {
		return localToPixels;
	}
	
	public final AffineTransform getPixelsToLocal() {
		return pixelsToLocal;
	}
	
	public final double getZoomScalePixelsPerMeter() {
		 return zoomScalePixelsPerMeter;
	}
	
	//map layers///////////////////////////
	
	public void addLayer(MapLayer layer) {
		this.layers.add(layer);
		layer.setMapPanel(this);
	}
	
	public void removeLayer(MapLayer layer) {
		this.layers.remove(layer);
	}
	
	public java.util.List<MapLayer> getLayers() {
		return this.layers;
	}
	
	//end map layers/////////////////////////////
	
	public void addMapListener(MapListener listener) {
		this.mapListeners.add(listener);
	}
	
	public void removeMapListener(MapListener listener) {
		this.mapListeners.remove(listener);
	}
	
	public void setViewBounds(Rectangle2D bounds) {
		Rectangle pixelRect = this.getVisibleRect();
		
		//X and Y will allow different magnifications - take the smaller of the two
		double xScale = pixelRect.width / bounds.getWidth();
		double yScale = pixelRect.height / bounds.getHeight();
		double scale = (xScale > yScale) ? yScale : xScale;
		//calculate the offest so the center is the same
		double xOffset = bounds.getCenterX() - (xScale/scale) * bounds.getWidth()/2;
		double yOffset = bounds.getCenterY() - (yScale/scale) * bounds.getHeight()/2;
		localToPixels.scale(scale, scale);
		localToPixels.translate(-xOffset,-yOffset);
		
		
		Point2D pMin = new Point2D.Double(bounds.getMinX(),bounds.getMinY());
		Point2D pMax = new Point2D.Double(bounds.getMaxX(),bounds.getMaxY());
		localToPixels.transform(pMin, pMin);
		localToPixels.transform(pMax, pMax);
		
		updateTransforms();
	}
	
	public void resetLocalAnchor(double mx, double my) {
		double newAnchorXInOldLocal = LocalCoordinates.mercToLocalX(mx);
		double newAnchorYInOldLocal = LocalCoordinates.mercToLocalY(my);
		double oldScale = LocalCoordinates.getMetersPerMerc();
		
		LocalCoordinates.setLocalAnchor(mx, my);
		
		double newScale = LocalCoordinates.getMetersPerMerc();
		double zoomChange = oldScale/newScale;
		
		localToPixels.translate(newAnchorXInOldLocal,newAnchorYInOldLocal);
		localToPixels.scale(zoomChange, zoomChange);
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
		for(MapLayer layer:layers) {
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
		localToPixels.preConcatenate(zt);
System.out.println("ZoomScale=" + zoomScalePixelsPerMeter);
		updateTransforms();
		for(MapListener mapListener:mapListeners) {
			mapListener.onZoom(zoomScalePixelsPerMeter);
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
		localToPixels.preConcatenate(zt);
		updateTransforms();
		this.repaint();
	}
	
	//=================================
	// Private Methods
	//=================================
	
	private void updateTransforms() {
		zoomScalePixelsPerMeter = Math.sqrt(localToPixels.getDeterminant());
		try {
			pixelsToLocal = localToPixels.createInverse();
		}
		catch(Exception ex) {
			//should not fail
		}
	}

}
