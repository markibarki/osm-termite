package intransix.osm.termite.app.viewregion;

import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import intransix.osm.termite.app.ShutdownListener;
import intransix.osm.termite.app.preferences.Preferences;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

/**
 *
 * @author sutter
 */
public class ViewRegionManager implements ShutdownListener {
	
	private final static String MIN_LAT_TAG = "minLat";
	private final static String MIN_LON_TAG = "minLon";
	private final static String MAX_LAT_TAG = "maxLat";
	private final static String MAX_LON_TAG = "maxLon";
	private final static double INVALID_ANGLE = Double.MAX_VALUE;
	
	private final static double DEFAULT_MIN_LAT = -30.0;
	private final static double DEFAULT_MIN_LON = -150.0;
	private final static double DEFAULT_MAX_LAT = 60.0;
	private final static double DEFAULT_MAX_LON = 150.0;
	
	//transforms
	private AffineTransform mercatorToPixels = new AffineTransform();
	private AffineTransform pixelsToMercator = new AffineTransform();
	private Affine mercatorToPixelsFX = new Affine();
	private Affine pixelsToMercatorFX = new Affine();
	private double angleRad = 0;
	private double zoomScalePixelsPerMerc = 1.0;
	
	private java.util.List<MapListener> mapListeners = new ArrayList<MapListener>();

	private Pane mapPane;
	
	private boolean panOn = false;
	private double lastX;
	private double lastY;
	
	/** Constructor */
	public ViewRegionManager(Pane mapPane) {
		this.mapPane = mapPane;
	}
	
	/** This method returns the bounds of the map component in pixels. */
	public Bounds getPixelRect() {
		Bounds bounds = mapPane.getLayoutBounds();
//@TODO temporary colution
if((bounds.getHeight() <= 0)||(bounds.getWidth() <= 0)) {
	bounds = new BoundingBox(0,0,500,500);
}
		return bounds;
	}
	
	// <editor-fold defaultstate="collapsed" desc="Transform Methods">
	
	public final AffineTransform getPixelsToMercator() {
		return pixelsToMercator;
	}
	
	public final AffineTransform getMercatorToPixels() {
		return mercatorToPixels;
	}
	
	public final Affine getPixelsToMercatorFX() {
		return pixelsToMercatorFX;
	}
	
	public final Affine getMercatorToPixelsFX() {
		return mercatorToPixelsFX;
	}
	
	public final double getZoomScalePixelsPerMerc() {
		 return this.zoomScalePixelsPerMerc;
	}
	
	public final double getZoomScaleMercPerPixel() {
		 return 1/this.zoomScalePixelsPerMerc;
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
		Bounds mercBounds = new BoundingBox(minMX,minMY,maxMX - minMX,maxMY - minMY);
		setMercViewBounds(mercBounds);
	}
	
	public void setMercViewBounds(Bounds mercBounds) {
		Bounds pixelBounds = this.getPixelRect();
		
		//X and Y will allow different magnifications - take the smaller of the two
		double xScale = pixelBounds.getWidth() / mercBounds.getWidth();
		double yScale = pixelBounds.getHeight() / mercBounds.getHeight();
		double scale = (xScale > yScale) ? yScale : xScale;
		//calculate the offest so the center is the same
		double xOffset = (mercBounds.getMaxX() + mercBounds.getMinX())/2 - (xScale/scale) * mercBounds.getWidth()/2;
		double yOffset =(mercBounds.getMaxY() + mercBounds.getMinY())/2 - (yScale/scale) * mercBounds.getHeight()/2;
		mercatorToPixels.scale(scale, scale);
		mercatorToPixels.translate(-xOffset,-yOffset);
		
		
		Point2D pMin = new Point2D.Double(mercBounds.getMinX(),mercBounds.getMinY());
		Point2D pMax = new Point2D.Double(mercBounds.getMaxX(),mercBounds.getMaxY());
		mercatorToPixels.transform(pMin, pMin);
		mercatorToPixels.transform(pMax, pMax);
		
		updateTransforms();
	}
	
	/** This method sets the base rotation angle. Setting angleRad = 0 radians
	 * means north up.
	 * 
	 * @param angleRad	The rotation angle in radians.
	 */
	public void setRotation(double angleRad) {
		this.angleRad = angleRad;
		double scale = Math.sqrt(mercatorToPixels.getDeterminant());
		
		Bounds bounds = this.getPixelRect();
		Point2D centerPix = new Point2D.Double((bounds.getMinX() + bounds.getMaxX())/2,
				(bounds.getMinY() + bounds.getMaxY())/2);
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

		updateTransforms();
	}
	
	// </editor-fold>
	
		// <editor-fold defaultstate="collapsed" desc="Listeners">
	
	public void addMapListener(MapListener listener) {
		this.mapListeners.add(listener);
	}
	
	public void removeMapListener(MapListener listener) {
		this.mapListeners.remove(listener);
	}
	
	// </editor-fold>

