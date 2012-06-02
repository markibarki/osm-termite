package intransix.osm.termite.app;

import intransix.osm.termite.map.prop.FeatureInfoMap;
import intransix.osm.termite.app.gui.TermiteGui;
import intransix.osm.termite.app.gui.MapPanel;

import intransix.osm.termite.theme.*;
import intransix.osm.termite.map.geom.*;
import intransix.osm.termite.util.JsonIO;

import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.geom.*;

import intransix.osm.termite.svg.*;
import java.awt.geom.Rectangle2D;

import intransix.osm.termite.app.gui.StructureLayer;
import intransix.osm.termite.app.gui.TileLayer;

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
		String modelName = "model.json";
		
		//load the theme
		JSONObject themeJson = JsonIO.readJsonFile(themeFileName);
		Theme theme = Theme.parse(themeJson);
		
		JSONObject featureInfoJson = JsonIO.readJsonFile(featureInfoName);
		FeatureInfoMap featureInfoMap = FeatureInfoMap.parse(featureInfoJson);
		
		JSONObject modelJson = JsonIO.readJsonFile(modelName);
		OsmModel.parse(modelJson);
		
//		
//		String svgFileName = "test.svg";
//		String svgUriString = svgFile.toURI().toString();
//		java.io.File svgFile = new java.io.File(svgFileName)
//		SvgConverter svgConverter = new SvgConverter();
//		svgConverter.loadSvg(level,svgUriString,icm);
//		
//		SvgConverter svgConverter2 = new SvgConverter();
//		svgConverter2.createSvg(level,"testOut.svg", icm);
		
		OsmXml osmXml = new OsmXml();
		osmXml.parse("test2.xml");
		TermiteData termiteData = new TermiteData(featureInfoMap);
		termiteData.loadData(osmXml);
		
		StructureLayer structureLayer = new StructureLayer();
		structureLayer.setTheme(theme);
		structureLayer.setMap(termiteData);
		structureLayer.setStructure(2127658);
		
		TileLayer tileLayer = new TileLayer();
				
		//add to the map panel
		MapPanel mapDisplay = gui.getMap();
		mapDisplay.addLayer(tileLayer);
		mapDisplay.addLayer(structureLayer);
		
		TermiteStructure structure = structureLayer.getCurrentStructure();
		if(structure != null) {
			mapDisplay.setBounds(structure.getBounds());
		}
		
		mapDisplay.repaint();
		
	}

}
