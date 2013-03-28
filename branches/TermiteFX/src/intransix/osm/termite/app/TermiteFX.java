/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.app;

import intransix.osm.termite.app.basemap.BaseMapManager;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.app.filter.FilterManager;
import intransix.osm.termite.app.geocode.GeocodeManager;
import intransix.osm.termite.app.level.LevelManager;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.preferences.Preferences;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.gui.TermiteFXGui;
import intransix.osm.termite.gui.mode.EditorModeManager;
import intransix.osm.termite.map.theme.Theme;
import intransix.osm.termite.render.tile.TileLayer;
import intransix.osm.termite.util.JsonIO;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class TermiteFX extends Application {
	
	//=====================
	// Private Properties
	//=====================
	
	//this can be overridden using a command line argument
	private static String CONFIG_FILE_NAME = "config.json";
	
	private final static String VERSION = "0.0.8p2";
	
	private TermiteFXGui gui;
	private LoginManager loginManager = new LoginManager();
	
	private Theme theme;
	
	private MapDataManager mapDataManager;
	private BaseMapManager baseMapManager;
	private FeatureTypeManager featureTypeManager;
	private EditManager editManager;
	private GeocodeManager geocodeManager;
	private FilterManager filterManager;
	private LevelManager levelManager;
	
	private List<ShutdownListener> shutdownListeners = new ArrayList<ShutdownListener>();
	
	//==========================
	// Public Methods
	//==========================
	
	@Override
	public void start(Stage stage) throws Exception {
		try {
			setup(stage);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			exit();
		}
	}
	
	/** This method closes the system. */
	public void exit() {
		for(ShutdownListener listener:shutdownListeners) {
			listener.onShutdown();
		}
		try {
			Preferences.savePreferences(CONFIG_FILE_NAME);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * The main() method is ignored in correctly deployed JavaFX application. main() serves only as fallback in case the application can not be launched through
	 * deployment artifacts, e.g., in IDEs with limited FX support. NetBeans ignores main().
	 *
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}
	
	public void addShutdownListener(ShutdownListener listener) {
		if(!shutdownListeners.contains(listener)) {
			this.shutdownListeners.add(listener);
		}
	}
	
	public void removeShutdownListener(ShutdownListener listener) {
		this.shutdownListeners.remove(listener);
	}
	
	public LoginManager getLoginManager() {
		return loginManager;
	}
	
	//=====================
	// Private Methods
	//=====================
	
	private void setup(Stage stage) throws Exception {
		
		//do not exit when all windows close
		Platform.setImplicitExit(false);
		
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
		
		//create the gui
		gui = new TermiteFXGui(this,stage);
		gui.load();
		gui.setTheme(theme);
		
		//----------------
		// Managers
		//----------------
		
		//map data
		mapDataManager = new MapDataManager();
		mapDataManager.init();
		gui.setMapDataManager(mapDataManager);
		
		//base map
		baseMapManager = new BaseMapManager();
		baseMapManager.init();
		gui.setBaseMapManager(baseMapManager); //select the base map
		
		//featture type
		featureTypeManager = new FeatureTypeManager();
		featureTypeManager.init();
		gui.setFeatureTypeManager(featureTypeManager);
	
//set a feature type temporarily
featureTypeManager.setActiveFeatureType(featureTypeManager.getFeatureInfoMap().getRoot().getData());
		
		//filter
		filterManager = new FilterManager();
		mapDataManager.addMapDataListener(filterManager);
		
		//levels
		levelManager = new LevelManager(filterManager);
		mapDataManager.addMapDataListener(levelManager);

		
		//edit manager
		editManager = new EditManager(featureTypeManager,levelManager,mapDataManager);
		gui.setEditManager(editManager); //selection for edit properties
//
//		
//		//geocode
//		geocodeManager = new GeocodeManager();
//		geocodeManager.init(mapLayerManager);
//		
			
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
