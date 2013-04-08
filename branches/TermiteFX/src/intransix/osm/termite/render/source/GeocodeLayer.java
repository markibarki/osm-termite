package intransix.osm.termite.render.source;


import intransix.osm.termite.app.geocode.AnchorPoint;
import intransix.osm.termite.app.geocode.AnchorPointListener;
import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.geocode.GeocodeManager;
import intransix.osm.termite.gui.mode.source.GeocodeEditorMode;
import intransix.osm.termite.app.geocode.GeocodeMouseAction;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.gui.map.MapPane;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Affine;

/**
 *
 * @author sutter
 */
public class GeocodeLayer extends MapLayer implements MapListener, AnchorPointListener {
	
	public final static double SNAP_RADIUS_PIXELS = 4;
	
	private GeocodeManager geocodeManager;
	private GeocodeEditorMode geocodeEditorMode;
	
	private EventHandler<MouseEvent> mouseClickHandler;
	private EventHandler<MouseEvent> mouseMoveHandler;
	
	private GeocodeMouseAction mouseAction;
	
	//local coordinate definitions
	private AffineTransform mercToLocal;
	private AffineTransform localToMerc;
	private AffineTransform pixelToMerc;
	private double pixelsToLocalScale = 1.0;
	private double pixelsToMercatorScale = 1.0;
	
	private List<AnchorPointGraphic> anchorPointGraphics = new ArrayList<>();
	
	public GeocodeLayer() {
		this.setName("Geocode Layer");
		this.setOrder(MapLayer.ORDER_EDIT_MARKINGS);
		
		mouseClickHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(e.getButton() == MouseButton.PRIMARY) {
					mouseClicked(e);
				}
			}
		};
		
		mouseMoveHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				mouseMoved(e);
			}
		};
	}
	
	public void anchorPointsChanged(AnchorPoint[] anchorPoints) {
		AnchorPointGraphic apg;
		Point2D mercPoint;
		Point2D localPoint = new Point2D.Double();
		
		this.anchorPointGraphics.clear();
		for(AnchorPoint anchorPoint:anchorPoints) {
			apg = (AnchorPointGraphic)anchorPoint.anchorPointGraphic;
			if(anchorPoint.isActive) {
				if(apg == null) {
					apg = new AnchorPointGraphic();
					anchorPoint.anchorPointGraphic = apg;
				}
				mercPoint = anchorPoint.mercPoint;
				this.mercToLocal.transform(mercPoint, localPoint);
				apg.setCenterX(localPoint.getX());
				apg.setCenterY(localPoint.getY());
				apg.setRadius(AnchorPoint.RADIUS_PIX * this.pixelsToLocalScale);
				this.anchorPointGraphics.add(apg);
			}
		}
		
		this.getChildren().setAll(anchorPointGraphics);
	}
	
//	/** This adds an anchor point to the layer. */
//	public void addAnchorPoint(AnchorPointGraphic anchorPoint) {
//		this.getChildren().add(anchorPoint);
//	}
//	
//	public void removeAnchorPoint(AnchorPointGraphic anchorPoint) {
//		this.getChildren().remove(anchorPoint);
//	}
//
//	/** This method clears the anchor points from the level. */
//	public void clearAnchorPoints() {
//		this.getChildren().clear();
//	}
	
	public void setGeocodeManager(GeocodeManager geocodeManager) {
		this.geocodeManager = geocodeManager;
	}
	
	public void setGeocodeEditorMode(GeocodeEditorMode geocodeEditorMode) {
		this.geocodeEditorMode = geocodeEditorMode;
	}
	
	public void setMouseAction(GeocodeMouseAction mouseAction) {
		this.mouseAction = mouseAction;
	}
	
	public void on(MapPane mapPane) {
		mapPane.addEventHandler(MouseEvent.MOUSE_CLICKED,mouseClickHandler);
		mapPane.addEventHandler(MouseEvent.MOUSE_MOVED,mouseMoveHandler);
	}
	
	public void off(MapPane mapPane) {
		mapPane.removeEventHandler(MouseEvent.MOUSE_CLICKED,mouseClickHandler);
		mapPane.removeEventHandler(MouseEvent.MOUSE_MOVED,mouseMoveHandler);
	}
	
	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onMapViewChange(ViewRegionManager viewRegionManager, boolean zoomChanged) {
		//update the transformation for the mouse
		this.pixelToMerc = viewRegionManager.getPixelsToMercator();
		
		//update the stroke values
		if(zoomChanged) {
			pixelsToMercatorScale = viewRegionManager.getZoomScaleMercPerPixel();
			pixelsToLocalScale = viewRegionManager.getZoomScaleLocalPerPixel();

			if(geocodeManager != null) {
				for(AnchorPointGraphic apg:anchorPointGraphics) {
					apg.updateScale(pixelsToLocalScale);
				}
			}
		}
	}
	
	@Override
	public void onPanStart(ViewRegionManager vrm) {}
	
	@Override
	public void onPanStep(ViewRegionManager vrm) {}
	
	@Override
	public void onPanEnd(ViewRegionManager vrm) {}
	
	@Override
	public void onLocalCoordinatesSet(ViewRegionManager vrm) {
		this.mercToLocal = vrm.getMercatorToLocal();
		this.localToMerc = vrm.getLocalToMercator();
		this.pixelsToLocalScale = vrm.getZoomScaleLocalPerPixel();
		Affine localToMercFX = vrm.getLocalToMercatorFX();
		this.getTransforms().setAll(localToMercFX);
	}
	
