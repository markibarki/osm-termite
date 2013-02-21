package intransix.osm.termite.gui.mode.download;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.mode.EditorMode;
import intransix.osm.termite.render.checkout.DownloadLayer;
import intransix.osm.termite.render.checkout.DownloadToolbar;
import javafx.scene.control.ToolBar;

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
	
	public DownloadEditorMode() {
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
	public void turnOn() {
		downloadLayer.setActiveState(true);
downloadLayer.visibleProperty().setValue(true);
	}
	
	/** This method is called when the editor mode is turned off. 
	 */
	public void turnOff() {
		downloadLayer.setActiveState(false);
downloadLayer.visibleProperty().setValue(false);
	}	
	
	public void doDownload() {
//@TODO FIX THIS!
//		Rectangle2D selection = downloadLayer.getSelectionMercator();
//		if(selection == null) {
//			JOptionPane.showMessageDialog(null, "You must select a bounding box to download.");
//			return;
//		}
//		
//		//get the bounding box
//		double minLat = Math.toDegrees(MercatorCoordinates.myToLatRad(selection.getMaxY()));
//		double minLon = Math.toDegrees(MercatorCoordinates.mxToLonRad(selection.getMinX()));
//		double maxLat = Math.toDegrees(MercatorCoordinates.myToLatRad(selection.getMinY()));
//		double maxLon = Math.toDegrees(MercatorCoordinates.mxToLonRad(selection.getMaxX()));
//		
//		//run the load data task
//		MapDataRequestTask mdrt = new MapDataRequestTask(mapDataManager,minLat,minLon,maxLat,maxLon);
//		mdrt.execute();
	}
	
	public void doSearch(String searchString) {
//@TODO add message box
//		JOptionPane.showMessageDialog(null, "Search is not implmented.");
	}
	
	public void clearSelection() {
//@TODO fix this	
//		downloadLayer.clearSelection();
	}
	
	@Override
	public ToolBar getToolBar() {
		return toolBar;
	}
	

}
