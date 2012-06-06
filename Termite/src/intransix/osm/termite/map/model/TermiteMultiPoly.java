package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmRelation;
import java.util.ArrayList;
import intransix.osm.termite.map.osm.OsmObject;

/**
 * This class encapsulates a mutlipolygon. 
 * @author sutter
 */
public class TermiteMultiPoly extends TermiteObject {
	
	//===============
	// Properties
	//===============
	
	private ArrayList<TermiteLevel> levels = new ArrayList<TermiteLevel>();
	
	private OsmRelation osmRelation;
	
	//===============
	// Public Methods
	//===============
	
	/** This method loads the object from the osm relation. */
	public void load(OsmRelation osmRelation) {
//populate this!!!
	}
	
	//=======================
	// Package Methods
	//=======================
	
	/** This method gets the OSM relation associated with this multipolygon. */
	@Override
	OsmObject getOsmObject() {
		return osmRelation;
	}
}
