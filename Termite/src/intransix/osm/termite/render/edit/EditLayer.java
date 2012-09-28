package intransix.osm.termite.render.edit;

import intransix.osm.termite.app.edit.data.VirtualNode;
import intransix.osm.termite.app.edit.action.*;
import intransix.osm.termite.app.edit.*;
import intransix.osm.termite.app.edit.editobject.EditObject;
import intransix.osm.termite.app.edit.snapobject.SnapObject;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.event.*;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.app.maplayer.MapLayer;
import java.util.List;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.viewregion.ViewRegionManager;

/**
 * This layer controls the user interaction with the active map data. It is designed
 * to run with the editor modes for the Select Tool, Node Tool and Way Tool.
 * 
 * @author sutter
 */
public class EditLayer extends MapLayer implements  
		MouseListener, MouseMotionListener, KeyListener {
	
	//=========================
	// Properties 
	//=========================
	
	// <editor-fold defaultstate="collapsed" desc="Properties">
	
	public final static double SNAP_RADIUS_PIXELS = 4;
	
	private EditManager editManager;
	
	private OsmData osmData;
	private StyleInfo styleInfo = new StyleInfo();

	private MouseClickAction mouseClickAction;
	private MouseMoveAction moveMouseMoveAction;
	private MouseMoveAction snapMouseMoveAction;
	
	// </editor-fold>
	
	//=========================
	// Public Methods
	//=========================
	
	public EditLayer(EditManager editManager) {
		this.editManager = editManager;
		this.setName("Edit Layer");
		this.setOrder(MapLayer.ORDER_EDIT_MARKINGS);
	}
	
	// <editor-fold defaultstate="collapsed" desc="Accessors">
	
	/** This method sets the edit mode. */
	public void setMouseClickAction(MouseClickAction mouseClickAction) {
		this.mouseClickAction = mouseClickAction;
	}
	
	public void setMouseMoveActions(MouseMoveAction moveMouseMoveAction,
		MouseMoveAction snapMouseMoveAction) {
		this.moveMouseMoveAction = moveMouseMoveAction;
		this.snapMouseMoveAction = snapMouseMoveAction;
	}
	
	/** This mode sets the edit layer active. */
	@Override
	public void setActiveState(boolean isActive) {
		super.setActiveState(isActive);
		MapPanel mapPanel = this.getMapPanel();
		if(mapPanel != null) {
			if(isActive) {
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
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Render">
	
	/** This method renders the edit state. */
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform mercatorToPixels = getViewRegionManager().getMercatorToPixels();	
		Style style;
		
		//render selection
		List<Object> selection = editManager.getSelection();
		List<Integer> selectedWayNodes = editManager.getSelectedWayNodes();
		
		style = styleInfo.SELECT_STYLE;
		for(Object selectObject:selection) {
			if(selectObject instanceof OsmNode) {
				EditDrawable.renderPoint(g2, mercatorToPixels, ((OsmNode)selectObject).getPoint(),style);
			}
			else if(selectObject instanceof OsmWay) {
				EditDrawable.renderWay(g2, mercatorToPixels,(OsmWay)selectObject,style);
				
				//if this is a unique selected way, plot the selected nodes in the way
				if(selection.size() == 1) {
					OsmWay way = (OsmWay)selectObject;
					for(Integer index:selectedWayNodes) {
						if((index > -1)&&(index < way.getNodes().size())) {
							OsmNode node = way.getNodes().get(index);
							EditDrawable.renderPoint(g2, mercatorToPixels, node.getPoint(),style);
						}
					}
				}
			}
			else if(selectObject instanceof VirtualNode) {
				EditDrawable.renderPoint(g2, mercatorToPixels,((VirtualNode)selectObject).point,style);
			}
		}
		
		//render hover
		List<SnapObject> snapObjects = editManager.getSnapObjects();
		int activeSnapObject = editManager.getActiveSnapObject();
		
		if((activeSnapObject != -1)&&(activeSnapObject < snapObjects.size())) {
//System.out.println(("cnt = " + snapObjects.size() + "; active = " + activeSnapObject));
			SnapObject snapObject = snapObjects.get(activeSnapObject);
//System.out.println(snapObject);
			snapObject.render(g2, mercatorToPixels,styleInfo);
		}
		
		//render pending objects
		List<EditObject> pendingObjects = editManager.getPendingObjects();
		
		for(EditObject editObject:pendingObjects) {
			editObject.render(g2, mercatorToPixels, styleInfo);
		}
	}
	
	// </editor-fold>
	
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
		editManager.clearPreview();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		
		//read mouse location in global coordinates
		ViewRegionManager viewRegionManager = getViewRegionManager();
		AffineTransform pixelsToMercator = viewRegionManager.getPixelsToMercator();
		Point2D mouseMerc = new Point2D.Double(e.getX(),e.getY());
		pixelsToMercator.transform(mouseMerc, mouseMerc);
		double scalePixelsPerMerc = viewRegionManager.getZoomScalePixelsPerMerc();
		double mercRad = SNAP_RADIUS_PIXELS / scalePixelsPerMerc;
		double mercRadSq = mercRad * mercRad;
		
		//handle a move preview
		if(moveMouseMoveAction != null) {
			moveMouseMoveAction.mouseMoved(mouseMerc,mercRadSq,e);
		}
		
		//get the snap nodes for the move
		if(snapMouseMoveAction != null) {
			snapMouseMoveAction.mouseMoved(mouseMerc,mercRadSq,e);
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		AffineTransform pixelsToMercator = getViewRegionManager().getPixelsToMercator();
		Point2D mouseMerc = new Point2D.Double(e.getX(),e.getY());
		pixelsToMercator.transform(mouseMerc, mouseMerc);
		
		if(e.getButton() == MouseEvent.BUTTON1) {
			if(mouseClickAction != null) {
				//let the mouse edit action handle the press
				mouseClickAction.mousePressed(mouseMerc,e);
			}
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Key Listener and Focus Listener">
	
	/** Handle the key typed event from the text field. */
    @Override
	public void keyTyped(KeyEvent e) {
    }

    /** Handle the key-pressed event from the text field. */
	@Override
    public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_COMMA) {
			SnapSelectAction snapSelectAction = new SnapSelectAction(editManager);
			snapSelectAction.nextSnapOject();
		}
		else if(e.getKeyCode() == KeyEvent.VK_PERIOD) {
			SnapSelectAction snapSelectAction = new SnapSelectAction(editManager);
			snapSelectAction.previousSnapObject();
		}
		else if(e.getKeyCode() == KeyEvent.VK_M) {
			SelectEditorMode sem = editManager.getSelectEditorMode();
			if(sem.getModeEnabled()) {
				if(!sem.isInMoveState()) {
					sem.setMoveState();
				}
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			SelectEditorMode sem = editManager.getSelectEditorMode();
			if(sem.getModeEnabled()) {
				if(sem.isInMoveState()) {
					sem.setSelectState();
				}
			}
			else {
				WayEditorMode wem = editManager.getWayEditorMode();
				if(wem.getModeEnabled()) {
					wem.resetWayEdit();
				}
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
			DeleteSelectionAction deleteSelectionAction = new DeleteSelectionAction(editManager);
			deleteSelectionAction.deleteSelection();
		}
    }

    /** Handle the key-released event from the text field. */
    @Override
	public void keyReleased(KeyEvent e) {
    }
	
	// </editor-fold>


	//============================
	// Private Methods
	//============================

	


}
