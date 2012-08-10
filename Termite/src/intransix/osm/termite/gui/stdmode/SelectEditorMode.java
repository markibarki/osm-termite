package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.gui.FeatureSelectedListener;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.map.data.edit.*;
import intransix.osm.termite.map.data.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class SelectEditorMode extends EditorMode implements ActionListener,
		FeatureSelectedListener {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Select Tool";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/selectMode.png";
	
	private final static String DELETE_CMD = "delete";
	private final static String REMOVE_NODE_CMD = "remove";
	private final static String CHANGE_FEATURE_TYPE_CMD = "changeFeatureType";
	private final static String CREATE_LEVEL_CMD = "createLevel";
	
	private TermiteGui termiteGui;
	
	private JToolBar toolBar = null;
	
	private JButton deleteButton;
	private JButton removeNodeButton;
	private JButton changeFeatureTypeButton;
	private JButton createLevelButton;
	
	//====================
	// Public Methods
	//====================
	
	public SelectEditorMode(TermiteGui termiteGui) {
		this.termiteGui = termiteGui;
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
		MapLayer renderLayer = termiteGui.getRenderLayer();
		if(renderLayer != null) {
			renderLayer.setActiveState(true);
		}
		EditLayer editLayer = termiteGui.getEditLayer();
		if(editLayer != null) {
			editLayer.setActiveState(true);
			editLayer.setMouseEditAction(null);
		}
		
		if(toolBar == null) {
			createToolBar();
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
		MapLayer editLayer = termiteGui.getEditLayer();
		if(editLayer != null) {
			editLayer.setActiveState(false);
		}
		
		if(toolBar != null) {
			termiteGui.removeToolBar(toolBar);
		}
		termiteGui.removeFeatureSelectedListener(this);
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
		if(DELETE_CMD.equals(ae.getActionCommand())) {
			//works on a node or way or a collection of nodes and ways
			List<Object> selection = termiteGui.getSelection();
			OsmData osmData = termiteGui.getMapData();
			if((selection != null)&&(osmData != null)) {
				DeleteSelection ds = new DeleteSelection(osmData);
				ds.deleteSelection(selection);
			}
		}
		else if(REMOVE_NODE_CMD.equals(ae.getActionCommand())) {
			//works on a node selected within a way
			List<Object> selection = termiteGui.getSelection();
			List<Integer> selectedNodes = termiteGui.getWayNodeSelection();
			OsmData osmData = termiteGui.getMapData();
			if((selection != null)&&(selectedNodes != null)&&(osmData != null)) {
				if(!selection.isEmpty()) {
					Object obj = selection.get(0);
					if(obj instanceof OsmWay) {
						RemoveWayNodeEdit rwne = new RemoveWayNodeEdit(osmData);
						rwne.removeNodesFromWay((OsmWay)obj,selectedNodes);
					}
				}
			}
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
