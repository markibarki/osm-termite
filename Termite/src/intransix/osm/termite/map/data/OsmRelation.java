package intransix.osm.termite.map.data;

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
	
	private List<OsmMember> members = new ArrayList<OsmMember>();
	private String relationType;
	
	//=======================
	// Public Methods
	//=======================
	
	/** This method returns the list of members for this relation. It should NOT be edited. */
	public List<OsmMember> getMembers() {
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
		//read the relation type
		relationType = this.getProperty(OsmModel.KEY_TYPE);
	}
	
	/** This method should be called when the properties are updated. */
	@Override
	void propertiesUpdated(OsmData osmData) {
		//read the relation type
		relationType = this.getProperty(OsmModel.KEY_TYPE);
	}
	
	@Override
	void objectUpdated(OsmData osmData) {
	}
	
	/** This method should be called when the object is deleted. */
	@Override
	void objectDeleted(OsmData osmData) {
		super.objectDeleted(osmData);
		
		for(OsmMember member:members) {
			member.osmObject.removeRelation(this);
		}
		this.members.clear();
	}
	
}
