package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.model.EditData;
import intransix.osm.termite.map.model.UnchangedException;
import java.util.ArrayList;
import org.xml.sax.Attributes;

/**
 * This class holds the data for an OSM relation.
 * @author sutter
 */
public class OsmRelation extends OsmObject implements EditData<OsmRelation> {
	
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
	
	//----------------------
	// Edit Methods
	//----------------------
	
	/** This method is not used on create/delete. */
	@Override
	public EditData<OsmRelation> readInitialData(OsmRelation objectToDelete) throws UnchangedException {
		return null;
	}
		
	/** This method is only called on create. */
	@Override
	public void writeData(OsmRelation newRelation) throws UnchangedException, Exception {
		this.copyInto(newRelation);
	}
	
	//=====================
	// Package Methods
	//=====================
	
	/** This method makes a copy of this data object in the destination OsmData object. */
	void copyInto(OsmRelation newRelation) {
		for(OsmMember member:this.members) {
			OsmMember newMember = member.createCopy();
			newRelation.members.add(newMember);
		}
		super.copyInto(newRelation);
	}
}
