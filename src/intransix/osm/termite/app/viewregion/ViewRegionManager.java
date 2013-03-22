package intransix.osm.termite.app.viewregion;

import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import intransix.osm.termite.app.ShutdownListener;
import intransix.osm.termite.app.mapdata.MapDataListener;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.preferences.Preferences;
import intransix.osm.termite.gui.map.MapPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;

/**
 * This class controls the viewport for the map. It takes inputs to pan, zoom 
 * and set the viewport. It also monitors the pixels size of the viewport, which
 * in turn affects the visible viewport. When the viewport changes an event is
 * triggered.
 * 
 * @author sutter
 */
public class ViewRegionManager implements ShutdownListener, MapDataListener {
	
	//These constants are used to read and wrtie the stored configuration
	private final static String MIN_LAT_TAG = "minLat";
	private final static String MIN_LON_TAG = "minLon";
	private final static String MAX_LAT_TAG = "maxLat";
	private final static String MAX_LON_TAG = "maxLon";
	private final static double INVALID_ANGLE = Double.MAX_VALUE;
	
	private final static double DEFAULT_MIN_LAT = -30.0;
	private final static double DEFAULT_MIN_LON = -150.0;
	private final static double DEFAULT_MAX_LAT = 60.0;
	private final static double DEFAULT_MAX_LON = 150.0;
	
	/** this arbitrary number is used to set the scale for local coordinates. */
	private final static double LOCAL_COORDINATE_AREA = 1000000.0;
	
	//transforms
	private AffineTransform mercatorToPixels = new AffineTransform();
	private AffineTransform pixelsToMercator = new AffineTransform();
	private AffineTransform mercatorToLocal = new AffineTransform();
	private AffineTransform localToMercator = new AffineTransform();
	private Affine mercatorToPixelsFX = new Affine();
	private Affine pixelsToMercatorFX = new Affine();
	private Affine localToMercFX = new Affine();
	private double zoomScalePixelsPerMerc = 1.0;
	private double zoomScaleLocalPerMerc = 1.0;
	private double zoomScaleLocalPerPixel = 1.0;
	
	private java.util.List<MapListener> mapListeners = new ArrayList<MapListener>();

	//This is the pa
	private MapPane mapPane;
	
	private boolean panOn = false;
	private double lastX;
	private double lastY;
	
