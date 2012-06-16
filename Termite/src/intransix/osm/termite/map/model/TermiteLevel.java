package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.*;

/**
 * This class encapsulates the data associated with a level in a structure. 
 * 
 * @author sutter
 */
public class TermiteLevel extends TermiteObject<OsmWay> {
	
	//=========================
	// Properties
	//=========================
	
	public final static int DEFAULT_ZLEVEL = 0;
	public final static int INVALID_ZLEVEL = Integer.MIN_VALUE;
	
	private TermiteStructure structure;
	private ArrayList<TermiteNode> nodes = new ArrayList<TermiteNode>();
	private ArrayList<TermiteWay> ways = new ArrayList<TermiteWay>();
	private int zlevel = DEFAULT_ZLEVEL;
	
	private int versionOfLatestSort = TermiteObject.INVALID_TERMITE_VERSION;
	
	//the outline object for the level
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
	public void checkFeatureSort() {
		if(versionOfLatestSort != this.getDataVersion()) {
			//sort the ways
			Collections.sort(ways,new TermiteComparator());
			//don't bother sorting the nodes
		}
	}
	
	/** This method returns the nodes on the level. */
	public List<TermiteNode> getNodes() {
		return nodes;
	}
	
	/** This method returns the ways on the level. */
	public List<TermiteWay> getWays() {
		return ways;
	}
	
	/** This method returns the OsmObject associate with the level. */
	@Override
	public OsmWay getOsmObject() {
		return shell;
	}
	
	//====================
	// Package Methods
	//====================
	
	/** This method sets the structure object. */
	void setStructure(TermiteStructure structure, int zlevel) {
		this.structure = structure;
		this.zlevel = zlevel;
	}
	
	/** This method loads the level from the shell object. It is used when the 
	 * shell defines the level properties as opposed to a separate relation. */
	void setOsmWayShell(OsmWay osmShell) {
		this.shell = osmShell;
	}
	
	void init(TermiteData termiteData, OsmWay shell) {
		this.shell = shell;
	}
	
	void objectDeleted(TermiteData termiteData) {
//do something here once I figure out how levels are deleted		
	}
	
	/** This method adds a node to the level. */
	void addNode(TermiteNode node) {
		if(nodes.contains(node)) return;
		nodes.add(node);
		incrementDataVersion();
	}
	
	void removeNode(TermiteNode node) {
		nodes.remove(node);
		incrementDataVersion();
	}
	
	/** This method adds a way to the level. */
	void addWay(TermiteWay way) {
		if(ways.contains(way)) return;
		ways.add(way);
		incrementDataVersion();
	}
	
	void removeWay(TermiteWay way) {
		ways.remove(way);
		incrementDataVersion();
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
