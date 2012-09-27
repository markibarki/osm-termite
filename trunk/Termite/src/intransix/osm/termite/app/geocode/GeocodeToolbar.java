package intransix.osm.termite.app.geocode;

import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.render.source.SourceLayer;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class GeocodeToolbar extends JToolBar implements ActionListener, GeocodeStateListener {

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
	private final static String[] GEOCODE_CMDS = {
		"two","three","free"
	}; 
	private final static String[][] BUTTON_NAMES = {
		{"Translate [1]","Rotate/Scale [2]",null},
		{"Translate [1]","Rotate/Scale A [2]","Rotate/Scale B [3]"},
		{"Point 1 [1]","Point 2 [2]","Point 3 [3]"}
	};
	private final static String[] BUTTON_CMDS = {"b0","b1","b2"};
	
	private final static int TWO_POINT = 0;
	private final static int THREE_POINT_ORTHO = 1;
	private final static int FREE_TRANSFORM = 2;
	
	
	private final static int SPACE_X = 8;
	private final static int SPACE_Y = 8;
	
	private GeocodeEditorMode geocodeEditorMode;
	
	private JComboBox sourceSelector;
	
	private JToggleButton selectButton;
	private JToggleButton moveButton;
	
	private JToggleButton[] dynamicButtons;

	private ButtonGroup modeButtonGroup;
	
	private ButtonGroup typeButtonGroup;
	private JRadioButton[] radioButtons;
	
	public GeocodeToolbar(GeocodeEditorMode geocodeEditorMode) {
		this.geocodeEditorMode = geocodeEditorMode;
		initToolbar();
	}
	
	/** This method is called when the source layers are updated. */
	public void updateLayers(java.util.List<MapLayer> mapLayerList) {
		Object selection = sourceSelector.getSelectedItem();
		sourceSelector.removeAllItems();
		for(MapLayer layer:mapLayerList) {
			if((layer instanceof SourceLayer)&&(layer.isVisible())) {
				sourceSelector.addItem((SourceLayer)layer);
			}
		}
		if((selection != null)&&(mapLayerList.contains(selection))) {
			sourceSelector.setSelectedItem(selection);
		} 
		boolean newEnableState = (sourceSelector.getItemCount() > 0);
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
		switch(geocodeType) {
			case TWO_POINT:
				if(!radioButtons[0].isSelected()) {
					radioButtons[0].setSelected(true);
					configureForGeocodeType(TWO_POINT);
				}
				break;
				
			case THREE_POINT_ORTHO:
				if(!radioButtons[1].isSelected()) {
					radioButtons[1].setSelected(true);
					configureForGeocodeType(THREE_POINT_ORTHO);
				}
				break;
				
			case FREE_TRANSFORM:
				if(!radioButtons[2].isSelected()) {
					radioButtons[2].setSelected(true);
					configureForGeocodeType(FREE_TRANSFORM);
				}
				break;
		}
	}
	
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		String actionCommand = ae.getActionCommand();
		if(actionCommand == null) return;
		
		if(actionCommand.equals(SOURCE_SELECT_CMD)) {
			selectSource();
		}
		else if(actionCommand.equals(GEOCODE_CMDS[0])) {
			geocodeEditorMode.setGeocodeType(GeocodeEditorMode.GeocodeType.TWO_POINT);
			configureForGeocodeType(TWO_POINT);
		}
		else if(actionCommand.equals(GEOCODE_CMDS[1])) {
			geocodeEditorMode.setGeocodeType(GeocodeEditorMode.GeocodeType.THREE_POINT_ORTHO);
			configureForGeocodeType(THREE_POINT_ORTHO);
		}
		else if(actionCommand.equals(GEOCODE_CMDS[2])) {
			geocodeEditorMode.setGeocodeType(GeocodeEditorMode.GeocodeType.FREE_TRANSFORM);
			configureForGeocodeType(FREE_TRANSFORM);
		}
		else if(actionCommand.equals(SELECT_CMD)) {
			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.SELECT);
		}
		else if(actionCommand.equals(MOVE_CMD)) {
			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.MOVE);
		}
		else if(actionCommand.equals(BUTTON_CMDS[0])) {
			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.PLACE_P0);
		}
		else if(actionCommand.equals(BUTTON_CMDS[1])) {
			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.PLACE_P1);
		}
		else if(actionCommand.equals(BUTTON_CMDS[2])) {
			geocodeEditorMode.setLayerState(GeocodeEditorMode.LayerState.PLACE_P2);
		}
	}
	
		
	private void configureForGeocodeType(int type) {
		String[] dynamicNames = BUTTON_NAMES[type];
		int i = 0;
		for(JToggleButton button:dynamicButtons) {
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
	
	private void selectSource() {
		SourceLayer layer = (SourceLayer)sourceSelector.getSelectedItem();
		if(layer != null) {
			geocodeEditorMode.setSourceLayer(layer);
			enableButtons(true);
		}
		else {
			geocodeEditorMode.setSourceLayer(null);
			enableButtons(false);
		}
	}
	
	private void enableButtons(boolean enable) {
		selectButton.setEnabled(enable);
		moveButton.setEnabled(enable);
		for(JToggleButton button:dynamicButtons) {
			button.setEnabled(enable);
		}
		for(JRadioButton button:radioButtons) {
//free transform is disabled
if(button == radioButtons[FREE_TRANSFORM]) button.setEnabled(false);
else
			button.setEnabled(enable);
		}		
	}
	
	
	private void initToolbar() {	
		setFloatable(false);
		
		sourceSelector = new JComboBox();
		sourceSelector.setActionCommand(SOURCE_SELECT_CMD);
		sourceSelector.addActionListener(this);
		add(sourceSelector);
		
		//geocode type choice
		typeButtonGroup = new ButtonGroup();
		int cnt = GEOCODE_TYPE_NAMES.length;
		radioButtons = new JRadioButton[cnt];
		for(int i = 0; i < cnt; i++) {
			JRadioButton radioButton = new JRadioButton();
			radioButton.setText(GEOCODE_TYPE_NAMES[i]);
			radioButton.setActionCommand(GEOCODE_CMDS[i]);
			radioButton.addActionListener(this);
			typeButtonGroup.add(radioButton);
			add(radioButton);
			radioButtons[i] = radioButton;
		}
		
		//mode choice
		modeButtonGroup = new ButtonGroup();
		selectButton = new JToggleButton(SELECT_TEXT);
		selectButton.setActionCommand(SELECT_CMD);
		selectButton.addActionListener(this);
		modeButtonGroup.add(selectButton);
		add(selectButton);
		
		moveButton = new JToggleButton(MOVE_TEXT);
		moveButton.setActionCommand(MOVE_CMD);
		moveButton.addActionListener(this);
		modeButtonGroup.add(moveButton);
		add(moveButton);
		
		Box.Filler space1 = new Box.Filler(new Dimension(SPACE_X, SPACE_Y), new Dimension(SPACE_X, SPACE_Y), new Dimension(SPACE_X, SPACE_Y));
		add(space1);
		
		JLabel anchorLabel = new JLabel();
        anchorLabel.setText(ANCHOR_LABEL);
		add(anchorLabel);
		
		cnt = BUTTON_NAMES[0].length;
		dynamicButtons = new JToggleButton[cnt];
		for(int i = 0; i < cnt; i++) {
			JToggleButton button = new JToggleButton();
			button.addActionListener(this);
			button.setActionCommand(BUTTON_CMDS[i]);
			modeButtonGroup.add(button);
			add(button);
			dynamicButtons[i] = button;
		}
		
		//disable all buttons for now
		enableButtons(false);
		
	}
	
	
}
