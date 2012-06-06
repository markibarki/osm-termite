package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.prop.FeatureInfo;
import java.util.*;

/**
 *
 * @author sutter
 */
public class TermiteLevel extends TermiteObject {
	
	public final static int DEFAULT_ZLEVEL = 0;
	public final static int INVALID_ZLEVEL = Integer.MIN_VALUE;
	
	private TermiteStructure structure;
	private ArrayList<TermiteNode> nodes = new ArrayList<TermiteNode>();
	private ArrayList<TermiteWay> ways = new ArrayList<TermiteWay>();
	private int zlevel = DEFAULT_ZLEVEL;
	
	//valid only for method 1
	private OsmRelation osmRelation;
	private OsmWay shell;
	
	public TermiteStructure getStructure() {
		return structure;
	}
	
	public int getZlevel() {
		return zlevel;
	}
	
	public void orderFeatures() {
		//sort the ways
		Collections.sort(ways,new TermiteComparator());
		//don't bother sorting the nodes
	}
	
	public List<TermiteNode> getNodes() {
		return nodes;
	}
	
	public List<TermiteWay> getWays() {
		return ways;
	}
	
	//====================
	// Package Methods
	//====================

	ArrayList<TermiteNode> getTermiteNodes() {
		return nodes;
	}
	
	void setStructure(TermiteStructure structure) {
		this.structure = structure;
	}
	
	void loadFromRelation(OsmRelation osmRelation, TermiteData data) {
		this.osmRelation = osmRelation;
		
		//load members
		for(OsmMember osmMember:osmRelation.getMembers()) {
			OsmObject member = osmMember.member;
			if(osmMember.role.equalsIgnoreCase(OsmModel.ROLE_FEATURE)) {
				//method 1 - features collected by the level relation
				//load features in level relation
				if(member instanceof OsmNode) {
					TermiteNode termiteNode = ((OsmNode)member).getTermiteNode();
					termiteNode.setLevel(this);
				}
				else if(member instanceof OsmWay) {
					//create a virutal feature for this way
					TermiteWay termiteWay = ((OsmWay)member).getTermiteWay();
					termiteWay.addLevel(this);
				}
				else {
//add support for multipoly
				}
			}
			else if(osmMember.role.equalsIgnoreCase(OsmModel.ROLE_SHELL)) {
				if(member instanceof OsmWay) {
					this.shell = (OsmWay)member;
					TermiteWay termiteWay = shell.getTermiteWay();
					termiteWay.addLevel(this);
				}
			}
		}
	}
	
	/** This method is used for method 2 - level specified by node properties -
	 * To create a level object from the shell for a given level. Here we need the
	 * OSM object because the Termite object may not have been loaded yet. */
	void loadFromShell(OsmWay osmShell, TermiteData data) {
		this.osmRelation = null;
		this.shell = osmShell;
		TermiteWay termiteWay = shell.getTermiteWay();
		termiteWay.addLevel(this);
		
		this.zlevel = shell.getIntProperty(OsmModel.KEY_ZLEVEL,DEFAULT_ZLEVEL);
	}
	
	void addNode(TermiteNode node) {
		if(nodes.contains(node)) return;
		nodes.add(node);
	}
	
	void addWay(TermiteWay way) {
		if(ways.contains(way)) return;
		ways.add(way);
	}
	
	@Override
	OsmObject getOsmObject() {
		return osmRelation;
	}
	
	/** This comparator sorts features by zorder. */
	public class TermiteComparator implements Comparator<TermiteObject> {
		
		@Override
		public int compare(TermiteObject f1, TermiteObject f2) {
			FeatureInfo fi1 = f1.getFeatureInfo();
			int zorder1;
			if(fi1 != null) zorder1 = fi1.getZorder();
			else zorder1 = FeatureInfo.DEFAULT_ZORDER;
			
			FeatureInfo fi2 = f2.getFeatureInfo();
			int zorder2;
			if(fi2 != null) zorder2 = fi2.getZorder();
			else zorder2 = FeatureInfo.DEFAULT_ZORDER;
			
			return zorder1 - zorder2;
		}
	}

}
