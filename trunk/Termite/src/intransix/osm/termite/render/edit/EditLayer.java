package intransix.osm.termite.render.edit;

import java.util.*;
import java.awt.Graphics2D;
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
import intransix.osm.termite.gui.dialog.CreateLevelDialog;
import intransix.osm.termite.render.MapLayerManager;
import javax.swing.JOptionPane;

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
	
	public final static double SNAP_RADIUS_PIXELS = 4;
	
	private TermiteGui termiteGui;
	private List<EditStateListener> stateListeners = new ArrayList<EditStateListener>();
	private List<FeatureSelectedListener> featureSelectedListeners = new ArrayList<FeatureSelectedListener>();

	
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
	
	public EditLayer(MapLayerManager mapLayerManager, TermiteGui termiteGui) {
		super(mapLayerManager);
		this.termiteGui = termiteGui;

		this.setName("Edit Layer");
	}
	
	// <editor-fold defaultstate="collapsed" desc="Accessors">
	
		
	/** This adds a feature selected listener. */
	public void addFeatureSelectedListener(FeatureSelectedListener listener) {
		featureSelectedListeners.add(listener);
	}
	
	/** This removes a feature selected listener. */
	public void removeFeatureSelectedListener(FeatureSelectedListener listener) {
		featureSelectedListeners.remove(listener);
	}
	
	/** This method will dispatch a feature selected event. It should be called
	 * when a feature is selected to notify all interested objects. */
	public void setSelection(java.util.List<Object> objectSelection,
			java.util.List<Integer> wayNodeSelection) {
		
		//copy to the local list
		if(objectSelection != null) {
			if(objectSelection != this.selection) {
				clearSelection();
				for(Object selectObject:objectSelection) {
					this.selection.add(selectObject);
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
		
		//find the selection type
		FeatureSelectedListener.SelectionType selectionType;
		FeatureSelectedListener.WayNodeType wayNodeType;
		if((selection != null)||(selection.size() > 0)) {

			if(selection.size() == 1) {
				Object selectObject = selection.get(0);
				if(selectObject instanceof OsmWay) {
					selectionType = FeatureSelectedListener.SelectionType.WAY;
				}
				else if(selectObject instanceof OsmNode) {
					selectionType = FeatureSelectedListener.SelectionType.NODE;
				}
				else if(selectObject instanceof intransix.osm.termite.render.edit.VirtualNode) {
					selectionType = FeatureSelectedListener.SelectionType.VIRTUAL_NODE;
				}
				else {
					selection = null;
					selectionType = FeatureSelectedListener.SelectionType.NONE;
				}
			}
			else if(selection.size() > 1) {
				selectionType = FeatureSelectedListener.SelectionType.COLLECTION;
			}
			else {
				selectionType = FeatureSelectedListener.SelectionType.NONE;
			}
		}
		else {
			selectionType = FeatureSelectedListener.SelectionType.NONE;
		}
		
		//get the way node selection, if applicable
		if((wayNodeSelection != null)&&(selectionType == FeatureSelectedListener.SelectionType.WAY)) {
			//check way node selection
			int count = wayNodeSelection.size();
			if(count == 0) {
				wayNodeType = FeatureSelectedListener.WayNodeType.NONE;
			}
			else if(count == 1) {
				wayNodeType = FeatureSelectedListener.WayNodeType.SINGLE;
			}
			else {
				wayNodeType = FeatureSelectedListener.WayNodeType.MULTIPLE;
			}
		}
		else {
			//no way nodes selected
			wayNodeType = FeatureSelectedListener.WayNodeType.NONE;
		}
		
		for(FeatureSelectedListener listener:featureSelectedListeners) {
			listener.onFeatureSelected(selection,selectionType,wayNodeSelection,wayNodeType);
		}
	}
	
	
	public List<Object> getSelection() {
		return selection;
	}
	
	public List<Integer> getSelectedWayNodes() {
		return this.selectedWayNodes;
	}
	
	/** This method returns a way if the selection is a single way. Otherwise
	 * it returns null. 
	 * 
	 * @return 
	 */
	public OsmWay getWaySelection() {
		if(selection.size() == 1) {
			Object selected = selection.get(0);
			if(selected instanceof OsmWay) return (OsmWay)selected;
		}
		//if we get here there is not a single selected way
		return null;
	}
	
	/** This method returns a node if the selection is a single node. Otherwise
	 * it returns null. 
	 * 
	 * @return 
	 */
	public OsmNode getNodeSelection() {
		if(selection.size() == 1) {
			Object selected = selection.get(0);
			if(selected instanceof OsmNode) return (OsmNode)selected;
		}
		//if we get here there is not a single selected node
		return null;
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
		virtualNodeSelected = false;
		clearPending();
		getMapPanel().repaint();
	}
	
	public void clearWayNodesSelection() {
		selectedWayNodes.clear();
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
	
	/** This method ends the current way edit and starts a new one. */
	public void resetWayEdit() {
		//clear old working data
		clearSelection();
		//overwrite old way edit action with a new one
		setMouseEditAction(new WayToolAction());
	}
	
	/** This will end a move edit. */
	public void clearMoveEdit() {
		clearSelection();
		setMouseEditAction(null);
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
		
		AffineTransform mercatorToPixels = getMapPanel().getMercatorToPixels();	
		Style style;
		
		//render selection
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
		if((activeSnapObject != -1)&&(activeSnapObject < snapObjects.size())) {
//System.out.println(("cnt = " + snapObjects.size() + "; active = " + activeSnapObject));
			SnapObject snapObject = snapObjects.get(activeSnapObject);
//System.out.println(snapObject);
			snapObject.render(g2, mercatorToPixels,styleInfo);
		}
		
		//render pending objects
		for(EditObject editObject:pendingObjects) {
			editObject.render(g2, mercatorToPixels, styleInfo);
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
				mouseEditAction.mousePressed(dest,e);
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
				OsmWay selectWay = getWaySelection();
				if((selectWay != null)&&(selectObject instanceof OsmNode)&&
						(selectWay.getNodes().contains((OsmNode)selectObject))) {

					//select a node within a way
					int selectedIndex = selectWay.getNodes().indexOf((OsmNode)selectObject);
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
				}

				virtualNodeSelected = isVirtualNode;

				//report selection
				setSelection(selection, selectedWayNodes);
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
		if(e.getKeyCode() == KeyEvent.VK_COMMA) {
			if(!snapObjects.isEmpty()) {
				activeSnapObject--;
				if(activeSnapObject < -1) activeSnapObject = snapObjects.size() - 1;
				changed = true;
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_PERIOD) {
			if(!snapObjects.isEmpty()) {
				activeSnapObject++;
				if(activeSnapObject >= snapObjects.size()) activeSnapObject = -1;
				changed = true;
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_M) {
			startMove();
		}
		else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if(inMove())  {
				exitMove();
			}
			else if(mouseEditAction instanceof WayToolAction) {
				resetWayEdit();
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
			deleteSelection();
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
	
	// <editor-fold defaultstate="collapsed" desc="Move Mode Control">
	
	public void addEditStateListener(EditStateListener stateListener) {
		if(!stateListeners.contains(stateListener)) {
			stateListeners.add(stateListener);
		}
	}
	
	public void removeEditStateListener(EditStateListener stateListener) {
		stateListeners.remove(stateListener);
	}
	
	/** This method returns true if a move mode is active. */
	public boolean inMove() {
		return ((mouseEditAction instanceof MoveAction)||
				(mouseEditAction instanceof VirtualNodeAction));
	}
	
	public boolean startMove() {
		if((mouseEditAction == null)&&(!selection.isEmpty())) {
				
			if(virtualNodeSelected) {
				mouseEditAction = new VirtualNodeAction();
				mouseEditAction.init(osmData,this);
			}
			else {
				mouseEditAction = new MoveAction();
				mouseEditAction.init(osmData,this);
			}
			
			//notify listeners
			for(EditStateListener esl:stateListeners) {
				esl.editModeChanged(true);
			}

			getMapPanel().repaint();
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean exitMove() {
		if(inMove()) {
			clearEditAction();
			
			//notify listeners
			for(EditStateListener esl:stateListeners) {
				esl.editModeChanged(false);
			}
			
			getMapPanel().repaint();
			
			return true;
		}
		else {
			return false;
		}
	}
	// </editor-fold>
	
	// <editor-fold defaultstate="collapsed" desc="Edit Actions">
	
	public void deleteSelection() {
		//works on a node or way or a collection of nodes and ways
		if((!selection.isEmpty())&&(osmData != null)) {
			DeleteSelection ds = new DeleteSelection(osmData);
			ds.deleteSelection(selection);
			
			//clear the selection
			clearSelection();
		}
	}
	
	public void removeNodeFromWay() {
		//works on a node selected within a way
		if(osmData != null) {
			if((!selection.isEmpty())&&(!selectedWayNodes.isEmpty())) {
				Object obj = selection.get(0);
				if(obj instanceof OsmWay) {
					RemoveWayNodeEdit rwne = new RemoveWayNodeEdit(osmData);
					rwne.removeNodesFromWay((OsmWay)obj,selectedWayNodes);
				}
				
				clearWayNodesSelection();
			}
		}
	}
	
	public void createLevel() {
		if(selection.size() == 1) {
			Object parent = selection.get(0);
			if(parent instanceof OsmObject) {
				CreateLevelDialog cld = new CreateLevelDialog(null,osmData,(OsmObject)parent);
				cld.setVisible(true);
			}
			else {
				JOptionPane.showMessageDialog(null,"Invalid object type for creating a level.");
			}
		}
		else {
			JOptionPane.showMessageDialog(null,"A single object must be selected to create a level.");
		}
	}
	
	public void changeSelectionFeatureType() {
		if(!selection.isEmpty()) {
			TypeChangeEdit tce = new TypeChangeEdit(osmData);
			tce.modifyType(selection,featureInfo);
		}
		else {
			JOptionPane.showMessageDialog(null,"An object must be selected.");
		}
	}
	
	// </editor-fold>
	
	//============================
	// Private Methods
	//============================
	
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
