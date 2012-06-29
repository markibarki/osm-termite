package intransix.osm.termite.map.model;

import intransix.osm.termite.map.data.OsmWay;
import intransix.osm.termite.map.data.OsmNode;
import java.util.*;

/**
 * This is a class for unit testing way editing. It holds the reference way data which
 * the developer should manually set. It can then be used to compare with the result of the edit 
 * operations being tested. A test method is included.
 * 
 * This class allows the developer to track the state of the way over many operations,
 * simplifying writing the tests.
 * 
 * @author sutter
 */
public class WayTestData extends ObjectTestData {
	
	public List<Long> nodeIds = new ArrayList<Long>();

	@Override
	public void validate() {
			
		//check existence
		OsmWay way = osmData.getOsmWay(id);
		
		//check id
		assert(way.getId() == id);
		
		//check nodes
		List<OsmNode> nodes = way.getNodes();
		assert(nodes.size() == nodeIds.size());
		int index = 0;
		for(Long nid:nodeIds) {
			OsmNode node = nodes.get(index++);
			Long id = node.getId();
			assert(nid.equals(id));
		}
		
		//check properties - both directions to make sure they are the same
		baseValidate(way);
	}
	
	@Override
	public void validateDeleted() {
		
		//check it is not in the data
		assert(osmData.getOsmWay(id) == null);
		
		//check nodes no longer reference this way
		for(long nid:nodeIds) {
			OsmNode node = osmData.getOsmNode(nid);
			for(OsmWay ow:node.getWays()) {
				long owid = ow.getId();
				assert(owid != id);
			}
		}
	}
}
