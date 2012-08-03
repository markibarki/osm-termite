/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.source.SourceLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JToolBar;

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
	
	private final static String SET_P1_CMD = "p1";
	private final static String SET_P2_CMD = "p2";
	private final static String SELECT_CMD = "select";
	
	private TermiteGui termiteGui;
	
	private JToolBar toolBar = null;
	
	private JButton p1Button;
	private JButton p2Button;
	private JButton selectButton;
	
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
			sourceLayer.setActiveState(true);
		}
		
		if(toolBar != null) {
			termiteGui.removeToolBar(toolBar);
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		SourceLayer sourceLayer = termiteGui.getSourceLayer();
		if(SET_P1_CMD.equals(ae.getActionCommand())) {
			sourceLayer.setLayerState(SourceLayer.LayerState.PLACE_P1);
		}
		else if(SET_P2_CMD.equals(ae.getActionCommand())) {
			sourceLayer.setLayerState(SourceLayer.LayerState.PLACE_P2);
		}
		else if(SELECT_CMD.equals(ae.getActionCommand())) {
			sourceLayer.setLayerState(SourceLayer.LayerState.SELECT);
		}
	}
	
	private void createToolBar() {	
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		
		p1Button = new JButton("Set Translate Anchor");
		p1Button.setActionCommand(SET_P1_CMD);
		p1Button.addActionListener(this);
		toolBar.add(p1Button);
		
		p2Button = new JButton("Set Rotation/Scale Anchor");
		p2Button.setActionCommand(SET_P2_CMD);
		p2Button.addActionListener(this);
		toolBar.add(p2Button);
		
		selectButton = new JButton("Transform Source");
		selectButton.setActionCommand(SELECT_CMD);
		selectButton.addActionListener(this);
		toolBar.add(selectButton);
		
		//add action listeners
		
	}
}
