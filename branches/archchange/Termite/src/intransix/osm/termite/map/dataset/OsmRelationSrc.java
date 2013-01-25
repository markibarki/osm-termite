package intransix.osm.termite.map.dataset;

import intransix.osm.termite.app.mapdata.download.MapDataRequest;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmMember;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmRelation;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;

/**
 * This class holds the data for an OSM relation.
 * @author sutter
 */
public class OsmRelationSrc extends OsmSrcData {
	
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
	
	/** This method removes all members from the relation. */
	public void clearMembers() {
		members.clear();
	}
	/** This method retrieves the member list. It can be used to populate the
	 * member list. 
	 * 
	 * @return 
	 */
	public List<Member> getMembers() {
		return members;
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
	
	public OsmRelationSrc(long id) {
		super(OsmModel.TYPE_RELATION, id);
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
