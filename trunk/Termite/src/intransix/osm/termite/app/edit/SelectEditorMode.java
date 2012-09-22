package intransix.osm.termite.app.edit;

import intransix.osm.termite.app.mode.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.app.edit.FeatureSelectedListener;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.app.edit.action.SelectClickAction;
import intransix.osm.termite.app.edit.action.SelectSnapMoveAction;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.edit.action.*;
import intransix.osm.termite.map.data.edit.*;
import intransix.osm.termite.map.data.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class SelectEditorMode extends EditorMode implements ActionListener,
		FeatureSelectedListener,EditStateListener {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Select Tool";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/selectMode.png";
	
	private final static String SELECT_TEXT = "Select [esc]";
	private final static String MOVE_TEXT = "Move [m]";
	
	private final static String SELECT_CMD = "select";
	private final static String MOVE_CMD = "move";
	private final static String DELETE_CMD = "delete";
	private final static String REMOVE_NODE_CMD = "remove";
	private final static String CHANGE_FEATURE_TYPE_CMD = "changeFeatureType";
	private final static String CREATE_LEVEL_CMD = "createLevel";
	
	private final static int SPACE_X = 8;
	private final static int SPACE_Y = 8;
	
	private EditManager editManager;
	private EditLayer editLayer;
	
	private JToolBar toolBar = null;
	
	private ButtonGroup modeButtonGroup;
	private JToggleButton selectButton;
	private JToggleButton moveButton;
	
	private JButton deleteButton;
	private JButton removeNodeButton;
	private JButton changeFeatureTypeButton;
	private JButton createLevelButton;
	
	private List<EditStateListener> stateListeners = new ArrayList<EditStateListener>();
	
	private ChangeFeatureTypeAction changeFeatureTypeAction;
	private CreateLevelAction createLevelAction;
	private DeleteSelectionAction deleteSelectionAction;
	private RemoveNodeFromWayAction removeNodeFromWayAction;
	
	private boolean inMoveState = false;
	
	//====================
	// Public Methods
	//====================
	
	public SelectEditorMode(EditLayer editLayer) {
		this.editLayer = editLayer;
		createToolBar();
		
		//I need to separate out the ui, then this will go in ui class
		this.addEditStateListener(this);
		
		changeFeatureTypeAction = new ChangeFeatureTypeAction(editManager);
		createLevelAction = new CreateLevelAction(editManager);
		deleteSelectionAction = new DeleteSelectionAction(editManager);
		removeNodeFromWayAction = new RemoveNodeFromWayAction(editManager);
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
	}
	
	public void addEditStateListener(EditStateListener stateListener) {
		if(!stateListeners.contains(stateListener)) {
			stateListeners.add(stateListener);
		}
	}
	
	public void removeEditStateListener(EditStateListener stateListener) {
		stateListeners.remove(stateListener);
	}
	
//////////////////////////////////////////////////
	public boolean isInMoveState() {
		return inMoveState;
	}
	
	public boolean setSelectState() {
		if((this.getModeEnabled())&&(!inMoveState)) {
			MouseClickAction mouseClickAction = new SelectClickAction(editManager);
			mouseClickAction.init();
			
			MouseMoveAction snapAction = new SelectSnapMoveAction(editManager);	
			snapAction.init();
			
			editLayer.setMouseClickAction(mouseClickAction);
			editLayer.setMouseMoveActions(null, snapAction);
			
			editManager.clearPreview();
			
			inMoveState = true;
			
			//notify listeners
			for(EditStateListener esl:stateListeners) {
				esl.editStateChanged(false);
			}
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean setMoveState() {
		if((this.getModeEnabled())&&(inMoveState)) {
			List<Object> selection = editManager.getSelection();
			if(selection.isEmpty()) return false;
			
			//set up move state
			MouseClickAction mouseClickAction;
			if(editManager.getVirtualNodeSelected()) {
				mouseClickAction = new VirtualNodeClickAction(editManager);		
			}
			else {
				mouseClickAction = new MoveClickAction(editManager);
			}
			mouseClickAction.init();
			
			MouseMoveAction snapAction = new CreateSnapMoveAction(editManager);
			snapAction.init();
			MouseMoveAction moveAction = new MoveMoveMoveAction(editManager);
			moveAction.init();
			
			editLayer.setMouseClickAction(mouseClickAction);
			editLayer.setMouseMoveActions(moveAction, snapAction);
			
			inMoveState = false;
			
			//notify listeners
			for(EditStateListener esl:stateListeners) {
				esl.editStateChanged(true);
			}
			
			return true;	
		}
		else {
			return false;
		}
	}
//////////////////////////////////////////////////
	
	@Override
	public JToolBar getToolBar() {
		if(toolBar == null) {
			createToolBar();
		}
		return toolBar;
	}
	
	
	/** This method is called when the edit state changes. */
	@Override
	public void editStateChanged(boolean inMove) {
		if(inMove) {
			if(!moveButton.isSelected()) {
				moveButton.setSelected(true);
			}
		}
		else {
			if(!selectButton.isSelected()) {
				selectButton.setSelected(true);
			}
		}
	}
	
		
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
				else if(selectObject instanceof intransix.osm.termite.app.edit.VirtualNode) {
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
				
		deleteButton.setEnabled(deleteEnabled);
		removeNodeButton.setEnabled(removeEnabled);
		changeFeatureTypeButton.setEnabled(featureTypeEnabled);
		createLevelButton.setEnabled(levelEnabled);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if(SELECT_CMD.equals(ae.getActionCommand())) {
			setSelectState();
		}
		else if(MOVE_CMD.equals(ae.getActionCommand())) {
			boolean success = setMoveState();
			if(!success) {
				//update the edit mode to be the active 
				this.editStateChanged(false);
			}
		}
		else if(DELETE_CMD.equals(ae.getActionCommand())) {
			deleteSelectionAction.deleteSelection();
		}
		else if(REMOVE_NODE_CMD.equals(ae.getActionCommand())) {
			removeNodeFromWayAction.removeNodeFromWay();
		}
		else if(CHANGE_FEATURE_TYPE_CMD.equals(ae.getActionCommand())) {
			changeFeatureTypeAction.changeSelectionFeatureType();
		}
		else if(CREATE_LEVEL_CMD.equals(ae.getActionCommand())) {
			createLevelAction.createLevel();
		}
	}
	
	private void createToolBar() {	
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		
		modeButtonGroup = new ButtonGroup();
		selectButton = new JToggleButton(SELECT_TEXT);
		selectButton.setActionCommand(SELECT_CMD);
		selectButton.addActionListener(this);
		modeButtonGroup.add(selectButton);
		toolBar.add(selectButton);
		
		moveButton = new JToggleButton(MOVE_TEXT);
		moveButton.setActionCommand(MOVE_CMD);
		moveButton.addActionListener(this);
		modeButtonGroup.add(moveButton);
		toolBar.add(moveButton);
		
		Box.Filler space1 = new Box.Filler(new Dimension(SPACE_X, SPACE_Y), new Dimension(SPACE_X, SPACE_Y), new Dimension(SPACE_X, SPACE_Y));
		toolBar.add(space1);
		
		deleteButton = new JButton("Delete");
		deleteButton.setActionCommand(DELETE_CMD);
		deleteButton.addActionListener(this);
		toolBar.add(deleteButton);
		
		removeNodeButton = new JButton("Remove Node");
		removeNodeButton.setActionCommand(REMOVE_NODE_CMD);
		removeNodeButton.addActionListener(this);
		toolBar.add(removeNodeButton);
		
		changeFeatureTypeButton = new JButton("Change Feature Type");
		changeFeatureTypeButton.setActionCommand(CHANGE_FEATURE_TYPE_CMD);
		changeFeatureTypeButton.addActionListener(this);
		toolBar.add(changeFeatureTypeButton);
		
		createLevelButton = new JButton("Create Level");
		createLevelButton.setActionCommand(CREATE_LEVEL_CMD);
		createLevelButton.addActionListener(this);
		toolBar.add(createLevelButton);
		
		//add action listeners
		
	}

}
