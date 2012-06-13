/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmWay;
import java.util.List;

/**
 *
 * @author sutter
 */
public class UpdateRemoveNode implements EditData<OsmWay> {

	private int index;
	
	public UpdateRemoveNode(int nodeIndex) {
		this.index = nodeIndex;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	public EditData<OsmWay> readInitialData(OsmWay way) throws UnchangedException {
		List<Long> nodeIds = way.getNodeIds();
		if(nodeIds.size() <= index) {
			throw new UnchangedException("Invalid node index for way: " + way.getId());
		}
		long nodeId = nodeIds.get(index);
		UpdateInsertNode undoUpdate = new UpdateInsertNode(nodeId, index);
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
		if(nodeIds.size() <= index) {
			throw new UnchangedException("Invalid node index for way: " + way.getId());
		}
		nodeIds.remove(index);
	}
}
