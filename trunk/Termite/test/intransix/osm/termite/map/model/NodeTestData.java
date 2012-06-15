package intransix.osm.termite.map.model;

import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.osm.OsmNode;
import intransix.osm.termite.map.osm.OsmWay;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
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
	public void validateNode() {
		
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
		
		//check properties - both directions to make sure they are the same
		checkProperties(oNode,props);
		
		FeatureInfo fi = tNode.getFeatureInfo();
		if(fi == null) assert(featureInfoName == null);
		else assert(featureInfoName.equals(fi.getName()));
		
		assert(oNode.getLocalVersion() >= minOsmVersion);
		assert(tNode.getTermiteLocalVersion() >= minTermiteVersion);
	}
	
	/** This method validates a node was deleted. */
	void validateNodeDelete() {
		
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
