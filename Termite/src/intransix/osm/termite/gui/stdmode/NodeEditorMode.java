package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.*;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.edit.NodeToolAction;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 *
 * @author sutter
 */
public class NodeEditorMode implements EditorMode, ActionListener {
	
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Node Tool";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/nodeMode.png";
	
	private TermiteGui termiteGui;
	private JToolBar toolBar = null;
	
	//====================
	// Public Methods
	//====================
	
	public NodeEditorMode(TermiteGui termiteGui) {
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
		EditLayer editLayer = termiteGui.getEditLayer();
		if(editLayer != null) {
			editLayer.setActiveState(true);
			editLayer.setMouseEditAction(new NodeToolAction());
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
		EditLayer editLayer = termiteGui.getEditLayer();
		if(editLayer != null) {
			editLayer.setActiveState(false);
			editLayer.setMouseEditAction(null);
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
		testButton.setActionCommand("yyy");
		testButton.addActionListener(this);
	}
}
