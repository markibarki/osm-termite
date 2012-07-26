package intransix.osm.termite.map.data;

import java.util.List;

/**
 * This method inserts a node into a way.
 * 
 * @author sutter
 */
public class UpdateInsertNode extends EditData<OsmWay> {
	
	//========================
	// Properties
	//========================
	
	private final static int INVALID_INDEX = -1;
	
	private long nodeId;
	private int index;
	
	//========================
	// Constructor
	//========================
	
	/** Constructor. 
	 * 
	 * @param nodeId		The id of the node to add
	 * @param nodeIndex		The index at which to add the node. This refers to 
	 *						the index at the time the instruction is exectued, not
	 *						at the time the instruction is created.
	 */
	public UpdateInsertNode(long nodeId, int nodeIndex) {
		this.nodeId = nodeId;
		this.index = nodeIndex;
	}
	
	/** Constructor to add the node at the end of the list
	 * 
	 * @param nodeId		The id of the node to add
	 * @param nodeIndex		The index at which to add the node
	 */
	public UpdateInsertNode(long nodeId) {
		this.nodeId = nodeId;
		this.index = INVALID_INDEX;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	EditData<OsmWay> readInitialData(OsmData osmData, OsmWay way) throws UnchangedException {
		UpdateRemoveNode undoUpdate = new UpdateRemoveNode(index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	void writeData(OsmData osmData, OsmWay way, int editNumber) throws UnchangedException, Exception {
		//set the property
		List<OsmNode> nodes = way.getNodes();
		if(index == INVALID_INDEX) {
			index = nodes.size();
		}
		if((index < 0)||(index > nodes.size())) {
			throw new UnchangedException("Invalid node index for way: " + way.getId());
		}

		//get the neighboring nodes
		OsmNode prevNode = null;
		OsmNode nextNode = null;
		if(index > 0) {
			prevNode = nodes.get(index-1);
		}
		if(index < nodes.size()) {
			nextNode = nodes.get(index);
		}
		
		//update the nodes
		OsmNode node = osmData.getOsmNode(nodeId, true);
		if(node != null) {
			nodes.add(index,node);
			node.addWay(way);
		}
		else {
			//this should never happen
			throw new UnchangedException("An unknown error occurred looking up or creating node: " + nodeId);
		}
		
		//update the segments
		List<OsmSegmentWrapper> osws = way.getSegments();
		if((prevNode != null)&&(nextNode != null)) {
			OsmSegmentWrapper removeOsw = osws.remove(index-1);
			OsmSegment removeSegment = removeOsw.segment;
			//make sure there are no more copies of this segment in this way
			//this shouldn't happen in theory
			boolean moreCopies = false;
			for(OsmSegmentWrapper t:osws) {
				if(t.segment == removeSegment) moreCopies = true;
				break;
			}
			if(!moreCopies) {
				removeSegment.removeWay(way);
				//check if there are no references to this way
				if(removeSegment.getOsmWays().isEmpty()) {
					osmData.discardSegment(removeSegment);
				}
			}
		}
		if(prevNode != null) {
			OsmSegment segment = osmData.getOsmSegment(prevNode, node);
			OsmSegmentWrapper osw = new OsmSegmentWrapper(segment,prevNode,node);
			osws.add(index-1, osw);
			segment.addWay(way);
		}
		if(nextNode != null) {
			OsmSegment segment = osmData.getOsmSegment(node,nextNode);
			OsmSegmentWrapper osw = new OsmSegmentWrapper(segment,node,nextNode);
			osws.add(index, osw);
			segment.addWay(way);
		}
		
		//update version
		way.setDataVersion(osmData,editNumber);
		way.setContainingObjectDataVersion(osmData,editNumber);
	}
}
