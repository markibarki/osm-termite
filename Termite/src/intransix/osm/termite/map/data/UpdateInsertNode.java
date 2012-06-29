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
	
	private OsmData osmData;
	private long nodeId;
	private int index;
	
	//========================
	// Constructor
	//========================
	
	/** Constructor. 
	 * 
	 * @param osmData		The data manager
	 * @param nodeId		The id of the node to add
	 * @param nodeIndex		The index at which to add the node
	 */
	public UpdateInsertNode(OsmData osmData, long nodeId, int nodeIndex) {
		this.osmData = osmData;
		this.nodeId = nodeId;
		this.index = nodeIndex;
	}
	
	/** Constructor to add the node at the end of the list
	 * 
	 * @param osmData		The data manager
	 * @param nodeId		The id of the node to add
	 * @param nodeIndex		The index at which to add the node
	 */
	public UpdateInsertNode(OsmData osmData, long nodeId) {
		this.osmData = osmData;
		this.nodeId = nodeId;
		this.index = INVALID_INDEX;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	EditData<OsmWay> readInitialData(OsmWay way) throws UnchangedException {
		UpdateRemoveNode undoUpdate = new UpdateRemoveNode(osmData,index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	void writeData(OsmWay way, int editNumber) throws UnchangedException, Exception {
		//set the property
		List<OsmNode> nodes = way.getNodes();
		if(index == INVALID_INDEX) {
			index = nodes.size();
		}
		if((index < 0)||(index > nodes.size())) {
			throw new UnchangedException("Invalid node index for way: " + way.getId());
		}
		OsmNode node = osmData.getOsmNode(nodeId, true);
		if(node != null) {
			nodes.add(index,node);
			node.addWay(way);
		}
		else {
			//this should never happen
			throw new UnchangedException("An unknown error occurred looking up or creating node: " + nodeId);
		}
		
		//update version
		way.setDataVersion(editNumber);
		way.setContainingObjectDataVersion(editNumber);
	}
}
