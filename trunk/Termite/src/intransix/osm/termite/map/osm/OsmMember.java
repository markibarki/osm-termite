package intransix.osm.termite.map.osm;

/** This class encapsulates data for an OsmRelation member. */
public class OsmMember {
	public String role;
	public String type;
	public Long memberId;
	
	public OsmMember(Long memberId, String type, String role) {
		this.memberId = memberId;
		this.type = type;
		this.role = role;
	}
	
	public OsmMember createCopy() {
		return new OsmMember(memberId,type,role);
	}
}
