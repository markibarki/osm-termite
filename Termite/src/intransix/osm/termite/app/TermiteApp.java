package intransix.osm.termite.app;

import javax.swing.*; 

import intransix.osm.termite.app.gui.TermiteGui;
import intransix.osm.termite.app.gui.MapPanel;

import intransix.osm.termite.theme.*;
import intransix.osm.termite.map.geom.*;
import java.awt.Color;
import java.awt.geom.Path2D;


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
		loadMap();
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
	private void loadMap() {
		Theme theme = new Theme();
		theme.addStyle(new Style("buildingpart","room",Color.BLUE,Color.ORANGE,2));
		theme.addStyle(new Style("buildingpart","stairs",Color.GRAY,Color.BLACK,2));
		theme.addStyle(new Style("buildingpart","hallway",Color.GREEN,null,0));
		theme.addStyle(new Style("buildingpart","wall",Color.BLACK,null,4));
		theme.addStyle(new Style("buildingpart","door",Color.RED,Color.BLACK,2));
		theme.addStyle(new Style("furnishing","table",Color.GRAY,Color.BLACK,2));
		theme.addStyle(new Style("furnishing","chair",Color.DARK_GRAY,Color.BLACK,2));
		theme.addStyle(new Style("furnishing","shelf",Color.MAGENTA,Color.BLACK,2));
		theme.setDeafultStyle(new Style("app","object",Color.LIGHT_GRAY,Color.BLACK,2));
		
		//make a map and do something with it, for a test
		Structure structure = new Structure();
		structure.setId(1);
		Level level = new Level();
		level.setId(1);
		structure.addLevel(level);
		
		
		PathFeature f;
		Path2D path;
		
		path = new Path2D.Double();
		path.moveTo(10,10);
		path.lineTo(100,10);
		path.lineTo(100,100);
		path.lineTo(10,10);
		path.closePath();
		f = new PathFeature(Feature.FeatureType.AREA);
		f.setPath(path);
		f.setId(1);
		f.setProperty("buildingpart","room");
		level.addFeature(f);
		
		path = new Path2D.Double();
		path.moveTo(50,80);
		path.lineTo(200,120);
		path.lineTo(100,200);
		path.lineTo(50,80);
		path.closePath();
		f = new PathFeature(Feature.FeatureType.AREA);
		f.setPath(path);
		f.setId(2);
		f.setProperty("buildingpart","hallway");
		level.addFeature(f);
		
		path = new Path2D.Double();
		path.moveTo(300,100);
		path.lineTo(200,140);
		path.lineTo(70,230);
		f = new PathFeature(Feature.FeatureType.LINE);
		f.setPath(path);
		f.setId(3);
		f.setProperty("furnishing","shelf");
		level.addFeature(f);
		
		path = new Path2D.Double();
		path.moveTo(160,100);
		path.lineTo(120,140);
		path.lineTo(120,60);
		path.closePath();
		path.moveTo(150,100);
		path.lineTo(130,120);
		path.lineTo(130,80);
		path.closePath();
		path.setWindingRule(Path2D.WIND_EVEN_ODD);
		f = new PathFeature(Feature.FeatureType.MULTIAREA);
		f.setPath(path);
		f.setId(4);
		level.addFeature(f);
		
		//add to the map panel
		MapPanel mapDisplay = gui.getMap();
		mapDisplay.setTheme(theme);
		mapDisplay.setStructure(structure);
		mapDisplay.setLevel(1);
		mapDisplay.repaint();
	}

}
