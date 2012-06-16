package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;

/**
 *
 * @author sutter
 */
public class TermiteMember<T extends TermiteObject> {
	public T termiteObject;
	public String role;
	
	public TermiteMember(T termiteObject, String role) {
		this.termiteObject = termiteObject;
		this.role = role;
	}
	
	public static TermiteMember createTermiteMember(TermiteData termiteData, OsmMember member) {
		TermiteObject to;
		if(member.type.equalsIgnoreCase(OsmModel.TYPE_NODE)) {
			to = termiteData.getNode(member.memberId);
		}
		else if(member.type.equalsIgnoreCase(OsmModel.TYPE_WAY)) {
			to = termiteData.getWay(member.memberId);
		}
		else if(member.type.equalsIgnoreCase(OsmModel.TYPE_RELATION)) {
			to = termiteData.getRelation(member.memberId);
		}
		else {
			to = null;
		}
		
		return new TermiteMember(to,member.role);
	}
}
