package intransix.osm.termite.render.edit;

import intransix.osm.termite.app.level.LevelSelectedListener;
import intransix.osm.termite.app.edit.FeatureSelectedListener;
import intransix.osm.termite.app.feature.FeatureTypeListener;
import intransix.osm.termite.app.mapdata.MapDataListener;
import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.event.*;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.data.edit.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.gui.*;
import java.awt.*;
import java.util.List;
import intransix.osm.termite.gui.dialog.CreateLevelDialog;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import javax.swing.JOptionPane;

/**
 * This layer controls the user interaction with the active map data. It is designed
 * to run with the editor modes for the Select Tool, Node Tool and Way Tool.
 * 
 * @author sutter
 */
public class EditLayer extends MapLayer implements MapDataListener, 
		FeatureTypeListener, LevelSelectedListener,
		MouseListener, MouseMotionListener, KeyListener {
	
	//=========================
	// Properties 
	//=========================
	
	// <editor-fold defaultstate="collapsed" desc="Properties">
	
	public final static double SNAP_RADIUS_PIXELS = 4;
	
	private List<EditStateListener> stateListeners = new ArrayList<EditStateListener>();
	private List<FeatureSelectedListener> featureSelectedListeners = new ArrayList<FeatureSelectedListener>();

	
	private OsmData osmData;
	private StyleInfo styleInfo = new StyleInfo();

	private MouseClickAction mouseClickAction;
	private MouseMoveAction moveMouseMoveAction;
	private MouseMoveAction snapMouseMoveAction;
	
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
	
	// </editor-fold>
	
	//=========================
	// Public Methods
	//=========================
	
	public EditLayer() {
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
	
	public List<SnapObject> getSnapObjects() {
		return snapObjects;
	}
	
	public int getActiveSnapObject() {
		return activeSnapObject;
	}
	
	public void setActiveSnapObject(int setActiveSnapObject) {
		this.activeSnapObject = setActiveSnapObject;
	}
	
	public boolean getVirtualNodeSelected() {
		return virtualNodeSelected;
	}
	
	public void setVirtualNodeSelected(boolean virtualNodeSelected) {
		this.virtualNodeSelected = virtualNodeSelected;
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
		notifyContentChange();
	}
	
	public void clearWayNodesSelection() {
		selectedWayNodes.clear();
		virtualNodeSelected = false;
		clearPending();
		notifyContentChange();
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
		setMouseClickAction(new WayToolClickAction());
	}
	
	/** This will end a move edit. */
	public void clearMoveEdit() {
		clearSelection();
		setMouseClickAction(new SelectClickAction());
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
	public void setMouseClickAction(MouseClickAction mouseClickAction) {
		if(mouseClickAction != null) {
			this.mouseClickAction = mouseClickAction;
			mouseClickAction.init(osmData, this);
		}
		else {
			this.mouseClickAction = null;
			clearAll();
		}
	}
	
	public void setMouseMoveActions(MouseMoveAction moveMouseMoveAction,
		MouseMoveAction snapMouseMoveAction) {
		
		this.moveMouseMoveAction = moveMouseMoveAction;
		if(moveMouseMoveAction != null) {
			moveMouseMoveAction.init(osmData, this);
		}
		this.snapMouseMoveAction = snapMouseMoveAction;
		if(snapMouseMoveAction != null) {
			snapMouseMoveAction.init(osmData, this);
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
		
		AffineTransform mercatorToPixels = getViewRegionManager().getMercatorToPixels();	
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
		
		//reinitialize the actions
		if(mouseClickAction != null) {
			mouseClickAction.init(osmData, this);
		}
		if(moveMouseMoveAction != null) {
			moveMouseMoveAction.init(osmData, this);
		}
		if(snapMouseMoveAction != null) {
			snapMouseMoveAction.init(osmData, this);
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
	public void onFeatureTypeSelected(FeatureInfo featureInfo) {
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
		
		boolean isActive = (!snapObjects.isEmpty())||(!movingNodes.isEmpty());
		
		//repaint if there is a hit or if the hit status changes
		if(wasActive||isActive) {
			notifyContentChange();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		AffineTransform pixelsToMercator = getViewRegionManager().getPixelsToMercator();
		Point2D mouseMerc = new Point2D.Double(e.getX(),e.getY());
		pixelsToMercator.transform(mouseMerc, mouseMerc);
		
		if(e.getButton() == MouseEvent.BUTTON1) {
			
			EditDestPoint dest = getDestinationPoint(mouseMerc);
			
			if(mouseClickAction != null) {
				//let the mouse edit action handle the press
				mouseClickAction.mousePressed(dest,e);
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
			else if(mouseClickAction instanceof WayToolClickAction) {
				resetWayEdit();
			}
		}
		else if(e.getKeyCode() == KeyEvent.VK_DELETE) {
			deleteSelection();
		}
		
		if(changed) {
			notifyContentChange();
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
		return ((mouseClickAction instanceof MoveClickAction)||
				(mouseClickAction instanceof VirtualNodeClickAction));
	}
	
	public boolean startMove() {
		if((mouseClickAction instanceof SelectClickAction)&&(!selection.isEmpty())) {
				
			if(virtualNodeSelected) {
				mouseClickAction = new VirtualNodeClickAction();
				mouseClickAction.init(osmData,this);
			}
			else {
				mouseClickAction = new MoveClickAction();
				mouseClickAction.init(osmData,this);
			}
			
			//notify listeners
			for(EditStateListener esl:stateListeners) {
				esl.editModeChanged(true);
			}

			notifyContentChange();
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
			
			notifyContentChange();
			
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
		mouseClickAction = null;
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
		notifyContentChange();
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
