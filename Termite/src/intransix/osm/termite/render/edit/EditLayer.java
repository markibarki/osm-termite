package intransix.osm.termite.render.edit;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.*;
import java.awt.event.*;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.data.edit.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.util.GraduatedList;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.gui.*;
import intransix.osm.termite.gui.stdmode.*;
import intransix.osm.termite.render.edit.EditNode;
import intransix.osm.termite.render.edit.EditSegment;
import java.awt.MouseInfo;

/**
 * This layer controls the user interaction with the active map data. It is designed
 * to run with the editor modes for the Select Tool, Node Tool and Way Tool.
 * 
 * @author sutter
 */
public class EditLayer extends MapLayer implements MapDataListener, 
		FeatureLayerListener, LevelSelectedListener,  
		MouseListener, MouseMotionListener, KeyListener, FocusListener {
	
	//=========================
	// Properties 
	//=========================
	
	// <editor-fold defaultstate="collapsed" desc="Properties">
	
	/** This indicates the mode of the edit layer. */
	private enum EditMode {
		SelectTool,
		NodeTool,
		WayTool,
		Other
	}
	
	private enum ActionMode {
		SelectAction,
		CreateNodeAction,
		MoveAction,
		NoAction
	}
	
	public final static double SNAP_RADIUS_PIXELS = 3;
	
	private OsmData osmData;
	private EditManager editManager;
	private StyleInfo styleInfo = new StyleInfo();
	
	private EditMode editMode;
	private ActionMode actionMode;
	
	private FeatureInfo featureInfo;
	private OsmWay activeStructure;
	private OsmRelation activeLevel;
	
	//these variables hold the hover state
	private List<SnapObject> snapObjects = new ArrayList<SnapObject>();
	private int activeSnapObject = -1;
	
	//this holds the active selection
	private List<Object> selection = new ArrayList<Object>();
	private boolean virtualNodeSelected = false;
	private OsmWay activeWay = null;
	private int activeWayIndex = -1;
	
	//these are nodes that will be displayed with in the edit preview set
	//some of these are also moving nodes
	private List<EditObject> pendingObjects = new ArrayList<EditObject>();
	//There are nodes that are moving  with the mouse
	private List<EditNode> movingNodes = new ArrayList<EditNode>();
	//these are pending nodes that also travel from a fixed node to a node tied to 
	//the mouse location - they are used to check some snap cases.
	private List<EditSegment> pendingSnapSegments = new ArrayList<EditSegment>();
	
	
	//working variables
	private EditDestPoint moveStartPoint;
	private List<SnapSegment> workingSnapSegments = new ArrayList<SnapSegment>();
	
	// </editor-fold>
	
	//=========================
	// Public Methods
	//=========================
	
	/** This returns the current selection. */
	public List<Object> getSelection() {
		return selection;
	}
	
	/** This method sets the edit mode. */
	public void setEditMode(EditorMode editorMode) {
		if(editorMode instanceof SelectEditorMode) {
			editMode = EditMode.SelectTool;
			actionMode = ActionMode.SelectAction;
			clearAll();
		}
		else if(editorMode instanceof NodeEditorMode) {
			editMode = EditMode.NodeTool;
			actionMode = ActionMode.CreateNodeAction;
			clearSelection();
			
			//initialize virtual node with a dummy point - it will get updated on a mouse move
			setNodeToolPendingData(new Point2D.Double());
		}
		else if(editorMode instanceof WayEditorMode) {
			editMode = EditMode.WayTool;
			actionMode = ActionMode.CreateNodeAction;
			//clear selection only if there is not an active way
			//otherwise the selection is the active way
			if(activeWay == null) {
				clearSelection();
			}
			
			//initialize virtual node with a dummy point - it will get updated on a mouse move
			setWayToolPendingData(new Point2D.Double());
		}
		else {
			editMode = EditMode.Other;
			actionMode = ActionMode.NoAction;
			clearAll();
		}
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
	
	// <editor-fold defaultstate="collapsed" desc="Render">
	
	/** This method renders the edit state. */
	@Override
	public void render(Graphics2D g2) {
		
		AffineTransform mercatorToPixels = getMapPanel().getMercatorToPixels();		
		
		//render hover
		if((activeSnapObject != -1)&&(activeSnapObject < snapObjects.size())) {
System.out.println(("cnt = " + snapObjects.size() + "; active = " + activeSnapObject));
			g2.setColor(styleInfo.HOVER_PRESELECT_COLOR);
			SnapObject snapObject = snapObjects.get(activeSnapObject);
System.out.println(snapObject);
			snapObject.render(g2, mercatorToPixels,styleInfo);
		}
		
		//render pending objects
		g2.setColor(styleInfo.PENDING_COLOR);
		for(EditObject editObject:pendingObjects) {
			editObject.render(g2, mercatorToPixels, styleInfo);
		}
		
		//render selection
		g2.setColor(styleInfo.SELECT_COLOR);
		for(Object selectObject:selection) {
			if(selectObject instanceof OsmNode) {
				EditDrawable.renderPoint(g2, mercatorToPixels, ((OsmNode)selectObject).getPoint(),
						styleInfo.RADIUS_PIXELS);
			}
			else if(selectObject instanceof OsmWay) {
				EditDrawable.renderWay(g2, mercatorToPixels,(OsmWay)selectObject);
			}
			else if(selectObject instanceof VirtualNode) {
				EditDrawable.renderPoint(g2, mercatorToPixels,((VirtualNode)selectObject).point,
						styleInfo.RADIUS_PIXELS);
			}
		}
		
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Map Data Listener">
	/** This method is called when the map data is set. */
	@Override
	public void onMapData(OsmData osmData) {
		this.osmData = osmData;
		if(osmData != null) {
			editManager = new EditManager(osmData);
		}
		else {
			editManager = null;
			activeStructure = null;
			activeLevel = null;
		}
	}
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Feature Layer Listener">
	/** This method is called when a feature layer is selected. It may be called 
	 * with the value null if a selection is cleared an no new selection is made. 
	 * 
	 * @param featureInfo	The selected feature type
	 */
	@Override
	public void onFeatureLayerSelected(FeatureInfo featureInfo) {
		this.featureInfo = featureInfo;
	}
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Level Selected Listener">
	/** This method is called when a map level is selected. It may be called 
	 * with the value null for the level or the level and the structure. 
	 * 
	 * @param structure		The footprint in the outdoor map for the selected level
	 * @param level			The selected level
	 */
	@Override
	public void onLevelSelected(OsmWay structure, OsmRelation level) {
		this.activeStructure = structure;
		this.activeLevel = level;
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
		this.getMapPanel().requestFocusInWindow();
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		clearHover();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		
		if(actionMode == ActionMode.NoAction) return;
		
		boolean wasActive = (!snapObjects.isEmpty())||(!movingNodes.isEmpty());

		//clear snapObjects
		snapObjects.clear();
		workingSnapSegments.clear();
		
		//read mouse location in global coordinates
		MapPanel mapPanel = getMapPanel();
		AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();
		Point2D mouseMerc = new Point2D.Double(e.getX(),e.getY());
		pixelsToMercator.transform(mouseMerc, mouseMerc);
		double scalePixelsPerMerc = mapPanel.getZoomScalePixelsPerMerc();
		double mercRad = SNAP_RADIUS_PIXELS / scalePixelsPerMerc;
		double mercRadSq = mercRad * mercRad;
		
		//handle a move preview
		if(!movingNodes.isEmpty()) {
			updateMovingNodes(mouseMerc);
		}
		
		//check for hovering over these objects
		SnapObject snapObject;
		GraduatedList<OsmObject> objectList = osmData.getOrderedList();
		for(java.util.List<OsmObject> subList:objectList.getLists()) {
			for(OsmObject mapObject:subList) {
				//make sure edit is enabled for this object
				if(!mapObject.editEnabled()) continue;
				
				//do the hover check
				if(mapObject instanceof OsmNode) {
					//check for a node hit
					snapObject = SnapNode.testNode((OsmNode)mapObject, mouseMerc, mercRadSq);
					if(snapObject != null) {
						snapObjects.add(snapObject);
					}
					
					//check for a segment hit
					for(OsmSegment segment:((OsmNode)mapObject).getSegments()) {
						if(!segment.editEnabled()) continue;
						
						//only do the segments that start with this node, to avoid doing them twice
						if(segment.getNode1() == mapObject) {
							if(actionMode == ActionMode.SelectAction) {
								//selection preview - when we are selecting an object
								
								//check for a virtual node hit
								snapObject = SnapVirtualNode.testVirtualNodeHit(segment, mouseMerc, mercRadSq);
								if(snapObject != null) {
									snapObjects.add(snapObject);
								}
								
								//check for way hit
								SnapWay.loadHitWays(segment, mouseMerc, mercRadSq, snapObjects);
							}
							else {
								//snap preview - when we are in an edit
								//check for segment and extension hit
								SnapSegment snapSegment = SnapSegment.testSegmentHit(segment,
										mouseMerc,mercRadSq);
								if(snapSegment != null) {
									snapObjects.add(snapSegment);
									workingSnapSegments.add(snapSegment);
								}
							}		
						}
					}
				}
			}			
		}
		
		//check for snapping in the pending snap segments
		SnapSegment ss = null;
		if(pendingSnapSegments != null) {
			Point2D mercPix00 = new Point2D.Double(0,0);
			Point2D mercPix10 = new Point2D.Double(1,0);
			Point2D mercPix01 = new Point2D.Double(0,1);
			pixelsToMercator.transform(mercPix00, mercPix00);
			pixelsToMercator.transform(mercPix10, mercPix10);
			pixelsToMercator.transform(mercPix01, mercPix01);
		
			for(EditSegment es:pendingSnapSegments) {
				//check for horizontal snap
				ss = SnapSegment.getHorOrVertSnapSegment(es,mouseMerc,mercRadSq,mercPix00,mercPix10,mercPix01);
				if(ss != null) {
					snapObjects.add(ss);
					workingSnapSegments.add(ss);
				}
				else {
					//only check perpicular if it is not already a horizontal or vertical snap
					//check for perps from both ends
					ss = SnapSegment.getPerpSegment(es,mouseMerc,mercRadSq);
					if(ss != null) {
						snapObjects.add(ss);
						workingSnapSegments.add(ss);
					}
				}
			}
		}
		
		//check for intersections
		SnapIntersection.loadIntersectiona(workingSnapSegments, mouseMerc, mercRadSq, snapObjects);
		workingSnapSegments.clear();
		
		//order the snap objects and select the active one
		if(snapObjects.size() > 1) {
			Collections.sort(snapObjects);
		}
		activeSnapObject = snapObjects.isEmpty() ? -1 : 0;
		
		boolean isActive = (!snapObjects.isEmpty())||(!movingNodes.isEmpty());
		
		//repaint if there is a hit or if the hit status changes
		if(wasActive||isActive) {
			mapPanel.repaint();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		MapPanel mapPanel = getMapPanel();
		AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();
		Point2D mouseMerc = new Point2D.Double(e.getX(),e.getY());
		pixelsToMercator.transform(mouseMerc, mouseMerc);
		
		if(e.getButton() == MouseEvent.BUTTON1) {
			
			EditDestPoint dest = getDestinationPoint(mouseMerc);

			if(editMode == EditMode.SelectTool) {
					
				if(actionMode == ActionMode.CreateNodeAction) {
					if(!selection.isEmpty()) {
						OsmSegment segment = ((EditVirtualNode)selection.get(0)).osmSegment;
						editManager.nodeInserted(segment,dest,activeLevel);
					}

					//clear selection. user has to reselect to move again
					//but move mode is still active because it is controlled elsewhere
					clearPending();
					clearSelection();
				}
				else if(actionMode == ActionMode.MoveAction) {
					if(!selection.isEmpty()) {
						//execute the move
						editManager.selectionMoved(selection,moveStartPoint,dest);
					}
					//stay in move mode, but deselect
					//user has to end move mode and restart it to move again
					//but keep the selection but update the start point
					//so the user can move this object again without reselecting it
					clearPending();
					moveStartPoint.point.setLocation(dest.point);
				}
				else if(actionMode == ActionMode.SelectAction) {

					//store the latest point used for selection, for the move anchor
					moveStartPoint = dest;
						
					Object selectObject = null;
					
					//do a selection
					if((activeSnapObject > -1)&&(activeSnapObject < snapObjects.size())) {	

						//a little preprocessing
						SnapObject snapObject = snapObjects.get(activeSnapObject);
						moveStartPoint.point = snapObject.snapPoint;
						if(snapObject instanceof SnapNode) {
							//use the node point as the start point
							moveStartPoint.snapNode = ((SnapNode)snapObject).node;
						}

						//get the edit object for this snap object
						selectObject = snapObject.getSelectObject();
					}
					
					boolean wasVirtualNode = virtualNodeSelected;
					boolean isVirtualNode = selectObject instanceof EditVirtualNode;
					
					//handle selection
					if((!selection.isEmpty())&&(e.isShiftDown())&&(!wasVirtualNode)&&(!isVirtualNode)) {
						//shift - add or remove object
						if(selectObject != null) {
							if(selection.contains(selectObject)) {
								selection.remove(selectObject);
							}
							else {
								selection.add(selectObject);
							}
						}
						activeWay = null;
						activeWayIndex = -1;
					}
					else {
						//check norma select or select node in a way
						if((activeWay != null)&&(selectObject instanceof OsmNode)) {
							//special case - select a node in the active way
							if(activeWay.getNodes().contains((OsmNode)selectObject)) {
								activeWayIndex = activeWay.getNodes().indexOf((OsmNode)selectObject);
							}
						}
						else {
							//normal - replace selection with new object
							clearSelection();
							if(selectObject != null) {
								selection.add(selectObject);
							}
							virtualNodeSelected = isVirtualNode;

							if(selectObject instanceof OsmWay) {
								activeWay = (OsmWay)selectObject;
								this.activeWayIndex = -1;
							}
							else {
								activeWay = null;
								activeWayIndex = -1;
							}
						}
					}
				}
			}
			else if(editMode == EditMode.NodeTool) {
				//execute a node addition
				OsmNode node = editManager.nodeToolClicked(dest,featureInfo,activeLevel);
				//prepare for next
				setNodeToolPendingData(mouseMerc);
			}
			else if(editMode == EditMode.WayTool) {
				//execute a way node addition
				activeWay = editManager.wayToolClicked(activeWay,activeWayIndex,dest,featureInfo,activeLevel);
				//make sure this is selected
				if(selection.isEmpty()) {
					selection.add(activeWay);
				}
				//prepare for next
				setWayToolPendingData(mouseMerc);
			}
		
			getMapPanel().repaint();
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
		boolean changed = false;
		if((e.getKeyCode() == KeyEvent.VK_LEFT)||(e.getKeyCode() == KeyEvent.VK_UP)) {
			if(!snapObjects.isEmpty()) {
				activeSnapObject--;
				if(activeSnapObject < -1) activeSnapObject = snapObjects.size() - 1;
				changed = true;
			}
		}
		else if((e.getKeyCode() == KeyEvent.VK_RIGHT)||(e.getKeyCode() == KeyEvent.VK_DOWN)) {
			if(!snapObjects.isEmpty()) {
				activeSnapObject++;
				if(activeSnapObject >= snapObjects.size()) activeSnapObject = -1;
				changed = true;
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_M) {
			if(editMode == EditMode.SelectTool) {
				if(virtualNodeSelected) {
					actionMode = ActionMode.CreateNodeAction;
				}
				else {
					actionMode = ActionMode.MoveAction;
					loadPendingFromSelection();
				}
				
				//get the current mouse location and update the nodes that move with the mouse
				MapPanel mapPanel = getMapPanel();
				AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();
				java.awt.Point mouseInApp = MouseInfo.getPointerInfo().getLocation();
				java.awt.Point mapPanelInApp = mapPanel.getLocationOnScreen();
				Point2D mousePix = new Point2D.Double(mouseInApp.x - mapPanelInApp.x,mouseInApp.y - mapPanelInApp.y);
				Point2D mouseMerc = new Point2D.Double(); 
				pixelsToMercator.transform(mousePix,mouseMerc);
				updateMovingNodes(mouseMerc);

				changed = true;
			}
		}
		
		if(changed) {
			getMapPanel().repaint();
//			java.awt.Toolkit.getDefaultToolkit().beep();
		}
    }

    /** Handle the key-released event from the text field. */
    @Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_M) {
			if(editMode == EditMode.SelectTool) {
				actionMode = ActionMode.SelectAction;
				clearPending();
				getMapPanel().repaint();
			}
		}
    }
	
	@Override
	public void focusGained(FocusEvent e) {
		//it would be nice if we could get the key state for any keys that are help down
		//when focus is gained
	}
	
	@Override
	public void focusLost(FocusEvent e) {
		if(editMode == EditMode.SelectTool) {
			//we have no choice but to clear the move or create since we will not captuer events
			actionMode = ActionMode.SelectAction;
			clearPending();
			getMapPanel().repaint();
		}
	}
	
	// </editor-fold>
	
	//============================
	// Private Methods
	//============================
	
	private void clearAll() {
		clearHover();
		clearPending();
		clearSelection();
	}
	
	/** This method clears the hover variables. */
	private void clearHover() {
		snapObjects.clear();
		activeSnapObject = -1;
		getMapPanel().repaint();
	}	
	
	
	/** This method clears the current selection. */
	private void clearSelection() {
		selection.clear();
		activeWay = null;
		activeWayIndex = -1;
		virtualNodeSelected = false;
		clearPending();
		getMapPanel().repaint();
	}
	
		/** This method clears all data in the pending state. */
	private void clearPending() {
		pendingObjects.clear();
		pendingSnapSegments.clear();
		movingNodes.clear();
	}
	
	/** This method returns the current edit destination point based on the
	 * currently selected hover node or segment. 
	 * 
	 * @param mouseMerc		The location of the mouse in mercator coordinates.
	 * @return				The EditDestPoint
	 */
	private EditDestPoint getDestinationPoint(Point2D mouseMerc) {
		EditDestPoint edp = new EditDestPoint();
		edp.point = new Point2D.Double();
		
		if((activeSnapObject > -1)&&(activeSnapObject < snapObjects.size())) {
			SnapObject snapObject = snapObjects.get(activeSnapObject);
			edp.point.setLocation(snapObject.snapPoint);
			
			if(snapObject instanceof SnapNode) {
				edp.snapNode = ((SnapNode)snapObject).node;
			}	
		}
		else {
			edp.point.setLocation(mouseMerc);
		}
		
		return edp;
	}
	
	/** This method updates the location of any node moving with the mouse. */
	private void updateMovingNodes(Point2D mouseMerc) {
		if(actionMode == ActionMode.MoveAction) {
			if(moveStartPoint != null) {
				double dx = mouseMerc.getX() - moveStartPoint.point.getX();
				double dy = mouseMerc.getY() - moveStartPoint.point.getY();
				for(EditNode en:movingNodes) {
					//for a move, all nodes should be real so node should exist
					if(en.node != null) {
						Point2D nodePoint = en.node.getPoint();
						en.point.setLocation(nodePoint.getX() + dx, nodePoint.getY() + dy);
					}
				}
			}
		}
		else if(actionMode == ActionMode.CreateNodeAction) {
			//for create, there should be just one node here
			//it follows the mouse
			for(EditNode en:movingNodes) {
				en.point.setLocation(mouseMerc);
			}
		}
	}
	
	/** This method loads the pending state from the current selection. */
	private void loadPendingFromSelection() {
		
		movingNodes.clear();
		pendingObjects.clear();
		pendingSnapSegments.clear();
		
		if(actionMode == ActionMode.MoveAction) {
			//I created a class to do this...
			PendingSelectionLoader psl = new PendingSelectionLoader();
			psl.loadPendingFromSelectionForMove();
		}
		else if(actionMode == ActionMode.CreateNodeAction) {
			//there should be one and it should be a virtual node
			for(Object selectObject:selection) {
				if(selectObject instanceof VirtualNode) {
					EditVirtualNode evn = new EditVirtualNode((VirtualNode)selectObject);
					movingNodes.add(evn.enVirtual);
					pendingObjects.add(evn.enVirtual);
					pendingObjects.add(evn.es1);
					pendingObjects.add(evn.es2);
					pendingSnapSegments.add(evn.es1);
					pendingSnapSegments.add(evn.es2);
				}
			}
		}
	}
	
	/** This method sets the pending data when the node tool is selected. */
	private void setNodeToolPendingData(Point2D mouseMerc) {
		clearPending();
		
		EditNode en = new EditNode(mouseMerc,featureInfo);
		movingNodes.add(en);
		pendingObjects.add(en);
	}
	
	/** This method sets the pending data when the way tool is selected. */
	private void setWayToolPendingData(Point2D mouseMerc) {
		clearPending();
		
		//get the node to add
		EditNode en = new EditNode(mouseMerc,null);
		movingNodes.add(en);
		pendingObjects.add(en);
		//get the segment from the previous node
		if(activeWay != null) {
			List<OsmNode> nodes = activeWay.getNodes();
			
			if(!nodes.isEmpty()) {
				//set the active way to end if it is not already set
				if((activeWayIndex < 0)||(activeWayIndex >= nodes.size())) {
					activeWayIndex = nodes.size() - 1;
				}

				OsmNode activeNode = nodes.get(activeWayIndex);
				EditNode en2 = new EditNode(activeNode);
				EditSegment es = new EditSegment(null,en,en2);
				pendingObjects.add(en2);
				pendingObjects.add(es);
				pendingSnapSegments.add(es);
			}
		}
	}
	
	class PendingSelectionLoader {
		
		private HashMap<Object,EditObject> editMap = new HashMap<Object,EditObject>();
		private boolean lastNodeWasNew;
		private boolean lastSegmentWasNew;
		private boolean lastSegmentWasDynamic;
		
		private EditNode getEditNode(OsmNode node) {
			EditObject editObject = editMap.get(node);
			if(editObject == null) {
				lastNodeWasNew = true;
				EditNode en = new EditNode(node);
				editMap.put(node,en);
				return en;
			}
			else {
				lastNodeWasNew = false;
				return (EditNode)editObject;
			}
		}
		
		private EditSegment getEditSegment(OsmSegment segment) {
			EditObject editObject = editMap.get(segment);
			if(editObject == null) {
				//create a new segment
				lastSegmentWasNew = true;
				int newCount = 0;
				EditNode en1 = this.getEditNode(segment.getNode1());
				if(lastNodeWasNew) newCount++;
				EditNode en2 = this.getEditNode(segment.getNode2());
				if(lastNodeWasNew) newCount++;
				EditSegment editSegment = new EditSegment(segment,en1,en2);
				
				//segment is dynamic if one node is new and the other is not
				lastSegmentWasDynamic = (newCount == 1);
				
				return editSegment;
			}
			else {
				lastSegmentWasNew = false;
				//this variable is valid only for new segments
				lastSegmentWasDynamic = false;
				return (EditSegment)editObject;
			}
		}
		
		public void loadPendingFromSelectionForMove() {
			
			//add all unique nodes to the moving nodes
			EditNode editNode;
			for(Object selectObject:selection) {
				if(selectObject instanceof OsmNode) {
					//add this edit node
					editNode = this.getEditNode((OsmNode)selectObject);
					if(lastNodeWasNew) {
						movingNodes.add(editNode);
					}
				}
				else if(selectObject instanceof OsmWay) {
					for(OsmNode node:((OsmWay)selectObject).getNodes()) {
						editNode = this.getEditNode(node);
						if(lastNodeWasNew) {
							movingNodes.add(editNode);
						}
					}
				}
			}
			//get the segments for each of the moving nodes
			EditSegment editSegment;
			for(EditNode en:movingNodes) {
				pendingObjects.add(en);
				for(OsmSegment segment:en.node.getSegments()) {
					editSegment = getEditSegment(segment);
					if(lastSegmentWasNew) {
						pendingObjects.add(editSegment);
						if(lastSegmentWasDynamic) {
							pendingSnapSegments.add(editSegment);
						}
					}
				}
			}
		}
	
	}
	
	
}
