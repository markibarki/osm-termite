package intransix.osm.termite.map.osm;

import java.util.ArrayList;
import org.xml.sax.Attributes;

/**
 *
 * @author sutter
 */
public class OsmRelation extends OsmObject {
	
	private ArrayList<OsmMember> members = new ArrayList<OsmMember>();
	
	/** The argument is the combined type + osmId string. */
	public OsmRelation(long id) {
		super(OsmModel.TYPE_RELATION, id);
	}
	
	public ArrayList<OsmMember> getMembers() {
		return members;
	}
	
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
			long ref = OsmXml.getLong(attr,"ref",INVALID_ID);
			String role = attr.getValue("role");
			OsmObject object = osmData.getOsmObject(ref,type);
			if(object != null) {
				OsmMember member = new OsmMember();
				member.role = role;
				member.type = type;
				member.member = object;
				members.add(member);
			}
		}
	}
	
	//=====================
	// Package Methods
	//=====================
	
	/** This method makes a copy of this data object in the destination OsmData object. */
	@Override
	void createCopy(OsmData destOsmData) {
		OsmRelation newRelation = destOsmData.getOsmRelation(this.getId());
		for(OsmMember member:this.members) {
			OsmObject newObject = destOsmData.getOsmObject(member.member.getId(),member.type);
			OsmMember newMember = new OsmMember();
			newMember.role = member.role;
			newMember.type = member.type;
			newMember.member = newObject;
			newRelation.members.add(newMember);
		}
		copyFromBase(newRelation);
	}
}
