package intransix.osm.termite.map.model;

import intransix.osm.termite.map.feature.*;
import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.util.JsonIO;
import intransix.osm.termite.util.LocalCoordinates;
import intransix.osm.termite.util.MercatorCoordinates;
import java.util.*;
import org.json.JSONObject;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * This class holds several unit tests for editing nodes, ways and relations. The
 * tests are daisy chained because the same objects are used several times to 
 * test different edit operations, starting with creating the objects and ending 
 * with deleting them.
 * 
 * @author sutter
 */
public class EditTest {
		
	private OsmData baseOsmData;
	private OsmData osmData;
	private TermiteData termiteData;
	
	public EditTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		String featureInfoName = "featureInfo.json";
		String modelFileName = "model2.json";
		
		JSONObject featureInfoJson = JsonIO.readJsonFile(featureInfoName);
		FeatureInfoMap featureInfoMap = FeatureInfoMap.parse(featureInfoJson);
		
		JSONObject modelJson = JsonIO.readJsonFile(modelFileName);
		OsmModel.parse(modelJson);
		OsmModel.featureInfoMap = featureInfoMap;
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}
	
	@Before
	public void setUp() {
		//set local coordinates
		double baseLat = 40.376;
		double baseLon = -117.116;
		double mx = MercatorCoordinates.lonRadToMx(Math.toRadians(baseLon));
		double my = MercatorCoordinates.latRadToMy(Math.toRadians(baseLat));
		LocalCoordinates.setLocalAnchor(mx,my);
		
		baseOsmData = new OsmData();
		termiteData = new TermiteData();
		termiteData.loadData(baseOsmData);
		osmData = termiteData.getWorkingData();
		
		ObjectTestData.osmData = osmData;
		ObjectTestData.termiteData = termiteData;
	}
	
	@After
	public void tearDown() {
	}
	
	
	// TODO add test methods here.
	// The methods must be annotated with annotation @Test. For example:
	//
	@Test
	public void editTest() {
		
		//some working test data
		EditAction action;
		EditInstruction instr;
		
		int zlevel1 = 0;
		int zlevel2 = 1;
		long structureId = 1111;
		
		OsmNode osmNode;
		OsmWay osmWay;
		OsmRelation osmRelation;
		
		TermiteNode termiteNode;
		TermiteWay termiteWay;
		TermiteRelation termiteRelation;
		
		
		//////////////////////////////////////////////////////////////
		// Create Node
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(termiteData,"Test Create Node");
		
		NodeTestData n1Data = new NodeTestData();
		
		OsmNode oNode1 = new OsmNode();
		double x1 = 0;
		double y1 = 0;
		oNode1.setPosition(x1,y1);
		oNode1.setProperty(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel1));
		oNode1.setProperty(OsmModel.KEY_ZCONTEXT,String.valueOf(structureId));
		oNode1.setProperty("buildingpart","stairs");
		instr = new CreateInstruction(oNode1,termiteData);
		action.addInstruction(instr);
		
		n1Data.id = oNode1.getId();
		n1Data.x = x1;
		n1Data.y = y1;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		//test the node
		n1Data.props.put(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel1));
		n1Data.props.put(OsmModel.KEY_ZCONTEXT,String.valueOf(structureId));
		n1Data.props.put("buildingpart","stairs");
		
		n1Data.featureInfoName = "buildingpart:stairs";
		n1Data.zlevel = zlevel1;
		n1Data.structureId = structureId;
				
		n1Data.validate(); 
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.validateDeleted();
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.validate(); 
		
		//------------------------
		// Create Some more test nodes
		//------------------------
		
		action = new EditAction(termiteData,"Create more nodes");
		
		OsmNode oNode2 = new OsmNode();
		double x2 = 0;
		double y2 = 0;
		oNode2.setPosition(x2,y2);
		oNode2.setProperty(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel1));
		oNode2.setProperty(OsmModel.KEY_ZCONTEXT,String.valueOf(structureId));
		instr = new CreateInstruction(oNode2,termiteData);
		long idn2 = oNode2.getId();
		action.addInstruction(instr);
		
		OsmNode oNode3 = new OsmNode();
		double x3 = 0;
		double y3 = 0;
		oNode3.setPosition(x3,y3);
		oNode3.setProperty(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel1));
		oNode3.setProperty(OsmModel.KEY_ZCONTEXT,String.valueOf(structureId));
		instr = new CreateInstruction(oNode3,termiteData);
		long idn3 = oNode3.getId();
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		//////////////////////////////////////////////////////////////
		// Create Way
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(termiteData,"Test Create Way");
		
		WayTestData w1Data = new WayTestData();
		
		termiteNode = termiteData.getNode(n1Data.id);
		n1Data.minDataVersion = termiteNode.getDataVersion() + 1;
		
		OsmWay oWay1 = new OsmWay();
		List<Long> wayNodes = oWay1.getNodeIds();
		wayNodes.add(n1Data.id);
		wayNodes.add(idn2);
		wayNodes.add(idn3);
		wayNodes.add(n1Data.id);
		oWay1.setProperty("buildingpart","wall");
		instr = new CreateInstruction(oWay1,termiteData);
		w1Data.id = oWay1.getId();
		action.addInstruction(instr);
		
		n1Data.wayIds.add(w1Data.id);
		
		w1Data.nodeIds.add(n1Data.id);
		w1Data.nodeIds.add(idn2);
		w1Data.nodeIds.add(idn3);
		w1Data.nodeIds.add(n1Data.id);
		
		w1Data.props.put("buildingpart","wall");
		
		w1Data.featureInfoName = "buildingpart:wall";
		w1Data.structureId = structureId;
		w1Data.levelIds.add(zlevel1);
		
		w1Data.minDataVersion = 0;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.validate();
		w1Data.validate();
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.wayIds.remove(w1Data.id);
		
		n1Data.validate();
		w1Data.validateDeleted();
		
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.wayIds.add(w1Data.id);
		
		n1Data.validate();
		w1Data.validate();
		
		//------------------------
		// Create another way
		//------------------------
		
		action = new EditAction(termiteData,"Create another way");
		
		OsmNode oNode4 = new OsmNode();
		double x4 = 0;
		double y4 = 0;
		oNode4.setPosition(x4,y4);
		oNode4.setProperty(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel1));
		oNode4.setProperty(OsmModel.KEY_ZCONTEXT,String.valueOf(structureId));
		instr = new CreateInstruction(oNode4,termiteData);
		long idn4 = oNode4.getId();
		action.addInstruction(instr);
		
		OsmNode oNode5 = new OsmNode();
		double x5 = 0;
		double y5 = 0;
		oNode5.setPosition(x5,y5);
		oNode5.setProperty(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel1));
		oNode5.setProperty(OsmModel.KEY_ZCONTEXT,String.valueOf(structureId));
		instr = new CreateInstruction(oNode5,termiteData);
		long idn5 = oNode5.getId();
		action.addInstruction(instr);
		
		OsmNode oNode6 = new OsmNode();
		double x6 = 0;
		double y6 = 0;
		oNode6.setPosition(x6,y6);
		oNode6.setProperty(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel1));
		oNode6.setProperty(OsmModel.KEY_ZCONTEXT,String.valueOf(structureId));
		instr = new CreateInstruction(oNode6,termiteData);
		long idn6 = oNode6.getId();
		action.addInstruction(instr);
		
		OsmWay oWay2 = new OsmWay();
		List<Long> wayNodes2 = oWay2.getNodeIds();
		wayNodes2.add(idn4);
		wayNodes2.add(idn5);
		wayNodes2.add(idn6);
		wayNodes2.add(idn4);
		oWay2.setProperty("buildingpart","wall");
		instr = new CreateInstruction(oWay2,termiteData);
		long idw2 = oWay2.getId();
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		//////////////////////////////////////////////////////////////
		// Create Relation
		//////////////////////////////////////////////////////////////
		
		//---------------------
		//create a multipoly relation
		//---------------------
		
		action = new EditAction(termiteData,"Create a multipoly relation");
		
		RelationTestData r1Data = new RelationTestData();
		
		OsmRelation relation = new OsmRelation();
		List<OsmMember> members = relation.getMembers();
		members.add(new OsmMember(w1Data.id,"way","outer"));
		members.add(new OsmMember(idw2,"way","inner"));
		relation.setProperty(OsmModel.TAG_TYPE,"multipolygon");
		instr = new CreateInstruction(relation,termiteData);
		r1Data.id = relation.getId();
		action.addInstruction(instr);
		
		termiteWay = termiteData.getWay(w1Data.id);
		w1Data.minDataVersion = termiteWay.getDataVersion() + 1;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.members.add(new OsmMember(w1Data.id,"way","outer"));
		r1Data.members.add(new OsmMember(idw2,"way","inner"));
		
		r1Data.props.put(OsmModel.TAG_TYPE,OsmModel.TYPE_MULTIPOLYGON);
		
		w1Data.rels.add(r1Data.id);
		
		w1Data.validate();
		r1Data.validate();
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}	
		
		w1Data.rels.remove(r1Data.id);
				
		w1Data.validate();
		r1Data.validateDeleted();
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.rels.add(r1Data.id);
		
		w1Data.validate();
		r1Data.validate();
		
		//------------------
		// Create a generic relation
		//------------------
		
		action = new EditAction(termiteData,"Create a generic relation");
		
		RelationTestData r2Data = new RelationTestData();
		
		osmRelation = new OsmRelation();
		List<OsmMember> members2 = osmRelation.getMembers();
		members2.add(new OsmMember(w1Data.id,"way","aaa"));
		members2.add(new OsmMember(r1Data.id,"relation","bbb"));
		members2.add(new OsmMember(n1Data.id,"node","ccc"));
		osmRelation.setProperty(OsmModel.TAG_TYPE,"generic");
		osmRelation.setProperty("key","value");
		instr = new CreateInstruction(osmRelation,termiteData);
		r2Data.id = osmRelation.getId();
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r2Data.members.add(new OsmMember(w1Data.id,"way","aaa"));
		r2Data.members.add(new OsmMember(r1Data.id,"relation","bbb"));
		r2Data.members.add(new OsmMember(n1Data.id,"node","ccc"));
		
		r2Data.props.put(OsmModel.TAG_TYPE,"generic");
		r2Data.props.put("key","value");
		
		n1Data.rels.add(r2Data.id);
		w1Data.rels.add(r2Data.id);
		r1Data.rels.add(r2Data.id);
		
		r2Data.validate();
		n1Data.validate();
		r1Data.validate();
		w1Data.validate();
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.rels.remove(r2Data.id);
		w1Data.rels.remove(r2Data.id);
		r1Data.rels.remove(r2Data.id);
				
		r2Data.validateDeleted();
		n1Data.validate();
		r1Data.validate();
		w1Data.validate();
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.rels.add(r2Data.id);
		w1Data.rels.add(r2Data.id);
		r1Data.rels.add(r2Data.id);
		
		r2Data.validate();
		n1Data.validate();
		r1Data.validate();
		w1Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Edit Properties
		//////////////////////////////////////////////////////////////
		
		String initialKey;
		String finalKey;
		String initialValue;
		String finalValue;
		UpdateObjectProperty targetData;
		
		//=====================
		//change node level
		//=====================
				
		action = new EditAction(termiteData,"Test Edit Node Properties");
		
		termiteNode = termiteData.getNode(n1Data.id);
		osmNode = termiteNode.getOsmObject();
		termiteWay = termiteData.getWay(w1Data.id);
		osmWay = termiteWay.getOsmObject();
		
		initialKey = "zlevel";
		initialValue = "0";
		finalValue = "1";
		targetData = new UpdateObjectProperty(termiteData,initialKey,initialKey,finalValue);
		instr = new UpdateInstruction(osmNode,targetData);
		action.addInstruction(instr);
		
		n1Data.minDataVersion = termiteNode.getDataVersion() + 1;
	
		w1Data.minDataVersion = termiteWay.getDataVersion() + 1;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.props.put(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel2));
		n1Data.zlevel = zlevel2;
		w1Data.levelIds.add(zlevel2);
		
		n1Data.validate();
		w1Data.validate();
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.props.put(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel1));
		n1Data.zlevel = zlevel1;
		w1Data.levelIds.remove(zlevel2);
		
		n1Data.validate();
		w1Data.validate();
		
		//=====================
		//edit way type
		//=====================
		
		action = new EditAction(termiteData,"Test Edit Way Properties");
		
		osmWay = osmData.getOsmWay(w1Data.id);
		termiteWay = (TermiteWay)osmWay.getTermiteObject();
		osmRelation = osmData.getOsmRelation(r1Data.id);
		termiteRelation = termiteData.getRelation(r1Data.id);
		
		initialKey = "buildingpart";
		initialValue = "wall";
		finalValue = "unit";
		targetData = new UpdateObjectProperty(termiteData,initialKey,initialKey,finalValue);
		instr = new UpdateInstruction(osmWay,targetData);
		action.addInstruction(instr);
		
		w1Data.minDataVersion = termiteWay.getDataVersion() + 1;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.featureInfoName = "buildingpart:unit";
		w1Data.props.put(initialKey,finalValue);
		
		w1Data.validate();
		
		w1Data.minDataVersion = 0;
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.featureInfoName = "buildingpart:wall";
		w1Data.props.put(initialKey,initialValue);
		
		w1Data.validate();
		
		//=====================
		//edit generic multipoly properties, update key
		//=====================
		
		action = new EditAction(termiteData,"Test Edit Relation Properties");
		
		osmRelation = osmData.getOsmRelation(r2Data.id);
		
		initialKey = "key";
		finalKey = "keyPrime";
		initialValue = "value";
		targetData = new UpdateObjectProperty(termiteData,initialKey,finalKey,initialValue);
		instr = new UpdateInstruction(osmRelation,targetData);
		action.addInstruction(instr);
	
		r2Data.props.remove(initialKey);
		r2Data.props.put(finalKey,initialValue);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
	
		r2Data.validate();
	
		r2Data.props.remove(finalKey);
		r2Data.props.put(initialKey,initialValue);
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r2Data.validate();
		
		
		//////////////////////////////////////////////////////////////
		// Edit Node Location
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(termiteData,"Mobe a node");
		
		osmNode = osmData.getOsmNode(n1Data.id);
		termiteNode = termiteData.getNode(n1Data.id);
		termiteWay = termiteData.getWay(w1Data.id);
	
		double x1b = n1Data.x + 1;
		double y1b = n1Data.y + 1;
		UpdatePosition up = new UpdatePosition(x1b,y1b);
		instr = new UpdateInstruction(osmNode,up);
		action.addInstruction(instr);
		
		n1Data.x = x1b;
		n1Data.y = y1b;
		n1Data.minDataVersion = termiteNode.getDataVersion() + 1;
		w1Data.minDataVersion = termiteWay.getDataVersion() + 1;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.validate();
		w1Data.validate();
		
		//undo
		n1Data.x = x1;
		n1Data.y = y1;
		n1Data.minDataVersion = termiteNode.getDataVersion() + 1;
		w1Data.minDataVersion = termiteWay.getDataVersion() + 1;
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
				
		n1Data.validate();
		w1Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Remove Node from Way
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(termiteData,"Remove a node");
		
		osmWay = osmData.getOsmWay(w1Data.id);
		osmNode = osmData.getOsmNode(n1Data.id);
		termiteNode = termiteData.getNode(n1Data.id);
		termiteWay = termiteData.getWay(w1Data.id);
	
		UpdateRemoveNode urn = new UpdateRemoveNode(termiteData,0);
		instr = new UpdateInstruction(osmWay,urn);
		action.addInstruction(instr);
		
		w1Data.nodeIds.remove(0);
		//way is still on this node since it appeared twice
		n1Data.minDataVersion = termiteNode.getDataVersion();
		w1Data.minDataVersion = termiteWay.getDataVersion() + 1;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.validate();
		w1Data.validate();
		
		//undo
		w1Data.nodeIds.add(0,n1Data.id);
		n1Data.minDataVersion = termiteNode.getDataVersion();
		w1Data.minDataVersion = termiteWay.getDataVersion() + 1;
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
				
		n1Data.validate();
		w1Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Indert Node into Way
		//////////////////////////////////////////////////////////////
		
		//////////////////////////////////////////////////////////////
		// Remove Member from Relation
		//////////////////////////////////////////////////////////////
		
		//////////////////////////////////////////////////////////////
		// Insert Member into Relation
		//////////////////////////////////////////////////////////////
		
		//////////////////////////////////////////////////////////////
		// Move Members in Relation
		//////////////////////////////////////////////////////////////
		
		//=====================
		// Multipoly
		//=====================
		
		//=====================
		// Generic Relation
		//=====================
		
		//////////////////////////////////////////////////////////////
		// Edit Member Role
		//////////////////////////////////////////////////////////////
		
		//=====================
		// Generic Relation
		//=====================
		
		//////////////////////////////////////////////////////////////
		// Delete Objects
		//////////////////////////////////////////////////////////////
		
		//=====================
		// Generic Relation
		//=====================
		
		action = new EditAction(termiteData,"Delete a generic relation");
		
		osmRelation = osmData.getOsmRelation(r2Data.id);
	
		instr = new DeleteInstruction(osmRelation);
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r2Data.validateDeleted();
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
				
		r2Data.validate();
		
		//=====================
		// Multipoly
		//=====================

