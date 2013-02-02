package intransix.osm.termite.map.workingdata;

import intransix.osm.termite.app.filter.FilterRule;
import java.util.*;

/**
 *
 * @author sutter
 */
public class OsmSegment {
	
	private OsmNode node1;
	private OsmNode node2;
	private List<OsmWay> ways = new ArrayList<OsmWay>();
	
	private Object renderData;
	private Object editData;
	
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
	
	//---------------------------
	// Piggyback methods
	//---------------------------
	

	/** This method returns the render data.  */
	public Object getRenderData() {
		return renderData;
	}
	
	/** This method sets the render data. The render data is an arbitrary object
	 * that is set by the rendering software. */
	public void setRenderData(Object renderData) {
		this.renderData = renderData;
	}
	
	/** This method returns the edit data. */
	public Object getEditData() {
		return editData;
	}
	
	/** This method sets the edit data. The edit data is an arbitrary object
	 * that is set by the edit software.  */
	public void setEditData(Object editData) {
		this.editData = editData;
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
	
	//========================
	// Package Methods
	//========================
	
	public void addWay(OsmWay way) {
		if(!ways.contains(way)) {
			ways.add(way);
		}
	}
	
	public void removeWay(OsmWay way) {
		ways.remove(way);
	}
}
