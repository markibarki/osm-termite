package intransix.osm.termite.app.viewregion;

import intransix.osm.termite.util.MercatorCoordinates;
import java.util.ArrayList;
import intransix.osm.termite.app.ShutdownListener;
import intransix.osm.termite.app.preferences.Preferences;
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
	private Affine pixelToMercator = new Affine();
	private Affine mercatorToPixel = new Affine();
	
	private double pixelsToMercScale = 1;
	private double rotationDeg =0;
	
	//these are points that are mapped together pix - merc
	private double pixAnchorX;
	private double mercAnchorX;
	private double pixAnchorY;
	private double mercAnchorY;
	
	private java.util.List<MapListener> mapListeners = new ArrayList<MapListener>();
	private java.util.List<LocalCoordinateListener> localCoordinateListeners = new ArrayList<LocalCoordinateListener>();

	private Pane mapPane;
	
	private boolean panOn = false;
	private double lastX;
	private double lastY;
	
	/** This method sets the map component
	 * 
	 * @param mapComponent	The java UI Component that displays the map (the MapPanel) 
	 */
	public void setMapPane(Pane mapPane) {
		this.mapPane = mapPane;
	}
	
	/** This method returns the bounds of the map component in pixels. */
	public Bounds getPixelBounds() {
		return mapPane.getBoundsInLocal();
	}
	
	// <editor-fold defaultstate="collapsed" desc="Transform Methods">
	
	public final Affine getPixelsToMercator() {
		return pixelToMercator;
	}
	
	public final Affine getMercatorToPixels() {
		return mercatorToPixel;
	}
	
	public final double getZoomScaleLocalPerMeerc() {
		 return this.pixelsToMercScale;
	}
	
	public void setLatLonViewBounds(Bounds latLonBounds) {
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
	
		double pixWidth = mapPane.getWidth();
		double pixHeight = mapPane.getHeight();

		double pixelsToMercScaleX = mercBounds.getWidth() / pixWidth;
		double pixelsToMercScaleY = mercBounds.getHeight() / pixHeight;
		
		//local transform parameters
		pixelsToMercScale = (pixelsToMercScaleX > pixelsToMercScaleY) ? pixelsToMercScaleY : pixelsToMercScaleX;
		rotationDeg = 0;
		
		mercAnchorX = (mercBounds.getMaxX() + mercBounds.getMinX())/2;
		mercAnchorY = (mercBounds.getMaxY() + mercBounds.getMinY())/2;
		pixAnchorX = pixWidth/2;
		pixAnchorY = pixHeight/2;
		updateTransforms();
	}
	
	private void updateTransforms() {
		double c = Math.cos(Math.toRadians(rotationDeg));
		double s = Math.sin(Math.toRadians(rotationDeg));
		double xx = pixelsToMercScale * c;
		double xy = -pixelsToMercScale * s;
		double yx = pixelsToMercScale * s;
		double yy = pixelsToMercScale * c;
		double tx = mercAnchorX - (pixAnchorX * xx + pixAnchorY * xy);
		double ty = mercAnchorX - (pixAnchorX * yx + pixAnchorY * yy);
		
		pixelToMercator = Transform.affine(xx,xy,yx,xx,tx,ty);
		mercatorToPixel = createInverse(pixelToMercator);
		
		//set the transforms
		mapPane.getTransforms().setAll(pixelToMercator);
	}
	
	private Affine createInverse(Affine t1) {
		double det = t1.getMxx() * t1.getMyy() - t1.getMxy() * t1.getMyx();
		if(det == 0) throw new RuntimeException("Singular transform");
		
		Affine t2 = new Affine();
		double mxx = t1.getMyy() / det;
		double mxy = -t1.getMxy() / det;
		double myx = -t1.getMyx() / det;
		double myy = t1.getMxx() / det;
		double tx = -(mxx * t1.getTx() + mxy * t2.getTy());
		double ty = -(myx * t1.getTx() + myy * t2.getTy());
		return Transform.affine(mxx,myx,mxy,myy,tx,ty);			
	}
	
	/** This method sets the base rotation angle. Setting angleRad = 0 radians
	 * means north up.
	 * 
	 * @param angleRad	The rotation angle in radians.
	 */
	public void setRotation(double angleDeg) {
		this.rotationDeg = angleDeg;
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
	public void zoom(double zoomFactor, double fixedPixX, double fixedPixY) {
		pixelsToMercScale *= zoomFactor;
		//kep the specified pixel point fixed
		pixAnchorX = fixedPixX;
		pixAnchorY = fixedPixY;
		mercAnchorX = pixelToMercator.getMxx() * fixedPixX + pixelToMercator.getMxy() * fixedPixY + pixelToMercator.getTx(); 
		mercAnchorY = pixelToMercator.getMyx() * fixedPixX + pixelToMercator.getMyy() * fixedPixY + pixelToMercator.getTy();
		updateTransforms();
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
	
	public void translate(double deltaXPix, double deltaYPix) {
		//move the pix anchor point
		pixAnchorX += deltaXPix;
		pixAnchorY += deltaYPix;
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
		
		BoundingBox bounds = new BoundingBox(minLon,minLat,maxLon-minLon,maxLat-minLat);
		setLatLonViewBounds(bounds);
	}
	
	@Override
	public void onShutdown() {
		//save the viewport
		double maxPixX = mapPane.getWidth();
		double maxPixY = mapPane.getHeight();
		double maxMercX = pixelToMercator.getMxx() * maxPixX + pixelToMercator.getMxy() * maxPixY; 
		double maxMercY = pixelToMercator.getMyx() * maxPixX + pixelToMercator.getMyy() * maxPixY;
		double minMercX = pixelToMercator.getTx();
		double minMercY = pixelToMercator.getTy();
	
		//get lat lon range
		double minLat = Math.toDegrees(MercatorCoordinates.myToLatRad(maxMercY));
		double minLon = Math.toDegrees(MercatorCoordinates.mxToLonRad(minMercX));
		double maxLat = Math.toDegrees(MercatorCoordinates.myToLatRad(minMercY));
		double maxLon = Math.toDegrees(MercatorCoordinates.mxToLonRad(maxMercX));
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
	
//	private void dispatchLocalCoordinateChangeEvent(AffineTransform oldLocalToNewLocal) {
//		for(LocalCoordinateListener localCoordinateListener:localCoordinateListeners) {
//			localCoordinateListener.onLocalCoordinateChange(this, oldLocalToNewLocal);
//		}
//	}
	
}
