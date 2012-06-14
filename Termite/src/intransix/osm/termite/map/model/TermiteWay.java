package intransix.osm.termite.map.model;

import java.util.*;
import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.prop.FeatureInfo;

/**
 * This method encapsulates a way in the structure model.
 * 
 * @author sutter
 */
public class TermiteWay extends TermiteObject<OsmWay> {
	
	//===============
	// Properties
	//===============
	
	private OsmWay osmWay;
	private List<TermiteNode> nodes = new ArrayList<TermiteNode>();
	
	private boolean isArea = false;
	
	private ArrayList<TermiteLevel> levels = new ArrayList<TermiteLevel>();
	
	private TermiteMultiPoly multiPoly;
	
	//===============
	// Public Methods
	//===============
	
	/** This method gets the levels on which this way is located. */
	public List<TermiteLevel> getLevels() {
		return levels;
	}
	
	/** This method gets the levels on which this way is located. */
	public List<TermiteNode> getNodes() {
		return nodes;
	}
	
	/** This method returns true if the way forms an area and false otherwise. */
	public boolean getIsArea() {
		return isArea;
	}
	
	/** This method gets the multi poly relation for this way. */
	public TermiteMultiPoly getMultiPoly() {
		return multiPoly;
	}
	
	/** This method gets the OSM object associated with this object. */
	@Override
	public OsmWay getOsmObject() {
		return osmWay;
	}

	//====================
	// Package Methods
	//====================
	
	void init(TermiteData termiteData, OsmWay osmWay) {
		this.osmWay = osmWay;
		osmWay.setTermiteObject(this);
		
//		FeatureInfo oldInfo = this.getFeatureInfo();
		classify();
		
		//check for a change of ordering
//		int oldZorder = oldInfo.getZorder();
//		int newZorder = this.getFeatureInfo().getZorder();
		
		//check for setting the area parameter
		if((osmWay.getProperty("area") == null)&&(featureInfo != null)) {
			this.isArea = (featureInfo.getDefaultPath() == FeatureInfo.GEOM_TYPE_AREA);
		}
		
		//set the nodes
		for(Long nodeId:osmWay.getNodeIds()) {
			TermiteNode node = termiteData.getNode(nodeId,true);
			nodes.add(node);
			
			//set this way for this node (repeats handled OK)
			node.addWay(this);
			
			//set level from nodes - depends on nodes being loaded already!
			TermiteLevel level = node.getLevel();
			if(!levels.contains(level)) {
				levels.add(level);
				level.addWay(this);
			}
		}
	}
	
	void objectDeleted(TermiteData termiteData) {
		for(TermiteNode node:nodes) {
			node.removeWay(this);
		}
		nodes.clear();
		
		for(TermiteLevel level:levels) {
			level.removeWay(this);
		}
		levels.clear();
		
		if(multiPoly != null) {
			multiPoly.removeMemberObject(this);
		}
		
		this.osmWay = null;
	}
	
	@Override
	void propertiesUpdated(TermiteData termiteData) {
		
		//check properties
		FeatureInfo oldInfo = this.getFeatureInfo();
		classify();
		
		//check for setting the area parameter
		if((osmWay.getProperty("area") == null)&&(featureInfo != null)) {
			this.isArea = (featureInfo.getDefaultPath() == FeatureInfo.GEOM_TYPE_AREA);
		}
		
		//explicitly mark any ways in a multipolygon as changed
		if(this.multiPoly != null) {
			this.multiPoly.incrementTermiteVersion();
		}
		
		//flag this object as changed if relevent info changed
		this.incrementTermiteVersion();
	}
	
	/** This method adds a level to the way. */
	void addLevel(TermiteLevel level) {
		if(!levels.contains(level)) levels.add(level);
	}
	
	/** This method sets the multi poly relation for this way. */
	void setMultiPoly(TermiteMultiPoly multiPoly) {
		this.multiPoly = multiPoly;
		incrementTermiteVersion();
	}
	
	void removeNode(TermiteNode node) {
		nodes.remove(node);
		incrementTermiteVersion();
	}

}
