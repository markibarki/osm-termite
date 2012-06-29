package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import java.util.*;

/**
 * This is a class for unit testing relation editing. It holds the reference relation data which
 * the developer should manually set. It can then be used to compare with the result of the edit 
 * operations being tested. A test method is included.
 * 
 * This class allows the developer to track the state of the relation over many operations,
 * simplifying writing the tests.
 * 
 * @author sutter
 */
public class RelationTestData extends ObjectTestData {
	
	public List<OsmRelationSrc.Member> members = new ArrayList<OsmRelationSrc.Member>();
	public String relationType;
	
	/** This adds a member. */
	public void addMember(long id, String objectType, String role) {
		members.add(new OsmRelationSrc.Member(id,objectType,role));
	}
	
	/** This adds a member. */
	public void addMember(long id, String objectType, String role,int index) {
		members.add(index,new OsmRelationSrc.Member(id,objectType,role));
	}
	
	@Override
	public void validate() {
		
		//check existence
		OsmRelation relation = osmData.getOsmRelation(id);
		assert(relation != null);
		
		//check type
		if(relationType != null) {
			assert(relationType.equalsIgnoreCase(relation.getRelationType()));
		}
		else {
			assert(relation.getRelationType() ==  null);
		}
		
		//check members
		List<TermiteMember> osmMembers = relation.getMembers();
		assert(osmMembers.size() == members.size());
		int cnt = members.size();
		OsmRelationSrc.Member m1;
		TermiteMember m2;
		for(int i = 0; i < cnt; i++) {
			m1 = members.get(i);
			m2 = osmMembers.get(i);
			assert(m1.memberId == m2.termiteObject.getId());
			assert(m1.type.equals(m2.termiteObject.getObjectType()));
			if(m1.role != null) {
				assert m1.role.equals(m2.role);
			}
			else {
			}
		}
		
		baseValidate(relation);
		
	}
	
	@Override
	public void validateDeleted() {
		
		//check it is not in the data
		assert(osmData.getOsmRelation(id) == null);
			
		OsmObject osmObject;
		for(OsmRelationSrc.Member member:members) {
			if(member.type.equals("node")) {
				osmObject = osmData.getOsmNode(member.memberId);
			}
			else if(member.type.equals("way")) {
				osmObject = osmData.getOsmWay(member.memberId);
			}
			else if(member.type.equals("relation")) {
				osmObject = osmData.getOsmRelation(member.memberId);
			}
			else {
				osmObject = null;
			}
			List<OsmRelation> list = osmObject.getRelations();
			for(OsmRelation relation:list) {
				if(relation != null) {
					assert(relation.getId() != id);
				}
			}
			
		}
	}	
}
