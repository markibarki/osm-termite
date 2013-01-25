package intransix.osm.termite.map.dataset;

import intransix.osm.termite.map.workingdata.OsmMember;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.dataset.OsmSrcData;
import intransix.osm.termite.map.dataset.OsmWaySrc;
import intransix.osm.termite.map.dataset.OsmNodeSrc;
import intransix.osm.termite.map.dataset.OsmRelationSrc;
import intransix.osm.termite.util.MercatorCoordinates;
import java.util.*;
import javax.xml.stream.XMLStreamWriter;
import java.awt.geom.Point2D;

/**
 *
 * @author sutter
 */
public class OsmChangeSet {
	
	private long id;
	private String message;
	private List<OsmChangeObject> created = new ArrayList<OsmChangeObject>();
	private List<OsmChangeObject> updated = new ArrayList<OsmChangeObject>();
	private List<OsmChangeObject> deleted = new ArrayList<OsmChangeObject>();
	
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
	
	public void addCreated(OsmChangeObject changeObject) {
		created.add(changeObject);
	}
	
	public void addUpdated(OsmChangeObject changeObject) {
		updated.add(changeObject);
	}
	
	public void addDeleted(OsmChangeObject changeObject) {
		deleted.add(changeObject);
	}
	
	/** This returns the objects created during the session, in order so that no object is referenced
	 * from the list before it is created. */ 
	public List<OsmChangeObject> getCreated() {
		//sort so object are not references before created
		Collections.sort(created);
		return created;
	}
	
	/** This returns the objects updated during the session. */
	public List<OsmChangeObject> getUpdated() {
		return updated;
	}
	
	/** This returns the objects deleted during the session, in order so that no object is referenced
	 * from the list after it is deleted. */ 
	public List<OsmChangeObject> getDeleted() {
		//sort so objects are not referenced after the are deleted
		Collections.sort(deleted);
		Collections.reverse(deleted);
		return deleted;
	}
	
	public boolean isEmpty() {
		return ((created.isEmpty())&&(updated.isEmpty())&&(deleted.isEmpty()));
	}
}
