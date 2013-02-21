package intransix.osm.termite.render.edit;

import intransix.osm.termite.gui.mode.edit.SelectEditorMode;
import intransix.osm.termite.app.edit.SelectEditStateListener;
import intransix.osm.termite.gui.toolbar.ModeGroup;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


/**
 *
 * @author sutter
 */
public class SelectToolbar extends ToolBar implements SelectEditStateListener {
	
	private final static String SELECT_TEXT = "Select [esc]";
	private final static String MOVE_TEXT = "Move [m]";
	
	private final static int SPACE_X = 8;
	private final static int SPACE_Y = 8;
	
	private SelectEditorMode selectMode;
	
	private ModeGroup modeButtonGroup;
	private ToggleButton selectButton;
	private ToggleButton moveButton;
	
	private Button deleteButton;
	private Button removeNodeButton;
	private Button changeFeatureTypeButton;
	private Button createLevelButton;
	
	public SelectToolbar(SelectEditorMode selectMode) {
		this.selectMode = selectMode;
		selectMode.addEditStateListener(this);
		initComponents();
	}
	
	public void enableDelete(boolean deleteEnabled) {
		deleteButton.setDisable(!deleteEnabled);
	}
	public void enableRemoveNode(boolean removeEnabled) {
		removeNodeButton.setDisable(!removeEnabled);
	}
	
	public void enableChangeFeatureType(boolean featureTypeEnabled) {
		changeFeatureTypeButton.setDisable(!featureTypeEnabled);
	}
	
	public void enableCreateLevel(boolean levelEnabled) {
		createLevelButton.setDisable(!levelEnabled);
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
	
	private void initComponents() {	
		
		HBox.setHgrow(this,Priority.ALWAYS);
		VBox.setVgrow(this,Priority.ALWAYS);
		
		modeButtonGroup = new ModeGroup() {
			public void onSelect(Toggle toggle) {
				if(toggle == selectButton) {
					selectMode.setSelectState();
				}
				else if(toggle == moveButton) {
					boolean success = selectMode.setMoveState();
					if(!success) {
						//update the edit mode to be the active 
						SelectToolbar.this.editStateChanged(false);
					}
				}
			}
		};
		
		
		selectButton = new ToggleButton(SELECT_TEXT);
		selectButton.setToggleGroup(modeButtonGroup);
		this.getItems().add(selectButton);
		
		moveButton = new ToggleButton(MOVE_TEXT);
		moveButton.setToggleGroup(modeButtonGroup);
		this.getItems().add(moveButton);
		
//		Box.Filler space1 = new Box.Filler(new Dimension(SPACE_X, SPACE_Y), new Dimension(SPACE_X, SPACE_Y), new Dimension(SPACE_X, SPACE_Y));
//		this.add(space1);
		
		deleteButton = new Button("Delete");
		this.getItems().add(deleteButton);
		deleteButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				selectMode.deleteSelection();
			}
		});
		
		removeNodeButton = new Button("Remove Node");
		this.getItems().add(removeNodeButton);
		removeNodeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				selectMode.removeNodeFromWay();
			}
		});
		
		changeFeatureTypeButton = new Button("Change Feature Type");
		this.getItems().add(changeFeatureTypeButton);
		changeFeatureTypeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				selectMode.changeSelectionFeatureType();
			}
		});
		
		createLevelButton = new Button("Create Level");
		this.getItems().add(createLevelButton);
		createLevelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				selectMode.createLevel();
			}
		});
		
		//add action listeners
		
	}

	
}
