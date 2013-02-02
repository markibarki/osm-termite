package intransix.osm.termite.map.workingdata;

import intransix.osm.termite.map.dataset.OsmRelationSrc;
import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the data for an OSM relation.
 * 
 * @author sutter
 */
public class OsmRelation extends OsmObject<OsmRelationSrc> {
	
	//=======================
	// Properties
	//=======================
	
	private List<OsmMember> members = new ArrayList<OsmMember>();
	
	//=======================
	// Public Methods
	//=======================
	
	/** This method returns the list of members for this relation. It should NOT be edited. */
	public List<OsmMember> getMembers() {
		return members;
	}
	
	/** This method returns the relation type. */
	public String getRelationType() {
		return getProperty(OsmModel.KEY_TYPE);
	}
	
	//=======================
	// Package Methods
	//=======================
	
	/** Constructor. */
	OsmRelation(long id) {
		super(OsmModel.TYPE_RELATION, id);
	}
	
	/** Constructor. */
	OsmRelation() {
		super(OsmModel.TYPE_RELATION,OsmData.INVALID_ID);
	}
	
	/** This method should be called when the object is deleted. */
	@Override
	public void objectDeleted(OsmData osmData) {
		super.objectDeleted(osmData);
		
		for(OsmMember member:members) {
			member.osmObject.removeRelation(this);
		}
		this.members.clear();
	}
	
	/** Copies the source data to this instance. */
	@Override
	public void copyInto(OsmRelationSrc target) {
		super.copyInto(target);
		
		List<OsmMember> sm = this.getMembers();
		target.clearMembers();
		for(OsmMember m:sm) {
			target.addMember(m.osmObject.getId(),m.osmObject.getObjectType(),m.role);
		}
	}
	
	/** This method copies the src data to this object. */
	@Override
	public void copyFrom(OsmRelationSrc src, OsmData osmData) {
		super.copyFrom(src, osmData);
		
		List<OsmMember> osmMembers = this.getMembers();
		osmMembers.clear();
		for(OsmRelationSrc.Member m:src.getMembers()) {
			OsmObject osmObject = osmData.getOsmObject(m.memberId,m.type,true);
			if(osmObject != null) {
				osmMembers.add(new OsmMember(osmObject,m.role));
				osmObject.addRelation(this);
			}
			else {
				//this shouldn't happen - the type was unrecognized.
			}
		}
	}
	
}
