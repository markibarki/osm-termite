/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.source.GeocodeLayer;
import intransix.osm.termite.render.source.SourceLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class GeocodeEditorMode implements EditorMode, ActionListener {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Geocode";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/globe25.png";
	
	
	private final static String[] GEOCODE_TYPE_NAMES = {
		"Two Point",
		"Three Point Orthoganol",
		"Free Geocode"
	};
	private final static String[] GEOCODE_CMDS = {
		"two","three","free"
	}; 
	private final static String[][] BUTTON_NAMES = {
		{"Translate","Rotate/Scale",null},
		{"Translate","Rotate/Scale 1","Rotate/Scale 2"},
		{"Point 1","Point 2","Point 3"}
	};
	private final static String[] BUTTON_CMDS = {"b0","b1","b2"};
						
	private final static String SELECT_CMD = "select";
	private final static String MOVE_CMD = "move";
	
	private final static int TWO_POINT = 0;
	private final static int THREE_POINT_ORTHO = 1;
	private final static int FREE_TRANSFORM = 2;
	private final static int DEFAULT_TYPE = 0;
	
	private TermiteGui termiteGui;
	
	private int geocodeType;
	
	private JToolBar toolBar = null;
	
	private JToggleButton selectButton;
	private JToggleButton[] dynamicButtons;
	private JToggleButton moveButton;
	
	private ButtonGroup buttonGroup;
	private JRadioButton[] radioButtons;
	
	//====================
	// Public Methods
	//====================
	
	public GeocodeEditorMode(TermiteGui termiteGui) {
		this.termiteGui = termiteGui;
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
	
	
	/** This method is called when the editor mode is turned on. 
	 */
	@Override
	public void turnOn() {
		MapLayer renderLayer = termiteGui.getRenderLayer();
		if(renderLayer != null) {
			renderLayer.setActiveState(true);
		}
		SourceLayer sourceLayer = termiteGui.getSourceLayer();
		if(sourceLayer != null) {
			sourceLayer.setActiveState(true);
		}
		GeocodeLayer geocodeLayer = termiteGui.getGeocodeLayer();
		if(geocodeLayer != null) {
			geocodeLayer.setActiveState(true);
		}
		
		if(toolBar == null) {
			createToolBar();
		}
		termiteGui.addToolBar(toolBar);
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	@Override
	public void turnOff() {
		MapLayer renderLayer = termiteGui.getRenderLayer();
		if(renderLayer != null) {
			renderLayer.setActiveState(false);
		}
		SourceLayer sourceLayer = termiteGui.getSourceLayer();
		if(sourceLayer != null) {
			sourceLayer.setActiveState(false);
		}
		GeocodeLayer geocodeLayer = termiteGui.getGeocodeLayer();
		if(geocodeLayer != null) {
			geocodeLayer.setActiveState(false);
		}
		
		if(toolBar != null) {
			termiteGui.removeToolBar(toolBar);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		GeocodeLayer geocodeLayer = termiteGui.getGeocodeLayer();
		String actionCommand = ae.getActionCommand();
		if(actionCommand == null) return;
		
		if(actionCommand.equals(GEOCODE_CMDS[0])) {
			geocodeLayer.setGeocodeType(GeocodeLayer.GeocodeType.TWO_POINT);
		}
		else if(actionCommand.equals(GEOCODE_CMDS[1])) {
			geocodeLayer.setGeocodeType(GeocodeLayer.GeocodeType.THREE_POINT_ORTHO);
		}
		else if(actionCommand.equals(GEOCODE_CMDS[2])) {
			geocodeLayer.setGeocodeType(GeocodeLayer.GeocodeType.FREE_TRANSFORM);
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
		
		//geocode type choice
		buttonGroup = new ButtonGroup();
		int cnt = GEOCODE_TYPE_NAMES.length;
		radioButtons = new JRadioButton[cnt];
		for(int i = 0; i < cnt; i++) {
			JRadioButton radioButton = new JRadioButton();
			radioButton.setText(GEOCODE_TYPE_NAMES[0]);
			radioButton.setActionCommand(GEOCODE_CMDS[0]);
			radioButton.addActionListener(this);
			buttonGroup.add(radioButton);
			toolBar.add(radioButton);
			radioButtons[i] = radioButton;
		}
		
		//mode choice
		selectButton = new JToggleButton("Transform Source");
		selectButton.setActionCommand(SELECT_CMD);
		selectButton.addActionListener(this);
		toolBar.add(selectButton);
		
		cnt = BUTTON_NAMES[0].length;
		dynamicButtons = new JToggleButton[cnt];
		for(int i = 0; i < cnt; i++) {
			JToggleButton button = new JToggleButton();
			button.addActionListener(this);
			button.setActionCommand(BUTTON_CMDS[i]);
			toolBar.add(button);
			dynamicButtons[i] = button;
		}
		
		moveButton = new JToggleButton("Transform Source");
		moveButton.setActionCommand(MOVE_CMD);
		moveButton.addActionListener(this);
		toolBar.add(moveButton);
		
		//add action listeners
		
	}
	
	private void setGeocodeType(int type) {
		this.geocodeType = type;
		String[] dynamicNames = BUTTON_NAMES[type];
		int i = 0;
		for(JToggleButton button:dynamicButtons) {
			button.setText(dynamicNames[i++]);
		}
	}
}
