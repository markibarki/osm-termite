package intransix.osm.termite.map.model;

import java.util.*;
import java.awt.geom.*;
import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.feature.FeatureInfo;

/**
 * This class holds the active session data.
 * 
 * @author sutter
 */
public class TermiteData {
//	
//	//=====================
//	// Properties
//	//=====================
//	
//	private OsmData baseData;
//	private OsmData workingData;
//	private Rectangle2D bounds = null;
//	
//	private HashMap<Long,TermiteNode> nodeMap = new HashMap<Long,TermiteNode>();
//	private HashMap<Long,TermiteWay> wayMap = new HashMap<Long,TermiteWay>();
//	private HashMap<Long,TermiteRelation> relationMap = new HashMap<Long,TermiteRelation>();
//	
//	private HashMap<Long,TermiteStructure> structureMap = new HashMap<Long,TermiteStructure>();
//	
//	private TermiteStructure outdoorStructure;
//	private TermiteLevel outdoorLevel;
//	
//	private GraduatedList<TermiteObject> orderedMapObjects = new GraduatedList<TermiteObject>();
//	
//	//=====================
//	// Public Methods
//	//=====================
//	
//	/**Constructor. */
//	public TermiteData() {
//	}
//	
//	public TermiteStructure getOutdoorStructure() {
//		return outdoorStructure;
//	}
//	
//	public TermiteLevel getOutdoorLevel() {
//		return outdoorLevel;
//	}
//	
//	public TermiteStructure getStructure(Long structureId) {
//		return getStructure(structureId,false);
//	}
//	
//	public TermiteLevel getLevel(Long structureId, int zlevel) {
//		return getLevel(structureId, zlevel, false);
//	}
//	
//	public TermiteWay getWay(Long objectId) {
//		return getWay(objectId, false);
//	}
//	
//	public TermiteNode getNode(Long structureId) {
//		return getNode(structureId, false);
//	}
//	
//	public TermiteRelation getRelation(Long relationId) {
//		return getRelation(relationId, false);
//	}
//	
//	public GraduatedList<TermiteObject> getOrderedList() {
//		return orderedMapObjects;
//	}
//	
//	//-----------------------
//	// Load Data
//	//-----------------------
//	
//	/** This method loads the osm format data into the data model. */
//	public void loadData(OsmData baseData) {
//		
//		this.baseData = baseData;
//		this.workingData = baseData.createCopy();	
//
//		//create the outdoor objects
//		outdoorStructure = this.getStructure(OsmObject.INVALID_ID, true);
//		outdoorLevel = this.getLevel(OsmObject.INVALID_ID,0,true);
//
//		//--------------------------------------------------
//		//read the data from the osm object to the termite object
//		//--------------------------------------------------
//
//		//create the termite nodes
//		for(OsmNode osmNode:workingData.getOsmNodes()) {
//			//create the node
//			TermiteNode termiteNode = getNode(osmNode.getId(),true);
//			termiteNode.init(this,osmNode);	
//			
//			//add to ordered list
//			FeatureInfo featureInfo = termiteNode.getFeatureInfo();
//			int zorder = (featureInfo != null) ? featureInfo.getZorder() : FeatureInfo.DEFAULT_ZORDER;
//			orderedMapObjects.add(termiteNode, zorder);
//		}
//		
//		//create the termite ways
////MUST DO WAYS AFTER NODES FOR NOW
//		for(OsmWay osmWay:workingData.getOsmWays()) {
//			//create the way
//			TermiteWay termiteWay = getWay(osmWay.getId(),true);
//			termiteWay.init(this,osmWay);
//			
//			//add to ordered list
//			FeatureInfo featureInfo = termiteWay.getFeatureInfo();
//			int zorder = (featureInfo != null) ? featureInfo.getZorder() : FeatureInfo.DEFAULT_ZORDER;
//			orderedMapObjects.add(termiteWay, zorder);
//			
////need to add the shell and parent objects
//		}
//		
//		//create the objects based on relations
//		for(OsmRelation osmRelation:workingData.getOsmRelations()) {
//			Long memberId = osmRelation.getId();
//			//create the relation
//			TermiteRelation termiteRelation = this.getRelation(memberId, true);
//			termiteRelation.init(this,osmRelation);
//		}
//		
////long start = System.nanoTime();
////double durationMsec = 1e-6*(System.nanoTime() - start);
////System.out.println("Remote update time: " + durationMsec);
//	}
//	
//	public OsmData getWorkingData() {
//		return workingData;
//	}
//	
//	//==========================
//	// Package Methods
//	//==========================
//	
//
//	/** This method looks up the TermiteNode associated with the id. If the object
//	 * is not found and createRef is true, a new object is created and inserted into the map.
//	 * If createRef is false, null is returned. 
//	 * 
//	 * @param id		The OSM ID for the object
//	 * @createRef		If a new reference should be created this should be set to true.
//	 * @return			The object
//	 */
//	TermiteNode getNode(Long id, boolean createRef) {
//		TermiteNode node = this.nodeMap.get(id);
//		if((node == null)&&(createRef)) {
//			node = new TermiteNode();
//			nodeMap.put(id, node);
//		}
//		return node;
//	}
//	
//	/** This method looks up the TermiteWay associated with the id. If the object
//	 * is not found and createRef is true, a new object is created and inserted into the map.
//	 * If createRef is false, null is returned. 
//	 * 
//	 * @param id		The OSM ID for the object
//	 * @createRef		If a new reference should be created this should be set to true.
//	 * @return			The object
//	 */
//	TermiteWay getWay(Long id, boolean createRef) {
//		TermiteWay way = this.wayMap.get(id);
//		if((way == null)&&(createRef)) {
//			way = new TermiteWay();
//			wayMap.put(id, way);
//		}
//		return way;
//	}
//	
//	/** This method looks up the TermiteRelation associated with the id. If the object
//	 * is not found and createRef is true, a new object is created and inserted into the map.
//	 * If createRef is false, null is returned. 
//	 * 
//	 * @param id		The OSM ID for the object
//	 * @createRef		If a new reference should be created this should be set to true.
//	 * @return			The object
//	 */
//	TermiteRelation getRelation(Long id, boolean createRef) {
//		TermiteRelation relation = this.relationMap.get(id);
//		if((relation == null)&&(createRef)) {
//			relation = new TermiteRelation();
//			relationMap.put(id, relation);
//		}
//		return relation;
//	}
//	
//	/** This method looks up a level for the given structure id and zlevel value. */
//	TermiteLevel getLevel(Long structureId, int zlevel, boolean createRef) {
//		TermiteStructure structure = getStructure(structureId,createRef);
//		if(structure != null) {
//			TermiteLevel level = structure.lookupLevel(zlevel);
//			if((level == null)&&(createRef)) {
//				level = new TermiteLevel();
//				level.setStructure(structure,zlevel);
//				structure.addLevel(level);
//			}
//			return level;
//		}
//		else {
//			return null;
//		}
//	}
//	
//	/** This method looks up the TermiteStructure associated with the id. If the object
//	 * is not found and createRef is true, a new object is created and inserted into the map.
//	 * If createRef is false, null is returned. 
//	 * 
//	 * @param id		The OSM ID for the object
//	 * @createRef		If a new reference should be created this should be set to true.
//	 * @return			The object
//	 */
//	TermiteStructure getStructure(Long id, boolean createRef) {
//		TermiteStructure structure = this.structureMap.get(id);
//		if((structure == null)&&(createRef)) {
//			structure = new TermiteStructure();
//			structureMap.put(id, structure);
//		}
//		return structure;
//	}
//
//
//	/** This method is called from a delete instruction to remove the termite object. */
//	void deleteTermiteObject(TermiteObject termiteObject) {
//		OsmObject osmObject = termiteObject.getOsmObject();
//		Long id = osmObject.getId();
//		
//		termiteObject.objectDeleted(this);
//		
//		if(termiteObject instanceof TermiteNode) {
//			this.nodeMap.remove(id);
//		}
//		else if(termiteObject instanceof TermiteWay) {
//			this.wayMap.remove(id);
//		}
//		else if(termiteObject instanceof TermiteRelation) {
//			this.relationMap.remove(id);
//		}
////how about level and structure? These need to be deleted too
//	}
//	
//	//==========================
//	// Private Methods
//	//==========================
//	
//	
	// <editor-fold defaultstate="collapsed" desc="sample code fold">
	// </editor-fold>
}