	/** Constructor */
	public ViewRegionManager(MapPane mapPane) {
		this.mapPane = mapPane;
		
		//add a listener for a viewport change
		ChangeListener<Number> changeListener = new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, 
					Number oldValue, Number newValue) {
				checkViewportResize();
			}
		};
		mapPane.heightProperty().addListener(changeListener);
		mapPane.widthProperty().addListener(changeListener);

	}
	
	/** This method returns the bounds of the map component in pixels. */
	public Bounds getPixelRect() {
		Bounds bounds = mapPane.getLayoutBounds();

		//just in case...
		if((bounds.getHeight() <= 0)||(bounds.getWidth() <= 0)) {
			bounds = new BoundingBox(0,0,500,500);
			System.out.println("Map pane not initialized prior to bounds check.");
		}
		
		return bounds;
	}
	
	//-------------------
	// Transform Methods
	//-------------------
	
	/** This method gets the transform from pixels to mercator coordinates. */
	public final AffineTransform getPixelsToMercator() {
		return pixelsToMercator;
	}
	
	/** This method gets the transform from mercator to pixels coordinates. */
	public final AffineTransform getMercatorToPixels() {
		return mercatorToPixels;
	}
	
	/** This method gets the transform from mercator to pixels coordinates. */
	public final AffineTransform getMercatorToLocal() {
		return mercatorToLocal;
	}
	
	/** This method gets the transform from pixels to mercator coordinates. */
	public final AffineTransform getLocalToMercator() {
		return localToMercator;
	}
	
	/** This method gets the transform from pixels to mercator coordinates. */
	public final Affine getPixelsToMercatorFX() {
		return pixelsToMercatorFX;
	}
	
	/** This method gets the transform from mercator to pixels coordinates. */
	public final Affine getMercatorToPixelsFX() {
		return mercatorToPixelsFX;
	}
	
	/** This method gets the transform from pixels to mercator coordinates. */
	public final Affine getLocalToMercatorFX() {
		return localToMercFX;
	}
	
	/** This method gets the scale factor between pixels and mercator coordinates. */
	public final double getZoomScalePixelsPerMerc() {
		 return this.zoomScalePixelsPerMerc;
	}
	
	/** This method gets the scale factor between pixels and mercator coordinates. */
	public final double getZoomScaleMercPerPixel() {
		 return 1/this.zoomScalePixelsPerMerc;
	}
	
	public final double getZoomScaleLocalPerPixel() {
		return this.zoomScaleLocalPerPixel;
	}
	
	/** This method gets the scale factor between pixels and mercator coordinates. */
	public final double getZoomScaleLocalPerMerc() {
		 return this.zoomScaleLocalPerMerc;
	}
	
	/** This method sets the lat lon bounds for the visible display. The actual
	 * bounds will be modified depending on the dimensions of the map display. */
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
	
	/** This method sets the bounds in mercator coordinates (range 0 to 1) for the 
	 * display. The actual bounds will be modified depending on the dimensions
	 * of the map display. */
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
	
	//------------------------
	// Map Listeners
	//------------------------
	
	/** This method adds a map listener. */
	public void addMapListener(MapListener listener) {
		if(!mapListeners.contains(listener)) {
			this.mapListeners.add(listener);
		}
	}
	
	/** This method removes a map listener. */
	public void removeMapListener(MapListener listener) {
		this.mapListeners.remove(listener);
	}
	
	//--------------------------------
	// Viewport Control Methods
	//--------------------------------

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

		dispatchViewChangeEvent(true);
	}
	
	/** This method sets up dynamic panning, such as with a mouse or an animation. */
	public void startPan(double x, double y) {
		lastX = x;
		lastY = y;
		panOn = true;
		dispatchPanStartEvent();
	}
	
	/** This method should be called at the end of a dynamic pan event. */
	public void endPan(double x, double y) {
		panOn = false;
		dispatchPanEndEvent();
	}
	
	/** This method should be called to increment a dynamic pan. */
	public void panStep(double x, double y) {
		translateStep(x-lastX,y-lastY);
		lastX = x;
		lastY = y;
		dispatchPanStepEvent();
		dispatchViewChangeEvent(false);
	}
	
	/** This method translates the system the given number of pixels. This should
	 be used for a one-time pan event*/
	public void translate(double dx, double dy) {
		translateStep(dx,dy);
		dispatchViewChangeEvent(false);
	}
	
	/** This method sets the base rotation angle. Setting angleRad = 0 radians
	 * means north up.
	 * 
	 * @param angleRad	The rotation angle in radians.
	 */
	public void setRotation(double angleRad) {
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
		dispatchViewChangeEvent(true);
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
	public void onMapData(MapDataManager mapDataManager, boolean dataPresent) {
		if(dataPresent) {
			//set up the local coordinates - doesn't work well if we try global
			initializeLocalCoordinates(mapDataManager);
			dispatchLocalCoordinateEvent();
		}
	}
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	@Override
	public void osmDataChanged(MapDataManager mapDataManager, int editNumber) {
	}
	
	/** This returns the priority for this object as a map data listener. */
	@Override
	public int getMapDataListenerPriority() {
		return MapDataListener.PRIORITY_DATA_MODIFY_1;
	}
	
	/** This is a shutdown event to save the view of the map. */
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
	
	//===================================
	// Private Methods
	//===================================
	
	/** This method sets local coordinates based on the area of downloaded data. */
	private void initializeLocalCoordinates(MapDataManager mapDataManager) {
		//get the rectangle
		Rectangle2D downloadRectangle = mapDataManager.getDownloadBounds();
		//find the local scale
		double mercArea = downloadRectangle.getWidth()* downloadRectangle.getHeight();
		if(mercArea > 0) {
			zoomScaleLocalPerMerc = Math.sqrt(LOCAL_COORDINATE_AREA/mercArea);
			zoomScaleLocalPerPixel = this.zoomScaleLocalPerMerc / this.zoomScalePixelsPerMerc;
		}
		
		//create transforms
		double localToMercScaleFactor = 1.0 / zoomScaleLocalPerMerc;
		localToMercator = new AffineTransform(localToMercScaleFactor,0.0,
				0.0,localToMercScaleFactor,
				downloadRectangle.getMinX(),downloadRectangle.getMinY());
		try {
			mercatorToLocal = localToMercator.createInverse();
		}
		catch(Exception ex) {
			//this should not happen
			throw new RuntimeException("Failed transform inverse");
		}

		localToMercFX = Transform.affine(localToMercScaleFactor,0.0,
				0.0,localToMercScaleFactor,
				downloadRectangle.getMinX(),downloadRectangle.getMinY());
	}
	
	/** This method does a pan, implementing the action for a one time or dynamic pan. */
	private void translateStep(double dx, double dy) {
		AffineTransform zt = new AffineTransform();
		zt.translate(dx,dy);
		mercatorToPixels.preConcatenate(zt);
		updateTransforms();
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
			
			zoomScaleLocalPerPixel = this.zoomScaleLocalPerMerc / this.zoomScalePixelsPerMerc;
		}
		catch(Exception ex) {
			//should not fail
		}
	}
	
	private double lastWidth = -1;
	private double lastHeight = -1;
	
	/** This method triggers a view change event is the viewport size changes. */
	private void checkViewportResize() {
		double width = mapPane.getWidth();
		double height = mapPane.getHeight();
		if((width != lastWidth)||(height != lastHeight)) {
			lastWidth = width;
			lastHeight = height;
			dispatchViewChangeEvent(false);
		}
	}
	
	private void dispatchViewChangeEvent(boolean zoomChanged) {
		for(MapListener mapListener:mapListeners) {
			mapListener.onMapViewChange(this,zoomChanged);
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
	
	private void dispatchLocalCoordinateEvent() {
		for(MapListener mapListener:mapListeners) {
			mapListener.onLocalCoordinatesSet(this);
		}
	}
}
