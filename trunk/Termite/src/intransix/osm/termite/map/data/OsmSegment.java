package intransix.osm.termite.map.data;

import java.util.*;

/**
 *
 * @author sutter
 */
public class OsmSegment {
	
	private OsmNode node1;
	private OsmNode node2;
	private List<OsmWay> ways = new ArrayList<OsmWay>();
	
	private int filterState;
	
	public OsmSegment() {}
	
	public OsmSegment(OsmNode nodeA, OsmNode nodeB) {
		boolean inOrder = isInOrder(nodeA,nodeB);
		if(inOrder) {
			node1 = nodeA;
			node2 = nodeB;
		}
		else {
			node1 = nodeB;
			node2 = nodeA;
		}
	}
	
	public List<OsmWay> getOsmWays() {
		return ways;
	}
	
	public OsmNode getNode1() {
		return node1;
	}
	
	public OsmNode getNode2() {
		return node2;
	}
	
	public static Object getKey(OsmNode nodeA, OsmNode nodeB) {
		if(isInOrder(nodeA,nodeB)) {
			return nodeA.getId() + "|" + nodeB.getId();
		}
		else {
			return nodeB.getId() + "|" + nodeA.getId();
		}
	}
	
	public Object getKey() {
		return getKey(node1,node2);
	}
	
	public static boolean isInOrder(OsmNode nodeA, OsmNode nodeB) {
		return (nodeA.getId() < nodeB.getId());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof OsmSegment) {
			return ((((OsmSegment)obj).node1 == this.node1)&&(((OsmSegment)obj).node2 == this.node2));
		}
		else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return getKey().hashCode();
	}
	
	//------------------------
	//filter
	//-------------------------
	
	public void setFilterState(int state) {
		this.filterState = state;
	}
	
	public boolean editEnabled() {
		return ((filterState & FilterRule.EDIT_ENABLED) != 0);
	}
	
	public boolean renderEnabled() {
		return ((filterState & FilterRule.RENDER_ENABLED) != 0);
	}
	
	public int getFilterState() {
		return filterState;
	}
	
	public void bitwiseAndFilterState(int state) {
		filterState &= state;
	}
	
	public void bitwiseOrFilterState(int state) {
		filterState |= state;
	}
	
	//========================
	// Package Methods
	//========================
	
	void addWay(OsmWay way) {
		if(!ways.contains(way)) {
			ways.add(way);
		}
	}
	
	void removeWay(OsmWay way) {
		ways.remove(way);
	}
}
