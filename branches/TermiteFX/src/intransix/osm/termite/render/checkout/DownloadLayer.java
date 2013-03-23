package intransix.osm.termite.render.checkout;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.gui.map.MapPane;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;

/**
 * This class allows the user to select a region of the map for which to download data.
 * @author sutter
 */
public class DownloadLayer extends MapLayer implements MapListener {
	
	//======================
	// Properties
	//======================
	private final static Color FILL_COLOR = Color.rgb(0,0,255,.25);
	private final static Color STROKE_COLOR = Color.rgb(0,0,255,.75);
	private final static float STROKE_WIDTH = 2;
	
	private double startX, startY;
	private Rectangle selection = new Rectangle();
	private boolean selectionActive = false;
	private boolean selecting = false;
	
	private AffineTransform pixelToMerc;
	
	private EventHandler<MouseEvent> mouseClickHandler;
	private EventHandler<MouseEvent> mouseMoveHandler;
	
	//======================
	// Public Methods
	//======================
	
	/** Constructor */
	public DownloadLayer() {
		this.setName("Checkout Search Layer");
		this.setOrder(MapLayer.ORDER_EDIT_MARKINGS);
		
		selection.setFill(FILL_COLOR);
		selection.setStroke(STROKE_COLOR);
	
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
	
	public void on(MapPane mapPane) {
		mapPane.addEventHandler(MouseEvent.MOUSE_CLICKED,mouseClickHandler);
		mapPane.addEventHandler(MouseEvent.MOUSE_MOVED,mouseMoveHandler);
	}
	
	public void off(MapPane mapPane) {
		mapPane.removeEventHandler(MouseEvent.MOUSE_CLICKED,mouseClickHandler);
		mapPane.removeEventHandler(MouseEvent.MOUSE_MOVED,mouseMoveHandler);
	}
	
		
	/** Returns the bounds of the current selection. Returns null if there is no selection. */
	public Bounds getSelectionBoundsMercator() {
		if(selectionActive) {
			return selection.getLayoutBounds();
		}
		else {
			return null;
		}
	}
	
	/** This method clears the selection. */
	public void clearSelection() {
		this.getChildren().remove(selection);
		selectionActive = false;
		selecting = false;
	}
	
	//-------------------------
	// Mouse Events
	//-------------------------
	
	/** Processes a mouse click. */
	public void mouseClicked(MouseEvent e) {
		Point2D point = new Point2D.Double(e.getX(),e.getY());
		pixelToMerc.transform(point,point);
		double mercX = point.getX();
		double mercY = point.getY();
		if(!selecting) {
			startX = mercX;
			startY = mercY;
			if(!selectionActive) {
				selectionActive = true;
				this.getChildren().add(selection);
			}
			updateSelection(mercX,mercY);
			selecting = true;
		}
		else {
			updateSelection(mercX,mercY);
			selecting = false;
		}
	}

	/** Processes a mouse move. */
	public void mouseMoved(MouseEvent e) {
		if(selecting) {
			Point2D point = new Point2D.Double(e.getX(),e.getY());
			pixelToMerc.transform(point,point);
			double mercX = point.getX();
			double mercY = point.getY();
			updateSelection(mercX,mercY);
		}
	}
	
	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onMapViewChange(ViewRegionManager viewRegionManager, boolean zoomChanged) {
		
		this.pixelToMerc = viewRegionManager.getPixelsToMercator();
		
		//update the stroke width to be the right nubmer of pixels
		if((zoomChanged)&&(selection != null)) {
			selection.setStrokeWidth(STROKE_WIDTH / viewRegionManager.getZoomScalePixelsPerMerc());
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
	}
	
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
//		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//			clearSelection();
//		}
//    }
//
//    /** Handle the key-released event from the text field. */
//    @Override
//	public void keyReleased(KeyEvent e) {
//    }
//	
//	// </editor-fold>


	//=================================
	// Private Methods
	//=================================	
	
	/** This method updates the selection for a new mouse point. */
	private void updateSelection(double mercX, double mercY) {
		selection.setX(startX < mercX ? startX : mercX);
		selection.setY(startY < mercY ? startY : mercY);
		selection.setWidth(Math.abs(mercX - startX));
		selection.setHeight(Math.abs(mercY - startY));
	}

}
