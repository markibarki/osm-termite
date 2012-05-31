package intransix.osm.termite.map.geom;

import java.util.HashMap;
import intransix.osm.termite.map.osm.*;
import java.util.Collection;

/**
 *
 * @author sutter
 */
public class TermiteData {
	
	//=====================
	// Properties
	//=====================
	
	private final static long FIRST_ID = -1;
	
	private long nextId = FIRST_ID;
	private HashMap<Long,TermiteNode> nodeMap = new HashMap<Long,TermiteNode>();
	private HashMap<Long,TermiteWay> wayMap = new HashMap<Long,TermiteWay>();
	private HashMap<Long,TermiteFeature> featureMap = new HashMap<Long,TermiteFeature>();
	private HashMap<Long,TermiteLevel> levelMap = new HashMap<Long,TermiteLevel>();
	private HashMap<Long,TermiteStructure> structureMap = new HashMap<Long,TermiteStructure>();
	
	private boolean doNodeLevelLabels = false;
	
	//=====================
	// Public Methods
	//=====================
	
	/** This returns the flag indicating of the nodes contain a reference to the level (true)
	 * or if there is a level relation containing the features on the level (false).
	 */
	public boolean getDoNodeLevelLabel() {
		return doNodeLevelLabels;
	}
	
	// <editor-fold defaultstate="collapsed" desc="Lookup and create methods">
	
	/** This method loads a node by ID. If create reference is true, a new node
	 * will be created if the requested one is not found. */
	public TermiteNode getTermiteNode(long id, boolean createReference) {
		TermiteNode node = nodeMap.get(id);
		if((node == null)&&(createReference)) {
			node = new TermiteNode(id);
			nodeMap.put(id,node);
		}
		return node;
	}
	
	/** This method creates a new node with a temporary id. The temporary id will
	 * be converted to a real id if it is submitted to OSM. */
	public TermiteNode createNode() {
		long id = this.getNextId();
		TermiteNode node = new TermiteNode(id);
		nodeMap.put(id, node);
		return node;
	}
	
	/** This method loads a way by ID. If create reference is true, a new node
	 * will be created if the requested one is not found. */
	public TermiteWay getTermiteWay(long id, boolean createReference) {
		TermiteWay way = wayMap.get(id);
		if((way == null)&&(createReference)) {
			way = new TermiteWay(id);
			wayMap.put(id,way);
		}
		return way;
	}
	
	/** This method creates a new node with a temporary id. The temporary id will
	 * be converted to a real id if it is submitted to OSM. */
	public TermiteWay createWay() {
		long id = this.getNextId();
		TermiteWay way = new TermiteWay(id);
		wayMap.put(id,way);
		return way;
	}
	
	/** This method loads a feature by ID. If create reference is true, a new node
	 * will be created if the requested one is not found. */
	public TermiteFeature getTermiteFeature(long id, boolean createReference) {
		TermiteFeature feature = featureMap.get(id);
		if((feature == null)&&(createReference)) {
			feature = new TermiteFeature(id);
			featureMap.put(id,feature);
		}
		return feature;
	}
	
	/** This method creates a new node with a temporary id. The temporary id will
	 * be converted to a real id if it is submitted to OSM. */
	public TermiteFeature createFeature() {
		long id = this.getNextId();
		TermiteFeature feature = new TermiteFeature(id);
		featureMap.put(id,feature);
		return feature;
	}
	
	/** This method loads a feature by ID. If create reference is true, a new node
	 * will be created if the requested one is not found. */
	public TermiteLevel getTermiteLevel(long id, boolean createReference) {
		TermiteLevel level = levelMap.get(id);
		if((level == null)&&(createReference)) {
			level = new TermiteLevel(id);
			levelMap.put(id,level);
		}
		return level;
	}
	
	/** This method creates a new node with a temporary id. The temporary id will
	 * be converted to a real id if it is submitted to OSM. */
	public TermiteLevel createLevel() {
		long id = this.getNextId();
		TermiteLevel level = new TermiteLevel(id);
		levelMap.put(id,level);
		return level;
	}
	
	/** This method loads a feature by ID. If create reference is true, a new node
	 * will be created if the requested one is not found. */
	public TermiteStructure getTermiteStructure(long id, boolean createReference) {
		TermiteStructure structure = structureMap.get(id);
		if((structure == null)&&(createReference)) {
			structure = new TermiteStructure(id);
			structureMap.put(id,structure);
		}
		return structure;
	}
	
