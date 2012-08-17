/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.stdmode;

import intransix.osm.termite.gui.EditorMode;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.gui.maplayer.MapLayerManager;
import intransix.osm.termite.render.MapLayer;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.edit.WayToolAction;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 *
 * @author sutter
 */
public class WayEditorMode extends EditorMode {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Way Tool";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/wayMode.png";
	
	private TermiteGui termiteGui;
	private MapLayer renderLayer;
	private EditLayer editLayer;
	private JToolBar toolBar = null;
	
	//====================
	// Public Methods
	//====================
	
	public WayEditorMode(TermiteGui termiteGui) {
		this.termiteGui = termiteGui;
	}
	
	/** This method will be called to set needed map layers. */
	@Override
	public void setLayers(MapLayerManager mapLayerManager) {
		editLayer = mapLayerManager.getEditLayer();
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
	
	
	/** This method is called when the editor mode is turned on. 
	 */
	@Override
	public void turnOn() {
		if(renderLayer != null) {
			renderLayer.setActiveState(true);
		}
		if(editLayer != null) {
			editLayer.setActiveState(true);
			editLayer.setMouseEditAction(new WayToolAction());
		}
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	@Override
	public void turnOff() {
		if(renderLayer != null) {
			renderLayer.setActiveState(false);
		}
		if(editLayer != null) {
			editLayer.setActiveState(false);
			editLayer.setMouseEditAction(null);
		}
	}
	
	private void createToolBar() {	
		toolBar = null;
	}
}
