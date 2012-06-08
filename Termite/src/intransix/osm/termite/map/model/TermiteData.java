package intransix.osm.termite.map.model;

import java.util.*;
import java.awt.geom.*;
import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.util.MercatorCoordinates;

/**
 * This class holds the active session data.
 * 
 * @author sutter
 */
public class TermiteData {
	
	//=====================
	// Properties
	//=====================
	
	private OsmData baseData;
	private OsmData workingData;
	private Rectangle2D bounds = null;
	
	private HashMap<Long,TermiteNode> nodeMap = new HashMap<Long,TermiteNode>();
	private HashMap<Long,TermiteWay> wayMap = new HashMap<Long,TermiteWay>();
	private HashMap<Long,TermiteMultiPoly> multiPolyMap = new HashMap<Long,TermiteMultiPoly>();
	private HashMap<Long,TermiteLevel> levelMap = new HashMap<Long,TermiteLevel>();
	private HashMap<Long,TermiteStructure> structureMap = new HashMap<Long,TermiteStructure>();
	
	private TermiteStructure outdoorStructure;
	private TermiteLevel outdoorLevel;
	
	//=====================
	// Public Methods
	//=====================
	
	/**Constructor. */
	public TermiteData() {
	}
	
	/** This method looks up the TermiteNode associated with the id. If the object
	 * is not found and createRef is true, a new object is created and inserted into the map.
	 * If createRef is false, null is returned. 
	 * 
	 * @param id		The OSM ID for the object
	 * @createRef		If a new reference should be created this should be set to true.
	 * @return			The object
	 */
	public TermiteNode getNode(Long id, boolean createRef) {
		TermiteNode node = this.nodeMap.get(id);
		if((node == null)&&(createRef)) {
			node = new TermiteNode();
			nodeMap.put(id, node);
		}
		return node;
	}
	
	/** This method looks up the TermiteWay associated with the id. If the object
	 * is not found and createRef is true, a new object is created and inserted into the map.
	 * If createRef is false, null is returned. 
	 * 
	 * @param id		The OSM ID for the object
	 * @createRef		If a new reference should be created this should be set to true.
	 * @return			The object
	 */
	public TermiteWay getWay(Long id, boolean createRef) {
		TermiteWay way = this.wayMap.get(id);
		if((way == null)&&(createRef)) {
			way = new TermiteWay();
			wayMap.put(id, way);
		}
		return way;
	}

	/** This method looks up the TermiteLevel associated with the id. If the object
	 * is not found and createRef is true, a new object is created and inserted into the map.
	 * If createRef is false, null is returned. 
	 * 
	 * @param id		The OSM ID for the object
	 * @createRef		If a new reference should be created this should be set to true.
	 * @return			The object
	 */
	public TermiteLevel getLevel(Long id, boolean createRef) {
		TermiteLevel level = this.levelMap.get(id);
		if((level == null)&&(createRef)) {
			level = new TermiteLevel();
			levelMap.put(id, level);
		}
		return level;
	}
	
	/** This method looks up the TermiteStructure associated with the id. If the object
	 * is not found and createRef is true, a new object is created and inserted into the map.
	 * If createRef is false, null is returned. 
	 * 
	 * @param id		The OSM ID for the object
	 * @createRef		If a new reference should be created this should be set to true.
	 * @return			The object
	 */
	public TermiteStructure getStructure(Long id, boolean createRef) {
		TermiteStructure structure = this.structureMap.get(id);
		if((structure == null)&&(createRef)) {
			structure = new TermiteStructure();
			structureMap.put(id, structure);
		}
		return structure;
	}
	
	/** This method looks up the TermiteMultiPoly associated with the id. If the object
	 * is not found and createRef is true, a new object is created and inserted into the map.
	 * If createRef is false, null is returned. 
	 * 
	 * @param id		The OSM ID for the object
	 * @createRef		If a new reference should be created this should be set to true.
	 * @return			The object
	 */
	public TermiteMultiPoly getMultiPoly(Long id, boolean createRef) {
		TermiteMultiPoly multiPoly = this.multiPolyMap.get(id);
		if((multiPoly == null)&&(createRef)) {
			multiPoly = new TermiteMultiPoly();
			multiPolyMap.put(id, multiPoly);
		}
		return multiPoly;
	}
	
	/** This method looks up a level for the given structure id and zlevel velue. */
	public TermiteLevel getLevel(Long structureId, int zlevel) {
		TermiteStructure structure = getStructure(structureId,false);
		if(structure != null) {
			return structure.lookupLevel(zlevel);
		}
		else {
			return null;
		}
	}
	
	public OsmData getWorkingData() {
		return workingData;
	}
	
	public OsmData getBaseData() {
		return baseData;
	}
	
