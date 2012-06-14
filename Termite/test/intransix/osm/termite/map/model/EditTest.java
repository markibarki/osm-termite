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
		
		//////////////////////////////////////////////////////////////
		// Create Node
		//////////////////////////////////////////////////////////////
		
		action = new EditAction(termiteData,"Test Create Node");
		
		OsmNode oNode1 = new OsmNode();
		double x1 = 0;
		double y1 = 0;
		oNode1.setPosition(x1,y1);
		oNode1.setProperty(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel1));
		oNode1.setProperty(OsmModel.KEY_ZCONTEXT,String.valueOf(structureId));
		oNode1.setProperty("buildingpart","stairs");
		instr = new CreateInstruction(oNode1,termiteData);
		long idn1 = oNode1.getId();
		action.addInstruction(instr);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		//test node
		List<Long> n1WayIds = new ArrayList<Long>();
		HashMap<String,String> n1Props = new HashMap<String,String>();
		n1Props.put(OsmModel.KEY_ZLEVEL,String.valueOf(zlevel1));
		n1Props.put(OsmModel.KEY_ZCONTEXT,String.valueOf(structureId));
		n1Props.put("buildingpart","stairs");
		
		String n1FeatureInfoName = "buildingpart:stairs";
		
		validateNode(idn1,x1,y1,structureId,zlevel1,n1WayIds,n1Props,n1FeatureInfoName,0,0); 
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		validateNodeDelete(idn1,structureId,zlevel1,n1WayIds);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		validateNode(idn1,x1,y1,structureId,zlevel1,n1WayIds,n1Props,n1FeatureInfoName,0,0); 
		
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
		
		TermiteNode tNode1 = termiteData.getNode(idn1);
		int minN1TermiteVersion = tNode1.getTermiteLocalVersion() + 1;
		
		OsmWay oWay1 = new OsmWay();
		List<Long> wayNodes = oWay1.getNodeIds();
		wayNodes.add(idn1);
		wayNodes.add(idn2);
		wayNodes.add(idn3);
		wayNodes.add(idn1);
		oWay1.setProperty("buildingpart","wall");
		instr = new CreateInstruction(oWay1,termiteData);
		long idw1 = oWay1.getId();
		action.addInstruction(instr);
		
		n1WayIds.add(idw1);
		
		List<Long> w1NodeIds = new ArrayList<Long>();
		w1NodeIds.add(idn1);
		w1NodeIds.add(idn2);
		w1NodeIds.add(idn3);
		w1NodeIds.add(idn1);
		HashMap<String,String> w1Props = new HashMap<String,String>();
		w1Props.put("buildingpart","wall");
		
		String w1FeatureInfoName = "buildingpart:wall";
		
		List<Integer> w1Levels = new ArrayList<Integer>();
		w1Levels.add(zlevel1);
		
		long w1MultiPolyId = OsmObject.INVALID_ID;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		validateNode(idn1,x1,y1,structureId,zlevel1,n1WayIds,n1Props,n1FeatureInfoName,0,minN1TermiteVersion);
		validateWay(idw1,w1NodeIds,w1Levels,w1Props,w1MultiPolyId,w1FeatureInfoName,0,0);
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1WayIds.remove(idw1);
		
		validateNode(idn1,x1,y1,structureId,zlevel1,n1WayIds,n1Props,n1FeatureInfoName,0,minN1TermiteVersion);
		validateWayDeleted(idw1,w1NodeIds,structureId,w1Levels,w1MultiPolyId);
		
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		n1WayIds.add(idw1);
		
		validateNode(idn1,x1,y1,structureId,zlevel1,n1WayIds,n1Props,n1FeatureInfoName,0,minN1TermiteVersion);
		validateWay(idw1,w1NodeIds,w1Levels,w1Props,w1MultiPolyId,w1FeatureInfoName,0,0);
		
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
		
		OsmRelation relation = new OsmRelation();
		List<OsmMember> members = relation.getMembers();
		OsmMember member = new OsmMember(idw1,"way","outer");
		members.add(member);
		member = new OsmMember(idw2,"way","inner");
		members.add(member);
		relation.setProperty("type","multipolygon");
		instr = new CreateInstruction(relation,termiteData);
		long idr1 = relation.getId();
		action.addInstruction(instr);
		
		TermiteWay tWay1 = termiteData.getWay(idw1);
		int minW1TermiteVersion = tWay1.getTermiteLocalVersion() + 1;
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1MultiPolyId = idr1;
		List<OsmMember> r1Members = new ArrayList<OsmMember>();
		r1Members.add(new OsmMember(idw1,"way","outer"));
		r1Members.add(new OsmMember(idw2,"way","inner"));
		HashMap<String,String> r1Props = new HashMap<String,String>();
		r1Props.put(OsmModel.TAG_TYPE,OsmModel.TYPE_MULTIPOLYGON);
		
		validateWay(idw1,w1NodeIds,w1Levels,w1Props,w1MultiPolyId,w1FeatureInfoName,0,minW1TermiteVersion);
		validateRelation(idr1,r1Props,r1Members,0,0);
		
		try {
			boolean success = action.undoAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
				
		w1MultiPolyId = OsmObject.INVALID_ID;		
				
		validateWay(idw1,w1NodeIds,w1Levels,w1Props,w1MultiPolyId,w1FeatureInfoName,0,minW1TermiteVersion);
		validateRelationDeleted(idr1,true,r1Members);
		
		try {
			boolean success = action.doAction();
			assert(success);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			assert(false);
		}
		
		w1MultiPolyId = idr1;
		
		validateWay(idw1,w1NodeIds,w1Levels,w1Props,w1MultiPolyId,w1FeatureInfoName,0,minW1TermiteVersion);
		validateRelation(idr1,r1Props,r1Members,0,0);
		
		//------------------
		// Create a generic relation
		//------------------
		
		
		//////////////////////////////////////////////////////////////
		// Edit Properties
		//////////////////////////////////////////////////////////////
		
		//=====================
		//edit node properties
		//=====================
		
		//=====================
		//edit way properties
		//=====================
		
		//=====================
		//edit generic multipoly properties
		//=====================
		
		
		//////////////////////////////////////////////////////////////
		// Edit Node Location
		//////////////////////////////////////////////////////////////
		
		
		//////////////////////////////////////////////////////////////
		// Indert Node into Way
		//////////////////////////////////////////////////////////////
		
		
		//////////////////////////////////////////////////////////////
		// Remove Node from Way
		//////////////////////////////////////////////////////////////
		
		//////////////////////////////////////////////////////////////
		// Insert Member into Relation
		//////////////////////////////////////////////////////////////
		
		//=====================
		// Multipoly
		//=====================
		
		//=====================
		// Generic Relation
		//=====================
		
		//////////////////////////////////////////////////////////////
		// Remove Member from Relation
		//////////////////////////////////////////////////////////////
		
		//=====================
		// Multipoly
		//=====================
		
		//=====================
		// Generic Relation
		//=====================
		
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
		
//		
//		OsmNode oNode2 = new OsmNode();
//		double x2 = 0;
//		double y2 = 0;
//		oNode2.setPosition(x2,y2);
//		oNode2.setProperty(OsmModel.KEY_ZLEVEL,zlevel1);
//		oNode2.setProperty(OsmModel.KEY_ZCONTEXT,zcontext);
//		instr = new CreateInstruction(oNode2,termiteData);
//		long idn2 = oNode2.getId();
//		action.addInstruction(instr);
//		
//		OsmNode oNode3 = new OsmNode();
//		double x3 = 0;
//		double y3 = 0;
//		oNode3.setPosition(x3,y3);
//		oNode3.setProperty(OsmModel.KEY_ZLEVEL,zlevel1);
//		oNode3.setProperty(OsmModel.KEY_ZCONTEXT,zcontext);
//		instr = new CreateInstruction(oNode3,termiteData);
//		long idn3 = oNode3.getId();
//		action.addInstruction(instr);
//		
//		OsmNode oNode4 = new OsmNode();
//		double x4 = 0;
//		double y4 = 0;
//		oNode4.setPosition(x4,y4);
//		oNode4.setProperty(OsmModel.KEY_ZLEVEL,zlevel1);
//		oNode4.setProperty(OsmModel.KEY_ZCONTEXT,zcontext);
//		oNode4.setProperty("buildingpart","stairs");
//		instr = new CreateInstruction(oNode3,termiteData);
//		long idn4 = oNode3.getId();
//		action.addInstruction(instr);
//		
//		//outdoor node
//		OsmNode oNode0 = new OsmNode();
//		double x0 = 0;
//		double y0 = 0;
//		oNode3.setPosition(x0,y0);
//		instr = new CreateInstruction(oNode3,termiteData);
//		long idn0 = oNode0.getId();
//		action.addInstruction(instr);
//		
//		OsmWay oWay1 = new OsmWay();
//		List<Long> wayNodes = oWay1.getNodeIds();
//		wayNodes.add(idn1);
//		wayNodes.add(idn2);
//		wayNodes.add(idn3);
//		wayNodes.add(idn1);
//		oWay1.setProperty("buildingpart","wall");
//		instr = new CreateInstruction(oWay1,termiteData);
//		long idw1 = oWay1.getId();
//		action.addInstruction(instr);
		
//		OsmRelation oRel1 = new OsmRelation();
//		List<OsmMember> members = oRel1.getMembers();
//		OsmMember member = new OsmMember(idw1,"way","outer");
//		members.add(member);
//		member = new OsmMember(idw1,"way","inner");
//		members.add(member);
//		oRel1.setProperty("type","multipolygon");
//		instr = new CreateInstruction(oRel1,termiteData);
//		action.addInstruction(instr);
		
//		TermiteWay testWay = termiteData.getWay(wayId);
//		OsmWay osmWay = testWay.getOsmObject();
//		UpdateObjectProperty targetData2 = new UpdateObjectProperty(termiteData,"buildingpart","buildingpart","room");
//		instr = new UpdateInstruction(osmWay,targetData2);
//		action.addInstruction(instr);
//		
//				TermiteNode testNode = termiteData.getNode(nodeId);
//		OsmNode osmNode = testNode.getOsmObject();
//		double x = osmNode.getX();
//		double y = osmNode.getY();
//		UpdatePosition targetData = new UpdatePosition(x + 100,y+50);
//		instr = new UpdateInstruction(osmNode,targetData);
//		action.addInstruction(instr);
		
//		try {
//			action.doAction();
//		}
//		catch(Exception ex) {
//			ex.printStackTrace();
//		}
		
	}
	
	/** This method validates the content of a node. */
	private void validateNode(long id, 
			double x, double y, 
			long structureId, int zlevel,
			List<Long> wayIds,
			HashMap<String,String> props,
			String featureInfoName,
			int minOsmVersion,
			int minTermiteVersion) {
		
		//check existence
		OsmNode oNode = osmData.getOsmNode(id);
		TermiteNode tNode = termiteData.getNode(id);
		assert(oNode == tNode.getOsmObject());
		assert(tNode == oNode.getTermiteObject());
		
		//check location
		assert(oNode.getId() == id);
		assert(oNode.getX() == x);
		assert(oNode.getY() == y);
		
		//check level
		TermiteLevel level = termiteData.getLevel(structureId,zlevel);
		assert(tNode.getLevel() == level);
		
		//check ways
		List<TermiteWay> ways = tNode.getWays();
		assert(ways.size() == wayIds.size());
		for(TermiteWay tWay:ways) {
			OsmWay oWay = tWay.getOsmObject();
			Long wid = oWay.getId();
			assert(wayIds.contains(wid));
		}
		
		//check properties - both directions to make sure they are the same
		checkProperties(oNode,props);
		
		FeatureInfo fi = tNode.getFeatureInfo();
		if(fi == null) assert(featureInfoName == null);
		else assert(featureInfoName.equals(fi.getName()));
		
		assert(oNode.getLocalVersion() >= minOsmVersion);
		assert(tNode.getTermiteLocalVersion() >= minTermiteVersion);
	}
	
	/** This method validates a node was deleted. */
	void validateNodeDelete(long id, long structureId, int zlevel, List<Long> wayIds) {
		
		//check it is not in the data
		assert(osmData.getOsmNode(id) == null);
		assert(termiteData.getNode(id) == null);
		
		//check no on level
		TermiteLevel level = termiteData.getLevel(structureId,zlevel);
		for(TermiteNode tn: level.getNodes()) {
			OsmNode on = tn.getOsmObject();
			long nid = on.getId();
			assert(nid != id);
		}
		
		//check not in ways
		Long nid = (Long)id;
		for(Long wid:wayIds) {
			TermiteWay tWay = termiteData.getWay(wid);
			OsmWay oWay = tWay.getOsmObject();
			List<Long> onids = oWay.getNodeIds();
			assert(!onids.contains(nid));
			for(TermiteNode tNode:tWay.getNodes()) {
				OsmNode oNode = tNode.getOsmObject();
				long tnid = oNode.getId();
				assert(tnid != id);
			}
		}
	}
	
	private void validateWay(long id,
			List<Long> nodeIds,
			List<Integer> levelIds,
			HashMap<String,String> props,
			long multiPolyId,
			String featureInfoName,
			int minOsmVersion,
			int minTermiteVersion) {
		
		
		//check existence
		OsmWay oWay = osmData.getOsmWay(id);
		TermiteWay tWay = termiteData.getWay(id);
		assert(oWay == tWay.getOsmObject());
		assert(tWay == oWay.getTermiteObject());
		
		//check location
		assert(oWay.getId() == id);
		
		//check levels
		List<TermiteLevel> levels = tWay.getLevels();
		assert(levels.size() == levelIds.size());
		for(TermiteLevel level:levels) {
			Integer zlevel = level.getZlevel();
			assert(levelIds.contains(zlevel));
		}
		
		//check nodes
		List<Long> oNodeIds = oWay.getNodeIds();
		assert(oNodeIds.size() == nodeIds.size());
		for(Long nid:nodeIds) {
			assert(oNodeIds.contains(nid));
		}
		List<TermiteNode> nodes = tWay.getNodes();
		assert(nodes.size() == nodeIds.size());
		for(TermiteNode tNode:nodes) {
			OsmNode oNode = tNode.getOsmObject();
			Long nid = oNode.getId();
			assert(nodeIds.contains(nid));
		}
		
		//check properties - both directions to make sure they are the same
		checkProperties(oWay,props);
		
		//check multipoly
		if(multiPolyId != OsmObject.INVALID_ID) {
			TermiteMultiPoly tmp = termiteData.getMultiPoly(id,false);
			if(tmp != null) {
				assert(tmp.getWays().contains(tWay));
			}
		}
		
		FeatureInfo fi = tWay.getFeatureInfo();
		if(fi == null) assert(featureInfoName == null);
		else assert(featureInfoName.equals(fi.getName()));
		
		assert(oWay.getLocalVersion() >= minOsmVersion);
		assert(tWay.getTermiteLocalVersion() >= minTermiteVersion);
		
	}
	
	void validateWayDeleted(long id,
			List<Long> nodeIds,
			long structureId,
			List<Integer> levelIds,
			long multiPolyId) {
		
		//check it is not in the data
		assert(osmData.getOsmWay(id) == null);
		assert(termiteData.getWay(id) == null);
		
		//check levels
		for(int lid:levelIds) {
			TermiteLevel level = termiteData.getLevel(structureId, lid);
			for(TermiteWay tw:level.getWays()) {
				OsmWay ow = tw.getOsmObject();
				long owid = ow.getId();
				assert(owid != id);
			}
		}
		
		//check nodes
		for(long nid:nodeIds) {
			TermiteNode node = termiteData.getNode(nid);
			for(TermiteWay tw:node.getWays()) {
				OsmWay ow = tw.getOsmObject();
				long owid = ow.getId();
				assert(owid != id);
			}
		}
		
		if(multiPolyId != OsmObject.INVALID_ID) {
			TermiteMultiPoly tmp = termiteData.getMultiPoly(id,false);
			if(tmp != null) {
				for(TermiteWay tw:tmp.getWays()) {
					OsmWay ow = tw.getOsmObject();
					long owid = ow.getId();
					assert(owid != id);
				}
			}
		}
	}
		
	private void validateRelation(long id,
			HashMap<String,String> props,
			List<OsmMember> members,
			int minOsmVersion,
			int minTermiteVersion) {
		
		//check existence
		OsmRelation oRelation = osmData.getOsmRelation(id);
		assert(oRelation != null);
		
		//check ways
		List<OsmMember> oMembers = oRelation.getMembers();
		assert(oMembers.size() == members.size());
		int cnt = members.size();
		OsmMember m1;
		OsmMember m2;
		for(int i = 0; i < cnt; i++) {
			m1 = members.get(i);
			m2 = oMembers.get(i);
			assert(m1.memberId == m2.memberId);
			assert(m1.type.equals(m2.type));
			if(m1.role != null) {
				assert m1.role.equals(m2.role);
			}
			else {
				assert(m2.role == null);
			}
		}
		
		//check properties - both directions to make sure they are the same
		checkProperties(oRelation,props);
		
		assert(oRelation.getLocalVersion() >= minOsmVersion);
		
		//Multipoly case
		String type = oRelation.getProperty(OsmModel.TAG_TYPE);
		if((type != null)&&(type.equals(OsmModel.TYPE_MULTIPOLYGON))) {
			
			TermiteMultiPoly mp = termiteData.getMultiPoly(id,false);
			assert(mp.getOsmObject() == oRelation);
			assert(oRelation.getTermiteObject() == mp);
			
			List<TermiteWay> ways = mp.getWays();
			assert(ways.size() == members.size());
			cnt = members.size();
			OsmMember m;
			TermiteWay w;
			for(int i = 0; i < cnt; i++) {
				m = members.get(i);
				w = ways.get(i);
				long id1 = m.memberId;
				long id2 = w.getOsmObject().getId(); 
				assert(id1 == id2);
			}
			//I should select the proper value...
			assert(mp.getMainWay() != null);
			
			assert(mp.getTermiteLocalVersion() >= minTermiteVersion);
		}
		
	}
	
	void validateRelationDeleted(long id,
			boolean isMultiPoly,
			List<OsmMember> membersForMultiPolyCase) {
		
		//check it is not in the data
		assert(osmData.getOsmRelation(id) == null);
		
		if(isMultiPoly) {
			assert(termiteData.getMultiPoly(id,false) == null);
			
			for(OsmMember member:membersForMultiPolyCase) {
				if(member.type.equals("way")) {
					TermiteWay way = termiteData.getWay(member.memberId);
					assert(way.getMultiPoly() == null);
				}
			}
		}
	}
	
	private void checkProperties(OsmObject o, HashMap<String,String> p) {
		//check properties - both directions to make sure they are the same
		for(String key:p.keySet()) {
			String refValue = p.get(key);
			String actValue = o.getProperty(key);
			assert(refValue.equals(actValue));
		}
		Collection<String> actProps = o.getPropertyKeys();
		for(String key:actProps) {
			String refValue = p.get(key);
			String actValue = o.getProperty(key);
			assert(actValue.equals(refValue));
		}
	}
}
