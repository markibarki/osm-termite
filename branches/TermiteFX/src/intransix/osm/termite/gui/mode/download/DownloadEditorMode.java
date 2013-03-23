package intransix.osm.termite.gui.mode.download;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.gui.dialog.MessageDialog;
import intransix.osm.termite.gui.mode.EditorMode;
import intransix.osm.termite.gui.task.MapDataRequestTask;
import intransix.osm.termite.render.checkout.DownloadLayer;
import intransix.osm.termite.render.checkout.DownloadToolbar;
import java.awt.geom.Rectangle2D;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 *
 * @author sutter
 */
public class DownloadEditorMode extends EditorMode {
	//====================
	// Properties
	//====================
	
	private final static String MODE_NAME = "Download Mode";
	private final static String ICON_NAME = "/intransix/osm/termite/resources/stdmodes/downloadMode.png";


	private MapDataManager mapDataManager;
	private DownloadLayer downloadLayer;
	private DownloadToolbar toolBar = null;	
	
	//====================
	// Public Methods
	//====================
	
	public DownloadEditorMode(MapLayerManager mapLayerManager) {
		super(mapLayerManager);
		setDataEnabledStates(false,true);
		
		toolBar = new DownloadToolbar(this);
	}
	
	public void setMapDataManager(MapDataManager mapDataManager) {
		this.mapDataManager = mapDataManager;
	}
	
	public void setDownloadLayer(DownloadLayer downloadLayer) {
		this.downloadLayer = downloadLayer;
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
	public void turnOn(MapLayerManager mapLayerManager) {
		mapLayerManager.addLayer(downloadLayer);
downloadLayer.on(mapLayerManager.getMapPane());	
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	public void turnOff(MapLayerManager mapLayerManager) {
		mapLayerManager.removeLayer(downloadLayer);
downloadLayer.off(mapLayerManager.getMapPane());	
	}	
	
	public void doDownload() {
		Bounds selection = downloadLayer.getSelectionBoundsMercator();
		if(selection == null) {
			MessageDialog.show("You must select a bounding box to download.");
			return;
		}
		Rectangle2D downloadRectangle = new Rectangle2D.Double(selection.getMinX(),selection.getMinY(),
				selection.getWidth(),selection.getHeight());
		
		//run the load data task
		MapDataRequestTask mdrt = new MapDataRequestTask(mapDataManager,
				downloadRectangle);
		mdrt.execute();
	}
	
	public void doSearch(String searchString) {
		MessageDialog.show("Search is not implmented.");
	}
	
	public void clearSelection() {	
		downloadLayer.clearSelection();
	}
	
	@Override
	public ToolBar getToolBar() {
		return toolBar;
	}
	

}
