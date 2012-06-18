package intransix.osm.termite.map.model;

import java.util.*;
import intransix.osm.termite.map.osm.*;
/**
 *
 * @author sutter
 */
public class LevelTestData {
	//this does not extend from ObjectTestData because it is not an osm object
	public long structureId;
	public int zlevel;
	
	public ArrayList<Long> nodeIds = new ArrayList<Long>();
	public ArrayList<Long> wayIds = new ArrayList<Long>();
	
	
	public void validate() {
		TermiteLevel level = ObjectTestData.termiteData.getLevel(structureId, zlevel);
		
		//check nodes
		int index = 0;
		assert(nodeIds.size() == level.getNodes().size());
		for(TermiteNode tNode:level.getNodes()) {
			OsmNode oNode = tNode.getOsmObject();
			assert(nodeIds.get(index++).equals(oNode.getId()));
		}
		
		//check ways
		index = 0;
		assert(wayIds.size() == level.getWays().size());
		for(TermiteWay tWay:level.getWays()) {
			OsmWay oWay = tWay.getOsmObject();
			assert(wayIds.get(index++).equals(oWay.getId()));
		}
	}
	
	
}
