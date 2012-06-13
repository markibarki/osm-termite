package intransix.osm.termite.app;

import intransix.osm.termite.map.model.EditAction;
import intransix.osm.termite.map.model.EditInstruction;
import intransix.osm.termite.map.model.UpdatePosition;
import intransix.osm.termite.map.model.UpdateInsertMember;
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
import intransix.osm.termite.util.LocalCoordinates;
import intransix.osm.termite.util.MercatorCoordinates;

import org.json.*;

import java.util.*;


/**
 *
 * @author sutter
 */
public class TermiteApp {
	
	//=====================
	// Private Properties
	//=====================
	
	private TermiteGui gui;
	
	private TermiteData termiteData;
	
	
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
		
		//method 1
		String modelFileName = "model1.json";
		String mapDataFileName = "morgantownMall.xml";
		long structureId = 2127658L;
		double baseLat = 39.627177;
		double baseLon = -79.997989;
		
//		String modelFileName = "model2.json";
//		String mapDataFileName = "nodeTestBuilding.xml";
//		long structureId = 167142181L;
//		double baseLat = 40.376;
//		double baseLon = -117.116;
		
		//set local coordinates
		double mx = MercatorCoordinates.lonRadToMx(Math.toRadians(baseLon));
		double my = MercatorCoordinates.latRadToMy(Math.toRadians(baseLat));
		LocalCoordinates.setLocalAnchor(mx,my);
		
		//load the theme
		JSONObject themeJson = JsonIO.readJsonFile(themeFileName);
		Theme theme = Theme.parse(themeJson);
		
		JSONObject featureInfoJson = JsonIO.readJsonFile(featureInfoName);
		FeatureInfoMap featureInfoMap = FeatureInfoMap.parse(featureInfoJson);
		
		JSONObject modelJson = JsonIO.readJsonFile(modelFileName);
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
		
		OsmParser osmParser = new OsmParser();
		OsmData osmData = osmParser.parse(mapDataFileName);
		termiteData = new TermiteData();
		termiteData.loadData(osmData);
		
		TermiteStructure structure = termiteData.getStructure(structureId);
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
		
//		Timer timer = new Timer();
//		TimerTask timerTask = new TimerTask() {
//			public void run() {
//				doTestAction();
//			}
//		};
//		timer.schedule(timerTask,3000);
		
	}
	
	private void doTestAction() {
		EditAction action = termiteData.createAction("Test Action");
		EditInstruction instr;
		
		TermiteNode testNode = termiteData.getNode(1710315717L);
		OsmNode osmNode = testNode.getOsmObject();
		double x = osmNode.getX();
		double y = osmNode.getY();
		UpdatePosition targetData = new UpdatePosition(x + 10,y+5);
		instr = termiteData.getUpdateInstruction(osmNode,targetData);
		action.addInstruction(instr);
		
//		OsmData osmData = termiteData.getWorkingData();
//		Long id1 = osmData.getNextId();
//		OsmNode oNode1 = new OsmNode(id1);
//		oNode1.setPosition(x, y-10);
//		instr = termiteData.getCreateInstruction(oNode1);
//		action.addInstruction(instr);
//		
//		Long id2 = osmData.getNextId();
//		OsmNode oNode2 = new OsmNode(id2);
//		oNode2.setPosition(x-1, y-11);
//		instr = termiteData.getCreateInstruction(oNode2);
//		action.addInstruction(instr);
//		
//		Long id3 = osmData.getNextId();
//		OsmNode oNode3 = new OsmNode(id3);
//		oNode3.setPosition(x+1, y-11);
//		instr = termiteData.getCreateInstruction(oNode3);
//		action.addInstruction(instr);
//		
//		Long id4 = osmData.getNextId();
//		OsmWay way = new OsmWay(id4);
//		List<Long> wayNodes = way.getNodeIds();
//		wayNodes.add(id1);
//		wayNodes.add(id2);
//		wayNodes.add(id3);
//		wayNodes.add(id1);
//		instr = termiteData.getCreateInstruction(way);
//		action.addInstruction(instr);
//		
//		TermiteLevel level = termiteData.getLevel(2127658L,0);
//		OsmRelation osmLevel = (OsmRelation)level.getOsmObject();
//		int index = osmLevel.getMembers().size();
//		OsmMember newMember = new OsmMember(id4,"way","buildingpart");
//		UpdateInsertMember uim = new UpdateInsertMember(newMember,index);
//		instr = termiteData.getUpdateInstruction(osmLevel,uim);
//		action.addInstruction(instr);
//		
//		Long id5 = osmData.getNextId();
//		OsmRelation relation = new OsmRelation(id5);
//		List<OsmMember> members = relation.getMembers();
//		OsmMember member = new OsmMember(158933100L,"way","outer");
//		members.add(member);
//		member = new OsmMember(id4,"way","inner");
//		members.add(member);
//		relation.setProperty("type","multipolygon");
//		instr = termiteData.getCreateInstruction(relation);
//		action.addInstruction(instr);
		
		try {
			termiteData.doAction(action);
			gui.getMap().repaint();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

}
