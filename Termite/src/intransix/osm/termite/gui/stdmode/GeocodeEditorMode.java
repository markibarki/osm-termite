package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.gui.maplayer.MapLayerManager;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.source.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import javax.swing.*;
import java.util.List;

/**
 *
 * @author sutter
 */
public class GeocodeEditorMode extends EditorMode implements ActionListener, GeocodeStateListener {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Geocode";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/globe25.png";
	
	
	private final static String SOURCE_SELECT_CMD = "source";
	
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
						
	private final static String SELECT_TEXT = "Select [esc]";
	private final static String SELECT_CMD = "select";
	private final static String MOVE_TEXT = "Move [m]";
	private final static String MOVE_CMD = "move";
	private final static String ANCHOR_LABEL = "Place Anchors: ";
	
	private final static int TWO_POINT = 0;
	private final static int THREE_POINT_ORTHO = 1;
	private final static int FREE_TRANSFORM = 2;
	
	private final static int SPACE_X = 8;
	private final static int SPACE_Y = 8;
	
	private TermiteGui termiteGui;
	private GeocodeLayer geocodeLayer;
	private MapLayer renderLayer;
	
	private int geocodeType;
	
	private JToolBar toolBar = null;
	
	private JComboBox sourceSelector;
	
	private JToggleButton selectButton;
	private JToggleButton moveButton;
	
	private JToggleButton[] dynamicButtons;

	private ButtonGroup modeButtonGroup;
	
	private ButtonGroup typeButtonGroup;
	private JRadioButton[] radioButtons;
	
	//====================
	// Public Methods
	//====================
	
	public GeocodeEditorMode(TermiteGui termiteGui) {
		this.termiteGui = termiteGui;
		createToolBar();
	}
	
	/** This method will be called to set needed map layers. */
	@Override
	public void setLayers(MapLayerManager mapLayerManager) {
		geocodeLayer = mapLayerManager.getGeocodeLayer();
		geocodeLayer.addGeocodeStateListener(this);
		renderLayer = mapLayerManager.getRenderLayer();
	}
	
	/** This method returns the name of the editor mode. 
	 * 
	 * @return		The name of the editor mode 
	 */
	public String getName() {
		return MODE_NAME;
	}
	
	/** This method returns the name for image for the edit mode button on the
	 * edit mode toolbar. 
	 * 
	 * @return		The name of the icon image for this mode. 
	 */
	public String getIconImageName() {
		return ICON_NAME;
	}
	
	@Override
	public void geocodeModeChanged(GeocodeLayer.LayerState layerState) {
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
	public void geocodeTypeChanged(GeocodeLayer.GeocodeType geocodeType) {
		switch(geocodeType) {
			case TWO_POINT:
				if(!radioButtons[0].isSelected()) {
					radioButtons[0].setSelected(true);
					this.setGeocodeType(TWO_POINT);
				}
				break;
				
			case THREE_POINT_ORTHO:
				if(!radioButtons[1].isSelected()) {
					radioButtons[1].setSelected(true);
					this.setGeocodeType(THREE_POINT_ORTHO);
				}
				break;
				
			case FREE_TRANSFORM:
				if(!radioButtons[2].isSelected()) {
					radioButtons[2].setSelected(true);
					this.setGeocodeType(FREE_TRANSFORM);
				}
				break;
		}
	}
	
	/** This method is called when the source layers are updated. */
	public void updateSourceLayers(java.util.List<SourceLayer> sourceLayers) {
		Object selection = sourceSelector.getSelectedItem();
		sourceSelector.removeAllItems();
		for(SourceLayer layer:sourceLayers) {
			sourceSelector.addItem(layer);
		}
		if((selection != null)&&(sourceLayers.contains(selection))) {
			sourceSelector.setSelectedItem(selection);
		} 
	}
	
	
	/** This method is called when the editor mode is turned on. 
	 */
	@Override
	public void turnOn() {
		if(renderLayer != null) {
			renderLayer.setActiveState(true);
		}

		if(geocodeLayer != null) {
			geocodeLayer.setActiveState(true);
			geocodeLayer.setHidden(false);
		}
	
		termiteGui.addToolBar(toolBar);
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	@Override
	public void turnOff() {
		if(renderLayer != null) {
			renderLayer.setActiveState(false);
		}

		if(geocodeLayer != null) {
			geocodeLayer.setActiveState(false);
			geocodeLayer.setHidden(true);
		}
		
		if(toolBar != null) {
			termiteGui.removeToolBar(toolBar);
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
			geocodeLayer.setGeocodeType(GeocodeLayer.GeocodeType.TWO_POINT);
			this.setGeocodeType(TWO_POINT);
		}
		else if(actionCommand.equals(GEOCODE_CMDS[1])) {
			geocodeLayer.setGeocodeType(GeocodeLayer.GeocodeType.THREE_POINT_ORTHO);
			this.setGeocodeType(THREE_POINT_ORTHO);
		}
		else if(actionCommand.equals(GEOCODE_CMDS[2])) {
			geocodeLayer.setGeocodeType(GeocodeLayer.GeocodeType.FREE_TRANSFORM);
			this.setGeocodeType(FREE_TRANSFORM);
		}
		else if(actionCommand.equals(SELECT_CMD)) {
			geocodeLayer.setLayerState(GeocodeLayer.LayerState.SELECT);
		}
		else if(actionCommand.equals(MOVE_CMD)) {
			geocodeLayer.setLayerState(GeocodeLayer.LayerState.MOVE);
		}
		else if(actionCommand.equals(BUTTON_CMDS[0])) {
			geocodeLayer.setLayerState(GeocodeLayer.LayerState.PLACE_P0);
		}
		else if(actionCommand.equals(BUTTON_CMDS[1])) {
			geocodeLayer.setLayerState(GeocodeLayer.LayerState.PLACE_P1);
		}
		else if(actionCommand.equals(BUTTON_CMDS[2])) {
			geocodeLayer.setLayerState(GeocodeLayer.LayerState.PLACE_P2);
		}
	}
	
	private void createToolBar() {	
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		
		sourceSelector = new JComboBox();
		sourceSelector.setActionCommand(SOURCE_SELECT_CMD);
		sourceSelector.addActionListener(this);
		toolBar.add(sourceSelector);
		
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
			toolBar.add(radioButton);
			radioButtons[i] = radioButton;
		}
		
		//mode choice
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
		
		JLabel anchorLabel = new JLabel();
        anchorLabel.setText(ANCHOR_LABEL);
		toolBar.add(anchorLabel);
		
		cnt = BUTTON_NAMES[0].length;
		dynamicButtons = new JToggleButton[cnt];
		for(int i = 0; i < cnt; i++) {
			JToggleButton button = new JToggleButton();
			button.addActionListener(this);
			button.setActionCommand(BUTTON_CMDS[i]);
			modeButtonGroup.add(button);
			toolBar.add(button);
			dynamicButtons[i] = button;
		}
		
		//disable all buttons for now
		enableButtons(false);
		
	}
	
	private void setGeocodeType(int type) {
		this.geocodeType = type;
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
			geocodeLayer.setSourceLayer(layer);
			enableButtons(true);
		}
		else {
			geocodeLayer.setSourceLayer(null);
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
if(button == radioButtons[FREE_TRANSFORM]) continue;

			button.setEnabled(enable);
		}		
	}
}
