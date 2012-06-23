package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.map.model.TermiteLevel;
import intransix.osm.termite.map.model.TermiteStructure;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.structure.StructureLayer;
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
	
	/** This method returns the submode toolbar that will be active when this mode is
	 * active.
	 * 
	 * @return		The submode toolbar 
	 */
	public JToolBar getSubmodeToolbar() {
		if(toolBar == null) {
			createToolBar();
		}
		return toolBar;
	}
	
	/** This method is called when the editor mode is turned on. 
	 */
	public void turnOn() {
		MapPanel mapPanel = termiteGui.getMapPanel();
		mapPanel.addLayer(termiteGui.getRenderLayer());
		mapPanel.addLayer(termiteGui.getEditLayer());
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	public void turnOff() {
		MapPanel mapPanel = termiteGui.getMapPanel();
		mapPanel.removeLayer(termiteGui.getRenderLayer());
		mapPanel.removeLayer(termiteGui.getEditLayer());
		mapPanel.removeMouseListener(termiteGui.getEditLayer());
		mapPanel.removeMouseMotionListener(termiteGui.getEditLayer());
	}
	
//TEST FUNCTION!!!
	public void actionPerformed(ActionEvent ae) {
		termiteGui.clearEditData();
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
