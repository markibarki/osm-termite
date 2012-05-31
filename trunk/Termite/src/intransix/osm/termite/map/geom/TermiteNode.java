package intransix.osm.termite.map.geom;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import intransix.osm.termite.map.osm.*;

/**
 *
 * @author sutter
 */
public class TermiteNode extends TermiteObject {
	
	//====================
	// Properties
	//====================
	public static HashSet<String> GEOMETRIC_TAGS = new HashSet<String>(); 
	static {
		String[] temp = {TermiteLevel.TAG_ZLEVEL,TermiteLevel.TAG_STRUCTURE};
		for(String tag:temp) {
			GEOMETRIC_TAGS.add(tag);
		}
	}
	
	private Point2D point;
	
	private TermiteLevel level = null;
	private ArrayList<TermiteWay> ways = new ArrayList<TermiteWay>();
	
	private OsmNode osmNode;
	
	//====================
	// Public Methods
	//====================
	
	public int getZlevel() {
		return this.getIntProperty(TermiteLevel.TAG_ZLEVEL,TermiteLevel.INVALID_ZLEVEL);
	}
	
	public long getStructureId() {
		return this.getLongProperty(TermiteLevel.TAG_STRUCTURE,INVALID_ID);
	}
	
	//====================
	// Package Methods
	//====================
	
	/** Constructor. It should only be called by members of this package. */
	TermiteNode(long id) {
		super(id);
	}
	
	void load(OsmNode osmNode, TermiteData data) {
		this.osmNode = osmNode;
		copyProperties(osmNode);
		point = new Point2D.Double(osmNode.getLon(),osmNode.getLat());
	}
	
	void addWay(TermiteWay way) {
		this.ways.add(way);
	}
	
	void setLevel(TermiteLevel level) {
		this.level = level;
	}
	
	/** This method returns true if the given node is a feature. */
	boolean isFeature() {
		for(String tag:this.getPropertyKeys()) {
			if(!GEOMETRIC_TAGS.contains(tag)) return true;
		}
		return false;
	}
	
}
