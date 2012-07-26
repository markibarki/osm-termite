package intransix.osm.termite.map.data;

import java.util.List;

/**
 * This class removes a node from a way.
 * 
 * @author sutter
 */
public class UpdateRemoveNode extends EditData<OsmWay> {

	//========================
	// Properties
	//========================
	
	private int index;
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor
	 * 
	 * @param index			The index of the member to remove. This refers to 
	 *						the index at the time the instruction is executed
	 *						and not when the instruction is created. If another
	 *						node is added or removed, this can change the index
	 *						at instruction execution time relative to the index when
	 *						at the time before the action is executed.
	 */
	public UpdateRemoveNode(int nodeIndex) {
		this.index = nodeIndex;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	EditData<OsmWay> readInitialData(OsmData osmData, OsmWay way) throws UnchangedException {
		List<OsmNode> nodes = way.getNodes();
		if(index >= nodes.size()) {
			throw new UnchangedException("Invalid node index for way: " + way.getId());
		}
		OsmNode node = nodes.get(index);
		UpdateInsertNode undoUpdate = new UpdateInsertNode(node.getId(),index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	void writeData(OsmData osmData, OsmWay way, int editNumber) throws UnchangedException, Exception {
		//remove node from osm way
		List<OsmNode> nodes = way.getNodes();
		if(index >= nodes.size()) {
			throw new UnchangedException("Invalid node index for way: " + way.getId());
		}
		
		//get the neighboring nodes
		OsmNode prevNode = null;
		OsmNode nextNode = null;
		if(index > 0) {
			prevNode = nodes.get(index-1);
		}
		if(index < nodes.size()-1) {
			nextNode = nodes.get(index+1);
		}
		
		//remove the node
		OsmNode removedNode = nodes.remove(index);
		
		//remove the way from the node if it was in the way once
		if(!nodes.contains(removedNode)) {
			removedNode.removeWay(way);
		}
		
		//update the segments
		List<OsmSegmentWrapper> osws = way.getSegments();
		if(nextNode != null) {
			OsmSegmentWrapper removeOsw = osws.remove(index);
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
		if((prevNode != null)&&(nextNode != null)) {
			OsmSegment segment = osmData.getOsmSegment(prevNode,nextNode);
			OsmSegmentWrapper osw = new OsmSegmentWrapper(segment,prevNode,nextNode);
			osws.add(index-1, osw);
			segment.addWay(way);
		}
		
		//update version
		way.setDataVersion(osmData,editNumber);
		way.setContainingObjectDataVersion(osmData,editNumber);
	}
}
