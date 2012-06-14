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
//		String modelFileName = "model1.json";
//		String mapDataFileName = "morgantownMall.xml";
//		final long structureId = 2127658L;
//		double baseLat = 39.627177;
//		double baseLon = -79.997989;
//		final long wayId = 158933100L;
//		final long nodeId = 1710315717L;
		
		String modelFileName = "model2.json";
		String mapDataFileName = "nodeTestBuilding.xml";
		final long structureId = 167142181L;
		double baseLat = 40.376;
		double baseLon = -117.116;
		final long wayId = 167142563L;
		final long nodeId = 1785444150L;
		
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
			structure.calculateBounds();
			mapDisplay.setBounds(structure.getBounds());
		}
		
		mapDisplay.repaint();
		
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			public void run() {
				doTestAction(structureId,wayId,nodeId);
			}
		};
		timer.schedule(timerTask,3000);
		
	}
	
	private void doTestAction(long structureId, long wayId, long nodeId) {
		EditAction action = new EditAction(termiteData,"Test Action");
		EditInstruction instr;
		
		String zlevel = "0";
		String zcontext = String.valueOf(structureId);
		
		TermiteNode testNode = termiteData.getNode(nodeId);
		OsmNode osmNode = testNode.getOsmObject();
		double x = osmNode.getX();
		double y = osmNode.getY();
		UpdatePosition targetData = new UpdatePosition(x + 100,y+50);
		instr = new UpdateInstruction(osmNode,targetData);
		action.addInstruction(instr);
		
		OsmNode oNode1 = new OsmNode();
		oNode1.setPosition(x, y-50);
		oNode1.setProperty(OsmModel.KEY_ZLEVEL,zlevel);
		oNode1.setProperty(OsmModel.KEY_ZCONTEXT,zcontext);
		instr = new CreateInstruction(oNode1,termiteData);
		long id1 = oNode1.getId();
		action.addInstruction(instr);
		
		OsmNode oNode2 = new OsmNode();
		oNode2.setPosition(x-20, y-10);
		oNode2.setProperty(OsmModel.KEY_ZLEVEL,zlevel);
		oNode2.setProperty(OsmModel.KEY_ZCONTEXT,zcontext);
		instr = new CreateInstruction(oNode2,termiteData);
		long id2 = oNode2.getId();
		action.addInstruction(instr);
		
		OsmNode oNode3 = new OsmNode();
		oNode3.setPosition(x+20, y-10);
		oNode3.setProperty(OsmModel.KEY_ZLEVEL,zlevel);
		oNode3.setProperty(OsmModel.KEY_ZCONTEXT,zcontext);
		instr = new CreateInstruction(oNode3,termiteData);
		long id3 = oNode3.getId();
		action.addInstruction(instr);
		
		TermiteWay testWay = termiteData.getWay(wayId);
		OsmWay osmWay = testWay.getOsmObject();
		UpdateObjectProperty targetData2 = new UpdateObjectProperty(termiteData,"buildingpart","buildingpart","room");
		instr = new UpdateInstruction(osmWay,targetData2);
		action.addInstruction(instr);
		
		OsmWay way = new OsmWay();
		List<Long> wayNodes = way.getNodeIds();
		wayNodes.add(id1);
		wayNodes.add(id2);
		wayNodes.add(id3);
		wayNodes.add(id1);
		way.setProperty("buildingpart","wall");
		instr = new CreateInstruction(way,termiteData);
		long id4 = way.getId();
		action.addInstruction(instr);
		
		OsmRelation relation = new OsmRelation();
		List<OsmMember> members = relation.getMembers();
		OsmMember member = new OsmMember(wayId,"way","outer");
		members.add(member);
		member = new OsmMember(id4,"way","inner");
		members.add(member);
		relation.setProperty("type","multipolygon");
		instr = new CreateInstruction(relation,termiteData);
		action.addInstruction(instr);
		
		try {
			action.doAction();
			gui.getMap().repaint();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

}
