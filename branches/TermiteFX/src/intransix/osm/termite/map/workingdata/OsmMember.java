package intransix.osm.termite.map.workingdata;

/**
 *
 * @author sutter
 */
public class OsmMember<T extends OsmObject> {
	public T osmObject;
	public String role;
	
	public OsmMember(T osmObject, String role) {
		this.osmObject = osmObject;
		this.role = role;
	}
}
