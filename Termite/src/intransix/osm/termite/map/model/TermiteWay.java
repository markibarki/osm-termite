package intransix.osm.termite.map.model;

import java.util.*;
import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.prop.FeatureInfo;

/**
 * This method encapsulates a way in the structure model.
 * 
 * @author sutter
 */
public class TermiteWay extends TermiteObject {
	
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
	
	/** This method gets the OSM way associated with this object. */
	public OsmWay getOsmWay() {
		return osmWay;
	}
	
	/** This method gets the multi poly relation for this way. */
	public TermiteMultiPoly getMultiPoly() {
		return multiPoly;
	}

	//====================
	// Package Methods
	//====================
	
	void load(OsmWay osmWay, TermiteData termiteData) {
		this.osmWay = osmWay;
		osmWay.setTermiteWay(this);
		
		//update for the change in the way
		update(termiteData);
	}
	
	void update(TermiteData termiteData) {
		classify();
		
		//set the nodes
		nodes.clear();
		for(Long nodeId:osmWay.getNodeIds()) {
			TermiteNode node = termiteData.getNode(nodeId,true);
			nodes.add(node);
		}
	}
	
	/** This method adds a level to the way. */
	void addLevel(TermiteLevel level) {
		if(!levels.contains(level)) levels.add(level);
	}
	
	/** This methods sets the OsmWay. */
	void setOsmWay(OsmWay osmWay) {
		this.osmWay = osmWay;
	}
	
	/** This method sets the multi poly relation for this way. */
	void setMultiPoly(TermiteMultiPoly multiPoly) {
		this.multiPoly = multiPoly;
	}
	
	/** This method gets the OSM object associated with this object. */
	@Override
	OsmObject getOsmObject() {
		return osmWay;
	}
	
	/** This method overrides the classify method to add the functionality 
	 * of checking the default area parameter. */
	@Override
	void classify() {
		super.classify();
		
		//check for setting the area parameter
		FeatureInfo featureInfo = this.getFeatureInfo();
		if(featureInfo != null) {
			if(osmWay.getProperty("area") == null) {
				this.isArea = (featureInfo.getDefaultPath() == FeatureInfo.GEOM_TYPE_AREA);
			}
		}
	}
	

	
	

	
	
}
