package intransix.osm.termite.map.model;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.mapdata.instruction.UpdatePosition;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.app.mapdata.instruction.DeleteInstruction;
import intransix.osm.termite.map.dataset.*;
import intransix.osm.termite.app.mapdata.instruction.EditAction;
import intransix.osm.termite.app.mapdata.instruction.UpdateInsertMember;
import intransix.osm.termite.app.mapdata.instruction.EditInstruction;
import intransix.osm.termite.app.mapdata.instruction.UpdateRemoveNode;
import intransix.osm.termite.app.mapdata.instruction.UpdateInstruction;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.app.mapdata.instruction.UpdateObjectProperty;
import intransix.osm.termite.app.mapdata.instruction.UpdateInsertNode;
import intransix.osm.termite.app.mapdata.instruction.UpdateMemberOrder;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.app.mapdata.instruction.CreateInstruction;
import intransix.osm.termite.app.mapdata.instruction.UpdateRemoveMember;
import intransix.osm.termite.app.mapdata.instruction.UpdateRole;
import intransix.osm.termite.map.feature.*;
import intransix.osm.termite.util.JsonIO;
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
	
	private MapDataManager mapDataManager;	
	private OsmData osmData;
	private OsmDataSet dataSet;
	
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
		
		dataSet = new OsmDataSet();
		mapDataManager = new MapDataManager();
		mapDataManager.setData(dataSet);
		osmData = mapDataManager.getOsmData();
		
		ObjectTestData.osmData = osmData;
		ObjectTestData.mapDataManager = mapDataManager;
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
		
		OsmNode osmNode;
		OsmWay osmWay;
		OsmRelation osmRelation;
		
		
		//////////////////////////////////////////////////////////////
		// Create Node
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(mapDataManager,"Test Create Node");
		
		NodeTestData n1Data = new NodeTestData();
		
		OsmNodeSrc oNode1 = new OsmNodeSrc();
		double x1 = 0;
		double y1 = 0;
		oNode1.setPosition(x1,y1);
		oNode1.putProperty("test","xxx");
		oNode1.putProperty("buildingpart","stairs");
		instr = new CreateInstruction(oNode1,mapDataManager);
		action.addInstruction(instr);
		
		n1Data.id = oNode1.getId();
		n1Data.x = x1;
		n1Data.y = y1;
		n1Data.props.put("test","xxx");
		n1Data.props.put("buildingpart","stairs");
		n1Data.featureInfoName = "buildingpart:stairs";
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		n1Data.validate(); 
		
		//undo
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.validateDeleted();
		
		//redo
	
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		n1Data.validate(); 
		
		//------------------------
		// Create Some more test nodes
		//------------------------
		
		action = new EditAction(mapDataManager,"Create more nodes");
		
		OsmNodeSrc oNode2 = new OsmNodeSrc();
		double x2 = 0;
		double y2 = 0;
		oNode2.setPosition(x2,y2);
		instr = new CreateInstruction(oNode2,mapDataManager);
		long idn2 = oNode2.getId();
		action.addInstruction(instr);
		
		OsmNodeSrc oNode3 = new OsmNodeSrc();
		double x3 = 0;
		double y3 = 0;
		oNode3.setPosition(x3,y3);
		instr = new CreateInstruction(oNode3,mapDataManager);
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
		
		action = new EditAction(mapDataManager,"Test Create Way");
		
		WayTestData w1Data = new WayTestData();
		
		OsmWaySrc oWay1 = new OsmWaySrc();
		List<Long> wayNodes = oWay1.getNodeIds();
		wayNodes.add(n1Data.id);
		wayNodes.add(idn2);
		wayNodes.add(idn3);
		wayNodes.add(n1Data.id);
		oWay1.putProperty("buildingpart","wall");
		instr = new CreateInstruction(oWay1,mapDataManager);
		w1Data.id = oWay1.getId();
		action.addInstruction(instr);
		
		n1Data.wayIds.add(w1Data.id);
		
		w1Data.nodeIds.add(n1Data.id);
		w1Data.nodeIds.add(idn2);
		w1Data.nodeIds.add(idn3);
		w1Data.nodeIds.add(n1Data.id);	
		w1Data.props.put("buildingpart","wall");
		w1Data.featureInfoName = "buildingpart:wall";
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
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
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		
		//------------------------
		// Create another way
		//------------------------
		
		action = new EditAction(mapDataManager,"Create another way");
		
		OsmNodeSrc oNode4 = new OsmNodeSrc();
		double x4 = 0;
		double y4 = 0;
		oNode4.setPosition(x4,y4);
		instr = new CreateInstruction(oNode4,mapDataManager);
		long idn4 = oNode4.getId();
		action.addInstruction(instr);
		
		OsmNodeSrc oNode5 = new OsmNodeSrc();
		double x5 = 0;
		double y5 = 0;
		oNode5.setPosition(x5,y5);
		instr = new CreateInstruction(oNode5,mapDataManager);
		long idn5 = oNode5.getId();
		action.addInstruction(instr);
		
		OsmNodeSrc oNode6 = new OsmNodeSrc();
		double x6 = 0;
		double y6 = 0;
		oNode6.setPosition(x6,y6);
		instr = new CreateInstruction(oNode6,mapDataManager);
		long idn6 = oNode6.getId();
		action.addInstruction(instr);
		
		OsmWaySrc oWay2 = new OsmWaySrc();
		List<Long> wayNodes2 = oWay2.getNodeIds();
		wayNodes2.add(idn4);
		wayNodes2.add(idn5);
		wayNodes2.add(idn6);
		wayNodes2.add(idn4);
		oWay2.putProperty("buildingpart","wall");
		instr = new CreateInstruction(oWay2,mapDataManager);
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
		
		action = new EditAction(mapDataManager,"Create a multipoly relation");
		
		RelationTestData r1Data = new RelationTestData();
		
		OsmRelationSrc relationSrc = new OsmRelationSrc();
		relationSrc.addMember(w1Data.id,"way","outer");
		relationSrc.addMember(idw2,"way","inner");
		relationSrc.putProperty(OsmModel.KEY_TYPE,"multipolygon");
		instr = new CreateInstruction(relationSrc,mapDataManager);
		r1Data.id = relationSrc.getId();
		action.addInstruction(instr);
		
		r1Data.addMember(w1Data.id,"way","outer");
		r1Data.addMember(idw2,"way","inner");
		r1Data.props.put(OsmModel.KEY_TYPE,OsmModel.TYPE_MULTIPOLYGON);
		r1Data.relationType = "multipolygon";
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.rels.add(r1Data.id);
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
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
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		w1Data.validate();
		r1Data.validate();
		
		//------------------
		// Create a generic relation
		//------------------
		
		action = new EditAction(mapDataManager,"Create a generic relation");
		
		RelationTestData r2Data = new RelationTestData();
		
		relationSrc = new OsmRelationSrc();
		relationSrc.addMember(w1Data.id,"way","aaa");
		relationSrc.addMember(r1Data.id,"relation","bbb");
		relationSrc.addMember(n1Data.id,"node","ccc");
		relationSrc.putProperty(OsmModel.KEY_TYPE,"generic");
		relationSrc.putProperty("key","value");
		instr = new CreateInstruction(relationSrc,mapDataManager);
		r2Data.id = relationSrc.getId();
		action.addInstruction(instr);
		
		r2Data.addMember(w1Data.id,"way","aaa");
		r2Data.addMember(r1Data.id,"relation","bbb");
		r2Data.addMember(n1Data.id,"node","ccc");
		r2Data.props.put(OsmModel.KEY_TYPE,"generic");
		r2Data.relationType = "generic";
		r2Data.props.put("key","value");
				
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
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
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
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
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
				
		action = new EditAction(mapDataManager,"Test Edit Node Properties");
		

		osmNode = osmData.getOsmNode(n1Data.id);
		
		initialKey = "test";
		initialValue = "xxx";
		finalValue = "yyy";
		targetData = new UpdateObjectProperty(initialKey,initialKey,finalValue);
		instr = new UpdateInstruction(osmNode,targetData);
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.props.put(initialKey,finalValue);
		n1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.props.put(initialKey,initialValue);
		n1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//=====================
		//edit way type
		//=====================
		
		action = new EditAction(mapDataManager,"Test Edit Way Properties");
		
		osmWay = osmData.getOsmWay(w1Data.id);
		
		initialKey = "buildingpart";
		initialValue = "wall";
		finalValue = "unit";
		targetData = new UpdateObjectProperty(initialKey,initialKey,finalValue);
		instr = new UpdateInstruction(osmWay,targetData);
		action.addInstruction(instr);
		
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
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
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
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//=====================
		//edit generic multipoly properties, update key
		//=====================
		
		action = new EditAction(mapDataManager,"Test Edit Relation Properties");
		
		osmRelation = osmData.getOsmRelation(r2Data.id);
		
		initialKey = "key";
		finalKey = "keyPrime";
		initialValue = "value";
		targetData = new UpdateObjectProperty(initialKey,finalKey,initialValue);
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
		
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
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
		
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		
		//////////////////////////////////////////////////////////////
		// Edit Node Location
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(mapDataManager,"Move a node");
		
		osmNode = osmData.getOsmNode(n1Data.id);
	
		double x1b = n1Data.x + 1;
		double y1b = n1Data.y + 1;
		UpdatePosition up = new UpdatePosition(x1b,y1b);
		instr = new UpdateInstruction(osmNode,up);
		action.addInstruction(instr);
		
		n1Data.x = x1b;
		n1Data.y = y1b;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//undo
		n1Data.x = x1;
		n1Data.y = y1;
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
				
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Remove Node from Way
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(mapDataManager,"Remove a node");
		
		osmWay = osmData.getOsmWay(w1Data.id);
	
		UpdateRemoveNode urn = new UpdateRemoveNode(0);
		instr = new UpdateInstruction(osmWay,urn);
		action.addInstruction(instr);
		
		w1Data.nodeIds.remove(0);
		//node still contains way because it was in twice
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//undo
		w1Data.nodeIds.add(0,n1Data.id);
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
				
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//redo, so we can add
		
		w1Data.nodeIds.remove(0);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//------------------------------
		//remove the other instance of this node
		//------------------------------
		
		action = new EditAction(mapDataManager,"Remove a node");
		
		UpdateRemoveNode urn2 = new UpdateRemoveNode(2);
		instr = new UpdateInstruction(osmWay,urn2);
		action.addInstruction(instr);
		
		w1Data.nodeIds.remove(2);
		n1Data.wayIds.remove(w1Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Indert Node into Way
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(mapDataManager,"Insert a node");
		
		osmWay = osmData.getOsmWay(w1Data.id);
	
		UpdateInsertNode uin = new UpdateInsertNode(n1Data.id,0);
		instr = new UpdateInstruction(osmWay,uin);
		action.addInstruction(instr);
		
		w1Data.nodeIds.add(0,n1Data.id);
		n1Data.wayIds.add(w1Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//undo
		w1Data.nodeIds.remove(0);
		n1Data.wayIds.remove(w1Data.id);
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
				
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//redo
		w1Data.nodeIds.add(0,n1Data.id);
		n1Data.wayIds.add(w1Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//---------------------
		//now add another at the end, like when we started
		//---------------------
		
		action = new EditAction(mapDataManager,"Insert a node");
		
		UpdateInsertNode uin2 = new UpdateInsertNode(n1Data.id,3);
		instr = new UpdateInstruction(osmWay,uin2);
		action.addInstruction(instr);
		
		w1Data.nodeIds.add(3,n1Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
	
		
		//////////////////////////////////////////////////////////////
		// Remove Member from Relation
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(mapDataManager,"Remove a member");
		
		osmRelation = osmData.getOsmRelation(r1Data.id);
		
		UpdateRemoveMember urm = new UpdateRemoveMember(0);
		instr = new UpdateInstruction(osmRelation,urm);
		action.addInstruction(instr);
		
		r1Data.members.remove(0);
		w1Data.rels.remove(r1Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//undo
		r1Data.addMember(w1Data.id,"way","outer",0);
		w1Data.rels.add(r1Data.id);
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//redo
		
		r1Data.members.remove(0);
		w1Data.rels.remove(r1Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Insert Member into Relation
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(mapDataManager,"Insert a member");
		
		osmRelation = osmData.getOsmRelation(r1Data.id);

		UpdateInsertMember uim = new UpdateInsertMember(w1Data.id,"way","outer",0);
		instr = new UpdateInstruction(osmRelation,uim);
		action.addInstruction(instr);
		
		r1Data.addMember(w1Data.id,"way","outer",0);
		w1Data.rels.add(r1Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//undo
		r1Data.members.remove(0);
		w1Data.rels.remove(r1Data.id);
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//redo
		r1Data.addMember(w1Data.id,"way","outer",0);
		w1Data.rels.add(r1Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Move Members in Relation
		//////////////////////////////////////////////////////////////
		
		//---------------
		// move legally
		//---------------
		
		action = new EditAction(mapDataManager,"Move member");
		
		osmRelation = osmData.getOsmRelation(r2Data.id);
		
		UpdateMemberOrder umo = new UpdateMemberOrder(1,0);
		instr = new UpdateInstruction(osmRelation,umo);
		action.addInstruction(instr);
		
		r2Data.members.add(0,r2Data.members.remove(1));
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//undo
		r2Data.members.add(1,r2Data.members.remove(0));
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//-------------
		//move illegally
		//-------------
		
		action = new EditAction(mapDataManager,"Move member");
		
		osmRelation = osmData.getOsmRelation(r2Data.id);
		
		umo = new UpdateMemberOrder(1,25);
		instr = new UpdateInstruction(osmRelation,umo);
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(!success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Edit Member Role
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(mapDataManager,"Edit member role");
		
		osmRelation = osmData.getOsmRelation(r2Data.id);
		
		UpdateRole ur = new UpdateRole("xyz",1);
		instr = new UpdateInstruction(osmRelation,ur);
		action.addInstruction(instr);
		
		OsmRelationSrc.Member m;
		String finalRole = "xyz";
		
		m = r2Data.members.get(1);
		String originalRole = m.role;
		m.role = finalRole;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//undo
		
		m = r2Data.members.get(1);
		m.role = originalRole;
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//////////////////////////////////////////////////////////////
		// Delete Objects
		//////////////////////////////////////////////////////////////
		
		//=====================
		// Relation
		//=====================
		
		action = new EditAction(mapDataManager,"Delete a relation");
		
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
		
		//undo
		w1Data.rels.add(r2Data.id);
		n1Data.rels.add(r2Data.id);
		r1Data.rels.add(r2Data.id);
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r2Data.dataVersion = mapDataManager.test_getLatestEditNumber();
				
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		r2Data.validate();
		
		//redo
		w1Data.rels.remove(r2Data.id);
		n1Data.rels.remove(r2Data.id);
		r1Data.rels.remove(r2Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r2Data.validateDeleted();
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		
		//=====================
		// Way
		//=====================
		
		//---------------------
		// Failed delete attempt
		//---------------------
		
		action = new EditAction(mapDataManager,"Delete a way");
		
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
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		
		//----------------------
		// Empty relation
		//----------------------
		
		action = new EditAction(mapDataManager,"Remove ways from relation 1");
		
		osmRelation = osmData.getOsmRelation(r1Data.id);
		
		instr = new UpdateInstruction(osmRelation,new UpdateRemoveMember(0));
		action.addInstruction(instr);
		
		instr = new UpdateInstruction(osmRelation,new UpdateRemoveMember(0));
		action.addInstruction(instr);
		
		w1Data.rels.remove(r1Data.id);
		r1Data.members.clear();
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		r1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
				
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		
		//---------------------
		// successful delete delete attempt
		//---------------------
		
		action = new EditAction(mapDataManager,"Delete a way");
		
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
		
		w1Data.validateDeleted();
		n1Data.validate();
		r1Data.validate();
		
		//undo
		n1Data.wayIds.add(w1Data.id);
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		
		//=====================
		// Node
		//=====================
		
		//------------------------
		//failed delete attempt
		//-----------------------
		
		action = new EditAction(mapDataManager,"Delete a node");
		
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
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		
		//-----------------------
		//remove node from way
		//-----------------------
		
		action = new EditAction(mapDataManager,"Remove nodes from a way");
		
		osmWay = osmData.getOsmWay(w1Data.id);
	
		instr = new UpdateInstruction(osmWay, new UpdateRemoveNode(3));
		action.addInstruction(instr);
		
		instr = new UpdateInstruction(osmWay, new UpdateRemoveNode(0));
		action.addInstruction(instr);
		
		n1Data.wayIds.remove(w1Data.id);
		w1Data.nodeIds.remove(n1Data.id);
		w1Data.nodeIds.remove(n1Data.id);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
		
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		
		//-------------------------
		//successful delete attempt
		//-------------------------
		
		action = new EditAction(mapDataManager,"Delete a node");
		
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
		w1Data.validate();
		r1Data.validate();
		
		 //undo
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1Data.dataVersion = mapDataManager.test_getLatestEditNumber();
				
		n1Data.validate();
		w1Data.validate();
		r1Data.validate();
		
	}
	
	
}
