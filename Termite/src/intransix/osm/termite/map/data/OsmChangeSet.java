package intransix.osm.termite.map.data;

import java.util.*;

/**
 *
 * @author sutter
 */
public class OsmChangeSet {
	
	private long id;
	private String message;
	private List<OsmObject> created = new ArrayList<OsmObject>();
	private List<OsmObject> updated = new ArrayList<OsmObject>();
	private List<OsmSrcData> deleted = new ArrayList<OsmSrcData>();
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
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
	
	public boolean isEmpty() {
		return ((created.isEmpty())&&(updated.isEmpty())&&(deleted.isEmpty()));
	}
	
}
