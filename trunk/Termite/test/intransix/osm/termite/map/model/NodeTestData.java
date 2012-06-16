package intransix.osm.termite.map.model;

import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.osm.OsmNode;
import intransix.osm.termite.map.osm.OsmWay;
import java.util.ArrayList;
import java.util.HashMap;
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
	public long structureId;
	public int zlevel;
	public List<Long> wayIds = new ArrayList<Long>();

	
	/** This method validates the content of a node. */
	@Override
	public void validate() {
		
		//check existence
		OsmNode oNode = osmData.getOsmNode(id);
		TermiteNode tNode = termiteData.getNode(id);
		assert(oNode == tNode.getOsmObject());
		assert(tNode == oNode.getTermiteObject());
		
		//check location
		assert(oNode.getId() == id);
		assert(oNode.getX() == x);
		assert(oNode.getY() == y);
		
		//check level
		TermiteLevel level = termiteData.getLevel(structureId,zlevel);
		assert(tNode.getLevel() == level);
		
		//check ways
		List<TermiteWay> ways = tNode.getWays();
		assert(ways.size() == wayIds.size());
		for(TermiteWay tWay:ways) {
			OsmWay oWay = tWay.getOsmObject();
			Long wid = oWay.getId();
			assert(wayIds.contains(wid));
		}
		
		baseValidate(tNode,oNode);
	}
	
	/** This method validates a node was deleted. */
	@Override
	public void validateDeleted() {
		
		//check it is not in the data
		assert(osmData.getOsmNode(id) == null);
		assert(termiteData.getNode(id) == null);
		
		//check no on level
		TermiteLevel level = termiteData.getLevel(structureId,zlevel);
		for(TermiteNode tn: level.getNodes()) {
			OsmNode on = tn.getOsmObject();
			long nid = on.getId();
			assert(nid != id);
		}
		
		//check not in ways
		Long nid = (Long)id;
		for(Long wid:wayIds) {
			TermiteWay tWay = termiteData.getWay(wid);
			OsmWay oWay = tWay.getOsmObject();
			List<Long> onids = oWay.getNodeIds();
			assert(!onids.contains(nid));
			for(TermiteNode tNode:tWay.getNodes()) {
				OsmNode oNode = tNode.getOsmObject();
				long tnid = oNode.getId();
				assert(tnid != id);
			}
		}
	}
	
}
