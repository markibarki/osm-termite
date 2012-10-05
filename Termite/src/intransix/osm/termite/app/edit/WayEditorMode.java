package intransix.osm.termite.app.edit;

import intransix.osm.termite.app.edit.action.*;
import intransix.osm.termite.app.mode.EditorMode;
import intransix.osm.termite.render.*;
import intransix.osm.termite.render.edit.EditLayer;
import java.awt.Cursor;

/**
 * This mode is used to draw ways. 
 * 
 * @author sutter
 */
public class WayEditorMode extends EditorMode {
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Way Tool";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/wayMode.png";
	
	private EditManager editManager;
	private EditLayer editLayer;
	
	//====================
	// Public Methods
	//====================
	
	public WayEditorMode(EditManager editManager) {
		this.editManager = editManager;
		this.editLayer = editManager.getEditLayer();
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
		if(editLayer != null) {
			editLayer.setActiveState(true);
			loadMouseActions();
		}
		MapPanel mapPanel = editLayer.getMapPanel();
		mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	@Override
	public void turnOff() {
		if(editLayer != null) {
			editLayer.setActiveState(false);
			
			MapPanel mapPanel = editLayer.getMapPanel();
			mapPanel.setCursor(Cursor.getDefaultCursor());
			
		}
	}
	
	/** This method removes any current active way, leaving the program in 
	 * way edit mode. */ 
	public void resetWayEdit() {
		if(this.getModeActive()) {
			editManager.clearPreview();
			editManager.clearPending();
			editManager.clearSelection();
			loadMouseActions();
		}
	}
	
	/** This method loads the proper mouse actions for this mode. */
	private void loadMouseActions() {
		//set up move state
		MouseClickAction mouseClickAction = new WayToolClickAction(editManager);		
		mouseClickAction.init();

		MouseMoveAction snapAction = new CreateSnapMoveAction(editManager);
		snapAction.init();
		MouseMoveAction moveAction = new CreateMoveMoveAction(editManager);
		moveAction.init();

		editLayer.setMouseClickAction(mouseClickAction);
		editLayer.setMouseMoveActions(moveAction, snapAction);
	}

}
