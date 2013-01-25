package intransix.osm.termite.map.model;

import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.workingdata.OsmWay;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a class for unit testing node editing. It holds the reference node
 * data which the developer should manually set. It can then be used to compare
 * with the result of the edit operations being tested. A test method is
 * included.
 *
 * This class allows the developer to track the state of the node over many
 * operations, simplifying writing the tests.
 *
 * @author sutter
 */
public class NodeTestData extends ObjectTestData {

	public double x;
	public double y; 
	public List<Long> wayIds = new ArrayList<Long>();

	
	/** This method validates the content of a node. */
	@Override
	public void validate() {
		
		//check existence
		OsmNode node = osmData.getOsmNode(id);
		
		//check location
		assert(node.getId() == id);
		assert(node.getPoint().getX() == x);
		assert(node.getPoint().getY() == y);
		
		//check ways
		List<OsmWay> ways = node.getWays();
		assert(ways.size() == wayIds.size());
		for(OsmWay way:ways) {
			Long wid = way.getId();
			assert(wayIds.contains(wid));
		}
		
		baseValidate(node);
	}
	
	/** This method validates a node was deleted. */
	@Override
	public void validateDeleted() {
		
		//check it is not in the data
		assert(osmData.getOsmNode(id) == null);
		
		//check not in ways
		for(Long wid:wayIds) {
			OsmWay way = osmData.getOsmWay(wid);
			for(OsmNode node:way.getNodes()) {
				long nid = node.getId();
				assert(nid != id);
			}
		}
	}
	
}
