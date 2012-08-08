package intransix.osm.termite.app;

import intransix.osm.termite.map.data.OsmModel;
import intransix.osm.termite.map.theme.Theme;
import intransix.osm.termite.map.feature.FeatureInfoMap;
import intransix.osm.termite.util.JsonIO;
import intransix.osm.termite.gui.*;
import intransix.osm.termite.gui.stdmode.*;
import intransix.osm.termite.render.tile.TileInfo;
import org.json.*;

import java.util.*;
import java.awt.geom.Rectangle2D;
import javax.swing.UIManager;

/**
 *
 * @author sutter
 */
public class TermiteApp {
	
	//=====================
	// Private Properties
	//=====================
	
	private TermiteGui gui;
	private LoginManager loginManager = new LoginManager();
	
	private Theme theme;
	private FeatureInfoMap featureInfoMap;
	private List<TileInfo> tileInfoList;
	
	private SearchEditorMode searchEditorMode;
	private List<EditorMode> editModes = new ArrayList<EditorMode>();
	
	
	
	//=====================
	//
	//=====================
	
	public void TermiteApp() {
	}
	
	public TermiteGui getGui() {
		return gui;
	}
	
	public LoginManager getLoginManager() {
		return loginManager;
	}
	
	public Theme getTheme() {
		return theme;
	}
	
	public FeatureInfoMap getFeatureInfoMap() {
		return featureInfoMap;
	}
	
	public EditorMode getSearchMode() {
		return searchEditorMode;
	}
	
	public List<EditorMode> getEditModes() {
		return editModes;
	}
	
	public List<TileInfo> getBaseMapInfo() {
		return tileInfoList;
	}
	
	public Rectangle2D getInitialLatLonBounds() {
		return new Rectangle2D.Double(-117.116,40.376,.01,.01);
//		return new Rectangle2D.Double(-140,-40,280,100);
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
		String modelFileName = "model2.json";
		String baseMapFileName= "baseMapInfo.json";
		
		//load the theme
		JSONObject themeJson = JsonIO.readJsonFile(themeFileName);
		theme = Theme.parse(themeJson);
		
		JSONObject featureInfoJson = JsonIO.readJsonFile(featureInfoName);
		featureInfoMap = FeatureInfoMap.parse(featureInfoJson);
		
		JSONObject modelJson = JsonIO.readJsonFile(modelFileName);
		OsmModel.parse(modelJson);
		OsmModel.featureInfoMap = featureInfoMap;
		
		JSONObject mapInfoJson = JsonIO.readJsonFile(baseMapFileName);
		tileInfoList = TileInfo.parseInfoList(mapInfoJson);
		
		//create the gui
		gui = new TermiteGui(this);
		
		//create the editor modes
		searchEditorMode = new SearchEditorMode(gui);
		editModes.add(new SelectEditorMode(gui));
		editModes.add(new NodeEditorMode(gui));
		editModes.add(new WayEditorMode(gui));
		editModes.add(new GeocodeEditorMode(gui));

		gui.initialize();
	}
}
