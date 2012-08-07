package intransix.osm.termite.map.data;

import java.util.*;

/**
 *
 * @author sutter
 */
public class OsmChangeSet {
	
	private String message;
	private List<OsmObject> created = new ArrayList<OsmObject>();
	private List<OsmObject> updated = new ArrayList<OsmObject>();
	private List<OsmSrcData> deleted = new ArrayList<OsmSrcData>();
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public void addCreated(OsmObject osmObject) {
		created.add(osmObject);
	}
	
	public void addUpdated(OsmObject osmObject) {
		updated.add(osmObject);
	}
	
	public void addDeleted(OsmSrcData osmSrcData) {
		deleted.add(osmSrcData);
	}
	
}
