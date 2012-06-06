package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;


/**
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
	
	public OsmNode getOsmNode() {
		return osmNode;
	}
	
	public TermiteLevel getLevel() {
		return level;
	}
	
	//====================
	// Package Methods
	//====================
	
	void setOsmNode(OsmNode osmNode) {
		this.osmNode = osmNode;
	}
	
	void setLevel(TermiteLevel level) {
		this.level = level;
		level.addNode(this);
	}
	
	@Override
	OsmObject getOsmObject() {
		return osmNode;
	}

	
}
