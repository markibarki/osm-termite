package intransix.osm.termite.gui.toolbar;

import intransix.osm.termite.gui.mode.EditorMode;
import intransix.osm.termite.gui.mode.EditorModeListener;
import intransix.osm.termite.gui.mode.EditorModeManager;
import java.util.HashMap;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author sutter
 */
public class TermiteToolBar extends HBox implements EditorModeListener {
	private ToolBar modeToolBar;
	private ToolBar emptyToolBar;
	private ModeGroup group;
	private HashMap<EditorMode,ToggleButton> buttonMap = new HashMap<>();
	private EditorModeManager editorModeManager;
	
	public TermiteToolBar() {
		modeToolBar = new ToolBar();
		HBox.setHgrow(modeToolBar,Priority.NEVER);
		emptyToolBar = new ToolBar();
		HBox.setHgrow(modeToolBar,Priority.ALWAYS);
		group = new ModeGroup() {
			public void onSelect(Toggle toggle) {

				//we need the editor manager to proceed.
				if(editorModeManager == null) {
					return;
				}

				EditorMode newMode = null;
				if(toggle != null) {
					newMode = (EditorMode)toggle.getUserData();
				}
				
				editorModeManager.setEditorMode(newMode);
			}
		};
		
		this.getChildren().addAll(modeToolBar,emptyToolBar);
	}
	
	public void initModes() {
		List<EditorMode> modes = editorModeManager.getEditorModes();
		for(EditorMode mode:modes) {
			addMode(mode);
		}
		
	}
	
	public void setEditorModeManager(EditorModeManager editorModeManager) {
		this.editorModeManager = editorModeManager;
		editorModeManager.addModeListener(this);
	}
	
	/** This method is called when the mode changes. */
	public void activeModeChanged(EditorMode activeMode) {
		if(activeMode != null) {
			ToggleButton button = buttonMap.get(activeMode);
			if(button != null) {
				button.setSelected(true);
				ToolBar subModeToolBar = activeMode.getToolBar();
				if(subModeToolBar == null) subModeToolBar = emptyToolBar;
				this.getChildren().setAll(modeToolBar,subModeToolBar);
			}
			else {
				//shouldn't happen
				System.out.println("Unreconized mode");
			}
		}
	}
	
	/** This is called is a mode goes from disabled to enabled. */
	public void modeEnableChanged(EditorMode mode) {
		boolean isEnabled = mode.getModeEnabled();
		ToggleButton button = buttonMap.get(mode);
		if(button != null) {
			button.setDisable(!isEnabled);
		}
		else {
			//this shouldn't happen
			System.out.println("Unrecognized mode");
		}
	}
	
	private void addMode(EditorMode mode) {
		String modeName = mode.getName();
		String imageName = mode.getIconImageName();
		ToggleButton button = new ToggleButton();
		if(imageName != null) {
			Image image = new Image(getClass().getResourceAsStream(imageName));
			button.setGraphic(new ImageView(image));
			button.setTooltip(new Tooltip(modeName));
		}
		else {
			button.setText(modeName);
		}
		button.setToggleGroup(group);
		button.setUserData(mode);
		button.setDisable(!mode.getModeEnabled());
		modeToolBar.getItems().add(button);
		buttonMap.put(mode, button);
	}
	
}
