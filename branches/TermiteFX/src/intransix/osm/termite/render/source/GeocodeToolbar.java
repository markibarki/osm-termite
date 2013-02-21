package intransix.osm.termite.render.source;

import intransix.osm.termite.gui.mode.source.GeocodeEditorMode;
import intransix.osm.termite.app.geocode.GeocodeStateListener;
import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.gui.toolbar.ModeGroup;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * This is the submode toolbar for the geocode editor mode. 
 * @author sutter
 */
public class GeocodeToolbar extends ToolBar implements GeocodeStateListener {

	//=====================
	// Properties
	//=====================
	
	private final static String SOURCE_SELECT_CMD = "source";
	
	private final static String SELECT_TEXT = "Select [esc]";
	private final static String SELECT_CMD = "select";
	private final static String MOVE_TEXT = "Move [m]";
	private final static String MOVE_CMD = "move";
	private final static String ANCHOR_LABEL = "Place Anchors: ";
	
	private final static String[] GEOCODE_TYPE_NAMES = {
		"2 Point",
		"3 Pnt Ortho",
		"3 Pnt Free"
	};
	private final static GeocodeEditorMode.GeocodeType[] GEOCODE_CMDS = {
		GeocodeEditorMode.GeocodeType.TWO_POINT,
		GeocodeEditorMode.GeocodeType.THREE_POINT_ORTHO,
		GeocodeEditorMode.GeocodeType.FREE_TRANSFORM
	};

	private final static String[][] BUTTON_NAMES = {
		{"Translate [1]","Rotate/Scale [2]",null},
		{"Translate [1]","Rotate/Scale A [2]","Rotate/Scale B [3]"},
		{"Point 1 [1]","Point 2 [2]","Point 3 [3]"}
	};
	private final static GeocodeEditorMode.LayerState[] BUTTON_CMDS = {
		GeocodeEditorMode.LayerState.PLACE_P0,
		GeocodeEditorMode.LayerState.PLACE_P1,
		GeocodeEditorMode.LayerState.PLACE_P2
	};
	
	private final static int TWO_POINT = 0;
	private final static int THREE_POINT_ORTHO = 1;
	private final static int FREE_TRANSFORM = 2;
	
	
	private final static int SPACE_X = 8;
	private final static int SPACE_Y = 8;
	
	private GeocodeEditorMode geocodeEditorMode;
	
	private ChoiceBox<SourceLayer> sourceSelector;
	
	private ToggleButton selectButton;
	private ToggleButton moveButton;
	
	private ToggleButton[] dynamicButtons;

	private ModeGroup modeButtonGroup;
	
	private ToggleGroup typeButtonGroup;
	private RadioButton[] radioButtons;
	
	private List<SourceLayer> workingLayers = new ArrayList<>();
	
	//=====================
	// Public Methods
	//=====================
	
	public GeocodeToolbar(GeocodeEditorMode geocodeEditorMode) {
		this.geocodeEditorMode = geocodeEditorMode;
		initToolbar();
	}
	
	/** This method is called when the source layers are updated. */
	public void updateLayers(java.util.List<MapLayer> mapLayerList) {
		SingleSelectionModel<SourceLayer> model = sourceSelector.getSelectionModel();
		SourceLayer selectedLayer = model.getSelectedItem();
		boolean setSelection = false;
		
		workingLayers.clear();
		for(MapLayer mapLayer:mapLayerList) {
			if((mapLayer instanceof SourceLayer)&&(mapLayer.visibleProperty().getValue())) {
				workingLayers.add((SourceLayer)mapLayer);
				if(mapLayer == selectedLayer) {
					setSelection = true;
				}
			}
		}
		sourceSelector.getItems().setAll(workingLayers);
		if(setSelection) {
			model.select(selectedLayer);
		}
		
		boolean newEnableState = (workingLayers.size() > 0);
		if(newEnableState != geocodeEditorMode.getModeEnabled()) {
			geocodeEditorMode.setEnabled(newEnableState);
		}
	}
	
	
	@Override
	public void geocodeModeChanged(GeocodeEditorMode.LayerState layerState) {
		switch(layerState) {
			case INACTIVE:
			case SELECT:
				if(!selectButton.isSelected()) {
					selectButton.setSelected(true);
				}
				break;
				
			case PLACE_P0:
				if(!dynamicButtons[0].isSelected()) {
					dynamicButtons[0].setSelected(true);
				}
				break;
				
			case PLACE_P1:
				if(!dynamicButtons[1].isSelected()) {
					dynamicButtons[1].setSelected(true);
				}
				break;
				
			case PLACE_P2:
				if(!dynamicButtons[2].isSelected()) {
					dynamicButtons[2].setSelected(true);
				}
				break;
				
			case MOVE:
				if(!moveButton.isSelected()) {
					moveButton.setSelected(true);
				}
				break;
		}
	}
	
	@Override
	public void geocodeTypeChanged(GeocodeEditorMode.GeocodeType geocodeType) {
		int geocodeTypeIndex = getGeocodeTypeIndex(geocodeType);
		if(geocodeTypeIndex == -1) return;
		
		if(!radioButtons[geocodeTypeIndex].isSelected()) {
			radioButtons[geocodeTypeIndex].setSelected(true);
			configureForGeocodeType(geocodeTypeIndex);
		}
	}
	
