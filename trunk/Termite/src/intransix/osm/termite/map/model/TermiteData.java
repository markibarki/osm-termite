package intransix.osm.termite.map.model;

import java.util.HashMap;
import java.util.ArrayList;
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

	/** This method looks up the TermiteLevel associated with the id. If the level
	 * is not found null is returned. 
	 * 
	 * @param id		The OSM ID for the level relation associated with the level
	 * @return			The TermiteLevel object
	 */
	public TermiteLevel getLevel(Long id) {
		return this.levelMap.get(id);
	}
	
	/** This method looks up the TermiteStructure associated with the id. If the structure
	 * is not found null is returned. 
	 * 
	 * @param id		The OSM ID for the structure relation associated with the structure
	 * @return			The TermiteStructure object
	 */
	public TermiteStructure getStructure(Long id) {
		return this.structureMap.get(id);
	}
	
	/** This method looks up the TermiteMultiPoly associated with the id. If the multipoly
	 * is not found null is returned. 
	 * 
	 * @param id		The OSM ID for the multipoly relation associated with the multipoly
	 * @return			The TermiteMultiPoly object
	 */
	public TermiteMultiPoly getMultiPoly(Long id) {
		return this.multiPolyMap.get(id);
	}
	
	/** This method looks up a level for the given structure id and zlevel velue. */
	public TermiteLevel getLevel(Long structureId, int zlevel) {
		TermiteStructure structure = getStructure(structureId);
		if(structure != null) {
			return structure.lookupLevel(zlevel);
		}
		else {
			return null;
		}
	}
	
	/** This method loads the osm format data into the data model. */
	public void loadData(OsmData baseData) {
		
		this.baseData = baseData;
		this.workingData = baseData.createCopy();
		
		
		//create the outdoor objects
		outdoorLevel = new TermiteLevel();
		outdoorStructure = new TermiteStructure();
		outdoorStructure.addLevel(outdoorLevel);
		structureMap.put(OsmObject.INVALID_ID, outdoorStructure);
		levelMap.put(OsmObject.INVALID_ID, outdoorLevel);
		
		//---------------------
		// Create raw termite objects based on OSM objects
		//---------------------
		
		//create the termite nodes
		for(OsmNode osmNode:workingData.getOsmNodes()) {
			//create the node
			TermiteNode termiteNode = new TermiteNode();
			osmNode.setTermiteNode(termiteNode);
			termiteNode.setOsmNode(osmNode);
			termiteNode.classify();
		}
		
		//create the termite ways
		//WE MUST DO THIS AFTER RELATIONS SINCE WE RELY ON CHECK THAT RELATION EXISTS
		for(OsmWay osmWay:workingData.getOsmWays()) {
			//create the node
			TermiteWay termiteWay = new TermiteWay();
			osmWay.setTermiteWay(termiteWay);
			termiteWay.setOsmWay(osmWay);
			termiteWay.classify();
		}
		
		//create the objects based on relations
		for(OsmRelation osmRelation:workingData.getOsmRelations()) {
			String relationtype = osmRelation.getProperty(OsmModel.TAG_TYPE);
			//create the node
			if(OsmModel.TYPE_MULTIPOLYGON.equalsIgnoreCase(relationtype)) {
				TermiteMultiPoly termiteMultiPoly = new TermiteMultiPoly();
				termiteMultiPoly.load(osmRelation);
				multiPolyMap.put(osmRelation.getId(),termiteMultiPoly);
			}
			else if(OsmModel.TYPE_LEVEL.equalsIgnoreCase(relationtype)) {
				//METHOD 1 only:
				TermiteLevel termiteLevel = new TermiteLevel();
				termiteLevel.loadFromRelation(osmRelation,this);
				levelMap.put(osmRelation.getId(),termiteLevel);
			}
			else if(OsmModel.TYPE_STRUCTURE.equalsIgnoreCase(relationtype)) {
				TermiteStructure termiteStructure = new TermiteStructure();
				termiteStructure.load(osmRelation,this);
				structureMap.put(osmRelation.getId(),termiteStructure);
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
					
					//add related ways to the level
					for(OsmWay osmWay:osmNode.getWays()) {
						TermiteWay termiteWay = osmWay.getTermiteWay();
						termiteWay.addLevel(level);
					}
				}
			}
		}
		else {
			//add all unaccounted ways to the outside level
			//add nodes in a way to the associated level
			for(OsmWay osmWay:workingData.getOsmWays()) {
				TermiteWay termiteWay = osmWay.getTermiteWay();
				ArrayList<TermiteLevel> levels = termiteWay.getLevels();
				TermiteLevel level = null;
				//in this case, we must have the nubmer of levels equal 1
				if(levels.isEmpty()) {
					level = outdoorLevel;
					termiteWay.addLevel(level);
				}
				else if(levels.size() == 1) {
					level = levels.get(0);
				}
				else {
//for now, we will just take the first. I"m not sure what else to do.
level = levels.get(0);
				}
				for(OsmNode osmNode:osmWay.getNodes()) {
					TermiteNode termiteNode = osmNode.getTermiteNode();
					termiteNode.setLevel(level);
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
		
		//calculate bounds
		for(TermiteStructure structure:structureMap.values()) {
			structure.calculateBounds();
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
