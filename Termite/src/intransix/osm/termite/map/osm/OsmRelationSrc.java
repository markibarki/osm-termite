package intransix.osm.termite.map.osm;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;

/**
 * This class holds the data for an OSM relation.
 * @author sutter
 */
public class OsmRelationSrc extends OsmSrcData<OsmRelation> {
	
	//=======================
	// Properties
	//=======================
	
	private List<Member> members = new ArrayList<Member>();
	
	//=======================
	// Public Methods
	//=======================
	
	/** Constructor. */
	public OsmRelationSrc() {
		super(OsmModel.TYPE_RELATION,OsmData.INVALID_ID);
	}
	
	public List<Member> getMembers() {
		return members;
	}
	
	/** Copies the source data to this instance. */
	@Override
	public void copyInto(OsmRelation target, OsmData osmData) {
		super.copyInto(target,osmData);
		
		List<TermiteMember> osmMembers = target.getMembers();
		osmMembers.clear();
		for(Member m:members) {
			OsmObject osmObject = osmData.getOsmObject(m.memberId,m.type,true);
			osmMembers.add(new TermiteMember(osmObject,m.role));
			osmObject.addRelation(target);
		}
	}
	
	/** This method copies the src data to this object. */
	@Override
	public void copyFrom(OsmRelation src) {
		super.copyFrom(src);
		
		List<TermiteMember> sm = src.getMembers();
		members.clear();
		for(TermiteMember m:sm) {
			Member member = new Member(m.termiteObject.getId(),m.termiteObject.getObjectType(),m.role);
			members.add(member);
		}
	}
	
	//-------------------------
	// Parse Methods
	//-------------------------
	
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
			long ref = OsmParser.getLong(attr,"ref",OsmData.INVALID_ID);
			String role = attr.getValue("role");
			members.add(new Member(ref,type,role));
		}
	}
	
	//=======================
	// Package Methods
	//=======================
	
	OsmRelationSrc(long id) {
		super(OsmModel.TYPE_RELATION, id);
	}
	
	public class Member {
		public String role;
		public String type;
		public Long memberId;

		public Member(Long memberId, String type, String role) {
			this.memberId = memberId;
			this.type = type;
			this.role = role;
		}
	}
	
}
