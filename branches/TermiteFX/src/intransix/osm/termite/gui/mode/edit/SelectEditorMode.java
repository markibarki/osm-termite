package intransix.osm.termite.gui.mode.edit;

import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.edit.FeatureSelectedListener;
import intransix.osm.termite.app.edit.MouseClickAction;
import intransix.osm.termite.app.edit.MouseMoveAction;
import intransix.osm.termite.app.edit.SelectEditStateListener;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.gui.mode.EditorMode;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.edit.SelectToolbar;
import intransix.osm.termite.app.edit.action.SelectClickAction;
import intransix.osm.termite.app.edit.action.SelectSnapMoveAction;
import intransix.osm.termite.app.edit.action.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * This method is used to select objects and to take any actions assoicated with
 * selected objects, such as move, delete, etc.
 * 
 * @author sutter
 */
public class SelectEditorMode extends EditorMode implements FeatureSelectedListener {
	//====================
	// Properties
	//====================
	
	/** This enumeration indicates the selection type. */
	public enum SelectionType {
		NONE,	//no selection
		NODE,	//a single node selected
		VIRTUAL_NODE,	//a singel virtual node selected
		WAY,	//a single way selected
		COLLECTION	// a collection of nodes and ways (no virtual nodes)
	}
	
	/** This enumeration is valid if the selection is a way. This indicates
	 * if nodes within the way are a part of the selection. */
	public enum WayNodeType {
		NONE, //no way nodes selected
		SINGLE,	//a single way node selected
		MULTIPLE	//multiple way nodes selected
	}
	
	private final static String MODE_NAME = "Select Tool";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/selectMode.png";
	
	private EditManager editManager;
	private EditLayer editLayer;
	
	private SelectToolbar toolBar = null;
	
	private List<SelectEditStateListener> stateListeners = new ArrayList<SelectEditStateListener>();
	
	private ChangeFeatureTypeAction changeFeatureTypeAction;
	private CreateLevelAction createLevelAction;
	private DeleteSelectionAction deleteSelectionAction;
	private RemoveWayNodeAction removeNodeFromWayAction;
	
	private boolean inMoveState = false;
	
	//====================
	// Public Methods
	//====================
	
	/** Constructor. */
	public SelectEditorMode(EditManager editManager) {
		this.editManager = editManager;
		this.editLayer = editManager.getEditLayer();
		toolBar = new SelectToolbar(this);
		
		changeFeatureTypeAction = new ChangeFeatureTypeAction(editManager);
//		createLevelAction = new CreateLevelAction(editManager);
		deleteSelectionAction = new DeleteSelectionAction(editManager);
		removeNodeFromWayAction = new RemoveWayNodeAction(editManager);
	}
	
	/** This method will delete the selected item. */
	public void deleteSelection() {
		deleteSelectionAction.deleteSelection();
	}
	
	/** This method removes the selected way nodes from the selected way. */
	public void removeNodeFromWay() {
		removeNodeFromWayAction.removeNodeFromWay();
	}
	
	/** This method changes the selected feature's feature type. */
	public void changeSelectionFeatureType() {
		changeFeatureTypeAction.changeSelectionFeatureType();
	}
	
	/** This method creates a level for the selected way. */
	public void createLevel() {
throw new RuntimeException("Add create level action back");
//		createLevelAction.createLevel();
	}
	
	/** This method returns the name of the editor mode. 
	 * 
	 * @return		The name of the editor mode 
	 */
	@Override
	public String getName() {
		return MODE_NAME;
	}
	
	/** This method returns the name for image for the edit mode button on the
	 * edit mode toolbar. 
	 * 
	 * @return		The name of the icon image for this mode. 
	 */
	@Override
	public String getIconImageName() {
		return ICON_NAME;
	}
	
