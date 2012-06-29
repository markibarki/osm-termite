package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.model.TermiteData;
import intransix.osm.termite.map.osm.*;

/**
 *
 * @author sutter
 */
public class TermiteMember<T extends OsmObject> {
	public T termiteObject;
	public String role;
	
	public TermiteMember(T termiteObject, String role) {
		this.termiteObject = termiteObject;
		this.role = role;
	}
	
//	public static TermiteMember createTermiteMember(TermiteData termiteData, OsmMember member) {
//		OsmObject to;
//		OsmData osmData = termiteData.getWorkingData();
//		if(member.type.equalsIgnoreCase(OsmModel.TYPE_NODE)) {
//			to = osmData.getOsmNode(member.memberId);
//		}
//		else if(member.type.equalsIgnoreCase(OsmModel.TYPE_WAY)) {
//			to = osmData.getOsmWay(member.memberId);
//		}
//		else if(member.type.equalsIgnoreCase(OsmModel.TYPE_RELATION)) {
//			to = osmData.getOsmRelation(member.memberId);
//		}
//		else {
//			to = null;
//		}
//		
//		return new TermiteMember(to,member.role);
//	}
}
