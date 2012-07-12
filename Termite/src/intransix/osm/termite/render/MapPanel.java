package intransix.osm.termite.render;

import intransix.osm.termite.gui.maplayer.MapLayerManagerPane;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import intransix.osm.termite.map.data.OsmDataChangedListener;

/**
 *
 * @author sutter
 */
public class MapPanel extends JPanel implements OsmDataChangedListener,
		MouseListener, MouseMotionListener, MouseWheelListener  {
	
	//zoom factor for a mouse wheel click
	private final static double ROTATION_SCALE_FACTOR = 1.1;
	
	//max zoom before redefining local coordinates
	private final static double LOCAL_COORD_RESET_ZOOM = 2;
	
	//transforms
	private AffineTransform localToPixels = new AffineTransform();
	private AffineTransform pixelsToLocal = new AffineTransform();
	private AffineTransform mercatorToLocal = new AffineTransform();
	private AffineTransform localToMercator = new AffineTransform();
	private AffineTransform mercatorToPixels = new AffineTransform();
	private AffineTransform pixelsToMercator = new AffineTransform();
	
	private double zoomScalePixelsPerMerc = 1.0;
	private double zoomScalePixelsPerLocal = 1.0;
	private double zoomScaleLocalPerMerc = 1.0;
	
	private java.util.List<MapLayer> layers = new ArrayList<MapLayer>();
	
	private java.util.List<MapListener> mapListeners = new ArrayList<MapListener>();
	private java.util.List<LocalCoordinateListener> localCoordinateListeners = new ArrayList<LocalCoordinateListener>();
	
	
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
	
	// <editor-fold defaultstate="collapsed" desc="Transform Methods">
	
	public final AffineTransform getLocalToPixels() {
		return localToPixels;
	}
	
	public final AffineTransform getPixelsToLocal() {
		return pixelsToLocal;
	}
	
	public final AffineTransform getLocalToMercator() {
		return localToMercator;
	}
	
	public final AffineTransform getMercatorToLocal() {
		return mercatorToLocal;
	}
	
	public final AffineTransform getPixelsToMercator() {
		return pixelsToMercator;
	}
	
	public final AffineTransform getMercatorToPixels() {
		return mercatorToPixels;
	}
	
	public final double getZoomScalePixelsPerMerc() {
		 return this.zoomScalePixelsPerMerc;
	}
	
	public final double getZoomScalePixelsPerLocal() {
		 return this.zoomScalePixelsPerLocal;
	}
	
	public final double getZoomScaleLocalPerMeerc() {
		 return this.zoomScaleLocalPerMerc;
	}
	
	/** This method updates the local coordinates to match the current pixel coordinates. */
	public void resetLocalCoordinates() {
		AffineTransform oldLocalToNewLocal = new AffineTransform(localToMercator);
		
		mercatorToLocal = new AffineTransform(mercatorToPixels);
		this.updateTransforms();
		
		oldLocalToNewLocal.preConcatenate(mercatorToLocal);
		dispatchLocalCoordinateChangeEvent(oldLocalToNewLocal);
	}
	
	public void setViewBounds(Rectangle2D bounds) {
		AffineTransform oldLocalToNewLocal = new AffineTransform(localToMercator);
		
		
		Rectangle pixelRect = this.getVisibleRect();
		
		//X and Y will allow different magnifications - take the smaller of the two
		double xScale = pixelRect.width / bounds.getWidth();
		double yScale = pixelRect.height / bounds.getHeight();
		double scale = (xScale > yScale) ? yScale : xScale;
		//calculate the offest so the center is the same
		double xOffset = bounds.getCenterX() - (xScale/scale) * bounds.getWidth()/2;
		double yOffset = bounds.getCenterY() - (yScale/scale) * bounds.getHeight()/2;
		mercatorToPixels.scale(scale, scale);
		mercatorToPixels.translate(-xOffset,-yOffset);
		
		
		Point2D pMin = new Point2D.Double(bounds.getMinX(),bounds.getMinY());
		Point2D pMax = new Point2D.Double(bounds.getMaxX(),bounds.getMaxY());
		mercatorToPixels.transform(pMin, pMin);
		mercatorToPixels.transform(pMax, pMax);
		
		//define the local coordinate system to match the pizel coordinates for starters
		mercatorToLocal = new AffineTransform(mercatorToPixels);
		
		updateTransforms();
//teset to reset every zoom
this.resetLocalCoordinates();
		
		//notify local coordinate change event
		oldLocalToNewLocal.preConcatenate(mercatorToLocal);
		dispatchLocalCoordinateChangeEvent(oldLocalToNewLocal);
	}
	
	// </editor-fold>
	
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
	
	// <editor-fold defaultstate="collapsed" desc="Listeners">
	
	public void addMapListener(MapListener listener) {
		this.mapListeners.add(listener);
	}
	
	public void removeMapListener(MapListener listener) {
		this.mapListeners.remove(listener);
	}
	
	public void addLocalCoordinateListener(LocalCoordinateListener listener) {
		this.localCoordinateListeners.add(listener);
	}
	
	public void removeLocalCoordinateListener(LocalCoordinateListener listener) {
		this.localCoordinateListeners.remove(listener);
	}
	
	// </editor-fold>
	
//	public void resetLocalAnchor(double mx, double my) {
//		double newAnchorXInOldLocal = LocalCoordinates.mercToLocalX(mx);
//		double newAnchorYInOldLocal = LocalCoordinates.mercToLocalY(my);
//		double oldScale = LocalCoordinates.getMetersPerMerc();
//		
//		LocalCoordinates.setLocalAnchor(mx, my);
//		
//		double newScale = LocalCoordinates.getMetersPerMerc();
//		double zoomChange = oldScale/newScale;
//		
//		localToPixels.translate(newAnchorXInOldLocal,newAnchorYInOldLocal);
//		localToPixels.scale(zoomChange, zoomChange);
//		updateTransforms();
//	}
	
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
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	@Override
	public void osmDataChanged(int editNumber) {
		this.repaint();
	}
	
	//-------------------------
	// Mouse Events
	//-------------------------
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mouseDragged(MouseEvent e) {
		if(panOn) {
			panStep(e.getX(),e.getY());
		}
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
		if(panOn) {
			endPan(e.getX(),e.getY());
		}
	}
	
	public void mouseMoved(MouseEvent e) {
	
	}
	
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON2) {
			startPan(e.getX(),e.getY());
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if(panOn) {
			endPan(e.getX(),e.getY());
		}
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		
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
		mercatorToPixels.preConcatenate(zt);
		updateTransforms();
//teset to reset every zoom
this.resetLocalCoordinates();
		dispatchZoomEvent();
		this.repaint();
	}
	
	public void startPan(double x, double y) {
		lastX = x;
		lastY = y;
		panOn = true;
		dispatchPanStartEvent();
	}
	
	public void endPan(double x, double y) {
		panOn = false;
		dispatchPanEndEvent();
	}
	
	public void panStep(double x, double y) {
		translate(x-lastX,y-lastY);
		lastX = x;
		lastY = y;
	}
	
	public void translate(double dx, double dy) {
		AffineTransform zt = new AffineTransform();
		zt.translate(dx,dy);
		mercatorToPixels.preConcatenate(zt);
		updateTransforms();
		this.repaint();
	}
	
	//=================================
	// Private Methods
	//=================================
	
	/** This method takes should be called with the transforms mercatorToPixels
	 * and mercatorToLocal set. It calculates the rest of the matrices. */
	private void updateTransforms() {

		try {
			pixelsToMercator = mercatorToPixels.createInverse();
			localToMercator = mercatorToLocal.createInverse();
			
			pixelsToLocal = new AffineTransform(pixelsToMercator);
			pixelsToLocal.preConcatenate(mercatorToLocal);
			
			localToPixels = pixelsToLocal.createInverse();
			
			zoomScalePixelsPerMerc = Math.sqrt(mercatorToPixels.getDeterminant());
			zoomScalePixelsPerLocal = Math.sqrt(localToPixels.getDeterminant());
			zoomScaleLocalPerMerc = Math.sqrt(mercatorToLocal.getDeterminant());
		}
		catch(Exception ex) {
			//should not fail
		}
		
		//if we zoom too far away, calculate new local coordinates
//		if((zoomScalePixelsPerLocal > LOCAL_COORD_RESET_ZOOM)||(zoomScalePixelsPerLocal < 1/LOCAL_COORD_RESET_ZOOM)) {
//			this.resetLocalCoordinates();
//		}
	}
	
	private void dispatchZoomEvent() {
		for(MapListener mapListener:mapListeners) {
			mapListener.onZoom(this);
		}
	}
	
	private void dispatchPanStartEvent() {
		for(MapListener mapListener:mapListeners) {
			mapListener.onPanStart(this);
		}
	}
	
	private void dispatchPanEndEvent() {
		for(MapListener mapListener:mapListeners) {
			mapListener.onPanEnd(this);
		}
	}
	
	private void dispatchLocalCoordinateChangeEvent(AffineTransform oldLocalToNewLocal) {
		for(LocalCoordinateListener localCoordinateListener:localCoordinateListeners) {
			localCoordinateListener.onLocalCoordinateChange(this, oldLocalToNewLocal);
		}
	}

}
