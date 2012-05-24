package intransix.osm.termite.app;

import intransix.osm.termite.map.prop.FeatureInfoMap;
import intransix.osm.termite.app.gui.TermiteGui;
import intransix.osm.termite.app.gui.MapPanel;

import intransix.osm.termite.theme.*;
import intransix.osm.termite.map.geom.*;
import intransix.osm.termite.util.JsonIO;

import intransix.osm.termite.svg.*;

import org.json.*;


/**
 *
 * @author sutter
 */
public class TermiteApp {
	
	//=====================
	// Private Properties
	//=====================
	
	private TermiteGui gui;
	
	
	//=====================
	//
	//=====================
	
	public void TermiteApp() {
	}
	
	public void startup() {
		gui = new TermiteGui(this);
		gui.setVisible(true);
		
		//load a dummy map
		try {
			loadMap();
		}
		catch(Exception ex) {
			ex.printStackTrace();
			exit();
		}
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
	
	//dummy method
	private void loadMap() throws Exception {
		
		String themeFileName = "theme.json";
		String featureInfoName = "featureInfo.json";
		String svgFileName = "test.svg";
		java.io.File svgFile = new java.io.File(svgFileName);
		String svgUriString = svgFile.toURI().toString();
		
		//load the theme
		JSONObject themeJson = JsonIO.readJsonFile(themeFileName);
		Theme theme = Theme.parse(themeJson);
		
		JSONObject featureInfoJson = JsonIO.readJsonFile(featureInfoName);
		FeatureInfoMap icm = FeatureInfoMap.parse(featureInfoJson);
		
		SvgConverter svgConverter = new SvgConverter();
		svgConverter.loadSvg(svgUriString,icm);
		
		Structure structure = svgConverter.structure;
		
		//add to the map panel
		MapPanel mapDisplay = gui.getMap();
		mapDisplay.setTheme(theme);
		mapDisplay.setStructure(structure);
		mapDisplay.setLevel(1);
		mapDisplay.repaint();
	}

}
