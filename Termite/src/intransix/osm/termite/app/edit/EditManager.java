package intransix.osm.termite.app.edit;

import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.render.edit.EditLayer;

/**
 *
 * @author sutter
 */
public class EditManager {

	//=========================
	// Properties 
	//=========================
	
	private EditLayer editLayer;
	private SelectEditorMode selectMode;
	private NodeEditorMode nodeMode;
	private WayEditorMode wayMode;
	
	
	public EditLayer getEditLayer() {
		return editLayer;
	}
	
	public SelectEditorMode getSelectEditorMode() {
		return selectMode;
	}
	
	public NodeEditorMode getNodeEditorMode() {
		return nodeMode;
	}
	
	public WayEditorMode getWayEditorMode() {
		return wayMode;
	}
	
	public void init() {
		editLayer = new EditLayer();
		selectMode = new SelectEditorMode(editLayer);
		nodeMode = new NodeEditorMode(editLayer);
		wayMode = new WayEditorMode(editLayer);
		
		//add the listener for the ui state
		editLayer.addEditStateListener(selectMode);
	}
	
	//========================
	// Protected Methods
	//========================
	
	//========================
	// Package Methods
	//========================
}
