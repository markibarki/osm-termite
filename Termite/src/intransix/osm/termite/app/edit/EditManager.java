package intransix.osm.termite.app.edit;

import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.app.level.LevelManager;
import intransix.osm.termite.app.mapdata.MapDataListener;
import intransix.osm.termite.app.edit.editobject.EditObject;
import intransix.osm.termite.app.edit.editobject.EditSegment;
import intransix.osm.termite.app.edit.editobject.EditNode;
import intransix.osm.termite.app.edit.snapobject.SnapObject;
import intransix.osm.termite.app.edit.snapobject.SnapNode;
import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.data.edit.EditDestPoint;
import intransix.osm.termite.render.edit.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author sutter
 */
public class EditManager implements MapDataListener {

	//=========================
	// Properties 
	//=========================
	
	//------------------
	// Edit State
	//------------------
	
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
	
	private List<FeatureSelectedListener> featureSelectedListeners = new ArrayList<FeatureSelectedListener>();

	//-----------------
	// Edit Layer and Modes
	//-----------------
	
	private EditLayer editLayer;
	private SelectEditorMode selectMode;
	private NodeEditorMode nodeMode;
	private WayEditorMode wayMode;
	
	private LevelManager levelManager;
	private FeatureTypeManager featureTypeManager;
	private OsmData osmData;
	
	//===========================
	// Public Methods
	//===========================
	
	public EditManager(FeatureTypeManager featureTypeManager, LevelManager levelManager) {
		this.featureTypeManager = featureTypeManager;
		this.levelManager = levelManager;
	}
	
	//---------------------
	// Edit State Accessors
	//---------------------
	
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
	
	public List<EditObject> getPendingObjects() {
		return pendingObjects;
	}
	
	public List<EditNode> getMovingNodes() {
		return movingNodes;
	}
	
	public List<EditSegment> getPendingSnapSegments() {
		return pendingSnapSegments;
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
		
		for(FeatureSelectedListener listener:featureSelectedListeners) {
			listener.onFeatureSelected(selection,wayNodeSelection);
		}
	}
	
	public EditDestPoint getSelectionPoint() {
		return this.selectionPoint;
	}
	
	public void setSelectionPoint(EditDestPoint selectionPoint) {
		this.selectionPoint = selectionPoint;
	}	
	
	/** This method clears the current selection. */
	public void clearSelection() {
		selection.clear();
		selectedWayNodes.clear();
		virtualNodeSelected = false;
		clearPending();
	}
	
	public void clearWayNodesSelection() {
		selectedWayNodes.clear();
		virtualNodeSelected = false;
		clearPending();
	}
	
	/** This method clears all data in the pending state. */
	public void clearPending() {
		pendingObjects.clear();
		pendingSnapSegments.clear();
		movingNodes.clear();
	}
	
	public void clearPreview() {
		snapObjects.clear();
		activeSnapObject = -1;
	}
	
//////////////////////////////////////////////////////////////////////////
	
//	/** This method ends the current way edit and starts a new one. */
//	public void resetWayEdit() {
//		//clear old working data
//		clearSelection();
//		//overwrite old way edit action with a new one
//		editLayer.setMouseClickAction(new WayToolClickAction());
//	}
//	
//	/** This will end a move edit. */
//	public void clearMoveEdit() {
//		clearSelection();
//		editLayer.setMouseClickAction(new SelectClickAction());
//	}
//		
//		/** This method returns true if a move mode is active. */
//	public boolean inMove() {
//		return ((mouseClickAction instanceof MoveClickAction)||
//				(mouseClickAction instanceof VirtualNodeClickAction));
//	}
//////////////////////////////////////////////////////////////////////////
	
	//--------------------
	// Layer, Mode accessors
	//---------------------
	
	public OsmData getOsmData() {
		return osmData;
	}
	
	public FeatureTypeManager getFeatureTypeManager() {
		return featureTypeManager;
	}
	
	public LevelManager getLevelManager() { 
		return levelManager;
	}
	
	/** This adds a feature selected listener. */
	public void addFeatureSelectedListener(FeatureSelectedListener listener) {
		featureSelectedListeners.add(listener);
	}
	
	/** This removes a feature selected listener. */
	public void removeFeatureSelectedListener(FeatureSelectedListener listener) {
		featureSelectedListeners.remove(listener);
	}
	
	/** This is a method to look up the current mouse point, from the edit layer. */
	public Point2D getMousePointMerc() {
		return editLayer.getMapPanel().getMousePointMerc();
	}
	
	
	public EditLayer getEditLayer() {
		return editLayer;
	}
	
	public SelectEditorMode getSelectEditorMode() {
		return selectMode;
	}
	
	public NodeEditorMode getNodeEditorMode() {
		return nodeMode;
	}
	
	public WayEditorMode getWayEditorMode() {
		return wayMode;
	}
	
	public void init() {
		editLayer = new EditLayer(this);
		selectMode = new SelectEditorMode(editLayer);
		nodeMode = new NodeEditorMode(editLayer);
		wayMode = new WayEditorMode(editLayer);
	}	
	
		// <editor-fold defaultstate="collapsed" desc="Map Data Listener">
	/** This method is called when the map data is set. */
	@Override
	public void onMapData(OsmData osmData) {
		this.osmData = osmData;		
	}
	// </editor-fold>
	
	
	//========================
	// Protected Methods
	//========================
	
	//========================
	// Package Methods
	//========================
	
	
	//========================
	// Private Methods
	//========================
	
		/** This method returns the current edit destination point based on the
	 * currently selected hover node or segment. 
	 * 
	 * @param mouseMerc		The location of the mouse in mercator coordinates.
	 * @return				The EditDestPoint
	 */
	public EditDestPoint getDestinationPoint(Point2D mouseMerc) {
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
