package intransix.osm.termite.map.data;

import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * This object holds the data of an OSM way. 
 * 
 * @author sutter
 */
public class OsmWay extends OsmObject {
	
	//=======================
	// Properties
	//=======================
		
	private List<OsmNode> nodes = new ArrayList<OsmNode>();
	private boolean isArea;
	
	private List<OsmSegmentWrapper> segments = new ArrayList<OsmSegmentWrapper>();

	//=======================
	// Public Methods
	//=======================
	
	/** This method gets the nodes for this way. This list should NOT be edited. */
	public List<OsmNode> getNodes() {
		return nodes;
	}
	
	public List<OsmSegmentWrapper> getSegments() {
		return segments;
	}
	
	/** This returns true if the way should be an area. False indicates line. */
	public boolean getIsArea() {
		return isArea;
	}

	//====================
	// Package Methods
	//====================
	
	/** Constructor. */
	OsmWay(long id) {
		super(OsmModel.TYPE_WAY,id);
	}
	
	/** Constructor. */
	OsmWay() {
		super(OsmModel.TYPE_WAY,OsmData.INVALID_ID);
	}
	
	/** This overrides the base method to add functionality. */
	@Override
	void objectCreated(OsmData osmData) {
		featureCreatedProcessing(osmData);
		
		//read if this is an area
		boolean defaultIsArea = (getFeatureInfo().getDefaultPath() == FeatureInfo.GEOM_TYPE_AREA);
		isArea = this.getBooleanProperty(OsmModel.KEY_AREA,defaultIsArea);
	}
	
	/** This method should be calle when properties are updated. */
	@Override
	void objectUpdated(OsmData osmData) {
		featurePropertiesUpdatedProcessing(osmData);
		
		//read if this is an area
		boolean defaultIsArea = (getFeatureInfo().getDefaultPath() == FeatureInfo.GEOM_TYPE_AREA);
		isArea = this.getBooleanProperty(OsmModel.KEY_AREA,defaultIsArea);
	}
	
	/** This method should be called when the object is deleted. */
	@Override
	void objectDeleted(OsmData osmData) {
		super.objectDeleted(osmData);
		featureDeletedProcessing(osmData);
		
		for(OsmNode node:nodes) {
			node.removeWay(this);
		}
		nodes.clear();
		for(OsmSegmentWrapper osw:segments) {
			OsmSegment segment = osw.segment;
			segment.removeWay(this);
			if(segment.getOsmWays().isEmpty()) {
				osmData.discardSegment(segment);
			}
		}
	}
}
