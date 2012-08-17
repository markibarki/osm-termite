package intransix.osm.termite.render;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import intransix.osm.termite.map.data.OsmDataChangedListener;
import intransix.osm.termite.gui.MapDataListener;
import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.render.source.SourceLayer;

/**
 *
 * @author sutter
 */
public class MapPanel extends JPanel implements OsmDataChangedListener,
		MouseListener, MouseMotionListener, MouseWheelListener, MapDataListener,
		KeyListener {
	
	//zoom factor for a mouse wheel click
	private final static double ROTATION_SCALE_FACTOR = 1.1;
	private final static double KEY_SCALE_FACTOR = 1.1;
	private final static double BUTTON_SCALE_FACTOR = 1.5;
	
	private final static double KEY_TRANSLATE_FRACTION = .25;
	
	//max zoom before redefining local coordinates
	private final static double LOCAL_COORD_RESET_ZOOM = 2;
	
	private final static int BUTTON_X = 20;
	private final static int BUTTON_Y = 20;
	private final static int BUTTON_SPACING = 20;
	
	//used for frame selection
	private final static String NORTH_UP_ITEM = "North Up";
	
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
	private java.util.List<LayerStateListener> layerListeners = new ArrayList<LayerStateListener>();
	
	private boolean panOn = false;
	private double lastX;
	private double lastY;
	
	private JComboBox frameSelector;
	private JButton zoomInButton;
	private JButton zoomOutButton;
	
	public MapPanel() {
//        setBorder(BorderFactory.createLineBorder(Color.black));
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.addKeyListener(this);
		this.setBackground(Color.WHITE);
		
		initComponents();
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
	
	/** This method sets the base rotation angle. Setting angleRad = 0 radians
	 * means north up.
	 * 
	 * @param angleRad	The rotation angle in radians.
	 */
	public void setRotation(double angleRad) {
		AffineTransform oldLocalToNewLocal = new AffineTransform(localToMercator);
		
		double scale = Math.sqrt(mercatorToPixels.getDeterminant());
		
		Rectangle bounds = this.getVisibleRect();
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
	
	/** This method gets the location of the mouse in Mercator coordinates. It should
	 * be used if the mouse is needed outside of a mouse event. */
	public Point2D getMousePointMerc() {
		//get the current mouse location and update the nodes that move with the mouse
		java.awt.Point mouseInApp = MouseInfo.getPointerInfo().getLocation();
		java.awt.Point mapPanelInApp = getLocationOnScreen();
		Point2D mousePix = new Point2D.Double(mouseInApp.x - mapPanelInApp.x,mouseInApp.y - mapPanelInApp.y);
		Point2D mouseMerc = new Point2D.Double(); 
		pixelsToMercator.transform(mousePix,mouseMerc);
		
		return mouseMerc;
	}
	
	/** This method gets the location of the mouse in Pixel coordinates. It should
	 * be used if the mouse is needed outside of a mouse event. */
	public Point2D getMousePointPix() {
		//get the current mouse location and update the nodes that move with the mouse
		java.awt.Point mouseInApp = MouseInfo.getPointerInfo().getLocation();
		java.awt.Point mapPanelInApp = getLocationOnScreen();
		Point2D mousePix = new Point2D.Double(mouseInApp.x - mapPanelInApp.x,mouseInApp.y - mapPanelInApp.y);
		
		return mousePix;
	}
	
	// <editor-fold defaultstate="collapsed" desc="Map Layers">
	
	public void updateMapLayers(java.util.List<MapLayer> layers) {
		this.layers = layers;
		this.repaint();
	}
	
	/** This should be called with a list of active layers when the source layers
	 * are updated. 
	 * 
	 * @param sourceLayers	A list of active source layers
	 */
	public void updateSourceLayers(java.util.List<SourceLayer> sourceLayers) {
		Object selected = frameSelector.getSelectedItem();
		frameSelector.removeAllItems();
		if(sourceLayers.isEmpty()) {
			//hide selector
			frameSelector.setVisible(false);
		}
		else {
			//update contents
			frameSelector.setVisible(true);
			frameSelector.addItem(NORTH_UP_ITEM);
			for(SourceLayer layer:sourceLayers) {
				frameSelector.addItem(layer);
			}
			if((selected != null)&&(sourceLayers.contains(selected))) {
				frameSelector.setSelectedItem(selected);
			}
			else {
				frameSelector.setSelectedItem(NORTH_UP_ITEM);
			}
		}
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
	
	public void addLayerListener(LayerStateListener listener) {
		if(!layerListeners.contains(listener)) {
			this.layerListeners.add(listener);
		}
		listener.layerStateChanged(null, layers);
	}
	
	public void removeLayerListener(LayerStateListener listener) {
		this.layerListeners.remove(listener);
	}
	
	// </editor-fold>
	
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
		Composite originalComposite = g2.getComposite();
		Composite activeComposite;
		for(MapLayer layer:layers) {
			if((layer.getActiveState())&&(!layer.getHidden())) {
				//set the opacity for the layer
				activeComposite = layer.getComposite();
				if(activeComposite == null) {
					activeComposite = originalComposite;
				}
				g2.setComposite(activeComposite);
				
				//render
				layer.render(g2);
				
				//reset the transform
				g2.setTransform(originalTransform);
			}
		}
		g2.setComposite(originalComposite);
	}
	
	/** This method is called when the map data is set of cleared. It will be called 
	 * with the value null when the data is cleared. 
	 * 
	 * @param mapData	The map data object
	 */
	public void onMapData(OsmData mapData) {
		if(mapData != null) {
			mapData.addDataChangedListener(this);
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
	// UI Events
	//-------------------------
	
	// <editor-fold defaultstate="collapsed" desc="Mouse Listeners">
	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mouseDragged(MouseEvent e) {
		if(panOn) {
			panStep(e.getX(),e.getY());
		}
	}
	
	public void mouseEntered(MouseEvent e) {
		//so we can get key events
		this.requestFocusInWindow();
	}
	
	public void mouseExited(MouseEvent e) {
		if(panOn) {
			endPan(e.getX(),e.getY());
		}
	}
	
	public void mouseMoved(MouseEvent e) {
	
	}
	
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3) {
			startPan(e.getX(),e.getY());
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if(panOn) {
			endPan(e.getX(),e.getY());
		}
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		int rotation = -e.getWheelRotation();
		double scaleFactor = Math.pow(ROTATION_SCALE_FACTOR,rotation);
		double x = e.getX();
		double y = e.getY();
		
		zoom(scaleFactor,x,y);
	}
	
	// </editor-fold>
		
	// <editor-fold defaultstate="collapsed" desc="Key Listener">
	
	/** Handle the key typed event from the text field. */
    @Override
	public void keyTyped(KeyEvent e) {
    }

    /** Handle the key-pressed event from the text field. */
	@Override
    public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
			Point2D point = getMousePointPix();
			if(onScreen(point)) {
				//zoom at mouse location
				zoom(KEY_SCALE_FACTOR,point.getX(),point.getY());
			}
			else {
				//mouse off screen - zoom at center
				zoom(KEY_SCALE_FACTOR);
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
			Point2D point = getMousePointPix();
			if(onScreen(point)) {
				//zoom at mouse location
				zoom(1/KEY_SCALE_FACTOR,point.getX(),point.getY());
			}
			else {
				//mouse off screen - zoom at center
				zoom(1/KEY_SCALE_FACTOR);
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_UP) {
			int deltaY = (int)(KEY_TRANSLATE_FRACTION * this.getWidth());
			translate(0,deltaY);
		}
		else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
			int deltaY = (int)(KEY_TRANSLATE_FRACTION * this.getWidth());
			translate(0,-deltaY);
		}
		else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
			int deltaX = (int)(KEY_TRANSLATE_FRACTION * this.getWidth());
			translate(-deltaX,0);
		}
		else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
			int deltaX = (int)(KEY_TRANSLATE_FRACTION * this.getWidth());
			translate(deltaX,0);
		}
		
    }
	
	private boolean onScreen(Point2D point) {
		double x = point.getX();
		double y = point.getY();
		return ((x >= 0)&&(y >= 0)&&(x <= getWidth())&&(y <= getHeight()));
			
	}

    /** Handle the key-released event from the text field. */
    @Override
	public void keyReleased(KeyEvent e) {
    }
	
	// </editor-fold>
	
	/** This method zooms the specified amount about the center of the screen. */
	public void zoom(double zoomFactor) {
		int x = this.getWidth()/2;
		int y = this.getHeight()/2;
		zoom(zoomFactor,x,y);
	}
	
	/** This method zooms the specified amount about the specified point. */
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
	
	/** MapLayers call this when their state changes. */
	void layerStateChanged(MapLayer layer) {
		//notify listeners that the layer state changed
		notifyLayerListeners(layer);
	}
	
	//=================================
	// Private Methods
	//=================================

	/** This method notifies listeners of any layer state changes. */
	private void notifyLayerListeners(MapLayer layer) {
		for(LayerStateListener listener:layerListeners) {
			listener.layerStateChanged(layer,layers);
		}
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
	
	/** This method initializes some overlay controls for the map panel. */
	private void initComponents() {
		//layout - manual
		setLayout(null);
		Insets insets = this.getInsets();
		Font font = new Font(null,1,18);
		
		//zoom in and out buttons
		zoomInButton = new JButton();
		zoomInButton.setText("+");
		zoomInButton.setFont(font);
		zoomInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoom(BUTTON_SCALE_FACTOR);
            }
        });
		Dimension zoomInSize = zoomInButton.getPreferredSize();
		
		zoomOutButton = new JButton();
		zoomOutButton.setText("-");
		zoomOutButton.setFont(font);
		zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoom(1/BUTTON_SCALE_FACTOR);
            }
        });
		Dimension zoomOutSize = zoomOutButton.getPreferredSize();
		
		//layout buttons
		int buttonWidth = zoomInSize.width > zoomOutSize.width ? zoomInSize.width : zoomOutSize.width;
		int buttonHeight = zoomInSize.height > zoomOutSize.height ? zoomInSize.height : zoomOutSize.height;
		
		this.add(zoomInButton);
		int xIn = insets.left + BUTTON_X;
		int yIn = insets.right + BUTTON_Y;
		zoomInButton.setBounds(xIn,yIn,buttonWidth,buttonHeight);
		
		this.add(zoomOutButton);
		
		int xOut = xIn;
		int yOut = yIn + zoomInSize.height + BUTTON_SPACING;
		zoomOutButton.setBounds(xOut,yOut,buttonWidth,buttonHeight);
		
		//frame selection
		frameSelector = new JComboBox();
		frameSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				Object selection = frameSelector.getSelectedItem();
				if(selection instanceof SourceLayer) {
					double angleRad = ((SourceLayer)selection).getAngleRad();
					setRotation(-angleRad);
				}
				else {
					//when north up is selected
					setRotation(0);
				}
				repaint();
            }
        });
		
		//layout frame select
		this.add(frameSelector);
		int xSel = xIn + buttonWidth + BUTTON_SPACING;
		int ySel = yIn;
		frameSelector.setLocation(xSel,ySel);
		frameSelector.setVisible(false);
		
		//this is test population nubmers
		frameSelector.addItem(NORTH_UP_ITEM);
		Dimension selSize = frameSelector.getPreferredSize();
		frameSelector.setSize(selSize);
	}


}
