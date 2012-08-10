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
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.gui.*;
import java.awt.*;
import java.util.List;

/**
 * This layer controls the user interaction with the active map data. It is designed
 * to run with the editor modes for the Select Tool, Node Tool and Way Tool.
 * 
 * @author sutter
 */
public class EditLayer extends MapLayer implements MapDataListener, 
		FeatureLayerListener, LevelSelectedListener,
		MouseListener, MouseMotionListener, KeyListener {
	
	//=========================
	// Properties 
	//=========================
	
	// <editor-fold defaultstate="collapsed" desc="Properties">
	
	public final static double SNAP_RADIUS_PIXELS = 3;
	
	private TermiteGui termiteGui;
	
	private OsmData osmData;
	private StyleInfo styleInfo = new StyleInfo();

	private MouseEditAction mouseEditAction;
	
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
	private List<Integer> selectedWayNodes = new ArrayList<Integer>();
	private EditDestPoint selectionPoint;
	
	//these are nodes that will be displayed with in the edit preview set
	//some of these are also moving nodes
	private List<EditObject> pendingObjects = new ArrayList<EditObject>();
	//There are nodes that are moving  with the mouse
	private List<EditNode> movingNodes = new ArrayList<EditNode>();
	//these are pending nodes that also travel from a fixed node to a node tied to 
	//the mouse location - they are used to check some snap cases.
	private List<EditSegment> pendingSnapSegments = new ArrayList<EditSegment>();
	
	
	//working variables
	private List<SnapSegment> workingSnapSegments = new ArrayList<SnapSegment>();
	
	// </editor-fold>
	
	//=========================
	// Public Methods
	//=========================
	
	public EditLayer(TermiteGui termiteGui) {
		this.termiteGui = termiteGui;

		this.setName("Edit Layer");
	}
	
	// <editor-fold defaultstate="collapsed" desc="Accessors">
	
	public List<Object> getSelection() {
		return selection;
	}
	
	public List<Integer> getSelectedWayNodes() {
		return this.selectedWayNodes;
	}
	
	public OsmWay getActiveWay() {
		return activeWay;
	}
	
	public List<EditObject> getPendingObjects() {
		return pendingObjects;
	}
	
	public List<EditNode> getMovingNodes() {
		return movingNodes;
	}
	
	public List<EditSegment> getPendingSnapSegments() {
		return pendingSnapSegments;
	}
	
	/** This method clears the current selection. */
	public void clearSelection() {
		selection.clear();
		selectedWayNodes.clear();
		activeWay = null;
		virtualNodeSelected = false;
		clearPending();
		getMapPanel().repaint();
	}
	
		/** This method clears all data in the pending state. */
	public void clearPending() {
		pendingObjects.clear();
		pendingSnapSegments.clear();
		movingNodes.clear();
	}
	
	public OsmWay getActiveStructure() {
		return activeStructure;
	}
	
	public OsmRelation getActiveLevel() {
		return activeLevel;
	}
	
	public EditDestPoint getSelectionPoint() {
		return this.selectionPoint;
	}
	
	public void setSelectionPoint(EditDestPoint selectionPoint) {
		this.selectionPoint = selectionPoint;
	}
	
	public FeatureInfo getFeatureInfo() {
		return featureInfo;
	}
	
	/** This method gets the location of the mouse in Mercator coordinates. It should
	 * be used if the mouse is needed outside of a mouse event. */
	public Point2D getMousePoint() {
		//get the current mouse location and update the nodes that move with the mouse
		MapPanel mapPanel = getMapPanel();
		AffineTransform pixelsToMercator = mapPanel.getPixelsToMercator();
		java.awt.Point mouseInApp = MouseInfo.getPointerInfo().getLocation();
		java.awt.Point mapPanelInApp = mapPanel.getLocationOnScreen();
		Point2D mousePix = new Point2D.Double(mouseInApp.x - mapPanelInApp.x,mouseInApp.y - mapPanelInApp.y);
		Point2D mouseMerc = new Point2D.Double(); 
		pixelsToMercator.transform(mousePix,mouseMerc);
		
		return mouseMerc;
	}
	
	/** This method sets the edit mode. */
	public void setMouseEditAction(MouseEditAction mouseEditAction) {
		if(mouseEditAction != null) {
			this.mouseEditAction = mouseEditAction;
			mouseEditAction.init(osmData, this);
		}
		else {
			this.mouseEditAction = null;
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
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Render">
	
	/** This method renders the edit state. */
	@Override
	public void render(Graphics2D g2) {
		
		//set the opacity for the layer
		Composite originalComposite = null;
		Composite activeComposite = this.getComposite();
		if(activeComposite != null) {
			originalComposite = g2.getComposite();
			g2.setComposite(activeComposite);
		}
		
		AffineTransform mercatorToPixels = getMapPanel().getMercatorToPixels();		
		
		//render hover
		if((activeSnapObject != -1)&&(activeSnapObject < snapObjects.size())) {
//System.out.println(("cnt = " + snapObjects.size() + "; active = " + activeSnapObject));
			g2.setColor(styleInfo.HOVER_PRESELECT_COLOR);
			SnapObject snapObject = snapObjects.get(activeSnapObject);
//System.out.println(snapObject);
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
				
				if(selectObject == activeWay) {
					for(Integer index:selectedWayNodes) {
						if((index > -1)&&(index < activeWay.getNodes().size())) {
							OsmNode node = activeWay.getNodes().get(index);
							EditDrawable.renderPoint(g2, mercatorToPixels, node.getPoint(),
							styleInfo.RADIUS_PIXELS);
						}
					}
				}
			}
			else if(selectObject instanceof VirtualNode) {
				EditDrawable.renderPoint(g2, mercatorToPixels,((VirtualNode)selectObject).point,
						styleInfo.RADIUS_PIXELS);
			}
		}
		
		if(originalComposite != null) {
			g2.setComposite(originalComposite);
		}
	}
	
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Map Data Listener">
	/** This method is called when the map data is set. */
	@Override
	public void onMapData(OsmData osmData) {
		this.osmData = osmData;
		activeStructure = null;
		activeLevel = null;
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
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		clearHover();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		
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
		if((!movingNodes.isEmpty())&&(mouseEditAction != null)) {
			mouseEditAction.updateMovingNodes(mouseMerc);
		}
		
		//check for hovering over these objects
		SnapObject snapObject;
		List<OsmObject> objectList = osmData.getFeatureList();
		for(OsmObject mapObject:objectList) {
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
						if(mouseEditAction == null) {
							//selection preview - when we are selecting an object
							//select objects if no mouse edit action is active

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
							//do this when a mouse edit action is active
							
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
		
		//check for snapping in the pending snap segments
		SnapSegment ss;
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
		SnapIntersection.loadIntersections(workingSnapSegments, mouseMerc, mercRadSq, snapObjects);
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
			
			if(mouseEditAction != null) {
				//let the mouse edit action handle the press
				mouseEditAction.mousePressed(dest);
			}
			else {
				//do a selection with the press
				
				//store the latest point used for selection, for the move anchor
				selectionPoint = dest;

				Object selectObject = null;

				//do a selection
				if((activeSnapObject > -1)&&(activeSnapObject < snapObjects.size())) {	

					//a little preprocessing
					SnapObject snapObject = snapObjects.get(activeSnapObject);
					selectionPoint.point = snapObject.snapPoint;
					if(snapObject instanceof SnapNode) {
						//use the node point as the start point
						selectionPoint.snapNode = ((SnapNode)snapObject).node;
					}

					//get the edit object for this snap object
					selectObject = snapObject.getSelectObject();
				}

				boolean wasVirtualNode = virtualNodeSelected;
				boolean isVirtualNode = selectObject instanceof VirtualNode;

				//handle selection

				//check normal select or select node in a way
				if((activeWay != null)&&(selectObject instanceof OsmNode)&&
						(activeWay.getNodes().contains((OsmNode)selectObject))) {

					//select a node within a way
					int selectedIndex = activeWay.getNodes().indexOf((OsmNode)selectObject);
					if(e.isShiftDown()) {
						if(!selectedWayNodes.contains(selectedIndex)) {
							selectedWayNodes.add(selectedIndex);
						}
						else {
							selectedWayNodes.remove(selectedIndex);
						}
					}
					else {
						selectedWayNodes.clear();
						selectedWayNodes.add(selectedIndex);
					}
				}
				else {
					//normal select action

					//if shift is down do add/remove rather than replace selection
					//except do not allow virtual nodes to be selected with anything else
					boolean doAddRemove = ((e.isShiftDown())&&
							(!isVirtualNode)&&(!wasVirtualNode));

					if(doAddRemove) {
						if(selectObject != null) {
							if(selection.contains(selectObject)) {
								selection.remove(selectObject);
							}
							else {
								selection.add(selectObject);
							}
						}
					}
					else {
						selection.clear();
						if((selectObject != null)&&(!selection.contains(selectObject))) {
							selection.add(selectObject);
						}
					}

					//make sure selected nodes cleared
					this.selectedWayNodes.clear();

					//update the active way
					if((selectObject instanceof OsmWay)&&(selection.size() == 1)) {
						activeWay = (OsmWay)selectObject;
					}
					else {
						activeWay = null;
					}
				}

				virtualNodeSelected = isVirtualNode;

				//report selection
				termiteGui.setSelection(selection, selectedWayNodes);
				this.getMapPanel().repaint();
				
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
			changed = startMove();
		}
		else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			changed = exitMove();
		}
		
		if(changed) {
			getMapPanel().repaint();
//			java.awt.Toolkit.getDefaultToolkit().beep();
		}
    }

    /** Handle the key-released event from the text field. */
    @Override
	public void keyReleased(KeyEvent e) {
    }
	
	// </editor-fold>
	
	/** This method sets the selection, called from the TermiteGui. This is 
	 * called when this class sets the selection in the TermiteGui, so it must
	 * gracefully handle that state. This method should not be called by classes 
	 * trying to update the selection. the method should be called in TermiteGui.
	 * 
	 * @param selection			The selected objects
	 * @param wayNodeSelection	Any selected nodes in the way, if the selection
	 *							is a single way.
	 */
	public void setSelection(java.util.List<Object> selection,
			java.util.List<Integer> wayNodeSelection) {
		
		//see if the selection needs to be updated
		if(selection != null) {
			if(selection != this.selection) {
				clearSelection();
				for(Object selectObject:selection) {
					selection.add(selectObject);
				}
			}
			if(wayNodeSelection != null) {
				if(wayNodeSelection != this.selectedWayNodes) {
					this.selectedWayNodes.clear();
					for(Integer index:wayNodeSelection) {
						this.selectedWayNodes.add(index);
					}
				}
			}
		}
		else {
			if(!this.selection.isEmpty()) {
				clearSelection();
			}
		}
		
	}
	
	//============================
	// Package Methods
	//============================
	
	boolean exitMove() {
		if(inMove()) {
			clearEditAction();
			return true;
		}
		else {
			return false;
		}
	}
	
	//============================
	// Private Methods
	//============================
	
	/** This method returns true if a move mode is active. */
	private boolean inMove() {
		return ((mouseEditAction instanceof MoveAction)||
				(mouseEditAction instanceof VirtualNodeAction));
	}
	
	private boolean startMove() {
		if((mouseEditAction == null)&&(!selection.isEmpty())) {
				
			if(virtualNodeSelected) {
				mouseEditAction = new VirtualNodeAction();
				mouseEditAction.init(osmData,this);
			}
			else {
				mouseEditAction = new MoveAction();
				mouseEditAction.init(osmData,this);
			}

			return true;
		}
		else {
			return false;
		}
	}
	
	private void clearEditAction() {
		mouseEditAction = null;
		clearPending();
	}
	
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

}
