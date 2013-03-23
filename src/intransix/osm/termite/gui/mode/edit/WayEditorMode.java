package intransix.osm.termite.gui.mode.edit;

import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.edit.MouseClickAction;
import intransix.osm.termite.app.edit.MouseMoveAction;
import intransix.osm.termite.app.edit.action.*;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.gui.mode.EditorMode;
import intransix.osm.termite.render.edit.EditLayer;
import java.awt.Cursor;
import java.awt.geom.Point2D;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

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
	
	private EventHandler<MouseEvent> mouseClickHandler;
	private EventHandler<MouseEvent> mouseMoveHandler;
	
	//====================
	// Public Methods
	//====================
	
	public WayEditorMode(MapLayerManager mapLayerManager) {
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
		}
		
//@TODO fix cursor
//		MapPanel mapPanel = editLayer.getMapPanel();
//		mapPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
		MouseMoveAction mouseSnapAction = new CreateSnapMoveAction(editManager);
		mouseSnapAction.init();
		MouseMoveAction mouseMoveAction = new CreateMoveMoveAction(editManager);
		mouseMoveAction.init();
		
		editLayer.setMouseClickAction(mouseClickAction);
		editLayer.setMouseMoveActions(mouseMoveAction, mouseSnapAction);
	}


}