	/** This method returns true if a pan is active. */
	public boolean isPanning() {
		return panOn;
	}
	
	/** This method zooms the specified amount about the center of the screen. */
	public void zoom(double zoomFactor) {
		double x,y;
		if(mapPane != null) {
			x = mapPane.getWidth()/2;
			y = mapPane.getHeight()/2;
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

		dispatchZoomEvent();
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
		translateStep(x-lastX,y-lastY);
		lastX = x;
		lastY = y;
		dispatchPanStepEvent();
	}
	
	public void translate(double dx, double dy) {
//should we add a pan start and pan step event here?
		translateStep(dx,dy);
		dispatchPanEndEvent();
	}
	
	private void translateStep(double dx, double dy) {
		AffineTransform zt = new AffineTransform();
		zt.translate(dx,dy);
		mercatorToPixels.preConcatenate(zt);
		updateTransforms();
	}
	
	/** This method should be called after the UI is set up and the initial view 
	 * can be set. */
	public void setInitialView() {
		double minLat = Preferences.getDoubleProperty(MIN_LAT_TAG,INVALID_ANGLE);
		if(minLat == INVALID_ANGLE) minLat = DEFAULT_MIN_LAT;
		
		double minLon = Preferences.getDoubleProperty(MIN_LON_TAG,INVALID_ANGLE);
		if(minLon == INVALID_ANGLE) minLon = DEFAULT_MIN_LON;
		
		double maxLat = Preferences.getDoubleProperty(MAX_LAT_TAG,INVALID_ANGLE);
		if(maxLat == INVALID_ANGLE) maxLat = DEFAULT_MAX_LAT;
		
		double maxLon = Preferences.getDoubleProperty(MAX_LON_TAG,INVALID_ANGLE);
		if(maxLon == INVALID_ANGLE) maxLon = DEFAULT_MAX_LON;
		
		if((maxLat - minLat <= 0)||(maxLon - minLon <= 0)||
				(minLat < -90)||(minLon < -180)||(maxLat > 90)||(maxLon > 180)) {
			minLat = DEFAULT_MIN_LAT;
			minLon = DEFAULT_MIN_LON;
			maxLat = DEFAULT_MAX_LAT;
			maxLon = DEFAULT_MAX_LON;
		}
		
		Rectangle2D rect = new Rectangle2D.Double(minLon,minLat,maxLon-minLon,maxLat-minLat);
		setLatLonViewBounds(rect);
	}
	
	@Override
	public void onShutdown() {
		//save the viewport
		Point2D topLeft = new Point2D.Double();
		Point2D bottomRight = new Point2D.Double(mapPane.getWidth(),mapPane.getHeight());
		//transform to merc
		this.pixelsToMercator.transform(topLeft, topLeft);
		this.pixelsToMercator.transform(bottomRight, bottomRight);
		//get lat lon range
		double minLat = Math.toDegrees(MercatorCoordinates.myToLatRad(bottomRight.getY()));
		double minLon = Math.toDegrees(MercatorCoordinates.mxToLonRad(topLeft.getX()));
		double maxLat = Math.toDegrees(MercatorCoordinates.myToLatRad(topLeft.getY()));
		double maxLon = Math.toDegrees(MercatorCoordinates.mxToLonRad(bottomRight.getX()));
		//save values
		try {
			Preferences.setProperty(MIN_LAT_TAG,minLat);
			Preferences.setProperty(MIN_LON_TAG,minLon);
			Preferences.setProperty(MAX_LAT_TAG,maxLat);
			Preferences.setProperty(MAX_LON_TAG,maxLon);
		}
		catch(Exception ex) {
			//no luck
		}
	}
	
	

	/** This method takes should be called with the transforms mercatorToPixels
	 * and mercatorToLocal set. It calculates the rest of the matrices. */
	private void updateTransforms() {

		try {
			pixelsToMercator = mercatorToPixels.createInverse();	
			zoomScalePixelsPerMerc = Math.sqrt(mercatorToPixels.getDeterminant());
			
			mercatorToPixelsFX = Transform.affine(mercatorToPixels.getScaleX(),
					mercatorToPixels.getShearY(),
					mercatorToPixels.getShearX(),
					mercatorToPixels.getScaleY(),
					mercatorToPixels.getTranslateX(),
					mercatorToPixels.getTranslateY());
			pixelsToMercatorFX = Transform.affine(pixelsToMercator.getScaleX(),
					pixelsToMercator.getShearY(),
					pixelsToMercator.getShearX(),
					pixelsToMercator.getScaleY(),
					pixelsToMercator.getTranslateX(),
					pixelsToMercator.getTranslateY());
		}
		catch(Exception ex) {
			//should not fail
		}
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
	
	private void dispatchPanStepEvent() {
		for(MapListener mapListener:mapListeners) {
			mapListener.onPanStep(this);
		}
	}
	
	private void dispatchPanEndEvent() {
		for(MapListener mapListener:mapListeners) {
			mapListener.onPanEnd(this);
		}
	}
	
}
