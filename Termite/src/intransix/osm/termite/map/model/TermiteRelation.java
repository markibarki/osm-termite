package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sutter
 */
public class TermiteRelation extends TermiteObject<OsmRelation> {

	//===============
	// Properties
	//===============
	
	private OsmRelation osmRelation;
	private List<TermiteMember> members = new ArrayList<TermiteMember>();
	
	//===============
	// Public Methods
	//===============
	
	public List<TermiteMember> getMembers() {
		return members;
	}
	
	/** This method gets the OSM relation associated with this termite relation. */
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
	void incrementDataVersion() {
		super.incrementDataVersion();
	}
	
	@Override
	void init(TermiteData termiteData, OsmRelation osmRelation) {	
		this.osmRelation = osmRelation;
		osmRelation.setTermiteObject(this);
		OsmData osmData = termiteData.getWorkingData();
		
		for(OsmMember oMember:osmRelation.getMembers()) {
			OsmObject osmObject = osmData.getOsmObject(oMember.memberId,oMember.type);
if(osmObject != null) {
			TermiteObject termiteObject = osmObject.getTermiteObject();
			members.add(new TermiteMember(termiteObject,oMember.role));

			termiteObject.addRelation(this);
}
		}
	}
	
	@Override
	void objectDeleted(TermiteData termiteData) {
		super.objectDeleted(termiteData);
		
		for(TermiteMember member:members) {
			member.termiteObject.removeRelation(this);
		}
		this.members.clear();
		this.osmRelation = null;
	}
	
}
