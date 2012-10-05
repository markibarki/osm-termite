package intransix.osm.termite.app;

import java.util.*;
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
import intransix.osm.termite.app.preferences.Preferences;

import intransix.osm.termite.render.map.RenderLayer;
import intransix.osm.termite.render.tile.TileLayer;

import intransix.osm.termite.render.MapPanel;
import javax.swing.JOptionPane;
import intransix.osm.termite.gui.task.ShutdownTask;

/**
 *
 * @author sutter
 */
public class TermiteApp {
	
	//=====================
	// Private Properties
	//=====================
	
	//this can be overridden using a command line argument
	private static String CONFIG_FILE_NAME = "config.json";
	
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
	
	private List<ShutdownListener> shutdownListeners = new ArrayList<ShutdownListener>();
	
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
	
	public void addShutdownListener(ShutdownListener listener) {
		if(!shutdownListeners.contains(listener)) {
			this.shutdownListeners.add(listener);
		}
	}
	
	public void removeShutdownListener(ShutdownListener listener) {
		this.shutdownListeners.remove(listener);
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
		try {
			ShutdownTask st = new ShutdownTask(this);
			st.execute();
			st.blockUI();
		}
		catch(Exception ex) {
			JOptionPane.showMessageDialog(null,"There was an error shutting down.");
		}
		System.exit(0);
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		if(args.length > 0) {
			TermiteApp.CONFIG_FILE_NAME = args[0];
		}
		
		 //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				TermiteApp app = new TermiteApp();
				app.startup();
            }
        });
        
	}
	
	/** This method is automatically called at shutdown to save any desired data
	 * to the preference file, or take any other action at close. An object can
	 * register as a ShutdownListener if it wants to be notified at shutdown.
	 */
	public void preshutdown() throws Exception {
		for(ShutdownListener listener:shutdownListeners) {
			listener.onShutdown();
		}
		Preferences.savePreferences(CONFIG_FILE_NAME);
	}
	
	//=====================
	// Private Methods
	//=====================
	
	private void init() throws Exception {
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		//load the preferences
		Preferences.init(CONFIG_FILE_NAME);
		loadAppPreferences();
		
		//load the theme
		String themeFileName = Preferences.getProperty("themeFile");
		if(themeFileName == null) {
			throw new Exception("Theme file missing!");
		}
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
		mapDataManager.init();
		
		//base map
		baseMapManager = new BaseMapManager();
		baseMapManager.init();
		
		//featture type
		featureTypeManager = new FeatureTypeManager();
		featureTypeManager.init();
		mapDataManager.setFeatureTypeManager(featureTypeManager);
		
		//filter
		filterManager = new FilterManager();
		mapDataManager.setFilterManager(filterManager);
		filterManager.addFilterListener(mapDataManager);
		
		//levels
		levelManager = new LevelManager(mapDataManager, filterManager);
		mapDataManager.addMapDataListener(levelManager);
		
		//edit manager
		editManager = new EditManager(featureTypeManager,levelManager,mapDataManager);
		editManager.init();
		
		//view region manager
		viewRegionManager = new ViewRegionManager();
		viewRegionManager.setMapComponent(mapPanel);
		addShutdownListener(viewRegionManager);
		
		//map layers
		mapLayerManager = new MapLayerManager(viewRegionManager,mapPanel);
		
		//geocode
		geocodeManager = new GeocodeManager();
		geocodeManager.init(mapLayerManager);
		
		//populate tghe map layers
		mapLayerManager.addLayer(baseMapManager.getBaseMapLayer());
		mapLayerManager.addLayer(mapDataManager.getRenderLayer());
		mapLayerManager.addLayer(mapDataManager.getDownloadLayer());
		mapLayerManager.addLayer(editManager.getEditLayer());
		mapLayerManager.addLayer(geocodeManager.getGeocodeLayer());
		
		//editor modes
		modeManager = new EditorModeManager();
		mapDataManager.addMapDataListener(modeManager);
		modeManager.addMode(mapDataManager.getDownloadEditorMode());
		modeManager.addMode(editManager.getSelectEditorMode());
		modeManager.addMode(editManager.getNodeEditorMode());
		modeManager.addMode(editManager.getWayEditorMode());
		modeManager.addMode(geocodeManager.getGeocodeEditorMode());
		modeManager.setDefaultModes(mapDataManager.getDownloadEditorMode(),editManager.getSelectEditorMode());
		
		//gui initialization
		gui.setMapDataManager(mapDataManager); //undo/redo
		gui.setFeatureTypeManager(featureTypeManager); //feature selection
		gui.setBaseMapManager(baseMapManager); //select the base map
		gui.setEditManager(editManager); //selection for edit properties
		gui.setLevelManager(levelManager);
		gui.setMapLayerManager(mapLayerManager); //for map layer state editing (not display)
		gui.setViewRegionManager(viewRegionManager);
		gui.setModeManager(modeManager); //sets the mode
		this.addShutdownListener(gui);
		
		
		//more generic init
		RenderLayer renderLayer = mapDataManager.getRenderLayer();
		renderLayer.setTheme(theme);
		viewRegionManager.addLocalCoordinateListener(renderLayer);
		
		TileLayer tileLayer = baseMapManager.getBaseMapLayer();
		viewRegionManager.addMapListener(tileLayer);
		
		//set the view
		viewRegionManager.setInitialView();
		mapDataManager.clearData();
	}
	
	private void loadAppPreferences() {
		//network
		String proxyHost = Preferences.getProperty("proxyHost");
		if(proxyHost != null) {
			System.setProperty("http.proxyHost",proxyHost);
			String proxyPort = Preferences.getProperty("proxyPort");
			if(proxyPort != null) {
				System.setProperty("http.proxyPort",proxyPort);
			}
		}
	}
}
