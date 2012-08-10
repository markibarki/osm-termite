package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.gui.FeatureSelectedListener;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.edit.EditStateListener;
import intransix.osm.termite.map.data.edit.*;
import intransix.osm.termite.map.data.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
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
	
	private TermiteGui termiteGui;
	private EditLayer editLayer;
	
	private JToolBar toolBar = null;
	
	private ButtonGroup modeButtonGroup;
	private JToggleButton selectButton;
	private JToggleButton moveButton;
	
	private JButton deleteButton;
	private JButton removeNodeButton;
	private JButton changeFeatureTypeButton;
	private JButton createLevelButton;
	
	//====================
	// Public Methods
	//====================
	
	public SelectEditorMode(TermiteGui termiteGui) {
		this.termiteGui = termiteGui;
		createToolBar();
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
		if(editLayer == null) {
			editLayer = termiteGui.getEditLayer();
			editLayer.addGeocodeStateListener(this);
		}
		
		MapLayer renderLayer = termiteGui.getRenderLayer();
		if(renderLayer != null) {
			renderLayer.setActiveState(true);
		}
		if(editLayer != null) {
			editLayer.setActiveState(true);
			editLayer.setMouseEditAction(null);
			
			//update the edit mode
			this.editModeChanged(editLayer.inMove());
		}
		
		termiteGui.addToolBar(toolBar);
		termiteGui.addFeatureSelectedListener(this);
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	@Override
	public void turnOff() {
		MapLayer renderLayer = termiteGui.getRenderLayer();
		if(renderLayer != null) {
			renderLayer.setActiveState(false);
		}
		if(editLayer != null) {
			editLayer.setActiveState(false);
		}
		
		if(toolBar != null) {
			termiteGui.removeToolBar(toolBar);
		}
		termiteGui.removeFeatureSelectedListener(this);
	}
	
	
	/** This method is called when the edit state changes. */
	@Override
	public void editModeChanged(boolean inMove) {
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
	public void onFeatureSelected(List<Object> selection, SelectionType selectionType,
			List<Integer> wayNodeSelection, WayNodeType wayNodeType) {
		
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
				if(wayNodeType != FeatureSelectedListener.WayNodeType.NONE) {
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
			editLayer.exitMove();
		}
		else if(MOVE_CMD.equals(ae.getActionCommand())) {
			boolean success = editLayer.startMove();
			if(!success) {
				//update the edit mode to be the active 
				this.editModeChanged(editLayer.inMove());
			}
		}
		else if(DELETE_CMD.equals(ae.getActionCommand())) {
			editLayer.deleteSelection();
		}
		else if(REMOVE_NODE_CMD.equals(ae.getActionCommand())) {
			editLayer.removeNodeFromWay();
		}
		else if(CHANGE_FEATURE_TYPE_CMD.equals(ae.getActionCommand())) {
			//works on a node or way or collection of nodes and ways
		}
		else if(CREATE_LEVEL_CMD.equals(ae.getActionCommand())) {
			//works on a single way (or node?)
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
