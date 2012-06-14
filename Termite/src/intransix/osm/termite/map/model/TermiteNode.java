package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import java.util.*;
import intransix.osm.termite.map.prop.FeatureInfo;


/**
 * This method relates a node to the structure data model. 
 * 
 * @author sutter
 */
public class TermiteNode extends TermiteObject<OsmNode> {
	
	//====================
	// Properties
	//====================
	int zlevel = TermiteLevel.DEFAULT_ZLEVEL;
	long structureId = OsmObject.INVALID_ID;
		
	private TermiteLevel level = null;
	
	private OsmNode osmNode;
	
	private List<TermiteWay> ways = new ArrayList<TermiteWay>();
	
	//====================
	// Public Methods
	//====================
	
	/** This method gets the level for the node. */
	public TermiteLevel getLevel() {
		return level;
	}
	
	public List<TermiteWay> getWays() {
		return ways;
	}
	
	//====================
	// Package Methods
	//====================
	
	void addWay(TermiteWay way) {
		if(!ways.contains(way)) {
			ways.add(way);
			incrementTermiteVersion();
		}
	}
	
	void removeWay(TermiteWay way) {
		ways.remove(way);
		incrementTermiteVersion();
	}
	
	void init(TermiteData termiteData, OsmNode osmNode) {
		this.osmNode = osmNode;
		osmNode.setTermiteObject(this);
		
		//check properties
		this.classify();
		
		//get the level for this node
		zlevel = osmNode.getIntProperty(OsmModel.KEY_ZLEVEL,TermiteLevel.DEFAULT_ZLEVEL);
		structureId = osmNode.getLongProperty(OsmModel.KEY_ZCONTEXT,OsmObject.INVALID_ID);
		if(structureId != OsmObject.INVALID_ID) {
			level = termiteData.getLevel(structureId, zlevel,true);
		}
		else {
			level = termiteData.getOutdoorLevel();
		}
		
		//update objects that hold a reference to this
		level.addNode(this);
	}
	
	void objectDeleted(TermiteData termiteData) {
		if(level != null) {
			level.removeNode(this);
		}
		level = null;
		
		for(TermiteWay way:ways) {
			way.removeNode(this);
		}	
		ways.clear();
		
		osmNode = null;
	}
	
	@Override
	void propertiesUpdated(TermiteData termiteData) {
		
		//check properties
		FeatureInfo oldInfo = this.getFeatureInfo();
		this.classify();
		
		//check for a change of ordering
		int oldZorder = oldInfo.getZorder();
		int newZorder = this.getFeatureInfo().getZorder();
		
		//get the level for this node
		zlevel = osmNode.getIntProperty(OsmModel.KEY_ZLEVEL,TermiteLevel.DEFAULT_ZLEVEL);
		structureId = osmNode.getLongProperty(OsmModel.KEY_ZCONTEXT,OsmObject.INVALID_ID);
		TermiteLevel oldLevel = level;
		if(structureId != OsmObject.INVALID_ID) {
			level = termiteData.getLevel(structureId, zlevel,true);
		}
		else {
			level = termiteData.getOutdoorLevel();
		}
		
		if(level != oldLevel) {
			//mark the level and ways as updated
			oldLevel.removeNode(this);
			
			level.addNode(this);
			
			//explicitly update objects that hold a reference to this
			for(TermiteWay way:ways) {
				way.incrementTermiteVersion();
			}
		}
		else if(newZorder != oldZorder) {
			//explicitly flag this level as updated
			level.incrementTermiteVersion();
		}
		
		//flag this object as changed if relevent info changed
		this.incrementTermiteVersion();
	}
	
	/** This method sets the level. */
	void setLevel(TermiteLevel level) {
		this.level = level;
	}
	
	/** This method gets the OSM object. */
	@Override
	public OsmNode getOsmObject() {
		return osmNode;
	}

	
}
