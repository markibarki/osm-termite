package intransix.osm.termite.app;

import intransix.osm.termite.map.theme.Theme;
import intransix.osm.termite.util.JsonIO;
import intransix.osm.termite.gui.*;
import org.json.*;
import java.awt.geom.Rectangle2D;
import javax.swing.UIManager;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.mode.EditorModeManager;
import intransix.osm.termite.app.basemap.BaseMapManager;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.geocode.GeocodeManager;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.app.level.LevelManager;
import intransix.osm.termite.app.filter.FilterManager;

import intransix.osm.termite.render.map.RenderLayer;
import intransix.osm.termite.render.edit.EditLayer;

import intransix.osm.termite.render.MapPanel;

/**
 *
 * @author sutter
 */
public class TermiteApp {
	
	//=====================
	// Private Properties
	//=====================
	
	private final static String VERSION = "0.0.7p0";
	
	private TermiteGui gui;
	private LoginManager loginManager = new LoginManager();
	
	private Theme theme;
	
	private MapDataManager mapDataManager;
	private BaseMapManager baseMapManager;
	private FeatureTypeManager featureTypeManager;
	private EditManager editManager;
	private GeocodeManager geocodeManager;
	private MapLayerManager mapLayerManager;
	private EditorModeManager modeManager;
	private ViewRegionManager viewRegionManager;
	private FilterManager filterManager;
	private LevelManager levelManager;
	
	//=====================
	//
	//=====================
	
	public void TermiteApp() {
	}
	
	public TermiteGui getGui() {
		return gui;
	}
	
	public String getVersion() {
		return VERSION;
	}
	
	public LoginManager getLoginManager() {
		return loginManager;
	}
	
	public Theme getTheme() {
		return theme;
	}
	
	public void startup() {
		
		try {
			init();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			exit();
		}
		
		gui.setVisible(true);
	}
	
	public void exit() {
		System.exit(0);
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		 //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				TermiteApp app = new TermiteApp();
                app.startup();
            }
        });
        
	}
	
	//=====================
	// Private Methods
	//=====================
	
	private void init() throws Exception {
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		//load the initialization files
		String themeFileName = "theme.json";
		String featureInfoName = "featureInfo.json";
		String modelFileName = "model.json";
		String baseMapFileName= "baseMapInfo.json";
		
		//load the theme
		JSONObject themeJson = JsonIO.readJsonFile(themeFileName);
		theme = Theme.parse(themeJson);
		
		//----------------
		//create the gui
		//----------------
		gui = new TermiteGui(this);
		
		//ui component needed for initialization
		MapPanel mapPanel = gui.getMapPanel();		
		
		//----------------
		// Managers
		//----------------
		
		//map data
		mapDataManager = new MapDataManager();
		mapDataManager.init(modelFileName);
		
		//base map
		baseMapManager = new BaseMapManager();
		baseMapManager.init(baseMapFileName);
		
		//featture type
		featureTypeManager = new FeatureTypeManager();
		featureTypeManager.init(featureInfoName);
		
		//levels
		levelManager = new LevelManager(filterManager);
		mapDataManager.addMapDataListener(levelManager);
		
		//edit manager
		editManager = new EditManager(featureTypeManager,levelManager);
		editManager.init();
		mapDataManager.addMapDataListener(editManager);
		
		//geocode
		geocodeManager = new GeocodeManager();
		geocodeManager.init();
		
		//filter
		filterManager = new FilterManager();
		mapDataManager.addMapDataListener(filterManager);
		
		//view region manager
		viewRegionManager = new ViewRegionManager();
		viewRegionManager.setMapComponent(mapPanel);
		
		//map layers
		mapLayerManager = new MapLayerManager(viewRegionManager,mapPanel);
		mapLayerManager.addLayer(baseMapManager.getBaseMapLayer());
		mapLayerManager.addLayer(mapDataManager.getRenderLayer());
		mapLayerManager.addLayer(mapDataManager.getDownloadLayer());
		mapLayerManager.addLayer(editManager.getEditLayer());
		mapLayerManager.addLayer(geocodeManager.getGeocodeLayer());
		
		//editor modes
		modeManager = new EditorModeManager();
		modeManager.addMode(mapDataManager.getDownloadEditorMode());
		modeManager.addMode(editManager.getSelectEditorMode());
		modeManager.addMode(editManager.getNodeEditorMode());
		modeManager.addMode(editManager.getWayEditorMode());
		modeManager.addMode(geocodeManager.getGeocodeEditorMode());
		modeManager.setDefaultModes(mapDataManager.getDownloadEditorMode(),editManager.getSelectEditorMode());
		mapDataManager.addMapDataListener(modeManager);
		
		//gui initialization
		gui.setMapDataManager(mapDataManager); //undo/redo
		gui.setFeatureTypeManager(featureTypeManager); //feature selection
		gui.setBaseMapManager(baseMapManager); //select the base map
		gui.setEditManager(editManager); //selection for edit properties
		gui.setLevelManager(levelManager);
		gui.setMapLayerManager(mapLayerManager); //for map layer state editing (not display)
		gui.setViewRegionManager(viewRegionManager);
		gui.setModeManager(modeManager); //sets the mode
		
		//set the view
		Rectangle2D rect = new Rectangle2D.Double(-117.126,40.366,.03,.03);
		viewRegionManager.setLatLonViewBounds(rect);
	
		RenderLayer renderLayer = mapDataManager.getRenderLayer();
		renderLayer.setTheme(theme);
		
		mapDataManager.setOsmData(null);
	}
}