	/** This method loads the osm format data into the data model. */
	public void loadData(OsmData baseData) {
		
		this.baseData = baseData;
		this.workingData = baseData.createCopy();	
		
		//create the outdoor objects
		outdoorLevel = this.getLevel(OsmObject.INVALID_ID,true);
		outdoorStructure = this.getStructure(OsmObject.INVALID_ID, true);
		outdoorStructure.addLevel(outdoorLevel);
		
		//---------------------
		// Create raw termite objects based on OSM objects
		//---------------------
		
		//create the termite nodes
		for(OsmNode osmNode:workingData.getOsmNodes()) {
			//create the node
			TermiteNode termiteNode = getNode(osmNode.getId(),true);
			termiteNode.load(osmNode,this);
		}
		
		//create the termite ways
		//WE MUST DO THIS AFTER RELATIONS SINCE WE RELY ON CHECK THAT RELATION EXISTS
		for(OsmWay osmWay:workingData.getOsmWays()) {
			//create the node
			TermiteWay termiteWay = getWay(osmWay.getId(),true);
			termiteWay.load(osmWay,this);
		}
		
		//create the objects based on relations
		for(OsmRelation osmRelation:workingData.getOsmRelations()) {
			String relationtype = osmRelation.getProperty(OsmModel.TAG_TYPE);
			Long memberId = osmRelation.getId();
			//create the node
			if(OsmModel.TYPE_MULTIPOLYGON.equalsIgnoreCase(relationtype)) {
				TermiteMultiPoly termiteMultiPoly = this.getMultiPoly(memberId, true);
				termiteMultiPoly.load(osmRelation,this);
			}
			else if(OsmModel.TYPE_LEVEL.equalsIgnoreCase(relationtype)) {
				//METHOD 1 only:
				TermiteLevel termiteLevel = this.getLevel(memberId, true);
				termiteLevel.loadFromRelation(osmRelation,this);
			}
			else if(OsmModel.TYPE_STRUCTURE.equalsIgnoreCase(relationtype)) {
				TermiteStructure termiteStructure = this.getStructure(memberId, true);
				termiteStructure.load(osmRelation,this);
			}
		}
		
		//-------------------------
		// Finish loading - now that all objects are created
		//-------------------------
		
		//copy the properties from nodes and ways to features here
		//we couldn't do it before because they may not have been loaded
		if(OsmModel.doNodeLevelLabels) {
			for(OsmNode osmNode:workingData.getOsmNodes()) {
				TermiteNode termiteNode = osmNode.getTermiteNode();
				TermiteLevel level = null;
				//lookup level
				long structureId = osmNode.getLongProperty(OsmModel.KEY_ZCONTEXT,OsmObject.INVALID_ID);
				if(structureId != OsmObject.INVALID_ID) {
					int zlevel = osmNode.getIntProperty(OsmModel.KEY_ZLEVEL,TermiteLevel.DEFAULT_ZLEVEL);
					level = this.getLevel(structureId, zlevel);
				}
				else {
					level = outdoorLevel;
				}
				
				if(level != null) {
					//add node to level
					termiteNode.setLevel(level);
					level.addNode(termiteNode);
					
					//add related ways to the level
					for(OsmWay osmWay:osmNode.getWays()) {
						TermiteWay termiteWay = osmWay.getTermiteWay();
						termiteWay.addLevel(level);
						level.addWay(termiteWay);
					}
				}
			}
		}
		else {
			//add all unaccounted ways to the outside level
			//add nodes in a way to the associated level
			for(OsmWay osmWay:workingData.getOsmWays()) {
				TermiteWay termiteWay = osmWay.getTermiteWay();
				List<TermiteLevel> levels = termiteWay.getLevels();
				TermiteLevel level = null;
				//in this case, we must have the nubmer of levels equal 1
				if(levels.isEmpty()) {
					level = outdoorLevel;
					termiteWay.addLevel(level);
					level.addWay(termiteWay);
				}
				else if(levels.size() == 1) {
					level = levels.get(0);
				}
				else {
//for now, we will just take the first. I"m not sure what else to do.
level = levels.get(0);
				}
				for(TermiteNode tNode:termiteWay.getNodes()) {
					tNode.setLevel(level);
					level.addNode(tNode);
				}
			}
		}

		
//to include this, we need to populate the outdoor features in method 1
		//finalize the structures and levels
//		outdoorStructure = this.getTermiteStructure(TermiteObject.INVALID_ID,true);
//		outdoorLevel = this.getTermiteLevel(TermiteObject.INVALID_ID,true);
//		outdoorStructure.addLevel(outdoorLevel);
//		outdoorLevel.setProperty(OsmModel.KEY_ZLEVEL,String.valueOf(0));
//		outdoorLevel.setProperty(OsmModel.KEY_ZCONTEXT,String.valueOf(TermiteObject.INVALID_ID));		
//		
//		if(OsmModel.doNodeLevelLabels) {
//			//now distribute the nodes to the levels, in the case of method 2
//			for(TermiteNode termiteNode:nodeMap.values()) {
//				//lookup the level for this node
//				int zlevel = termiteNode.getZlevel();
//				long structureId = termiteNode.getStructureId();
//
//				if(structureId != OsmObject.INVALID_ID) {
//					TermiteStructure structure = this.getTermiteStructure(structureId,false);
//					if(structure != null) {
//						TermiteLevel level = structure.lookupLevel(zlevel);
//						termiteNode.setLevel(level);
//					}
//				}
//				else {
//					//belongs in outdoors
//					termiteNode.setLevel(outdoorLevel);
//				}
//			}
//		}
//		else {
//			for(TermiteFeature feature:featureMap.values()) {
//				if(feature.getLevels().isEmpty()) {
//					outdoorLevel.addFeature(feature);
//				}
//			}
//		}
		
		//calculate bounds and order features
		for(TermiteStructure structure:structureMap.values()) {
			structure.calculateBounds();
			for(TermiteLevel level:structure.getLevels()) {
				level.orderFeatures();
			}
		}
		

		
//remove the comments here to classify before the first draw
//		//classify the features
//		//they will be calssified again when the data is rendered (made non-dirty)
//		for(TermiteFeature feature:featureMap.values()) {
//			feature.classify();
//		}
	}
	
	//==========================
	// Package Methods
	//==========================
	

	
	//==========================
	// Private Methods
	//==========================
	
	
	// <editor-fold defaultstate="collapsed" desc="sample code fold">
	// </editor-fold>
}
