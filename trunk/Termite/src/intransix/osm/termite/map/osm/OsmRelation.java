package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.model.TermiteData;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;

/**
 * This class holds the data for an OSM relation.
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
	
	/** Constructor. */
	public OsmRelation(long id) {
		super(OsmModel.TYPE_RELATION, id);
	}
	public OsmRelation() {
		super(OsmModel.TYPE_RELATION,OsmData.INVALID_ID);
	}
	
	public List<TermiteMember> getMembers() {
		return members;
	}
	
	public String getRelationType() {
		return relationType;
	}
	
	//=======================
	// Package Methods
	//=======================
	
	@Override
	void propertiesUpdated(OsmData osmData) {
		super.propertiesUpdated(osmData);
		
		//read the relation type
		relationType = this.getProperty(OsmModel.TAG_TYPE);
	}
	
	@Override
	void objectDeleted(OsmData osmData) {
		super.objectDeleted(osmData);
		
		for(TermiteMember member:members) {
			member.termiteObject.removeRelation(this);
		}
		this.members.clear();
	}
	
}
