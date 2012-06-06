package intransix.osm.termite.app;

import intransix.osm.termite.map.model.*;
import intransix.osm.termite.map.prop.FeatureInfoMap;
import intransix.osm.termite.app.gui.TermiteGui;
import intransix.osm.termite.render.MapPanel;

import intransix.osm.termite.theme.*;
import intransix.osm.termite.util.JsonIO;

import intransix.osm.termite.map.osm.*;

import intransix.osm.termite.render.structure.StructureLayer;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.tile.TileLayer;
import intransix.osm.termite.util.MercatorCoordinates;

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
		
		double baseLat = 39.627177;
		double baseLon = -79.997989;
		
		OsmModel.mxOffset = MercatorCoordinates.lonRadToMx(Math.toRadians(baseLon));
		OsmModel.myOffset = MercatorCoordinates.latRadToMy(Math.toRadians(baseLat));
		
		//load the theme
		JSONObject themeJson = JsonIO.readJsonFile(themeFileName);
		Theme theme = Theme.parse(themeJson);
		
		JSONObject featureInfoJson = JsonIO.readJsonFile(featureInfoName);
		FeatureInfoMap featureInfoMap = FeatureInfoMap.parse(featureInfoJson);
		
		JSONObject modelJson = JsonIO.readJsonFile(modelName);
		OsmModel.parse(modelJson);
		OsmModel.featureInfoMap = featureInfoMap;
		
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
		OsmData osmData = osmXml.parse("test2.xml");
		TermiteData termiteData = new TermiteData();
		termiteData.loadData(osmData);
		
		TermiteStructure structure = termiteData.getStructure(2127658L);
		TermiteLevel level = structure.lookupLevel(0);
		
		StructureLayer structureLayer = new StructureLayer();
		structureLayer.setTheme(theme);
		structureLayer.setLevel(level);
		
		EditLayer editLayer = new EditLayer();
		editLayer.setLevel(level);
		
		
		String mapQuestUrlTemplate = "http://otile1.mqcdn.com/tiles/1.0.0/osm/%3$d/%1$d/%2$d.jpg";
		int mapQuestMaxZoom = 18;
		int mapQuestTileSize = 256;
		
		TileLayer tileLayer = new TileLayer(mapQuestUrlTemplate,mapQuestMaxZoom,mapQuestTileSize);
				
		//add to the map panel
		MapPanel mapDisplay = gui.getMap();
		mapDisplay.addLayer(tileLayer);
		mapDisplay.addLayer(structureLayer);
		mapDisplay.addLayer(editLayer);
		
		mapDisplay.addMapListener(tileLayer);
		
		if(structure != null) {
			mapDisplay.setBounds(structure.getBounds());
		}
		
		mapDisplay.repaint();
		
	}

}
