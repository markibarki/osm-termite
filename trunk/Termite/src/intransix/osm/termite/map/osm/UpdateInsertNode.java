package intransix.osm.termite.map.osm;

import java.util.List;

/**
 *
 * @author sutter
 */
public class UpdateInsertNode extends EditData<OsmWay> {
	
	private long nodeId;
	private int index;
	
	public UpdateInsertNode(long nodeId, int nodeIndex) {
		this.nodeId = nodeId;
		this.index = nodeIndex;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	public EditData<OsmWay> readInitialData(OsmWay way) throws UnchangedException {
		UpdateRemoveNode undoUpdate = new UpdateRemoveNode(index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	public void writeData(OsmWay way) throws UnchangedException, Exception {
		//set the property
		List<Long> nodeIds = way.getNodeIds();
		nodeIds.add(index,nodeId);
	}
}