//MUST REMOVE RELATION THAT WE ADDED THIS TO!!!!
		
		action = new EditAction(termiteData,"Delete a multipoly relation");
		
		osmRelation = osmData.getOsmRelation(r1Data.id);
	
		instr = new DeleteInstruction(osmRelation);
		action.addInstruction(instr);
		
		w1Data.minDataVersion = termiteData.getWay(w1Data.id).getDataVersion() + 1;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.validateDeleted();
		w1Data.validate();
		
		w1Data.minDataVersion = termiteData.getWay(w1Data.id).getDataVersion() + 1;
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
				
		r1Data.validate();
		w1Data.validate();
		
		//=====================
		// Way
		//=====================
		
		action = new EditAction(termiteData,"Delete a way");
		
		osmWay = osmData.getOsmWay(w1Data.id);
	
		instr = new DeleteInstruction(osmWay);
		action.addInstruction(instr);
		
		n1Data.wayIds.remove(w1Data.id);
		r1Data.members.remove(0);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.validateDeleted();
		n1Data.validate();
		r1Data.validate();
		
		//undo 
		n1Data.wayIds.add(w1Data.id);
		r1Data.members.add(0,new OsmMember(w1Data.id,"way","outer"));
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
				
		w1Data.validate();
		n1Data.validate();
		r1Data.validate();
		
		//=====================
		// Node
		//=====================
		
		action = new EditAction(termiteData,"Delete a node");
		
		osmNode = osmData.getOsmNode(n1Data.id);
	
		instr = new DeleteInstruction(osmNode);
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.validateDeleted();
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
				
		n1Data.validate();
	}
	
	
}
