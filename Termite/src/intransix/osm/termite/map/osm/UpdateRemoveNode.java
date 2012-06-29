package intransix.osm.termite.map.osm;

import java.util.List;

/**
 *
 * @author sutter
 */
public class UpdateRemoveNode implements EditData<OsmWay> {

	private OsmData osmData;
	private int index;
	
	public UpdateRemoveNode(OsmData osmData, int nodeIndex) {
		this.osmData = osmData;
		this.index = nodeIndex;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	public EditData<OsmWay> readInitialData(OsmWay way) throws UnchangedException {
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
	public void writeData(OsmWay way, int editNumber) throws UnchangedException, Exception {
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
