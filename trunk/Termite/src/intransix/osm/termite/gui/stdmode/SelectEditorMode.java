package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.map.model.TermiteLevel;
import intransix.osm.termite.map.model.TermiteStructure;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.structure.RenderLayer;
import intransix.osm.termite.map.theme.Theme;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class SelectEditorMode implements EditorMode, ActionListener {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Select Tool";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/selectMode.png";
	
	private TermiteGui termiteGui;
	
	private JToolBar toolBar = null;
	
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
		MapLayer editLayer = termiteGui.getEditLayer();
		if(editLayer != null) {
			editLayer.setActiveState(true);
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
		MapLayer editLayer = termiteGui.getEditLayer();
		if(editLayer != null) {
			editLayer.setActiveState(false);
		}
		
		if(toolBar != null) {
			termiteGui.removeToolBar(toolBar);
		}
	}
	
//TEST FUNCTION!!!
	public void actionPerformed(ActionEvent ae) {
		termiteGui.setMapData(null);
	}
	
	private void createToolBar() {	
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		JButton testButton = new JButton("Discard Data");
		toolBar.add(testButton);
		
		//add action listeners
		testButton.setActionCommand("xxx");
		testButton.addActionListener(this);
	}
}