	/** This method creates a new node with a temporary id. The temporary id will
	 * be converted to a real id if it is submitted to OSM. */
	public TermiteStructure createStructure() {
		long id = this.getNextId();
		TermiteStructure structure = new TermiteStructure(id);
		structureMap.put(id,structure);
		return structure;
	}
	
	// </editor-fold>
	
	/** This method loads the osm format data into the native data model. */
	public void loadData(OsmXml osmXml) {
		
		//create the objects based on relations
		//must do these first
		for(OsmRelation osmRelation:osmXml.getOsmRelations()) {
			String relationtype = osmRelation.getProperty(OsmRelation.TAG_TYPE);
			//create the node
			if(OsmRelation.TYPE_MULTIPOLYGON.equalsIgnoreCase(relationtype)) {
				TermiteFeature termiteFeature = this.getTermiteFeature(osmRelation.getId(),true);
				termiteFeature.load(osmRelation,this);
			}
			else if(OsmRelation.TYPE_LEVEL.equalsIgnoreCase(relationtype)) {
				//METHOD 1 only:
				TermiteLevel termiteLevel = this.getTermiteLevel(osmRelation.getId(),true);
				termiteLevel.load(osmRelation,this);
			}
			else if(OsmRelation.TYPE_STRUCTURE.equalsIgnoreCase(relationtype)) {
				TermiteStructure termiteStructure = this.getTermiteStructure(osmRelation.getId(),true);
				termiteStructure.load(osmRelation,this);
			}
		}
		
		//create the termite nodes
		for(OsmNode osmNode:osmXml.getOsmNodes()) {
			//create the node
			TermiteNode termiteNode = this.getTermiteNode(osmNode.getId(),true);
			termiteNode.load(osmNode, this);
			
			if(doNodeLevelLabels) {
				//Method 2 only:
				
				//check if this should be a feature
				//if so, create a virtual way and feature
				if(termiteNode.isFeature()) {
					createVirtualFeatureForNode(termiteNode);
				}	
			}	
		}
		
		//create the termite ways
		//WE MUST DO THIS AFTER RELATIONS SINCE WE RELY ON CHECK THAT RELATION EXISTS
		//WE MUST DO THIS AFTER NODES TO GET NODE LEVELS IN METHOD 2.
		for(OsmWay osmWay:osmXml.getOsmWays()) {
			//create the node
			TermiteWay termiteWay = this.getTermiteWay(osmWay.getId(),true);
			termiteWay.load(osmWay, this);

			if(doNodeLevelLabels) {
				//Method 2 only:
				
				//make a virtual feature if no multipolygon already claimed this way
				if(termiteWay.getFeature() == null) {
					createVirtualFeatureForWay(termiteWay);
				}			
			}
		}
		
		//now distribute the nodes to the levels, in the case of method 2
		if(doNodeLevelLabels) {
			for(TermiteNode termiteNode:nodeMap.values()) {
				//lookup the level for this node
				int zlevel = termiteNode.getZlevel();
				long structureId = termiteNode.getStructureId();

				if(structureId != OsmObject.INVALID_ID) {
					TermiteStructure structure = this.getTermiteStructure(structureId,false);
					if(structure != null) {
						TermiteLevel level = structure.lookupLevel(zlevel);
						termiteNode.setLevel(level);
//add code to add level to WAY and FEATURE
					}
				}
			}
		}
	}
	
	//==========================
	// Package Methods
	//==========================
	
	TermiteFeature createVirtualFeatureForNode(TermiteNode termiteNode) {
		TermiteWay virtualWay = this.createWay();
		virtualWay.setIsVirtual(true);
		virtualWay.addNode(termiteNode);
		
		return createVirtualFeatureForWay(virtualWay);
	}
	
	TermiteFeature createVirtualFeatureForWay(TermiteWay termiteWay) {		
		TermiteFeature virtualFeature = this.createFeature();
		virtualFeature.setIsVirtual(true);
		virtualFeature.addWay(termiteWay);
		
		return virtualFeature;
	}
	
	//==========================
	// Private Methods
	//==========================
	
	/** This method gets the next available termite id, to be used for generating
	 * temporary IDs. */
	private synchronized long getNextId() {
		return nextId++;
	}
	
	
	
}
