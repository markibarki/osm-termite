/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.map.model;

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
	public boolean isMultiPoly;
	
	@Override
	public void validate() {
		
		//check existence
		OsmRelation oRelation = osmData.getOsmRelation(id);
		assert(oRelation != null);
		
		//check ways
		List<OsmMember> oMembers = oRelation.getMembers();
		assert(oMembers.size() == members.size());
		int cnt = members.size();
		OsmMember m1;
		OsmMember m2;
		for(int i = 0; i < cnt; i++) {
			m1 = members.get(i);
			m2 = oMembers.get(i);
			assert(m1.memberId == m2.memberId);
			assert(m1.type.equals(m2.type));
			if(m1.role != null) {
				assert m1.role.equals(m2.role);
			}
			else {
				assert(m2.role == null);
			}
		}
		
		//check properties - both directions to make sure they are the same
		checkProperties(oRelation,props);
		
		assert(oRelation.getLocalVersion() >= minOsmVersion);
		
		//Multipoly case
		String type = oRelation.getProperty(OsmModel.TAG_TYPE);
		if((type != null)&&(type.equals(OsmModel.TYPE_MULTIPOLYGON))) {
			
			TermiteMultiPoly mp = termiteData.getMultiPoly(id,false);
			assert(mp.getOsmObject() == oRelation);
			assert(oRelation.getTermiteObject() == mp);
			
			List<TermiteWay> ways = mp.getWays();
			assert(ways.size() == members.size());
			cnt = members.size();
			OsmMember m;
			TermiteWay w;
			for(int i = 0; i < cnt; i++) {
				m = members.get(i);
				w = ways.get(i);
				long id1 = m.memberId;
				long id2 = w.getOsmObject().getId(); 
				assert(id1 == id2);
			}
			//I should select the proper value...
			assert(mp.getMainWay() != null);
			
			assert(mp.getTermiteLocalVersion() >= minTermiteVersion);
		}
		
	}
	
	@Override
	public void validateDeleted() {
		
		//check it is not in the data
		assert(osmData.getOsmRelation(id) == null);
		
		if(isMultiPoly) {
			assert(termiteData.getMultiPoly(id,false) == null);
			
			for(OsmMember member:members) {
				if(member.type.equals("way")) {
					TermiteWay way = termiteData.getWay(member.memberId);
					assert(way.getMultiPoly() == null);
				}
			}
		}
	}	
}
