package intransix.osm.termite.map.osm;

import java.util.ArrayList;
import org.xml.sax.Attributes;

/**
 * This class holds the data for an OSM relation.
 * @author sutter
 */
public class OsmRelation extends OsmObject<OsmRelation> {
	
	//=======================
	// Properties
	//=======================
	
	private ArrayList<OsmMember> members = new ArrayList<OsmMember>();
	
	//=======================
	// Public Methods
	//=======================
	
	/** Constructor. */
	public OsmRelation(long id) {
		super(OsmModel.TYPE_RELATION, id);
	}
	public OsmRelation() {
		super(OsmModel.TYPE_RELATION,OsmObject.INVALID_ID);
	}
	
	/** This method gets the member list for this relation. */
	public ArrayList<OsmMember> getMembers() {
		return members;
	}
	
	/** This method is used for XMl parsing. */
	@Override
	public void startElement(String name, Attributes attr, OsmData osmData) {
		//let the parent parse
		super.startElement(name,attr,osmData);
		
		//parse this node
		if(name.equalsIgnoreCase("relation")) {
			//parse common stuff
			parseElementBase(name, attr);
		}
		else if(name.equalsIgnoreCase("member")) {
			String type = attr.getValue("type");
			long ref = OsmParser.getLong(attr,"ref",INVALID_ID);
			String role = attr.getValue("role");
			OsmMember member = new OsmMember(ref,type,role);
			members.add(member);
		}
	}
	
	/** This method makes a copy of this data object in the destination OsmData object. */
	@Override
	public void copyInto(OsmRelation newRelation) {
		for(OsmMember member:this.members) {
			OsmMember newMember = member.createCopy();
			newRelation.members.add(newMember);
		}
		super.copyInto(newRelation);
	}
	
	/** This removes all references to this object from the member list. */
	public void removeMemberObject(Long oid, String type) {
		int cnt = members.size();
		OsmMember member;
		for(int i = cnt-1; i >= 0; i--) {
			member = members.get(i);
			if((member.memberId.equals(oid))&&(member.type.equals(type))) {
				members.remove(i);
			}
		}
	}
}
