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
	
	public final static double SNAP_RADIUS_PIXELS = 3;
	
	private OsmData osmData;
	private EditManager editManager;
	private StyleInfo styleInfo = new StyleInfo();
	
	private EditMode editMode;
	private FeatureInfo featureInfo;
	private OsmWay activeStructure;
	private OsmRelation activeLevel;
	
	//these variables hold the hover state
	private List<SnapObject> snapObjects = new ArrayList<SnapObject>();
	private int activeSnapObject = -1;
	
	//this holds the active selection
	private List<EditObject> selection = new ArrayList<EditObject>();
	
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
	private boolean inMove;
	private OsmWay activeWay;
	private boolean isEnd;
	
	// </editor-fold>
	
	//=========================
	// Public Methods
	//=========================
	
	/** This returns the current selection. */
	public List<EditObject> getSelection() {
		return selection;
	}
	
	/** This method sets the edit mode. */
	public void setEditMode(EditorMode editorMode) {
		if(editorMode instanceof SelectEditorMode) {
			editMode = EditMode.SelectTool;
			clearSelection();
		}
		else if(editorMode instanceof NodeEditorMode) {
			editMode = EditMode.NodeTool;
			clearSelection();
			//initialize virtual node with a dummy point - it will get updated on a mouse move
			setNodeToolPendingData(new Point2D.Double());
		}
		else if(editorMode instanceof WayEditorMode) {
			editMode = EditMode.WayTool;
			if(selection.size() == 1) {
				//if there is a single way selected, use it as the active way
				EditDrawable editDrawable = selection.get(0);
				if(editDrawable instanceof EditWay) {
					activeWay = ((EditWay)editDrawable).way;
					isEnd = true;
				}
				else {
					clearSelection();
				}
			}
			else {
				clearSelection();
			}
			//initialize virtual node with a dummy point - it will get updated on a mouse move
			setWayToolPendingData(new Point2D.Double());
		}
		else {
			editMode = EditMode.Other;
			clearSelection();
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
			g2.setColor(styleInfo.HOVER_PRESELECT_COLOR);
			SnapObject snapObject = snapObjects.get(activeSnapObject);
			snapObject.render(g2, mercatorToPixels,styleInfo);
		}
		
		//render pending objects
		g2.setColor(styleInfo.PENDING_COLOR);
		for(EditObject editObject:pendingObjects) {
			editObject.render(g2, mercatorToPixels, styleInfo);
		}
		
		//render selection
		g2.setColor(styleInfo.SELECT_COLOR);
		for(EditObject editObject:selection) {
			editObject.render(g2, mercatorToPixels, styleInfo);
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
		
		boolean wasActive = (snapObjects.isEmpty())||(!movingNodes.isEmpty());

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
							if((editMode == EditMode.SelectTool)&&(!inMove)) {
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
								if(snapObject != null) {
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
		activeSnapObject = snapObjects.size() - 1;
		
		boolean isActive = (snapObjects.isEmpty())||(!movingNodes.isEmpty());
		
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
		
		if(editMode == EditMode.SelectTool) {
			if(inMove) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					EditDestPoint dest = getDestinationPoint(mouseMerc);
					if((selection.size() == 1) && (selection.get(0) instanceof EditVirtualNode)) {
						OsmSegment segment = ((EditVirtualNode)selection.get(0)).osmSegment;
						editManager.nodeInserted(segment,dest,activeLevel);
						clearPending();
						
						//clear the selection after inserting a node
						clearSelection();
					}
					if(!selection.isEmpty()) {
						//execute the move
						editManager.selectionMoved(selection,moveStartPoint,dest);
						clearPending();

						//update the start point location - just change the point
						moveStartPoint.point.setLocation(dest.point);
					}
				}
			}
			else {
				//do a selection
				if((activeSnapObject > -1)&&(activeSnapObject < snapObjects.size())) {

					//store the latest point used for selection, for the move anchor
					moveStartPoint = new EditDestPoint();
					
					//a little preprocessing
					SnapObject snapObject = snapObjects.get(activeSnapObject);
					moveStartPoint.point = snapObject.snapPoint;
					if(snapObject instanceof SnapNode) {
						//use the node point as the start point
						moveStartPoint.snapNode = ((SnapNode)snapObject).node;
					}
					else if((snapObject instanceof SnapVirtualNode)||
							((selection.size() == 1)&&(selection.get(0) instanceof EditVirtualNode))){
						//only allow one SnapVirtualNode to be selected, and with no other objects
						//if we allow this it opens up a lot of funny use cases
						//and it makes the code harder
						selection.clear();
					}
					
					//get the edit object for this snap object
					EditObject editObject = snapObject.getSelectEditObject();
					
					//handle selection
					if(!e.isShiftDown()) {
						//no shift - replace selection
						selection.clear();
						if(editObject != null) {
							selection.add(editObject);
						}
					}
					else {
						//shift - add or remove object
						if(editObject != null) {
							if(selection.contains(editObject)) {
								selection.remove(editObject);
							}
							else {
								selection.add(editObject);
							}
						}
					}
				}
				else {
					moveStartPoint = null;
					clearSelection();
				}
			}
		}
		else if(editMode == EditMode.NodeTool) {
			//execute a node addition
			EditDestPoint dest = getDestinationPoint(mouseMerc);
			OsmNode node = editManager.nodeToolClicked(dest,featureInfo,activeLevel);
			//prepare for next
			setNodeToolPendingData(mouseMerc);
		}
		else if(editMode == EditMode.WayTool) {
			//execute a way node addition
			EditDestPoint dest = getDestinationPoint(mouseMerc);
			activeWay = editManager.wayToolClicked(activeWay,isEnd,dest,featureInfo,activeLevel);
			//make sure this is selected
			if(selection.isEmpty()) {
				EditWay editWay = EditObject.getEditWay(activeWay);
				selection.add(editWay);
			}
			//prepare for next
			setWayToolPendingData(mouseMerc);
		}
		getMapPanel().repaint();
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
			if(!inMove) {
				inMove = true;
				loadPendingFromSelection();

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
		if(inMove) {
			inMove = false;
			clearPending();
			getMapPanel().repaint();
		}
    }
	
	@Override
	public void focusGained(FocusEvent e) {
		
	}
	
	@Override
	public void focusLost(FocusEvent e) {
		inMove = false;
		clearPending();
	}
	
	// </editor-fold>
	
	//============================
	// Private Methods
	//============================
	
	// <editor-fold defaultstate="collapsed" desc="Hit Methods">
	
	/** This method clears the hover variables. */
	private void clearHover() {
		snapObjects.clear();
		activeSnapObject = -1;
		getMapPanel().repaint();
	}	
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Select Methods">
	
	/** This method clears the current selection. */
	private void clearSelection() {
		selection.clear();
		activeWay = null;
		inMove = false;
		clearPending();
		getMapPanel().repaint();
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
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Pending Methods">
	
	/** This method clears all data in the pending state. */
	private void clearPending() {
		pendingObjects.clear();
		pendingSnapSegments.clear();
		movingNodes.clear();
	}
	
	/** This method updates the location of any node moving with the mouse. */
	private void updateMovingNodes(Point2D mouseMerc) {
		if(inMove) {
			//this is a move - there may be several nodes
			if(moveStartPoint != null) {
				double dx = mouseMerc.getX() - moveStartPoint.point.getX();
				double dy = mouseMerc.getY() - moveStartPoint.point.getY();
				for(EditNode en:movingNodes) {
					Point2D nodePoint;
					if(en.node != null) {
						nodePoint = en.node.getPoint();
					}
					else {
						//CLUDGE - if we don't have a node, there can be only one selection point
						//which I have. But This has to be fixed because it will cause a bug in the future
						nodePoint = moveStartPoint.point;
					}
					en.point.setLocation(nodePoint.getX() + dx, nodePoint.getY() + dy);
				}
			}
		}
		else {
			//this is a new node - there should be just one node
			if(!movingNodes.isEmpty()) {
				EditNode en = movingNodes.get(0);
				en.point.setLocation(mouseMerc);
			}
		}
	}
	
	/** This method loads the pending state from the current selection. */
	private void loadPendingFromSelection() {
		
		movingNodes.clear();
		pendingObjects.clear();
		pendingSnapSegments.clear();
		
		for(EditObject editObject:selection) {
			editObject.loadMovingNodes(movingNodes);
		}
		for(EditObject editObject:selection) {
			editObject.loadPendingObjects(pendingObjects,pendingSnapSegments,movingNodes);
		}
	}
	
	/** This method sets the pending data when the node tool is selected. */
	private void setNodeToolPendingData(Point2D mouseMerc) {
		clearPending();
		
		EditNode en = EditObject.getEditNode(mouseMerc,featureInfo);
		movingNodes.add(en);
		pendingObjects.add(en);
	}
	
	/** This method sets the pending data when the way tool is selected. */
	private void setWayToolPendingData(Point2D mouseMerc) {
		clearPending();
		
		//get the node to add
		EditNode en = EditObject.getEditNode(mouseMerc,null);
		movingNodes.add(en);
		pendingObjects.add(en);
		//get the segment from the previous node
		OsmNode activeNode = null;
		if(activeWay != null) {
			List<OsmNode> nodes = activeWay.getNodes();
			
			if(!nodes.isEmpty()) {
				if(isEnd) {
					activeNode = nodes.get(nodes.size()-1);
				}
				else {
					activeNode = nodes.get(0);
				}
			}
		}
		if(activeNode != null) {
			EditNode en2 = EditObject.getEditNode(activeNode);
			EditSegment es = EditObject.getEditSegment(en,en2);
			pendingObjects.add(en2);
			pendingObjects.add(es);
			pendingSnapSegments.add(es);
		}
		
	}
	// </editor-fold>
	
}
