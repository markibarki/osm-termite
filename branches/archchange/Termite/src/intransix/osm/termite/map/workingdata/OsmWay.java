package intransix.osm.termite.map.workingdata;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.dataset.OsmWaySrc;
import java.util.ArrayList;
import java.util.List;

/**
 * This object holds the data of an OSM way. 
 * 
 * @author sutter
 */
public class OsmWay extends OsmObject<OsmWaySrc> {
	
	//=======================
	// Properties
	//=======================
		
	private List<OsmNode> nodes = new ArrayList<OsmNode>();
//	private boolean isArea;
	
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
//@TODO clean this line up
		FeatureInfo featureInfo = intransix.osm.termite.render.map.RenderLayer.getObjectFeatureInfo(this);
		boolean defaultIsArea = (featureInfo.getDefaultPath() == FeatureInfo.GEOM_TYPE_AREA);
		return this.getBooleanProperty(OsmModel.KEY_AREA,defaultIsArea);
	}
	
	/** This method returns true if the way forms a closed loop (first node equals last)
	 * This is not an indication of whether or not the objct is an area. */ 
	public boolean isClosed() {
		return ((nodes.size() > 2)&&(nodes.get(0) == nodes.get(nodes.size()-1)));
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
	
	/** This method should be called when the object is deleted. */
	@Override
	public void objectDeleted(OsmData osmData) {
		super.objectDeleted(osmData);
		
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
	
	/** Copies the source data to this instance. */
	@Override
	public void copyInto(OsmWaySrc target) {
		super.copyInto(target);
		
		List<Long> nodeIds = target.getNodeIds();
		nodeIds.clear();
		for(OsmNode node:this.getNodes()) {
			nodeIds.add(node.getId());
		}
		
		
	}
	
	/** This method copies the src data to this object. */
	@Override
	public void copyFrom(OsmWaySrc src, OsmData osmData) {
		super.copyFrom(src, osmData);
		
		List<OsmNode> nodes = this.getNodes();
		List<OsmSegmentWrapper> segments = this.getSegments();
		nodes.clear();
		OsmNode prevNode = null;
		OsmNode node;
		for(Long id:src.getNodeIds()) {
			node = osmData.getOsmNode(id, true);
			if(node != null) {
				nodes.add(node);
				node.addWay(this);
				if(prevNode != null) {
					OsmSegment segment = osmData.getOsmSegment(node,prevNode);
					OsmSegmentWrapper osw = new OsmSegmentWrapper(segment,node,prevNode);
					segments.add(osw);
					segment.addWay(this);
				}
			}
			else {
				//this shouldn't happen
				//if so, we will ignore the node
			}
			prevNode = node;
		}
	}
}
