package intransix.osm.termite.map.osm;

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
	
	private OsmData osmData;
	private int index;
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor
	 * 
	 * @param osmData		The data manager
	 * @param nodeIndex		The index of the node to remove
	 */
	public UpdateRemoveNode(OsmData osmData, int nodeIndex) {
		this.osmData = osmData;
		this.index = nodeIndex;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	EditData<OsmWay> readInitialData(OsmWay way) throws UnchangedException {
		List<OsmNode> nodes = way.getNodes();
		if(index >= nodes.size()) {
			throw new UnchangedException("Invalid node index for way: " + way.getId());
		}
		OsmNode node = nodes.get(index);
		UpdateInsertNode undoUpdate = new UpdateInsertNode(osmData,node.getId(),index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	void writeData(OsmWay way, int editNumber) throws UnchangedException, Exception {
		//remove node from osm way
		List<OsmNode> nodes = way.getNodes();
		if(index >= nodes.size()) {
			throw new UnchangedException("Invalid node index for way: " + way.getId());
		}
		OsmNode removedNode = nodes.remove(index);
		
		//remove the way from the node if it was in the way once
		if(!nodes.contains(removedNode)) {
			removedNode.removeWay(way);
		}
		
		//update version
		way.setDataVersion(editNumber);
		way.setContainingObjectDataVersion(editNumber);
	}
}