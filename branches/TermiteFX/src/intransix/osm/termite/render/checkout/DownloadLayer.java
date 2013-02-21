package intransix.osm.termite.render.checkout;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.maplayer.PaneLayer;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.render.tile.Tile;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Affine;
//import java.awt.Graphics2D;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import java.awt.event.MouseMotionListener;
//import java.awt.geom.AffineTransform;
//import java.awt.*;
//import java.awt.event.*;
//import java.awt.geom.*;

/**
 *
 * @author sutter
 */
public class DownloadLayer extends PaneLayer implements MapListener {
	
	private final static Color FILL_COLOR = Color.rgb(0,0,255,.25);
	private final static Color STROKE_COLOR = Color.rgb(0,0,255,.75);
	private final static float STROKE_WIDTH = 2;
	
	private double startX, startY;
	private Rectangle selection;
	private boolean selecting = false;
	private MapLayerManager mapLayerManager;
	private EventHandler<MouseEvent> mouseClickHandler;
	private EventHandler<MouseEvent> mouseMoveHandler;
	
	public void connect(MapLayerManager mapLayerManager){
		this.mapLayerManager = mapLayerManager;
	}
	
	public void disconnect(MapLayerManager mapLayerManager){
		this.mapLayerManager = null;
	}
	
	public Bounds getSelectionBoundsMercator() {
		return selection.getLayoutBounds();
	}
	
	public DownloadLayer() {
		this.setName("Checkout Search Layer");
		this.setOrder(MapLayer.ORDER_EDIT_MARKINGS);
		this.setVisible(false);
		
		this.setPrefSize(1.0,1.0);
		this.setMinSize(1.0,1.0);
		this.setMaxSize(1.0,1.0);
		
		mouseClickHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if(e.getButton() == MouseButton.PRIMARY) {
					e = (MouseEvent)e.copyFor(DownloadLayer.this,DownloadLayer.this);
					double mercX = e.getX();
					double mercY = e.getY();

					mouseClicked(mercX,mercY);
				}
			}
		};
		
		mouseMoveHandler = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				e = (MouseEvent)e.copyFor(DownloadLayer.this,DownloadLayer.this);
				double mercX = e.getX();
				double mercY = e.getY();
				mouseMoved(mercX,mercY);
			}
		};
		
		selection = new Rectangle();
selection.setFill(Color.BLUE);
//		selection.setFill(FILL_COLOR);
//		selection.setStroke(STROKE_COLOR);
//		selection.setStrokeWidth(0.01);
		
		temp = new Rectangle(.1,.1,.4,.5);
		temp.setFill(Color.RED);
		this.getChildren().add(temp);
		
		this.setStyle("-fx-background-color: yellow;");
	}
Rectangle temp;
	
	/** This mode sets the edit layer active. */
	@Override
	public void setActiveState(boolean isActive) {
		super.setActiveState(isActive);
		if(mapLayerManager != null) {
			if(isActive) {
				mapLayerManager.addMouseEventHandler(MouseEvent.MOUSE_CLICKED,mouseClickHandler);
				mapLayerManager.addMouseEventHandler(MouseEvent.MOUSE_MOVED,mouseMoveHandler);
			}
			else {
				mapLayerManager.removeMouseEventHandler(MouseEvent.MOUSE_CLICKED,mouseClickHandler);
				mapLayerManager.removeMouseEventHandler(MouseEvent.MOUSE_MOVED,mouseMoveHandler);
			}
		}
	}
	
	/** This method clears the selection. */
	public void clearSelection() {
		this.getChildren().remove(selection);
		selecting = false;
		this.notifyContentChange();
	}
	
	//-------------------------
	// Mouse Events
	//-------------------------
	
	public void mouseClicked(double mercX, double mercY) {
		if(!selecting) {
			startX = mercX;
			startY = mercY;
			updateSelection(mercX,mercY);
			this.getChildren().add(selection);
			selecting = true;
		}
		else {
			updateSelection(mercX,mercY);
			this.getChildren().remove(selection);
			selecting = false;
		}

temp.setX(temp.getX() + .1);
		notifyContentChange();
	}

	public void mouseMoved(double mercX, double mercY) {
		if(selecting) {
			updateSelection(mercX,mercY);
			notifyContentChange();
		}
	}
	
		//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onZoom(ViewRegionManager vrm) {		
		updateTransform(vrm);
	}
	
	@Override
	public void onPanStart(ViewRegionManager vrm) {}
	
	@Override
	public void onPanStep(ViewRegionManager vrm) {
		updateTransform(vrm);
	}
	
	@Override
	public void onPanEnd(ViewRegionManager vrm) {
		updateTransform(vrm);
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
	
	private void updateTransform(ViewRegionManager viewRegionManager) {
		Affine mercToPixelsFX = viewRegionManager.getMercatorToPixelsFX();
		this.getTransforms().setAll(mercToPixelsFX); 
	}
	/** This method updates the selection for a new mouse point. */
	private void updateSelection(double mercX, double mercY) {
		selection.setX(startX < mercX ? startX : mercX);
		selection.setY(startX < mercX ? startX : mercX);
		selection.setWidth(Math.abs(mercX - startX));
		selection.setHeight(Math.abs(mercY - startY));
System.out.println("Selection: " + selection.getX() + "," + selection.getY() + "," + 
		selection.getWidth() + "," + selection.getHeight());
		
	}
}