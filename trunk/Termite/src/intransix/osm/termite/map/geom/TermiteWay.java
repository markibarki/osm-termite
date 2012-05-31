package intransix.osm.termite.map.geom;

import java.util.ArrayList;
import intransix.osm.termite.map.osm.*;

/**
 *
 * @author sutter
 */
public class TermiteWay extends TermiteObject {
	
	private ArrayList<TermiteNode> nodes = new ArrayList<TermiteNode>();
	
//in theory there could be multiple features, but there is not supposed to be
	private TermiteFeature feature = null;
	
	private OsmWay osmWay;
	
	public TermiteFeature getFeature() {
		return feature;
	}
	
	//====================
	// Package Methods
	//====================
	
	/** Constructor. It should only be called by members of this package. */
	TermiteWay(long id) {
		super(id);
	}
	
	void load(OsmWay osmWay, TermiteData data) {
		this.osmWay = osmWay;
		copyProperties(osmWay);
		
		//load members
		for(OsmNode osmNode:osmWay.getNodes()) {
			TermiteNode termiteNode = data.getTermiteNode(osmNode.getId(), false);
			addNode(termiteNode);
		}
	}
	
	void addNode(TermiteNode node) {
		this.nodes.add(node);
		node.addWay(this);
	}
	
	void setFeature(TermiteFeature feature) {
		this.feature = feature;
	}
	
}
