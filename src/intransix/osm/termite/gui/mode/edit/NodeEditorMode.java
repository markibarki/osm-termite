package intransix.osm.termite.gui.mode.edit;

import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.edit.MouseClickAction;
import intransix.osm.termite.app.edit.MouseMoveAction;
import intransix.osm.termite.gui.mode.EditorMode;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.app.edit.action.NodeToolClickAction;
import intransix.osm.termite.app.edit.action.CreateMoveMoveAction;
import intransix.osm.termite.app.edit.action.CreateSnapMoveAction;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import java.awt.geom.Point2D;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
//import java.awt.Cursor;

/**
 * This is the edit mode used to create nodes.
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
	
	//====================
	// Public Methods
	//====================
	
	/** Constructor */
	public NodeEditorMode(MapLayerManager mapLayerManager) {
		super(mapLayerManager);
	}
	
//@TODO - fix setting of edit manager and layer
	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}
	
	public void setEditLayer(EditLayer editLayer) {
		this.editLayer = editLayer;
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
	public void turnOn(MapLayerManager mapLayerManager) {
		if(editLayer != null) {
			getMapLayerManager().addLayer(editLayer);
editLayer.on(mapLayerManager.getMapPane());				
			loadMouseActions();
			
//@TODO fix cursor
//			MapPanel mapPanel = editLayer.getMapPanel();
//			mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		}
		
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	@Override
	public void turnOff(MapLayerManager mapLayerManager) {
		if(editLayer != null) {
			getMapLayerManager().removeLayer(editLayer);
editLayer.off(mapLayerManager.getMapPane());	
//@TODO fix cursor
//			MapPanel mapPanel = editLayer.getMapPanel();
//			mapPanel.setCursor(Cursor.getDefaultCursor());
		}
	}
	
	/** This method loads the proper mouse actions for this mode. */
	private void loadMouseActions() {
		//set up move state
		MouseClickAction mouseClickAction = new NodeToolClickAction(editManager);		
		mouseClickAction.init();

		MouseMoveAction mouseSnapAction = new CreateSnapMoveAction(editManager);
		mouseSnapAction.init();
		MouseMoveAction mouseMoveAction = new CreateMoveMoveAction(editManager);
		mouseMoveAction.init();
		
		editLayer.setMouseClickAction(mouseClickAction);
		editLayer.setMouseMoveActions(mouseMoveAction, mouseSnapAction);
	}
	
}
