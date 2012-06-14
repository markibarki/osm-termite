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
	
	/** This method gets the OSM relation associated with this multipolygon. */
	@Override
	public OsmRelation getOsmObject() {
		return osmRelation;
	}
	
	/** This method loads the object from the osm relation. */
	public void setOsmRelation(OsmRelation osmRelation) {
		this.osmRelation = osmRelation;
	}
	
	//=======================
	// Package Methods
	//=======================
	
	@Override
	void incrementTermiteVersion() {
		super.incrementTermiteVersion();
		for(TermiteWay way:ways) {
			way.incrementTermiteVersion();
		}
	}
	
	void init(TermiteData termiteData, OsmRelation osmRelation) {	
		this.osmRelation = osmRelation;
		osmRelation.setTermiteObject(this);
		OsmData osmData = termiteData.getWorkingData();
		
		for(OsmMember member:osmRelation.getMembers()) {
			if(OsmModel.TYPE_WAY.equalsIgnoreCase(member.type)) {
				OsmWay osmWay = osmData.getOsmWay(member.memberId);
				TermiteWay termiteWay = (TermiteWay)osmWay.getTermiteObject();
				ways.add(termiteWay);
				
				termiteWay.setMultiPoly(this);
				
				//load the main way - use first for now
				if(mainWay == null) {
					mainWay = termiteWay;
				}
			}
		}
	}
	
	void objectDeleted(TermiteData termiteData) {
		for(TermiteWay way:ways) {
			way.setMultiPoly(null);
		}
		this.ways.clear();
		this.mainWay = null;
		this.osmRelation = null;
	}
	
	void removeMemberObject(TermiteWay termiteWay) {
		ways.remove(termiteWay);
		if(mainWay == termiteWay) {
			if(!ways.isEmpty()) {
				mainWay = ways.get(0);
			}
			else {
				mainWay = null;
			}
		}
		incrementTermiteVersion();
	}
	
	void propertiesUpdated(TermiteData termiteData) {
		//we just need to check that the tyep was not updated
		if(osmRelation != null) {
			String relationType = osmRelation.getProperty(OsmModel.TAG_TYPE);
			if(!OsmModel.TYPE_MULTIPOLYGON.equalsIgnoreCase(relationType)) {
				throw new RuntimeException("Changing the relation type for a Multipolygong not currently supported!");
			}
		} 
	}
	

}
