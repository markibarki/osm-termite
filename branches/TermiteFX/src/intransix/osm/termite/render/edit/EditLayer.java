package intransix.osm.termite.render.edit;

import intransix.osm.termite.app.edit.*;
import intransix.osm.termite.app.edit.data.VirtualNode;
import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.edit.editobject.EditNode;
import intransix.osm.termite.app.edit.editobject.EditObject;
import intransix.osm.termite.app.edit.editobject.EditSegment;
import intransix.osm.termite.app.edit.editobject.EditVirtualNode;
import intransix.osm.termite.app.edit.snapobject.SnapIntersection;
import intransix.osm.termite.app.edit.snapobject.SnapNode;
import intransix.osm.termite.app.edit.snapobject.SnapObject;
import intransix.osm.termite.app.edit.snapobject.SnapObject.SnapType;
import intransix.osm.termite.app.edit.snapobject.SnapSegment;
import intransix.osm.termite.app.edit.snapobject.SnapVirtualNode;
import intransix.osm.termite.app.edit.snapobject.SnapWay;
import intransix.osm.termite.app.viewregion.MapListener;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.workingdata.OsmWay;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * This layer controls the user interaction with the active map data. It is designed
 * to run with the editor modes for the Select Tool, Node Tool and Way Tool.
 * 
 * @author sutter
 */
public class EditLayer extends MapLayer implements FeatureSelectedListener, EditObjectChangedListener, MapListener {
	
	//=========================
	// Properties 
	//=========================
	
	// <editor-fold defaultstate="collapsed" desc="Properties">
	
	public final static double SNAP_RADIUS_PIXELS = 4;
	
	private ViewRegionManager viewRegionManager;
	
	private StyleInfo styleInfo = new StyleInfo();

	private EventHandler<MouseEvent> mouseClickHandler;
	private EventHandler<MouseEvent> mouseMoveHandler;
	
	private MouseClickAction mouseClickAction;
	private MouseMoveAction moveMouseMoveAction;
	private MouseMoveAction snapMouseMoveAction;
	
	private List<Node> selectObjects = new ArrayList<>();
	private List<Node> activeSnapObjects = new ArrayList<>();
	private List<Node> pendingObjects = new ArrayList<>();
	
	//initialize this with a dummy value
	private double pixelsToMerc = 1;
	
	Point2D latestMouseMerc = null;
	
	// </editor-fold>
	
	//=========================
	// Public Methods
	//=========================
	
