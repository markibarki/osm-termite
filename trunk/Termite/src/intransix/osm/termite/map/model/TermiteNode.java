package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import java.util.*;


/**
 * This method relates a node to the structure data model. 
 * 
 * @author sutter
 */
public class TermiteNode extends TermiteObject {
	
	//====================
	// Properties
	//====================
	
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
	
	/** This method sets the OsmNode. */
	void setOsmNode(OsmNode osmNode) {
		this.osmNode = osmNode;
		osmNode.setTermiteObject(this);
	}
	
	void addWay(TermiteWay way) {
		if(!ways.contains(way)) {
			ways.add(way);
		}
	}
	
	void updateLocalData(TermiteData termiteData) {
		//check properties
		this.classify();
		
/////////////////////////////////////////////////////////////////
		if(OsmModel.doNodeLevelLabels) {
			//get the level for this node
			int zlevel = osmNode.getIntProperty(OsmModel.KEY_ZLEVEL,TermiteLevel.DEFAULT_ZLEVEL);
			long structureId = osmNode.getLongProperty(OsmModel.KEY_ZCONTEXT,OsmObject.INVALID_ID);
			if(structureId != OsmObject.INVALID_ID) {
				level = termiteData.getLevel(structureId, zlevel,true);
			}
			else {
				level = termiteData.getOutdoorLevel();
			}
		}
		else {
			//no action
		}
///////////////////////////////////////////////////////////////////////
		
		//flag updates
		this.incrementTermiteVersion();	
		for(TermiteWay way:ways) {
			way.incrementTermiteVersion();
			TermiteMultiPoly mp = way.getMultiPoly();
			if(mp != null) {
				for(TermiteWay siblingWay:mp.getWays()) {
					siblingWay.incrementTermiteVersion();
				}
			}
		}
	}
	
	void updateRemoteData(TermiteData termiteData) {
	
/////////////////////////////////////////////////////////////////////////
		if(OsmModel.doNodeLevelLabels) {
			level.addNode(this);
		}
		else {
			//no action
		}
///////////////////////////////////////////////////////////////////////////
	}
	
	void objectDeleted(TermiteData termiteData) {
		
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
