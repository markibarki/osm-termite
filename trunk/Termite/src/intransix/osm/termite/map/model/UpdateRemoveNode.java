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

	private TermiteData termiteData;
	private int index;
	
	public UpdateRemoveNode(TermiteData termiteData, int nodeIndex) {
		this.termiteData = termiteData;
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
		UpdateInsertNode undoUpdate = new UpdateInsertNode(termiteData,nodeId,index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	public void writeData(OsmWay way) throws UnchangedException, Exception {
		//remove node from osm way
		List<Long> nodeIds = way.getNodeIds();
		if(nodeIds.size() <= index) {
			throw new UnchangedException("Invalid node index for way: " + way.getId());
		}
		Long idRemoved = nodeIds.remove(index);
		
		//remove node from termite way
		TermiteWay termiteWay = (TermiteWay)way.getTermiteObject();
		List<TermiteNode> nodes = termiteWay.getNodes();
		TermiteNode removedNode = nodes.remove(index);
		 
		//check to make sure that node is still not there, at another index
		if(!nodeIds.contains(idRemoved)) {			
			TermiteNode node = termiteData.getNode(idRemoved);
			if(node != null) {
				node.removeWay(termiteWay);
			}
		}
		
		//check levels
		TermiteLevel removedNodeLevel = removedNode.getLevel();
		boolean removeLevel = true;
		for(TermiteNode node:termiteWay.getNodes()) {
			if(node.getLevel() == removedNodeLevel)removeLevel = false;
		}
		if(removeLevel) {
			termiteWay.getLevels().remove(removedNodeLevel);
			removedNodeLevel.removeWay(termiteWay);
		}
		
		//flag as updated
		termiteWay.incrementDataVersion();
	}
}
