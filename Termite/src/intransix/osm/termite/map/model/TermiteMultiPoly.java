package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmRelation;
import java.util.*;
import intransix.osm.termite.map.osm.*;

/**
 * This class encapsulates a mutlipolygon. 
 * @author sutter
 */
public class TermiteMultiPoly extends TermiteObject<OsmRelation> {
	
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
		return mainWay;
	}
	
	/** This method returns the OSM relation for the multipolygon. */
	public OsmRelation getOsmRelation() {
		return osmRelation;
	}
	
	//=======================
	// Package Methods
	//=======================
	
		/** This method loads the object from the osm relation. */
	public void setOsmRelation(OsmRelation osmRelation) {
		this.osmRelation = osmRelation;
	}
	
	void updateLocalData(TermiteData termiteData) {
		OsmData osmData = termiteData.getWorkingData();
		
		ways.clear();
		mainWay = null;
		
		long minId = Long.MAX_VALUE;
		for(OsmMember member:osmRelation.getMembers()) {
			if(OsmModel.TYPE_WAY.equalsIgnoreCase(member.type)) {
				OsmWay osmWay = osmData.getOsmWay(member.memberId);
				TermiteWay termiteWay = (TermiteWay)osmWay.getTermiteObject();
				ways.add(termiteWay);
				
				//load the main way
//				if(minId > osmWay.getId()) {
if((minId > osmWay.getId())&&(osmWay.getId() > 0)) {
					minId = osmWay.getId();
					mainWay = (TermiteWay)osmWay.getTermiteObject();
				}
			}
		}
	}
	
	void updateRemoteData(TermiteData termiteData) {
this.incrementTermiteVersion();
		for(TermiteWay way:ways) {
			way.setMultiPoly(this);
		}
	}
	
	void objectDeleted(TermiteData termiteData) {
		
	}
	
	void incrementWaysTermiteVersion() {
		for(TermiteWay way:ways) {
			way.incrementTermiteVersion();
		}
	}
	
	/** This method gets the OSM relation associated with this multipolygon. */
	@Override
	public OsmRelation getOsmObject() {
		return osmRelation;
	}
}
