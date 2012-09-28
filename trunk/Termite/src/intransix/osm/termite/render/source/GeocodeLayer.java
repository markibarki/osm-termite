package intransix.osm.termite.render.source;


import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.app.geocode.GeocodeManager;
import intransix.osm.termite.app.geocode.GeocodeEditorMode;
import intransix.osm.termite.app.geocode.GeocodeMouseAction;
import intransix.osm.termite.render.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.*;

/**
 *
 * @author sutter
 */
public class GeocodeLayer extends MapLayer implements
		MouseListener, MouseMotionListener, KeyListener {
	
	private GeocodeManager geocodeManager;
	private GeocodeEditorMode geocodeEditorMode;
	
	private GeocodeMouseAction mouseAction;
	
	public GeocodeLayer(GeocodeManager geocodeManager) {
		this.geocodeManager = geocodeManager;
		
		this.setName("Geocode Layer");
		this.setOrder(MapLayer.ORDER_EDIT_MARKINGS);
	}
	
	public void setGeocodeEditorMode(GeocodeEditorMode geocodeEditorMode) {
		this.geocodeEditorMode = geocodeEditorMode;
	}
	
	public void setMouseAction(GeocodeMouseAction mouseAction) {
		this.mouseAction = mouseAction;
		mouseAction.init(this);
	}
	
	@Override
	public void render(Graphics2D g2) {
		AffineTransform mercToPixels = getViewRegionManager().getMercatorToPixels();
		
		GeocodeEditorMode.LayerState layerState = geocodeEditorMode.getLayerState();
		AnchorPoint[] anchorPoints = geocodeManager.getAnchorPoints();
		int selection = geocodeManager.getSelection();
		AffineTransform moveImageToMerc = geocodeManager.getMoveImageToMerc();
		
		//draw the points
		AnchorPoint ap;
		boolean isSelected;
		boolean inMove = (layerState == GeocodeEditorMode.LayerState.MOVE);
		for(int i = 0; i < 3; i++) {
			ap = anchorPoints[i];
			if(ap.mercPoint != null) {
				isSelected = (selection == i);
				ap.renderPoint(g2,mercToPixels,isSelected,inMove,moveImageToMerc);
			}
		}
	}
	
		
	// <editor-fold defaultstate="collapsed" desc="Mouse Listeners">
	
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		//no edit move with mouse drag - explicit move command needed
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		if((mouseAction != null)&&(mouseAction.doMove())) {
			Point2D mouseMerc = new Point2D.Double();
			ViewRegionManager viewRegionManager = getViewRegionManager();
			AffineTransform pixelsToMercator = viewRegionManager.getPixelsToMercator();
			mouseMerc.setLocation(e.getX(),e.getY());
			pixelsToMercator.transform(mouseMerc,mouseMerc);
			mouseAction.mouseMoved(mouseMerc, e);
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() != MouseEvent.BUTTON1) return;
		
		if(mouseAction != null) {
			Point2D mouseMerc = new Point2D.Double();
			ViewRegionManager viewRegionManager = getViewRegionManager();
			AffineTransform pixelsToMercator = viewRegionManager.getPixelsToMercator();
			mouseMerc.setLocation(e.getX(),e.getY());
			pixelsToMercator.transform(mouseMerc,mouseMerc);
			mouseAction.mousePressed(mouseMerc, e);
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
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
		//if we are in inactive
		if(geocodeEditorMode.getLayerState() == GeocodeEditorMode.LayerState.INACTIVE) return;
			
		if(e.getKeyCode() == KeyEvent.VK_M) {
			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.MOVE);
		}
		else if(e.getKeyCode() == KeyEvent.VK_1) {
			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.PLACE_P0);
		}
		else if(e.getKeyCode() == KeyEvent.VK_2) {
			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.PLACE_P1);
		}
		else if(e.getKeyCode() == KeyEvent.VK_3) {
			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.PLACE_P2);
		}
		else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.SELECT);
		}
    }

    /** Handle the key-released event from the text field. */
    @Override
	public void keyReleased(KeyEvent e) {
    }
	
	// </editor-fold>
	
	/** This mode sets the edit layer active. */
	@Override
	public void setActiveState(boolean isActive) {
		super.setActiveState(isActive);
		MapPanel mapPanel = this.getMapPanel();
		if(mapPanel != null) {
			if(isActive) {
				//activate the mouse listeners
				mapPanel.addMouseListener(this);
				mapPanel.addMouseMotionListener(this);
				mapPanel.addKeyListener(this);
			}
			else {
				mapPanel.removeMouseListener(this);
				mapPanel.removeMouseMotionListener(this);
				mapPanel.removeKeyListener(this);
			}
		}
	}
		
	//=====================
	// Private Methods
	//=====================
	
}
