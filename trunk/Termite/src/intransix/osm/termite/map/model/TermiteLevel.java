package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.prop.FeatureInfo;
import java.util.*;

/**
 * This class encapsulates the data associated with a level in a structure. 
 * 
 * @author sutter
 */
public class TermiteLevel extends TermiteObject {
	
	//=========================
	// Properties
	//=========================
	
	public final static int DEFAULT_ZLEVEL = 0;
	public final static int INVALID_ZLEVEL = Integer.MIN_VALUE;
	
	private TermiteStructure structure;
	private ArrayList<TermiteNode> nodes = new ArrayList<TermiteNode>();
	private ArrayList<TermiteWay> ways = new ArrayList<TermiteWay>();
	private int zlevel = DEFAULT_ZLEVEL;
	
	//valid only for method 1
	private OsmRelation osmRelation;
	private OsmWay shell;
	
	//=========================
	// Public Methods
	//=========================
	
	/** This method gets the structure associated with the level. */
	public TermiteStructure getStructure() {
		return structure;
	}
	
	/** This method gets the zlevel value associated with the level. */
	public int getZlevel() {
		return zlevel;
	}
	
	/** This method sorts the features by draw order. */
	public void orderFeatures() {
		//sort the ways
		Collections.sort(ways,new TermiteComparator());
		//don't bother sorting the nodes
	}
	
	/** This method returns the nodes on the level. */
	public List<TermiteNode> getNodes() {
		return nodes;
	}
	
	/** This method returns the ways on the level. */
	public List<TermiteWay> getWays() {
		return ways;
	}
	
	//====================
	// Package Methods
	//====================
	
	/** This method sets the structure object. */
	void setStructure(TermiteStructure structure) {
		this.structure = structure;
	}
	
	/** This method loads the relation from the OsmRelation object. */
	void loadFromRelation(OsmRelation osmRelation, TermiteData termiteData) {
		this.osmRelation = osmRelation;
		OsmData osmData = termiteData.getWorkingData();
		
		//load members
		for(OsmMember osmMember:osmRelation.getMembers()) {
			OsmObject member =  osmData.getOsmObject(osmMember.memberId, osmMember.type);
			if(member != null) {
				if(OsmModel.ROLE_FEATURE.equalsIgnoreCase(osmMember.role)) {
					//method 1 - features collected by the level relation
					//load features in level relation
					if(member instanceof OsmNode) {
						TermiteNode termiteNode = ((OsmNode)member).getTermiteNode();
						termiteNode.setLevel(this);
						this.addNode(termiteNode);
					}
					else if(member instanceof OsmWay) {
						//create a virutal feature for this way
						TermiteWay termiteWay = ((OsmWay)member).getTermiteWay();
						termiteWay.addLevel(this);
						this.addWay(termiteWay);
					}
				}
				else if(OsmModel.ROLE_SHELL.equalsIgnoreCase(osmMember.role)) {
					if(member instanceof OsmWay) {
						this.shell = (OsmWay)member;
						TermiteWay termiteWay = shell.getTermiteWay();
						termiteWay.addLevel(this);
						this.addWay(termiteWay);
					}
				}
			}
		}
	}
	
	/** This method loads the level from the shell object. It is used when the 
	 * shell defines the level properties as opposed to a separate relation. */
	void loadFromShell(OsmWay osmShell, OsmData data) {
		this.osmRelation = null;
		this.shell = osmShell;
		TermiteWay termiteWay = shell.getTermiteWay();
		termiteWay.addLevel(this);
		this.addWay(termiteWay);
		
		this.zlevel = shell.getIntProperty(OsmModel.KEY_ZLEVEL,DEFAULT_ZLEVEL);
	}
	
	/** This method adds a node to the level. */
	void addNode(TermiteNode node) {
		if(nodes.contains(node)) return;
		nodes.add(node);
	}
	
	/** This method adds a way to the level. */
	void addWay(TermiteWay way) {
		if(ways.contains(way)) return;
		ways.add(way);
	}
	
	/** This method returns the OsmObject associate with the level. */
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
