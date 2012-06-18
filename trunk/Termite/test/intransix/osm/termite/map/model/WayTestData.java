/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.map.model;

import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.osm.*;
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
	public long structureId;
	public List<Integer> levelIds = new ArrayList<Integer>();

	@Override
	public void validate() {
			
		//check existence
		OsmWay oWay = osmData.getOsmWay(id);
		TermiteWay tWay = termiteData.getWay(id);
		assert(oWay == tWay.getOsmObject());
		assert(tWay == oWay.getTermiteObject());
		
		//check location
		assert(oWay.getId() == id);
		
		//check levels
		List<TermiteLevel> levels = tWay.getLevels();
		assert(levels.size() == levelIds.size());
		for(TermiteLevel level:levels) {
			Integer zlevel = level.getZlevel();
			assert(levelIds.contains(zlevel));
		}
		
		//check nodes
		List<Long> oNodeIds = oWay.getNodeIds();
		assert(oNodeIds.size() == nodeIds.size());
		int index = 0;
		for(Long nid:nodeIds) {
			assert(nid.equals(oNodeIds.get(index++)));
		}
		List<TermiteNode> nodes = tWay.getNodes();
		assert(nodes.size() == nodeIds.size());
		index = 0;
		for(TermiteNode tNode:nodes) {
			OsmNode oNode = tNode.getOsmObject();
			Long nid = oNode.getId();
			assert(nodeIds.get(index++).equals(nid));
		}
		
		//check properties - both directions to make sure they are the same
		baseValidate(tWay,oWay);
		
	}
	
	@Override
	public void validateDeleted() {
		
		//check it is not in the data
		assert(osmData.getOsmWay(id) == null);
		assert(termiteData.getWay(id) == null);
		
		//check levels
		for(int lid:levelIds) {
			TermiteLevel level = termiteData.getLevel(structureId, lid);
			for(TermiteWay tw:level.getWays()) {
				OsmWay ow = tw.getOsmObject();
				long owid = ow.getId();
				assert(owid != id);
			}
		}
		
		//check nodes
		for(long nid:nodeIds) {
			TermiteNode node = termiteData.getNode(nid);
			for(TermiteWay tw:node.getWays()) {
				OsmWay ow = tw.getOsmObject();
				long owid = ow.getId();
				assert(owid != id);
			}
		}
	}
}
