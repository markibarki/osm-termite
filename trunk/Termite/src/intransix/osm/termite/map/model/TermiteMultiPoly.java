package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmRelation;
import java.util.*;
import intransix.osm.termite.map.osm.*;

/**
 * This class encapsulates a mutlipolygon. 
 * @author sutter
 */
public class TermiteMultiPoly extends TermiteObject {
	
	//===============
	// Properties
	//===============
	
	private OsmRelation osmRelation;
	private List<TermiteWay> ways = new ArrayList<TermiteWay>();
	private TermiteWay mainWay = null;
	
	//===============
	// Public Methods
	//===============
	
	public List<TermiteWay> getWays() {
		return ways;
	}
	
	public TermiteWay getMainWay() {
		if(mainWay == null) {
			long minId = Long.MAX_VALUE;
			for(TermiteWay tWay:ways) {
				OsmWay oWay = tWay.getOsmWay();
				if(minId > oWay.getId()) {
					minId = oWay.getId();
					mainWay = tWay;
				}
			}
		}
		return mainWay;
	}
	
	/** This method loads the object from the osm relation. */
	public void load(OsmRelation osmRelation, TermiteData termiteData) {
		this.osmRelation = osmRelation;
		update(termiteData);
	}
	
	void update(TermiteData termiteData) {
		OsmData osmData = termiteData.getWorkingData();
		for(OsmMember member:osmRelation.getMembers()) {
			if(OsmModel.TYPE_WAY.equalsIgnoreCase(member.type)) {
				OsmWay osmWay = osmData.getOsmWay(member.memberId);
				TermiteWay termiteWay = osmWay.getTermiteWay();
				termiteWay.setMultiPoly(this);
				ways.add(termiteWay);
			}
		}
	}
	
	/** This method returns the OSM relation for the multipolygon. */
	public OsmRelation getOsmRelation() {
		return osmRelation;
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
