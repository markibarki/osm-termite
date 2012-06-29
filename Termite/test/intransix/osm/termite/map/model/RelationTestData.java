/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.TermiteMember;
import intransix.osm.termite.map.osm.OsmMember;
import intransix.osm.termite.map.osm.OsmModel;
import intransix.osm.termite.map.osm.OsmRelation;
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
	
	public List<OsmMember> members = new ArrayList<OsmMember>();
	
	@Override
	public void validate() {
		
		//check existence
		OsmRelation oRelation = osmData.getOsmRelation(id);
		TermiteRelation tRelation = termiteData.getRelation(id);
		assert(oRelation != null);
		assert(tRelation.getOsmObject() == oRelation);
		assert(oRelation.getTermiteObject() == tRelation);
		
		//check ways
		List<OsmMember> oMembers = oRelation.getMembers();
		List<TermiteMember> tMembers = tRelation.getMembers();
		assert(oMembers.size() == members.size());
		assert(tMembers.size() == members.size());
		int cnt = members.size();
		OsmMember m1;
		OsmMember m2;
		TermiteMember m3;
		for(int i = 0; i < cnt; i++) {
			m1 = members.get(i);
			m2 = oMembers.get(i);
			m3 = tMembers.get(i);
			assert(m1.memberId == m2.memberId);
			assert(m1.type.equals(m2.type));
			if(m1.role != null) {
				assert m1.role.equals(m2.role);
				assert m1.role.equals(m3.role);
			}
			else {
				assert(m2.role == null);
				assert(m3.role == null);
			}
		}
		
		baseValidate(tRelation,oRelation);
		
	}
	
	@Override
	public void validateDeleted() {
		
		//check it is not in the data
		assert(osmData.getOsmRelation(id) == null);
		assert(termiteData.getRelation(id) == null);
			
		TermiteObject to;
		for(OsmMember member:members) {
			if(member.type.equals("node")) {
				to = termiteData.getNode(member.memberId);
			}
			else if(member.type.equals("way")) {
				to = termiteData.getWay(member.memberId);
			}
			else if(member.type.equals("relation")) {
				to = termiteData.getRelation(member.memberId);
			}
			else {
				to = null;
			}
			List<TermiteRelation> list = to.getRelations();
			for(TermiteRelation tRelation:list) {
				OsmRelation oRelation = tRelation.getOsmObject();
				if(oRelation != null) {
					assert(oRelation.getId() != id);
				}
			}
			
		}
	}	
}
