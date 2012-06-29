package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.model.TermiteData;
import java.util.ArrayList;
import java.util.List;

/**
 * This object holds the data of an OSM way. 
 * @author sutter
 */
public class OsmWay extends OsmObject {
	
	//=======================
	// Properties
	//=======================
		
	private List<OsmNode> nodes = new ArrayList<OsmNode>();
	private boolean isArea;

	//=======================
	// Public Methods
	//=======================
	
	/** Constructor. */
	public OsmWay(long id) {
		super(OsmModel.TYPE_WAY,id);
	}
	public OsmWay() {
		super(OsmModel.TYPE_WAY,OsmData.INVALID_ID);
	}
	
	/** This method gets the nodes for this way. */
	public List<OsmNode> getNodes() {
		return nodes;
	}
	
	/** This returns true if the way should be an area. False indicates line. */
	public boolean getIsArea() {
		return isArea;
	}

	//====================
	// Package Methods
	//====================
	
	@Override
	void propertiesUpdated(OsmData osmData) {
		super.propertiesUpdated(osmData);
		
		//read if this is an area
		boolean defaultIsArea = (getFeatureInfo().getDefaultPath() == FeatureInfo.GEOM_TYPE_AREA);
		isArea = this.getBooleanProperty(OsmModel.TAG_AREA,defaultIsArea);
	}
	
	@Override
	void objectDeleted(OsmData osmData) {
		super.objectDeleted(osmData);
		
		for(OsmNode node:nodes) {
			node.removeWay(this);
		}
		nodes.clear();
	}
}