//	@Override
//	public void render(Graphics2D g2) {
//		AffineTransform mercToPixels = getViewRegionManager().getMercatorToPixels();
//		
//		GeocodeEditorMode.LayerState layerState = geocodeEditorMode.getLayerState();
//		AnchorPoint[] anchorPoints = geocodeManager.getAnchorPoints();
//		int selection = geocodeManager.getSelection();
//		AffineTransform moveImageToMerc = geocodeManager.getMoveImageToMerc();
//		
//		//draw the points
//		AnchorPoint ap;
//		boolean isSelected;
//		boolean inMove = (layerState == GeocodeEditorMode.LayerState.MOVE);
//		for(int i = 0; i < 3; i++) {
//			ap = anchorPoints[i];
//			if(ap.mercPoint != null) {
//				isSelected = (selection == i);
//				ap.renderPoint(g2,mercToPixels,isSelected,inMove,moveImageToMerc);
//			}
//		}
//	}
//	
//		
//	// <editor-fold defaultstate="collapsed" desc="Mouse Listeners">
	
	public void mouseClicked(MouseEvent e) {
		Point2D mouseMerc = new Point2D.Double(e.getX(),e.getY());
		pixelToMerc.transform(mouseMerc,mouseMerc);
		if(e.getButton() == MouseButton.PRIMARY) {
			if(mouseAction != null) {
			
				//let the mouse edit action handle the press
				mouseAction.mousePressed(mouseMerc, pixelsToMercatorScale,e);
			}
		}
	}
	
	public void mouseMoved(MouseEvent e) {
		if((mouseAction != null)&&(mouseAction.doMove())) {
			//read mouse location in global coordinates
			Point2D mouseMerc = new Point2D.Double(e.getX(),e.getY());
			pixelToMerc.transform(mouseMerc,mouseMerc);
			
			mouseAction.mouseMoved(mouseMerc, pixelsToMercatorScale, e);
		}
	}

//	// </editor-fold>
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
//		//if we are in inactive
//		if(geocodeEditorMode.getLayerState() == GeocodeEditorMode.LayerState.INACTIVE) return;
//			
//		if(e.getKeyCode() == KeyEvent.VK_M) {
//			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.MOVE);
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_1) {
//			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.PLACE_P0);
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_2) {
//			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.PLACE_P1);
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_3) {
//			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.PLACE_P2);
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.SELECT);
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
//	/** This mode sets the edit layer active. */
//	@Override
//	public void setActiveState(boolean isActive) {
//		super.setActiveState(isActive);
//		MapPanel mapPanel = this.getMapPanel();
//		if(mapPanel != null) {
//			if(isActive) {
//				//activate the mouse listeners
//				mapPanel.addMouseListener(this);
//				mapPanel.addMouseMotionListener(this);
//				mapPanel.addKeyListener(this);
//			}
//			else {
//				mapPanel.removeMouseListener(this);
//				mapPanel.removeMouseMotionListener(this);
//				mapPanel.removeKeyListener(this);
//			}
//		}
//	}
		
	//=====================
	// Private Methods
	//=====================
	
}
