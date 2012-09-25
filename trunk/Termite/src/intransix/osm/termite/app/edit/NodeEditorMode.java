package intransix.osm.termite.app.edit;

import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.mode.EditorMode;
import intransix.osm.termite.gui.*;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.render.MapPanel;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.app.edit.action.NodeToolClickAction;
import intransix.osm.termite.app.edit.action.SelectClickAction;
import intransix.osm.termite.app.edit.action.CreateMoveMoveAction;
import intransix.osm.termite.app.edit.action.CreateSnapMoveAction;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JToolBar;

/**
 *
 * @author sutter
 */
public class NodeEditorMode extends EditorMode {
	
	//====================
	// Properties
	//====================
	private final static String MODE_NAME = "Node Tool";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/nodeMode.png";
	
	private EditManager editManager;
	private EditLayer editLayer;
	
	private JToolBar toolBar = null;
	
	//====================
	// Public Methods
	//====================
	
	public NodeEditorMode(EditManager editManager) {
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
			
			MapPanel mapPanel = editLayer.getMapPanel();
			mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
		
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
	
	private void createToolBar() {	
		toolBar = null;
	}
	
	private void loadMouseActions() {
		//set up move state
		MouseClickAction mouseClickAction = new NodeToolClickAction(editManager);		
		mouseClickAction.init();

		MouseMoveAction snapAction = new CreateSnapMoveAction(editManager);
		snapAction.init();
		MouseMoveAction moveAction = new CreateMoveMoveAction(editManager);
		moveAction.init();

		editLayer.setMouseClickAction(mouseClickAction);
		editLayer.setMouseMoveActions(moveAction, snapAction);
	}
}
