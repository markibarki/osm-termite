package intransix.osm.termite.render.edit;

import javax.swing.*;
import intransix.osm.termite.app.edit.SelectEditorMode;
import intransix.osm.termite.app.edit.SelectEditStateListener;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author sutter
 */
public class SelectToolbar extends JToolBar implements SelectEditStateListener, ActionListener {
	
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
	
	private SelectEditorMode selectMode;
	
	private ButtonGroup modeButtonGroup;
	private JToggleButton selectButton;
	private JToggleButton moveButton;
	
	private JButton deleteButton;
	private JButton removeNodeButton;
	private JButton changeFeatureTypeButton;
	private JButton createLevelButton;
	
	public SelectToolbar(SelectEditorMode selectMode) {
		this.selectMode = selectMode;
		selectMode.addEditStateListener(this);
		initComponents();
	}
	
	public void enableDelete(boolean deleteEnabled) {
		deleteButton.setEnabled(deleteEnabled);
	}
	public void enableRemoveNode(boolean removeEnabled) {
		removeNodeButton.setEnabled(removeEnabled);
	}
	
	public void enableChangeFeatureType(boolean featureTypeEnabled) {
		changeFeatureTypeButton.setEnabled(featureTypeEnabled);
	}
	
	public void enableCreateLevel(boolean levelEnabled) {
		createLevelButton.setEnabled(levelEnabled);
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
	
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if(SELECT_CMD.equals(ae.getActionCommand())) {
			selectMode.setSelectState();
		}
		else if(MOVE_CMD.equals(ae.getActionCommand())) {
			boolean success = selectMode.setMoveState();
			if(!success) {
				//update the edit mode to be the active 
				this.editStateChanged(false);
			}
		}
		else if(DELETE_CMD.equals(ae.getActionCommand())) {
			selectMode.deleteSelection();
		}
		else if(REMOVE_NODE_CMD.equals(ae.getActionCommand())) {
			selectMode.removeNodeFromWay();
		}
		else if(CHANGE_FEATURE_TYPE_CMD.equals(ae.getActionCommand())) {
			selectMode.changeSelectionFeatureType();
		}
		else if(CREATE_LEVEL_CMD.equals(ae.getActionCommand())) {
			selectMode.createLevel();
		}
	}
	
	private void initComponents() {	
		this.setFloatable(false);
		
		modeButtonGroup = new ButtonGroup();
		selectButton = new JToggleButton(SELECT_TEXT);
		selectButton.setActionCommand(SELECT_CMD);
		selectButton.addActionListener(this);
		modeButtonGroup.add(selectButton);
		this.add(selectButton);
		
		moveButton = new JToggleButton(MOVE_TEXT);
		moveButton.setActionCommand(MOVE_CMD);
		moveButton.addActionListener(this);
		modeButtonGroup.add(moveButton);
		this.add(moveButton);
		
		Box.Filler space1 = new Box.Filler(new Dimension(SPACE_X, SPACE_Y), new Dimension(SPACE_X, SPACE_Y), new Dimension(SPACE_X, SPACE_Y));
		this.add(space1);
		
		deleteButton = new JButton("Delete");
		deleteButton.setActionCommand(DELETE_CMD);
		deleteButton.addActionListener(this);
		this.add(deleteButton);
		
		removeNodeButton = new JButton("Remove Node");
		removeNodeButton.setActionCommand(REMOVE_NODE_CMD);
		removeNodeButton.addActionListener(this);
		this.add(removeNodeButton);
		
		changeFeatureTypeButton = new JButton("Change Feature Type");
		changeFeatureTypeButton.setActionCommand(CHANGE_FEATURE_TYPE_CMD);
		changeFeatureTypeButton.addActionListener(this);
		this.add(changeFeatureTypeButton);
		
		createLevelButton = new JButton("Create Level");
		createLevelButton.setActionCommand(CREATE_LEVEL_CMD);
		createLevelButton.addActionListener(this);
		this.add(createLevelButton);
		
		//add action listeners
		
	}

	
}
