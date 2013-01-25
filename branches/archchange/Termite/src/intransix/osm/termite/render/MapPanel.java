package intransix.osm.termite.render;

import intransix.osm.termite.app.maplayer.MapLayer;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import intransix.osm.termite.app.maplayer.MapLayerListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;

/**
 *
 * @author sutter
 */
public class MapPanel extends JPanel implements MapLayerListener,
		MouseListener, MouseMotionListener, MouseWheelListener, KeyListener,
		MousePositionSource {
	
	//zoom factor for a mouse wheel click
	private final static double ROTATION_SCALE_FACTOR = 1.1;
	private final static double KEY_SCALE_FACTOR = 1.1;
	private final static double BUTTON_SCALE_FACTOR = 1.5;
	
	private final static double KEY_TRANSLATE_FRACTION = .1;
	
	//max zoom before redefining local coordinates
	private final static double LOCAL_COORD_RESET_ZOOM = 2;
	
	private final static int BUTTON_X = 20;
	private final static int BUTTON_Y = 20;
	private final static int BUTTON_SPACING = 20;
	
	//used for frame selection
	private final static String NORTH_UP_ITEM = "North Up";
	
	private ViewRegionManager viewRegionManager;
	private java.util.List<MapLayer> layers = new ArrayList<MapLayer>();

	
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
	
	public void setViewRegionManager(ViewRegionManager vrm) {
		this.viewRegionManager = vrm;
	}

	/** This method gets the location of the mouse in Mercator coordinates. It should
	 * be used if the mouse is needed outside of a mouse event. */
	public Point2D getMousePointMerc() {
		//get the current mouse location and update the nodes that move with the mouse
		java.awt.Point mouseInApp = MouseInfo.getPointerInfo().getLocation();
		java.awt.Point mapPanelInApp = getLocationOnScreen();
		Point2D mousePix = new Point2D.Double(mouseInApp.x - mapPanelInApp.x,mouseInApp.y - mapPanelInApp.y);
		Point2D mouseMerc = new Point2D.Double(); 
		AffineTransform pixelsToMercator = this.viewRegionManager.getPixelsToMercator();
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
	
	/** This method is called when the map layer state changes, including enable,
	 * visible and opacity. */
	@Override
	public void layerStateChanged(MapLayer mapLayer) {
		//we need to reload the list in case the reference frame dropdown changes
		layerListChanged(layers);
	}
	
	/** This method is called when the content of a layer changes. */
	@Override
	public void layerContentChanged(MapLayer mapLayer) {
		if((mapLayer.isVisible())&&(mapLayer.getActiveState())) {
			this.repaint();
		}
	}
	
	/** This method is called when the map layer list changes. */
	@Override
	public void layerListChanged(java.util.List<MapLayer> mapLayerList) {
		this.layers = mapLayerList;
		
		//add any layer with a preferred angle to the coordBasis list
		java.util.List<MapLayer> refFrameLayers = new ArrayList<MapLayer>();
		for(MapLayer layer:layers) {
			//get the reference frame layers
			if((layer.hasPreferredAngle())&&(layer.isVisible())) refFrameLayers.add(layer);
		}
		updateRefFrameLayers(refFrameLayers);

		this.repaint();
	}
	
				
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
				viewRegionManager.zoom(KEY_SCALE_FACTOR,point.getX(),point.getY());
			}
			else {
				//mouse off screen - zoom at center
				viewRegionManager.zoom(KEY_SCALE_FACTOR);
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
			Point2D point = getMousePointPix();
			if(onScreen(point)) {
				//zoom at mouse location
				viewRegionManager.zoom(1/KEY_SCALE_FACTOR,point.getX(),point.getY());
			}
			else {
				//mouse off screen - zoom at center
				viewRegionManager.zoom(1/KEY_SCALE_FACTOR);
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_UP) {
			int deltaY = (int)(KEY_TRANSLATE_FRACTION * this.getWidth());
			viewRegionManager.translate(0,deltaY);
		}
		else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
			int deltaY = (int)(KEY_TRANSLATE_FRACTION * this.getWidth());
			viewRegionManager.translate(0,-deltaY);
		}
		else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
			int deltaX = (int)(KEY_TRANSLATE_FRACTION * this.getWidth());
			viewRegionManager.translate(-deltaX,0);
		}
		else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
			int deltaX = (int)(KEY_TRANSLATE_FRACTION * this.getWidth());
			viewRegionManager.translate(deltaX,0);
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
	
	
	/** This should be called with a list of active layers when the source layers
	 * are updated. 
	 * 
	 * @param sourceLayers	A list of active source layers
	 */
	private void updateRefFrameLayers(java.util.List<MapLayer> refFrameLayers) {
		Object selected = frameSelector.getSelectedItem();
		frameSelector.removeAllItems();
		if(refFrameLayers.isEmpty()) {
			//hide selector
			frameSelector.setVisible(false);
		}
		else {
			//update contents
			frameSelector.setVisible(true);
			frameSelector.addItem(NORTH_UP_ITEM);
			for(MapLayer layer:refFrameLayers) {
				frameSelector.addItem(layer);
			}
			if((selected != null)&&(refFrameLayers.contains(selected))) {
				frameSelector.setSelectedItem(selected);
			}
			else {
				frameSelector.setSelectedItem(NORTH_UP_ITEM);
			}
		}
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
			if((layer.getActiveState())&&(layer.isVisible())) {
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

	//-------------------------
	// UI Events
	//-------------------------
	
	// <editor-fold defaultstate="collapsed" desc="Mouse Listeners">
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(viewRegionManager.isPanning()) {
			viewRegionManager.panStep(e.getX(),e.getY());
		}
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		//so we can get key events
		this.requestFocusInWindow();
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		if(viewRegionManager.isPanning()) {
			viewRegionManager.endPan(e.getX(),e.getY());
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
	
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1) {
			viewRegionManager.startPan(e.getX(),e.getY());
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if(viewRegionManager.isPanning()) {
			viewRegionManager.endPan(e.getX(),e.getY());
		}
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		int rotation = -e.getWheelRotation();
		double scaleFactor = Math.pow(ROTATION_SCALE_FACTOR,rotation);
		double x = e.getX();
		double y = e.getY();
		
		viewRegionManager.zoom(scaleFactor,x,y);
	}
	
	// </editor-fold>

	
	
	//=================================
	// Private Methods
	//=================================

	
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
                viewRegionManager.zoom(BUTTON_SCALE_FACTOR);
            }
        });
		Dimension zoomInSize = zoomInButton.getPreferredSize();
		
		zoomOutButton = new JButton();
		zoomOutButton.setText("-");
		zoomOutButton.setFont(font);
		zoomOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewRegionManager.zoom(1/BUTTON_SCALE_FACTOR);
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
			@Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
				Object selection = frameSelector.getSelectedItem();
				if(viewRegionManager != null) {
					if(selection instanceof MapLayer) {
						double angleRad = ((MapLayer)selection).getPrerredAngleRadians();
						viewRegionManager.setRotation(-angleRad);
					}
					else {
						//when north up is selected
						viewRegionManager.setRotation(0);
					}
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
