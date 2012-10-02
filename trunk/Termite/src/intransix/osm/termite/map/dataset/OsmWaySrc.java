package intransix.osm.termite.map.dataset;

import intransix.osm.termite.app.mapdata.download.MapDataRequest;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.workingdata.OsmSegment;
import intransix.osm.termite.map.workingdata.OsmSegmentWrapper;
import intransix.osm.termite.map.workingdata.OsmWay;
import java.util.*;
import org.xml.sax.Attributes;

/**
 * This object holds the data of an OSM way. 
 * @author sutter
 */
public class OsmWaySrc extends OsmSrcData {
	
	//=======================
	// Properties
	//=======================
		
	private List<Long> nodeIds = new ArrayList<Long>();

	//=======================
	// Public Methods
	//=======================
	
	/** Constructor. */
	public OsmWaySrc() {
		super(OsmModel.TYPE_WAY,OsmData.INVALID_ID);
	}
	
	/** This method gets the nodes for this way. It can be used to populate the
	 * nodes in the way. */
	public List<Long> getNodeIds() {
		return nodeIds;
	}
	
	/** This adds a node to the end of the way. */
	public void addNodeId(long nodeId) {
		nodeIds.add(nodeId);
	}
	
	//------------------------------
	// Commit Methods
	//-----------------------------
	
	public boolean isDifferent(OsmWay osmWay) {
		//compare node lists
		List<OsmNode> nodes = osmWay.getNodes();
		int cnt = nodes.size();
		if(cnt != nodeIds.size()) return true;
		
		long id1;
		long id2;
		for(int i = 0; i < cnt; i++) {
			id1 = nodeIds.get(i);
			id2 = nodes.get(i).getId();
			if(id1 != id2) return true;
		}

		//compare properties
		return propertiesDifferent(osmWay);
	}

	//====================
	// Package Methods
	//====================
	
	public OsmWaySrc(long id) {
		super(OsmModel.TYPE_WAY,id);
	}

}
