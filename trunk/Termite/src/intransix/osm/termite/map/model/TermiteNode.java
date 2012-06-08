package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;


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
	
	//====================
	// Public Methods
	//====================
	
	/** This method gets the OSM node. */
	public OsmNode getOsmNode() {
		return osmNode;
	}
	
	/** This method gets the level for the node. */
	public TermiteLevel getLevel() {
		return level;
	}
	
	//====================
	// Package Methods
	//====================
	
	/** This method sets the OsmNode. */
	void load(OsmNode osmNode, TermiteData termiteData) {
		this.osmNode = osmNode;
		osmNode.setTermiteNode(this);
		update(termiteData);
	}
	
	void update(TermiteData termiteData) {
		//check properties
		this.classify();
		
//check level change!!!
		
	}
	
	/** This method sets the level. */
	void setLevel(TermiteLevel level) {
		this.level = level;
	}
	
	/** This method gets the OSM object. */
	@Override
	OsmObject getOsmObject() {
		return osmNode;
	}

	
}