	/** This method is called when the editor mode is turned on. 
	 */
	@Override
	public void turnOn() {
		if(editLayer != null) {
			editLayer.setActiveState(true);
			setSelectState();
			editManager.addFeatureSelectedListener(this);
		}
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	@Override
	public void turnOff() {
		if(editLayer != null) {
			editLayer.setActiveState(false);
			editManager.removeFeatureSelectedListener(this);
		}
		//remove from move state if it is there
		inMoveState = false;
	}
	
	/** This method adds an edit state listener. */
	public void addEditStateListener(SelectEditStateListener stateListener) {
		if(!stateListeners.contains(stateListener)) {
			stateListeners.add(stateListener);
		}
	}
	
	/** This method removes an edit state listener. */
	public void removeEditStateListener(SelectEditStateListener stateListener) {
		stateListeners.remove(stateListener);
	}
	
	/** This method returns true if a move is currently in progress. */
	public boolean isInMoveState() {
		return inMoveState;
	}
	
	/** This method sets the select submode. */
	public boolean setSelectState() {
		if(this.getModeEnabled()) {
			MouseClickAction mouseClickAction = new SelectClickAction(editManager);
			mouseClickAction.init();
			
			MouseMoveAction snapAction = new SelectSnapMoveAction(editManager);	
			snapAction.init();
			
			editLayer.setMouseClickAction(mouseClickAction);
			editLayer.setMouseMoveActions(null, snapAction);
			
			//clear any old state, but not selection
			editManager.clearPending();
			editManager.clearPreview();
			editLayer.notifyContentChange();
			
			inMoveState = false;
			
			//notify listeners
			for(SelectEditStateListener esl:stateListeners) {
				esl.editStateChanged(false);
			}
			
			return true;
		}
		else {
			return false;
		}
	}
	
	/** This method sets the move submode. */
	public boolean setMoveState() {
		if((this.getModeEnabled())&&(!inMoveState)) {
			List<Object> selection = editManager.getSelection();
			if(selection.isEmpty()) return false;
			
			//set up move state
			MouseClickAction mouseClickAction;
			MouseMoveAction moveAction;
			if(editManager.getVirtualNodeSelected()) {
				mouseClickAction = new VirtualNodeClickAction(editManager);	
				moveAction = new CreateMoveMoveAction(editManager);
			}
			else {
				mouseClickAction = new MoveClickAction(editManager);
				moveAction = new MoveMoveMoveAction(editManager);
			}
			mouseClickAction.init();
			
			MouseMoveAction snapAction = new CreateSnapMoveAction(editManager);
			snapAction.init();
			moveAction.init();
			
			editLayer.setMouseClickAction(mouseClickAction);
			editLayer.setMouseMoveActions(moveAction, snapAction);
			
			inMoveState = true;
			
			//notify listeners
			for(SelectEditStateListener esl:stateListeners) {
				esl.editStateChanged(true);
			}
			
			return true;	
		}
		else {
			return false;
		}
	}
	
	/** This method gets the toolbar. */
	@Override
	public JToolBar getToolBar() {
		return toolBar;
	}
	
	/** This method is called when a map feature is selected. The arguments selectionType
	 * and wayNodeType indicate the type of selection made. The list objects may be null
	 * if there is no selection for the list. 
	 * 
	 * @param selection			A list of the selected objects
	 * @param selectionType		The type objects objects in the selection
	 * @param wayNodeSelection	If the selection is a single way, this is a possible list
	 *							of selected nodes within the way.
	 * @param wayNodeType		This is the type of way nodes selected for the way, if applicable.
	 */
	@Override
	public void onFeatureSelected(List<Object> selection, List<Integer> wayNodeSelection) {
		
		//find the selection type
		SelectionType selectionType;
		WayNodeType wayNodeType;
		if((selection != null)||(selection.size() > 0)) {

			if(selection.size() == 1) {
				Object selectObject = selection.get(0);
				if(selectObject instanceof OsmWay) {
					selectionType = SelectionType.WAY;
				}
				else if(selectObject instanceof OsmNode) {
					selectionType = SelectionType.NODE;
				}
				else if(selectObject instanceof intransix.osm.termite.app.edit.data.VirtualNode) {
					selectionType = SelectionType.VIRTUAL_NODE;
				}
				else {
					selection = null;
					selectionType = SelectionType.NONE;
				}
			}
			else if(selection.size() > 1) {
				selectionType = SelectionType.COLLECTION;
			}
			else {
				selectionType = SelectionType.NONE;
			}
		}
		else {
			selectionType = SelectionType.NONE;
		}
		
		//get the way node selection, if applicable
		if((wayNodeSelection != null)&&(selectionType == SelectionType.WAY)) {
			//check way node selection
			int count = wayNodeSelection.size();
			if(count == 0) {
				wayNodeType = WayNodeType.NONE;
			}
			else if(count == 1) {
				wayNodeType = WayNodeType.SINGLE;
			}
			else {
				wayNodeType = WayNodeType.MULTIPLE;
			}
		}
		else {
			//no way nodes selected
			wayNodeType = WayNodeType.NONE;
		}
		
		
		//determine which buttons are active	
		boolean deleteEnabled = false;
		boolean removeEnabled = false;
		boolean featureTypeEnabled = false;
		boolean levelEnabled = false;
		
		switch(selectionType) {
			case NONE:
				break;
			case NODE:
				deleteEnabled = true;
				featureTypeEnabled = true;
				break;
			case VIRTUAL_NODE:
				break;
			case WAY:
				deleteEnabled = true;
				featureTypeEnabled = true;
				if(wayNodeType != WayNodeType.NONE) {
					removeEnabled = true;
				}
				levelEnabled = true;
				break;
			case COLLECTION:
				deleteEnabled = true;
				featureTypeEnabled = true;
				break;
			default:
				break;
		}
				
		toolBar.enableDelete(deleteEnabled);
		toolBar.enableRemoveNode(removeEnabled);
		toolBar.enableChangeFeatureType(featureTypeEnabled);
		toolBar.enableCreateLevel(levelEnabled);
	}
}
