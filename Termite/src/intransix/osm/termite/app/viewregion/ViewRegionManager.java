package intransix.osm.termite.app.viewregion;

import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.awt.Component;
import java.awt.MouseInfo;

/**
 *
 * @author sutter
 */
public class ViewRegionManager {
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
	
	private java.util.List<MapListener> mapListeners = new ArrayList<MapListener>();
	private java.util.List<LocalCoordinateListener> localCoordinateListeners = new ArrayList<LocalCoordinateListener>();

	private Component mapComponent;
	
	private boolean panOn = false;
	private double lastX;
	private double lastY;
	
	/** This method sets the map component
	 * 
	 * @param mapComponent	The java UI Component that displays the map (the MapPanel) 
	 */
	public void setMapComponent(Component mapComponent) {
		this.mapComponent = mapComponent;
	}
	
	/** This method returns the bounds of the map component in pixels. */
	public Rectangle getPixelRect() {
		return mapComponent.getBounds();
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
	
	public void setLatLonViewBounds(Rectangle2D latLonBounds) {
		double minLat = Math.toRadians(latLonBounds.getMinY());
		double minLon = Math.toRadians(latLonBounds.getMinX());
		double maxLat = Math.toRadians(latLonBounds.getMaxY());
		double maxLon = Math.toRadians(latLonBounds.getMaxX());
		
		double minMX = MercatorCoordinates.lonRadToMx(minLon); 
		double minMY = MercatorCoordinates.latRadToMy(maxLat); 
		double maxMX = MercatorCoordinates.lonRadToMx(maxLon); 
		double maxMY = MercatorCoordinates.latRadToMy(minLat); 
		Rectangle2D mercBounds = new Rectangle2D.Double(minMX,minMY,maxMX - minMX,maxMY - minMY);
		setMercViewBounds(mercBounds);
	}
	
	public void setMercViewBounds(Rectangle2D bounds) {
		AffineTransform oldLocalToNewLocal = new AffineTransform(localToMercator);
		
		
		Rectangle pixelRect = mapComponent.getBounds();
		
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
	
	/** This method sets the base rotation angle. Setting angleRad = 0 radians
	 * means north up.
	 * 
	 * @param angleRad	The rotation angle in radians.
	 */
	public void setRotation(double angleRad) {
		AffineTransform oldLocalToNewLocal = new AffineTransform(localToMercator);
		
		double scale = Math.sqrt(mercatorToPixels.getDeterminant());
		
		Rectangle bounds = mapComponent.getBounds();
		Point2D centerPix = new Point2D.Double(bounds.getCenterX(),bounds.getCenterY());
		Point2D centerMerc = new Point2D.Double();
		pixelsToMercator.transform(centerPix, centerMerc);
		
		//rotate and scale matrix
		mercatorToPixels = new AffineTransform();
		mercatorToPixels.setToRotation(angleRad);
		mercatorToPixels.scale(scale, scale);
		
		//correct so we have the right center point in pixels.
		Point2D centerPix2 = new Point2D.Double();
		mercatorToPixels.transform(centerMerc, centerPix2);
		double[] matrix = new double[6];
		mercatorToPixels.getMatrix(matrix);
		matrix[4] += centerPix.getX() - centerPix2.getX();
		matrix[5] += centerPix.getY() - centerPix2.getY();
		mercatorToPixels.setTransform(matrix[0],matrix[1],matrix[2],matrix[3],matrix[4],matrix[5]);
		 
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

	/** This method returns true if a pan is active. */
	public boolean isPanning() {
		return panOn;
	}
	
	/** This method zooms the specified amount about the center of the screen. */
	public void zoom(double zoomFactor) {
		int x,y;
		if(mapComponent != null) {
			x = mapComponent.getWidth()/2;
			y = mapComponent.getHeight()/2;
		}
		else {
			x = y = 0;
		}
		zoom(zoomFactor,x,y);
	}
	
	/** This method zooms the specified amount about the specified point. */
	public void zoom(double zoomFactor, double x, double y) {
		AffineTransform zt = new AffineTransform();
		zt.translate((1-zoomFactor)*x, (1-zoomFactor)*y);
		zt.scale(zoomFactor, zoomFactor);
		mercatorToPixels.preConcatenate(zt);
		updateTransforms();
//test to reset every zoom
this.resetLocalCoordinates();
		dispatchZoomEvent();
		mapComponent.repaint();
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
		mapComponent.repaint();
	}
	
	

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
