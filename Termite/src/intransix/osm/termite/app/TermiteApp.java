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
		
//		TermiteStructure structure = new TermiteStructure();
//		structure.setId(1);		
//		TermiteLevel level = new TermiteLevel();
//		level.setId(1);
//		structure.addLevel(level);
//		
//		SvgConverter svgConverter = new SvgConverter();
//		svgConverter.loadSvg(level,svgUriString,icm);
//		
//		SvgConverter svgConverter2 = new SvgConverter();
//		svgConverter2.createSvg(level,"testOut.svg", icm);
		
		OsmXml osmXml = new OsmXml();
		osmXml.parse("test.xml");
		TermiteData termiteData = new TermiteData();
		termiteData.loadData(osmXml);
		
//bounds calculation///////
double minX = 720;
double minY = 720;
double maxX = -720;
double maxY = -720;
int unloadedCount = 0;
for(OsmNode node:osmXml.getOsmNodes()) {
	if(!node.getIsLoaded()) {
		unloadedCount++;
		continue;
	}
	double lat = node.getLat();
	double lon = node.getLon();
	if(lat < minY) minY = lat;
	if(lon < minX) minX = lon;
	if(lat > maxY) maxY = lat;
	if(lon > maxX) maxX = lon;
}
Rectangle2D bounds = new Rectangle2D.Double(minX,minY,maxX - minX, maxY - minY);
////////
		
		//add to the map panel
		MapPanel mapDisplay = gui.getMap();
		mapDisplay.setTheme(theme);
		mapDisplay.setMap(termiteData);
mapDisplay.setBounds(bounds);
		mapDisplay.setStructure(2127658);
		mapDisplay.repaint();
		
	}

}