	//=====================
	// Private methods
	//=====================	
	
	/** figure out the index used locally for this command.
	 * returns -1 for invalid. */
	private int getGeocodeTypeIndex(GeocodeEditorMode.GeocodeType type) {
		int index = -1;
		for(int i = 0; i<GEOCODE_CMDS.length; i++) {
			if(type == GEOCODE_CMDS[i]) {
				index = i;
			}
		}
		return index;
	}
	
	private void configureForGeocodeType(int geocodeTypeIndex) {

		if((geocodeTypeIndex < 0)||(geocodeTypeIndex > BUTTON_NAMES.length)) return;
		
		String[] dynamicNames = BUTTON_NAMES[geocodeTypeIndex];
		int i = 0;
		for(ToggleButton button:dynamicButtons) {
			String name = dynamicNames[i++];
			if(name != null) {
				button.setVisible(true);
				button.setText(name);
			}
			else {
				button.setVisible(false);
			}
		}
	}
	
	private void enableButtons(boolean enable) {
		selectButton.setDisable(!enable);
		moveButton.setDisable(!enable);
		for(ToggleButton button:dynamicButtons) {
			button.setDisable(!enable);
		}
		for(RadioButton button:radioButtons) {
//free transform is disabled
if(button == radioButtons[FREE_TRANSFORM]) button.setDisable(true);
else
			button.setDisable(!enable);
		}		
	}
	
	
	private void initToolbar() {	
		
		HBox.setHgrow(this,Priority.ALWAYS);
		VBox.setVgrow(this,Priority.ALWAYS);
		
		sourceSelector = new ChoiceBox<>();
		SingleSelectionModel<SourceLayer> model = sourceSelector.getSelectionModel();
		model.selectedItemProperty().addListener(new ChangeListener<SourceLayer>(){
			@Override
			public void changed(ObservableValue<? extends SourceLayer> ov,
					SourceLayer oldLayer, SourceLayer newLayer) {
				
				//select the source
				if(newLayer != null) {
					geocodeEditorMode.setSourceLayer(newLayer);
					enableButtons(true);
				}
				else {
					geocodeEditorMode.setSourceLayer(null);
					enableButtons(false);
				}
				
			}
		});
		this.getItems().add(sourceSelector);
		
		//geocode type choice
		typeButtonGroup = new ToggleGroup();
		typeButtonGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
			@Override
			public void changed(ObservableValue<? extends Toggle> ov,
					Toggle oldToggle, Toggle newToggle) {
				
				if(newToggle != null) {
					GeocodeEditorMode.GeocodeType type = (GeocodeEditorMode.GeocodeType)newToggle.getUserData();
					geocodeEditorMode.setGeocodeType(type);
					int typeIndex = getGeocodeTypeIndex(type);
					configureForGeocodeType(typeIndex);
				}
			}
		});
		
		int cnt = GEOCODE_TYPE_NAMES.length;
		radioButtons = new RadioButton[cnt];
		for(int i = 0; i < cnt; i++) {
			RadioButton radioButton = new RadioButton();
			radioButton.setText(GEOCODE_TYPE_NAMES[i]);
			radioButton.setUserData(GEOCODE_CMDS[i]);
			radioButton.setToggleGroup(typeButtonGroup);
			this.getItems().add(radioButton);
			radioButtons[i] = radioButton;
		}
		
		//mode choice
		modeButtonGroup = new ModeGroup() {
			public void onSelect(Toggle toggle) {
				if(toggle == selectButton) {
					geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.SELECT);
				}
				else if(toggle == moveButton) {
					geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.MOVE);
				}
			}
		};

		selectButton = new ToggleButton(SELECT_TEXT);
		selectButton.setToggleGroup(modeButtonGroup);
		getItems().add(selectButton);
		
		moveButton = new ToggleButton(MOVE_TEXT);
		moveButton.setToggleGroup(modeButtonGroup);
		getItems().add(moveButton);
		
//		Box.Filler space1 = new Box.Filler(new Dimension(SPACE_X, SPACE_Y), new Dimension(SPACE_X, SPACE_Y), new Dimension(SPACE_X, SPACE_Y));
//		add(space1);
		
		Label anchorLabel = new Label();
        anchorLabel.setText(ANCHOR_LABEL);
		getItems().add(anchorLabel);
		
		cnt = BUTTON_NAMES[0].length;
		dynamicButtons = new ToggleButton[cnt];
		for(int i = 0; i < cnt; i++) {
			ToggleButton button = new ToggleButton();
			final GeocodeEditorMode.LayerState cmd = BUTTON_CMDS[i];
			button.setToggleGroup(modeButtonGroup);
			this.getItems().add(button);
			dynamicButtons[i] = button;
			button.setOnAction(new EventHandler<ActionEvent>() {
				@Override 
				public void handle(ActionEvent e) {
					geocodeEditorMode.setLayerState(cmd);
				}
			});
		}
		
		
		
		
		//disable all buttons for now
		enableButtons(false);
		
	}
	
	
}
