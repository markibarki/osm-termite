package intransix.osm.termite.map.model;

import intransix.osm.termite.map.feature.*;
import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.util.JsonIO;
import intransix.osm.termite.util.LocalCoordinates;
import intransix.osm.termite.util.MercatorCoordinates;
import java.util.*;
import org.json.JSONObject;
import org.junit.*;

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
		
		LevelTestData l1Data = new LevelTestData();
		l1Data.zlevel = zlevel1;
		l1Data.structureId = structureId;
		
		LevelTestData l2Data = new LevelTestData();
		l2Data.zlevel = zlevel2;
		l2Data.structureId = structureId;
		
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
		
		l1Data.nodeIds.add(n1Data.id);
		
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
		l1Data.validate();
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		l1Data.nodeIds.remove(n1Data.id);
		
		n1Data.validateDeleted();
		l1Data.validate();
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		l1Data.nodeIds.add(n1Data.id);
		
		n1Data.validate(); 
		l1Data.validate();
		
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
		
		l1Data.nodeIds.add(idn2);
		l1Data.nodeIds.add(idn3);
		
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
		
		l1Data.wayIds.add(w1Data.id);
		
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
		l1Data.validate();
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.wayIds.remove(w1Data.id);
		l1Data.wayIds.remove(w1Data.id);
		
		n1Data.validate();
		w1Data.validateDeleted();
		l1Data.validate();
		
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.wayIds.add(w1Data.id);
		l1Data.wayIds.add(w1Data.id);
		
		n1Data.validate();
		w1Data.validate();
		l1Data.validate();
		
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
		
		l1Data.nodeIds.add(idn4);
		l1Data.nodeIds.add(idn5);
		l1Data.nodeIds.add(idn6);
		l1Data.wayIds.add(idw2);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		l1Data.validate();
		
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
		l1Data.nodeIds.remove(n1Data.id);
		l2Data.nodeIds.add(n1Data.id);
		
		n1Data.validate();
		w1Data.validate();
		l1Data.validate();
		l2Data.validate();
		
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
		
		l1Data.nodeIds.add(n1Data.id);
		l2Data.nodeIds.remove(n1Data.id);
		
		n1Data.validate();
		w1Data.validate();
		l1Data.validate();
		l2Data.validate();
		
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
		l1Data.validate();
		
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
		l1Data.validate();
		
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
		l1Data.validate();
		
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
		l1Data.validate();
		
		//redo, so we can add
		
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
		l1Data.validate();
		
		//------------------------------
		//remove the other instance of this node
		//------------------------------
		
		action = new EditAction(termiteData,"Remove a node");
		
		UpdateRemoveNode urn2 = new UpdateRemoveNode(termiteData,2);
		instr = new UpdateInstruction(osmWay,urn2);
		action.addInstruction(instr);
		
		w1Data.nodeIds.remove(2);
		n1Data.wayIds.remove(w1Data.id);
		
		//wnow the node should be updated
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
		
		w1Data.validate();
		n1Data.validate();
		l1Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Indert Node into Way
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(termiteData,"Insert a node");
		
		osmWay = osmData.getOsmWay(w1Data.id);
		osmNode = osmData.getOsmNode(n1Data.id);
		termiteNode = termiteData.getNode(n1Data.id);
		termiteWay = termiteData.getWay(w1Data.id);
	
		UpdateInsertNode uin = new UpdateInsertNode(termiteData,n1Data.id,0);
		instr = new UpdateInstruction(osmWay,uin);
		action.addInstruction(instr);
		
		w1Data.nodeIds.add(0,n1Data.id);
		n1Data.wayIds.add(w1Data.id);
		//way is still on this node since it appeared twice
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
		l1Data.validate();
		
		//undo
		w1Data.nodeIds.remove(0);
		n1Data.wayIds.remove(w1Data.id);
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
		l1Data.validate();
		
		//redo
		w1Data.nodeIds.add(0,n1Data.id);
		n1Data.wayIds.add(w1Data.id);
		//way is still on this node since it appeared twice
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
		l1Data.validate();
		
		//---------------------
		//now add another at the end, like when we started
		//---------------------
		
		action = new EditAction(termiteData,"Insert a node");
		
		UpdateInsertNode uin2 = new UpdateInsertNode(termiteData,n1Data.id,3);
		instr = new UpdateInstruction(osmWay,uin2);
		action.addInstruction(instr);
		
		w1Data.nodeIds.add(3,n1Data.id);
		
		//node not updated here
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
		
		w1Data.validate();
		n1Data.validate();
		l1Data.validate();
		
		//-------------------------------------------
		//test adding a node from a different level
		//--------------------------------------------
		
		action = new EditAction(termiteData,"Create a node on a differnt level and insert");
		
		NodeTestData n7Data = new NodeTestData();
		
		OsmNode oNode7 = new OsmNode();
		double x7 = 0;
		double y7 = 0;
		oNode7.setPosition(x7,y7);
		oNode7.setProperty(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel2));
		oNode7.setProperty(OsmModel.KEY_ZCONTEXT,String.valueOf(structureId));
		oNode7.setProperty("buildingpart","stairs");
		instr = new CreateInstruction(oNode7,termiteData);
		action.addInstruction(instr);
		
		n7Data.id = oNode7.getId();
		n7Data.x = x7;
		n7Data.y = y7;
		n7Data.zlevel = zlevel2;
		n7Data.structureId = structureId;
		n7Data.minDataVersion = 0;
		n7Data.props.put("buildingpart","stairs");
		n7Data.props.put(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel2));
		n7Data.props.put(OsmModel.KEY_ZCONTEXT,String.valueOf(structureId));	
		n7Data.featureInfoName = "buildingpart:stairs";
		
		//add this to the way
		uin = new UpdateInsertNode(termiteData,oNode7.getId(),4);
		instr = new UpdateInstruction(osmWay,uin);
		action.addInstruction(instr);
		
		n7Data.wayIds.add(w1Data.id);
		
		w1Data.nodeIds.add(4,n7Data.id);
		w1Data.levelIds.add(zlevel2);
		w1Data.minDataVersion = termiteWay.getDataVersion() + 1;
		
		l2Data.nodeIds.add(n7Data.id);
		l2Data.wayIds.add(w1Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.validate();
		n7Data.validate();
		l1Data.validate();
		l2Data.validate();
		
		//-------------------------------------------
		//remove node from a different level
		//--------------------------------------------
		
		action = new EditAction(termiteData,"Remove a node");
		
		//add this to the way
		urn = new UpdateRemoveNode(termiteData,4);
		instr = new UpdateInstruction(osmWay,urn);
		action.addInstruction(instr);
		
		n7Data.wayIds.remove(w1Data.id);
		
		w1Data.nodeIds.remove(4);
		w1Data.levelIds.remove((Integer)zlevel2);
		
		termiteNode = termiteData.getNode(n7Data.id);
		termiteWay = termiteData.getWay(w1Data.id);
		
		n7Data.minDataVersion = termiteNode.getDataVersion() + 1;
		w1Data.minDataVersion = termiteWay.getDataVersion() + 1;
		
		l2Data.wayIds.remove(w1Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.validate();
		n7Data.validate();
		l1Data.validate();
		l2Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Remove Member from Relation
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(termiteData,"Remove a member");
		
		termiteRelation = termiteData.getRelation(r1Data.id);
		osmRelation = termiteRelation.getOsmObject();
		
		UpdateRemoveMember urm = new UpdateRemoveMember(termiteData,0);
		instr = new UpdateInstruction(osmRelation,urm);
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.members.remove(0);
		w1Data.rels.remove(r1Data.id);
		
		r1Data.validate();
		w1Data.validate();
		
		//undo
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.members.add(0,new OsmMember(w1Data.id,"way","outer"));
		w1Data.rels.add(r1Data.id);
		
		r1Data.validate();
		w1Data.validate();
		
		//redo
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.members.remove(0);
		w1Data.rels.remove(r1Data.id);
		
		r1Data.validate();
		w1Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Insert Member into Relation
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(termiteData,"Insert a member");
		
		termiteRelation = termiteData.getRelation(r1Data.id);
		osmRelation = termiteRelation.getOsmObject();
		
		OsmMember osmMember = new OsmMember(w1Data.id,"way","outer");
		UpdateInsertMember uim = new UpdateInsertMember(termiteData,osmMember,0);
		instr = new UpdateInstruction(osmRelation,uim);
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.members.add(0,new OsmMember(w1Data.id,"way","outer"));
		w1Data.rels.add(r1Data.id);
		
		r1Data.validate();
		w1Data.validate();
		
		//undo
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.members.remove(0);
		w1Data.rels.remove(r1Data.id);
		
		r1Data.validate();
		w1Data.validate();
		
		//redo
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.members.add(0,new OsmMember(w1Data.id,"way","outer"));
		w1Data.rels.add(r1Data.id);
		
		r1Data.validate();
		w1Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Move Members in Relation
		//////////////////////////////////////////////////////////////
		
		//---------------
		// move up
		//---------------
		
		action = new EditAction(termiteData,"Move member");
		
		termiteRelation = termiteData.getRelation(r2Data.id);
		osmRelation = termiteRelation.getOsmObject();
		
		UpdateMemberOrder umo = new UpdateMemberOrder(1,true);
		instr = new UpdateInstruction(osmRelation,umo);
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		osmMember = r2Data.members.remove(1);
		r2Data.members.add(0,osmMember);
		
		r2Data.validate();
		
		//undo
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		osmMember = r2Data.members.remove(0);
		r2Data.members.add(1,osmMember);
		
		r2Data.validate();
		
		//-------------
		//move down
		//-------------
		
		action = new EditAction(termiteData,"Move member");
		
		termiteRelation = termiteData.getRelation(r2Data.id);
		osmRelation = termiteRelation.getOsmObject();
		
		umo = new UpdateMemberOrder(1,false);
		instr = new UpdateInstruction(osmRelation,umo);
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		osmMember = r2Data.members.remove(1);
		r2Data.members.add(2,osmMember);
		
		r2Data.validate();
		
		//undo
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		osmMember = r2Data.members.remove(2);
		r2Data.members.add(1,osmMember);
		
		r2Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Edit Member Role
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(termiteData,"Edit member role");
		
		termiteRelation = termiteData.getRelation(r2Data.id);
		osmRelation = termiteRelation.getOsmObject();
		
		UpdateRole ur = new UpdateRole("xyz",1);
		instr = new UpdateInstruction(osmRelation,ur);
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		osmMember = r2Data.members.get(1);
		String originalRole = osmMember.role;
		osmMember.role = "xyz";
		
		r2Data.validate();
		
		//undo
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		osmMember = r2Data.members.get(1);
		osmMember.role = originalRole;
		
		r2Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Delete Objects
		//////////////////////////////////////////////////////////////
		
		//=====================
		// Relation
		//=====================
		
		action = new EditAction(termiteData,"Delete a relation");
		
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
		
		w1Data.rels.remove(r2Data.id);
		n1Data.rels.remove(r2Data.id);
		r1Data.rels.remove(r2Data.id);
		
		r2Data.validateDeleted();
		w1Data.validate();
		n1Data.validate();
		r1Data.validate();
		l1Data.validate();
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.rels.add(r2Data.id);
		n1Data.rels.add(r2Data.id);
		r1Data.rels.add(r2Data.id);
				
		r2Data.validate();
		w1Data.validate();
		n1Data.validate();
		r1Data.validate();
		l1Data.validate();
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.rels.remove(r2Data.id);
		n1Data.rels.remove(r2Data.id);
		r1Data.rels.remove(r2Data.id);
		
		r2Data.validateDeleted();
		w1Data.validate();
		n1Data.validate();
		r1Data.validate();
		l1Data.validate();
		
		
		
		//=====================
		// Way
		//=====================
		
		//---------------------
		// Failed delete attempt
		//---------------------
		
		action = new EditAction(termiteData,"Delete a way");
		
		osmWay = osmData.getOsmWay(w1Data.id);
	
		instr = new DeleteInstruction(osmWay);
		action.addInstruction(instr);
		
		//this should fail because there is a relation holding this way
		try {
			boolean success = action.doAction();
			assert(!success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.validate();
		n1Data.validate();
		r1Data.validate();
		l1Data.validate();
		l2Data.validate();
		
		//----------------------
		// Empty relations
		//----------------------
		
		action = new EditAction(termiteData,"Remove ways from relation 1");
		
		osmWay = osmData.getOsmWay(w1Data.id);
		osmRelation = osmData.getOsmRelation(r1Data.id);
		
		instr = new UpdateInstruction(osmRelation,new UpdateRemoveMember(termiteData,0));
		action.addInstruction(instr);
		
		instr = new UpdateInstruction(osmRelation,new UpdateRemoveMember(termiteData,0));
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.rels.remove(r1Data.id);
		r1Data.members.clear();
				
		w1Data.validate();
		n1Data.validate();
		r1Data.validate();
		l1Data.validate();
		l2Data.validate();
		
		//---------------------
		// successful delete delete attempt
		//---------------------
		
		action = new EditAction(termiteData,"Delete a way");
		
		osmWay = osmData.getOsmWay(w1Data.id);
	
		instr = new DeleteInstruction(osmWay);
		action.addInstruction(instr);
		
		//this should fail because there is a relation holding this way
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.wayIds.remove(w1Data.id);
		l1Data.wayIds.remove(w1Data.id);
		
		w1Data.validateDeleted();
		n1Data.validate();
		r1Data.validate();
		l1Data.validate();
		l2Data.validate();
		
		//undo
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.wayIds.add(w1Data.id);
		l1Data.wayIds.add(w1Data.id);
		
		w1Data.validate();
		n1Data.validate();
		r1Data.validate();
		l1Data.validate();
		l2Data.validate();
		
		//=====================
		// Node
		//=====================
		
		//------------------------
		//failed delete attempt
		//-----------------------
		
		action = new EditAction(termiteData,"Delete a node");
		
		osmNode = osmData.getOsmNode(n1Data.id);
	
		instr = new DeleteInstruction(osmNode);
		action.addInstruction(instr);
		
		//this should fail because there is a way holding this node
		try {
			boolean success = action.doAction();
			assert(!success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.validate();
		n1Data.validate();
		r1Data.validate();
		l1Data.validate();
		l2Data.validate();
		
		//-----------------------
		//remove node from way
		//-----------------------
		
		action = new EditAction(termiteData,"Remove nodes from a way");
		
		osmWay = osmData.getOsmWay(w1Data.id);
	
		instr = new UpdateInstruction(osmWay, new UpdateRemoveNode(termiteData,3));
		action.addInstruction(instr);
		
		instr = new UpdateInstruction(osmWay, new UpdateRemoveNode(termiteData,0));
		action.addInstruction(instr);
		
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.wayIds.remove(w1Data.id);
		w1Data.nodeIds.remove(n1Data.id);
		w1Data.nodeIds.remove(n1Data.id);
		
		n1Data.validate();
		w1Data.validate();
		
		//-------------------------
		//successful delete attempt
		//-------------------------
		
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
		
		l1Data.nodeIds.remove(n1Data.id);
		
		n1Data.validateDeleted();
		w1Data.validate();
		r1Data.validate();
		l1Data.validate();
		l2Data.validate();
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		l1Data.nodeIds.add(n1Data.id);
				
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		l1Data.validate();
		l2Data.validate();
		
	}
	
	
}
