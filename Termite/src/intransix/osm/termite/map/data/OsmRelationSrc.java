package intransix.osm.termite.map.data;

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
	
	/** This adds a member. */
	public void addMember(long id, String objectType, String role) {
		members.add(new Member(id,objectType,role));
	}
	
	/** This method retrieves the member list. It can be used to populate the
	 * member list. 
	 * 
	 * @return 
	 */
	public List<Member> getMembers() {
		return members;
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
			long ref = MapDataRequest.getLong(attr,"ref",OsmData.INVALID_ID);
			String role = attr.getValue("role");
			members.add(new Member(ref,type,role));
		}
	}
	
	//------------------------------
	// Commit Methods
	//-----------------------------
	
	public boolean isDifferent(OsmRelation osmRelation) {
		//compare node lists
		List<OsmMember> osmMembers = osmRelation.getMembers();
		int cnt = osmMembers.size();
		if(cnt != members.size()) return true;
		
		Member m;
		OsmMember om;
		for(int i = 0; i < cnt; i++) {
			m = members.get(i);
			om = osmMembers.get(i);
			if(m.isDifferent(om)) return true;
		}

		//compare properties
		return propertiesDifferent(osmRelation);
	}
	
	//=======================
	// Package Methods
	//=======================
	
	OsmRelationSrc(long id) {
		super(OsmModel.TYPE_RELATION, id);
	}
	
	/** Copies the source data to this instance. */
	@Override
	void copyInto(OsmRelation target, OsmData osmData) {
		super.copyInto(target,osmData);
		
		List<OsmMember> osmMembers = target.getMembers();
		osmMembers.clear();
		for(Member m:members) {
			OsmObject osmObject = osmData.getOsmObject(m.memberId,m.type,true);
			if(osmObject != null) {
				osmMembers.add(new OsmMember(osmObject,m.role));
				osmObject.addRelation(target);
			}
			else {
				//this shouldn't happen - the type was unrecognized.
			}
		}
	}
	
	/** This method copies the src data to this object. */
	@Override
	void copyFrom(OsmRelation src) {
		super.copyFrom(src);
		
		List<OsmMember> sm = src.getMembers();
		members.clear();
		for(OsmMember m:sm) {
			Member member = new Member(m.osmObject.getId(),m.osmObject.getObjectType(),m.role);
			members.add(member);
		}
	}
	
	//===========================
	// Internal Class
	//=========================== 
	
	public static class Member {
		public String role;
		public String type;
		public Long memberId;

		public Member(Long memberId, String type, String role) {
			this.memberId = memberId;
			this.type = type;
			this.role = role;
		}
		
		public boolean isDifferent(OsmMember osmMember) {
			//check object id and type
			if(osmMember.osmObject == null) {
				if(memberId != null) return true;
			}
			else {
				if( (osmMember.osmObject.getId() != (long)memberId) ||
						(!osmMember.osmObject.getObjectType().equals(type))) {
					return true;
				}
			}
			
			//check role
			if(osmMember.role == null) {
				if(role != null) {
					return true;
				}
				else if(!osmMember.role.equals(role)) {
					return true;
				}
			}
			
			//no change
			return false;
		}
	}
	
}
