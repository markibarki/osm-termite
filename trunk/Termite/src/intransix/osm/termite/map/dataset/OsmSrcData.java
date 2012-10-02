package intransix.osm.termite.map.dataset;

import intransix.osm.termite.util.PropertyPair;
import intransix.osm.termite.app.mapdata.download.MapDataRequest;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmObject;
import java.util.*;
import org.xml.sax.Attributes;

/**
 * This is the base class for a source object. They are used to set the data for
 * creating an object and for parsing downloaded data.
 * 
 * @author sutter
 */
public abstract class OsmSrcData {
	
	//======================
	// Properties
	//======================
	
	private long id;
	private String type;
	
	private String user;
	private String uid;
	private boolean visible;
	private String version;
	private String changeset;
	private String timestamp;
	
	private List<PropertyPair> properties = new ArrayList<PropertyPair>();
	
	//======================
	// Public Methods
	//======================
	
	/** This method gets the id. */
	public long getId() {
		return id;
	}
	
	/** This method gets the object type. */
	public String getObjectType() {
		return type;
	}
	
	public String getOsmObjectVersion() {
		return version;
	}
	
	public void setOsmObjectVersion(String version) {
		this.version = version;
	}
	
	/** This method adds a property to the src data. */
	public void addProperty(String key, String value) {
		properties.add(new PropertyPair(key,value));
	}
	
	/** This method returns the properties. */
	public List<PropertyPair> getProperties() {
		return properties;
	}
	
	//------------------------------
	// Commit Methods
	//-----------------------------
	
	/** This method compares properties of the source data to properties in the edit data. */
	protected boolean propertiesDifferent(OsmObject osmObject) {
		int newCount = osmObject.getPropertyKeys().size();
		if(newCount != properties.size()) return true;
		
		for(PropertyPair pp:properties) {
			String value = osmObject.getProperty(pp.key);
			if(value == null) {
				if(pp.value != null) return true;
			}
			
			if(!value.equals(pp.value)) return true;
		}
		
		//properties match
		return false;
	}
	
	//=======================
	// Package Methods
	//=======================
	
	/** Constructor where the id is assigned. */
	OsmSrcData(String type, long id) {
		this.type = type;
		this.id = id;
	}
	
	/** This method sets the id. */
	public void setId(long id) {
		this.id = id;
	}

	public void setUser(String user) { this.user = user;}
	public void setUid(String uid) { this.uid = uid;}
	public void setVisible(boolean visible) { this.visible = visible;}
	public void setVersion(String version) { this.version = version;}
	public void setChangeset(String changeset) { this.changeset = changeset;}
	public void setTimestamp(String timestamp) { this.timestamp = timestamp;}
	
	/** this method should be called by objects extending osm object so the
	 * base values can be parsed. */
	void parseElementBase(String name, Attributes attr) {
		
	}
}