	public EditLayer(ViewRegionManager viewRegionManager) {
		this.setName("Edit Layer");
		this.setOrder(MapLayer.ORDER_EDIT_MARKINGS);
		this.viewRegionManager = viewRegionManager;
		
		this.setPrefSize(1.0,1.0);
		this.setMinSize(1.0,1.0);
		this.setMaxSize(1.0,1.0);
		
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

	
	// <editor-fold defaultstate="collapsed" desc="Listener Implementation">
	
	@Override
	public void activeSnapObjectChanged(SnapObject snapObject) {
		List<Node> newSnapObjects = new ArrayList<>();
		
		if(snapObject instanceof SnapIntersection) {
			SnapSegment s1 = ((SnapIntersection)snapObject).s1;
			SnapSegment s2 = ((SnapIntersection)snapObject).s2;
			SegmentGraphic sg1 = new SegmentGraphic(s1.p1,s1.p2);
			SegmentGraphic sg2 = new SegmentGraphic(s2.p1,s2.p2);
			
			sg1.setStyle(getSegmentHoverStyle(s1),pixelsToMerc);
			sg2.setStyle(getSegmentHoverStyle(s2),pixelsToMerc);

			newSnapObjects.add(sg1);
			newSnapObjects.add(sg2);
		}
		else if(snapObject instanceof SnapNode) {
			NodeGraphic ng = new NodeGraphic(((SnapNode)snapObject).node);
			
			ng.setStyle(styleInfo.HOVER_SEGMENT_STYLE,pixelsToMerc);
			
			newSnapObjects.add(ng);
		}
		else if(snapObject instanceof SnapSegment) {
			SnapSegment s = ((SnapIntersection)snapObject).s1;
			SegmentGraphic sg = new SegmentGraphic(s.p1,s.p2);
			
			sg.setStyle(getSegmentHoverStyle(s),pixelsToMerc);
			
			newSnapObjects.add(sg);
		}
		else if(snapObject instanceof SnapVirtualNode) {
			NodeGraphic ng = new NodeGraphic(((SnapVirtualNode)snapObject).snapPoint);
			
			ng.setStyle(styleInfo.HOVER_SEGMENT_STYLE,pixelsToMerc);
			
			newSnapObjects.add(ng);
		}
		else if(snapObject instanceof SnapWay) {
			WayGraphic ng = new WayGraphic(((SnapWay)snapObject).way);
			
			ng.setStyle(styleInfo.HOVER_SEGMENT_STYLE,pixelsToMerc);
			
			newSnapObjects.add(ng);
		}
		
		this.getChildren().removeAll(activeSnapObjects);
		activeSnapObjects = newSnapObjects;
		this.getChildren().addAll(activeSnapObjects);
	}
	
	/** This determines the style for a segment. */
	private Style getSegmentHoverStyle(SnapSegment snapSegment) {
		if((snapSegment.snapType == SnapType.SEGMENT_EXT)||
				(snapSegment.snapType == SnapType.SEGMENT_PERP)) {
			return styleInfo.HOVER_EXTENSION_STYLE;
		}
		else {
			return styleInfo.HOVER_SEGMENT_STYLE;
		}
	}
	
	@Override
	public void pendingListChanged(List<EditObject> editObjects) {
		List<Node> newEditObjects = new ArrayList<>();
		
		Style style = styleInfo.PENDING_STYLE;
		
		for(EditObject editObject:editObjects) {

			if(editObject instanceof EditNode) {
				NodeGraphic ng = new NodeGraphic(((EditNode)editObject).node);
				ng.setStyle(style,this.pixelsToMerc);
				newEditObjects.add(ng);
			}
			else if(editObject instanceof EditSegment) {
				SegmentGraphic sg = new SegmentGraphic(((EditSegment)editObject).en1.node.getPoint(),((EditSegment)editObject).en2.node.getPoint());
				sg.setStyle(style,this.pixelsToMerc);
				newEditObjects.add(sg);
			}
			else if(editObject instanceof EditVirtualNode) {
				NodeGraphic ng = new NodeGraphic(((EditVirtualNode)editObject).enVirtual.point);
				ng.setStyle(style,this.pixelsToMerc);
				newEditObjects.add(ng);
			}
		}
		
		this.getChildren().removeAll(pendingObjects);
		pendingObjects = newEditObjects;
		this.getChildren().addAll(pendingObjects);
	}
	
	@Override
	public void onFeatureSelected(List<Object> selection, List<Integer> wayNodeSelection) {
		List<Node> newSelectObjects = new ArrayList<>();
		
		Style style = styleInfo.PENDING_STYLE;
		
		//main selection objects
		for(Object object:selection) {
			if(object instanceof OsmNode) {
				NodeGraphic ng = new NodeGraphic((OsmNode)object);
				ng.setStyle(style,this.pixelsToMerc);
				newSelectObjects.add(ng);
			}
			else if(object instanceof OsmWay) {
				WayGraphic wg = new WayGraphic((OsmWay)object);
				wg.setStyle(style,this.pixelsToMerc);
				newSelectObjects.add(wg);
			}
			else if(object instanceof VirtualNode) {
				NodeGraphic ng = new NodeGraphic(((VirtualNode)object).point);
				ng.setStyle(style,this.pixelsToMerc);
				newSelectObjects.add(ng);
			}
		}
		
		//add selected nodes in way
		if((wayNodeSelection != null)&&(selection.size() == 1)) {
			Object object = selection.get(0);
			if(object instanceof OsmWay) {
				List<OsmNode> nodes = ((OsmWay)object).getNodes();
				OsmNode node;
				for(Integer i:wayNodeSelection) {
					if((i >= 0)&&(i < nodes.size())) {
						node = nodes.get(i);
						NodeGraphic ng = new NodeGraphic((OsmNode)object);
						ng.setStyle(style,this.pixelsToMerc);
						newSelectObjects.add(ng);
					}
				}
			}
		}
		
		this.getChildren().removeAll(selectObjects);
		selectObjects = newSelectObjects;
		this.getChildren().addAll(selectObjects);
	}
	
	//--------------------------
	// MapListener Interface
	//--------------------------
	
	/** This method updates the active tile zoom used by the map. */
	@Override
	public void onMapViewChange(ViewRegionManager viewRegionManager, boolean zoomChanged) {		
		//update the stroke values
		if(zoomChanged) {
			this.pixelsToMerc = viewRegionManager.getZoomScaleMercPerPixel();
			
			if(this.selectObjects != null) {
				for(Node node:selectObjects) {
					((ShapeGraphic)node).setPixelsToMerc(pixelsToMerc);
				}
			}
			if(this.pendingObjects != null) {
				for(Node node:pendingObjects) {
					((ShapeGraphic)node).setPixelsToMerc(pixelsToMerc);
				}
			}
			if(this.activeSnapObjects != null) {
				for(Node node:activeSnapObjects) {
					((ShapeGraphic)node).setPixelsToMerc(pixelsToMerc);
				}
			}
			
			
//			this.setPixelsToMercScale(viewRegionManager.getZoomScaleMercPerPixel());
//			for(Node node:getChildren()) {
//				if(node instanceof Feature) {
//					((Feature)node).setPixelsToLocal(pixelsToLocalScale);
//				}
//			}
		}
	}
	
	@Override
	public void onPanStart(ViewRegionManager vrm) {}
	
	@Override
	public void onPanStep(ViewRegionManager vrm) {}
	
	@Override
	public void onPanEnd(ViewRegionManager vrm) {}
	
	// </editor-fold>
	
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
//		MapPanel mapPanel = this.getMapPanel();
//		if(mapPanel != null) {
//			if(isActive) {
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
		if(isActive) {
			this.addEventHandler(MouseEvent.MOUSE_CLICKED,mouseClickHandler);
			this.addEventHandler(MouseEvent.MOUSE_MOVED,mouseMoveHandler);
			this.setVisible(true);
		}
		else {
			this.removeEventHandler(MouseEvent.MOUSE_CLICKED,mouseClickHandler);
			this.removeEventHandler(MouseEvent.MOUSE_MOVED,mouseMoveHandler);
			//clear the last point received
			latestMouseMerc = null;
			this.setVisible(false);
		}
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Render">
	
	/** This method renders the edit state. */
//	@Override
//	public void render(Graphics2D g2) {
//		
//		AffineTransform mercatorToPixels = getViewRegionManager().getMercatorToPixels();	
//		Style style;
//		
//		//render selection
//		List<Object> selection = editManager.getSelection();
//		List<Integer> selectedWayNodes = editManager.getSelectedWayNodes();
//		
//		style = styleInfo.SELECT_STYLE;
//		for(Object selectObject:selection) {
//			if(selectObject instanceof OsmNode) {
//				EditDrawable.renderPoint(g2, mercatorToPixels, ((OsmNode)selectObject).getPoint(),style);
//			}
//			else if(selectObject instanceof OsmWay) {
//				EditDrawable.renderWay(g2, mercatorToPixels,(OsmWay)selectObject,style);
//				
//				//if this is a unique selected way, plot the selected nodes in the way
//				if(selection.size() == 1) {
//					OsmWay way = (OsmWay)selectObject;
//					for(Integer index:selectedWayNodes) {
//						if((index > -1)&&(index < way.getNodes().size())) {
//							OsmNode node = way.getNodes().get(index);
//							EditDrawable.renderPoint(g2, mercatorToPixels, node.getPoint(),style);
//						}
//					}
//				}
//			}
//			else if(selectObject instanceof VirtualNode) {
//				EditDrawable.renderPoint(g2, mercatorToPixels,((VirtualNode)selectObject).point,style);
//			}
//		}
//		
//		//render hover
//		List<SnapObject> snapObjects = editManager.getSnapObjects();
//		int activeSnapObject = editManager.getActiveSnapObject();
//		
//		if((activeSnapObject != -1)&&(activeSnapObject < snapObjects.size())) {
////System.out.println(("cnt = " + snapObjects.size() + "; active = " + activeSnapObject));
//			SnapObject snapObject = snapObjects.get(activeSnapObject);
////System.out.println(snapObject);
//			snapObject.render(g2, mercatorToPixels,styleInfo);
//		}
//		
//		//render pending objects
//		List<EditObject> pendingObjects = editManager.getPendingObjects();
//		
//		for(EditObject editObject:pendingObjects) {
//			editObject.render(g2, mercatorToPixels, styleInfo);
//		}
//	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Mouse Listeners">
	

	public void mouseClicked(MouseEvent e) {
		Point2D mouseMerc = new Point2D.Double(e.getX(),e.getY());
		if(e.getButton() == MouseButton.PRIMARY) {
			if(mouseClickAction != null) {
				//let the mouse edit action handle the press
				mouseClickAction.mousePressed(mouseMerc,e);
			}
		}
	}
//	
//	@Override
//	public void mouseDragged(MouseEvent e) {
//		//no edit move with mouse drag - explicit move command needed
//	}
//	
//	@Override
//	public void mouseEntered(MouseEvent e) {
//	}
//	
//	@Override
//	public void mouseExited(MouseEvent e) {
//		editManager.clearPreview();
//	}
//	
//	@Override
	public void mouseMoved(MouseEvent e) {
		
		//read mouse location in global coordinates
		latestMouseMerc = new Point2D.Double(e.getX(),e.getY());
		double scalePixelsPerMerc = viewRegionManager.getZoomScalePixelsPerMerc();
		double mercRad = SNAP_RADIUS_PIXELS / scalePixelsPerMerc;
		double mercRadSq = mercRad * mercRad;
		
		//handle a move preview
		if(moveMouseMoveAction != null) {
			moveMouseMoveAction.mouseMoved(latestMouseMerc,mercRadSq,e);
		}
		
		//get the snap nodes for the move
		if(snapMouseMoveAction != null) {
			snapMouseMoveAction.mouseMoved(latestMouseMerc,mercRadSq,e);
		}
	}
	
	public Point2D getMouseMerc() {
		return latestMouseMerc;
	}
//	
//	@Override
//	public void mousePressed(MouseEvent e) {
//		
//	}
//	
//	@Override
//	public void mouseReleased(MouseEvent e) {
//	}
//	
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
//		if(e.getKeyCode() == KeyEvent.VK_COMMA) {
//			SnapSelectAction snapSelectAction = new SnapSelectAction(editManager);
//			snapSelectAction.nextSnapOject();
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_PERIOD) {
//			SnapSelectAction snapSelectAction = new SnapSelectAction(editManager);
//			snapSelectAction.previousSnapObject();
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_M) {
//			SelectEditorMode sem = editManager.getSelectEditorMode();
//			if(sem.getModeEnabled()) {
//				if(!sem.isInMoveState()) {
//					sem.setMoveState();
//				}
//			}
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//			SelectEditorMode sem = editManager.getSelectEditorMode();
//			if(sem.getModeActive()) {
//				if(sem.isInMoveState()) {
//					sem.setSelectState();
//				}
//			}
//			else {
//				WayEditorMode wem = editManager.getWayEditorMode();
//				if(wem.getModeActive()) {
//					wem.resetWayEdit();
//				}
//			}
//		}
//		else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
//			DeleteSelectionAction deleteSelectionAction = new DeleteSelectionAction(editManager);
//			deleteSelectionAction.deleteSelection();
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
//
//	//============================
//	// Private Methods
//	//============================
//
//	


}
