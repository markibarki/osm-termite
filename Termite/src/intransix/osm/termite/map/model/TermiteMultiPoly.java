package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmRelation;
import java.util.ArrayList;
import intransix.osm.termite.map.osm.OsmObject;

/**
 *
 * @author sutter
 */
public class TermiteMultiPoly extends TermiteObject {
	
	private ArrayList<TermiteLevel> levels = new ArrayList<TermiteLevel>();
	
	private OsmRelation osmRelation;
	
	public void load(OsmRelation osmRelation) {
//populate this!!!
	}
	
	//=======================
	// Package Methods
	//=======================
	
	@Override
	OsmObject getOsmObject() {
		return osmRelation;
	}
}
