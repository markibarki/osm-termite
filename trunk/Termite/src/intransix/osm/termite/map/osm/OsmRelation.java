package intransix.osm.termite.map.osm;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the data for an OSM relation.
 * 
 * @author sutter
 */
public class OsmRelation extends OsmObject {
	
	//=======================
	// Properties
	//=======================
	
	private List<TermiteMember> members = new ArrayList<TermiteMember>();
	private String relationType;
	
	//=======================
	// Public Methods
	//=======================
	
	/** This method returns the list of members for this relation. It should NOT be edited. */
	public List<TermiteMember> getMembers() {
		return members;
	}
	
	/** This method returns the relation type. */
	public String getRelationType() {
		return relationType;
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
	
	/** This overrides the base method to add functionality. */
	@Override
	void objectCreated(OsmData osmData) {
		super.objectCreated(osmData);
		
		//read the relation type
		relationType = this.getProperty(OsmModel.TAG_TYPE);
	}
	
	/** This method should be called when the properties are updated. */
	@Override
	void propertiesUpdated(OsmData osmData) {
		super.propertiesUpdated(osmData);
		
		//read the relation type
		relationType = this.getProperty(OsmModel.TAG_TYPE);
	}
	
	/** This method should be called when the object is deleted. */
	@Override
	void objectDeleted(OsmData osmData) {
		super.objectDeleted(osmData);
		
		for(TermiteMember member:members) {
			member.termiteObject.removeRelation(this);
		}
		this.members.clear();
	}
	
}
