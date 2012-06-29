package intransix.osm.termite.map.data;

import java.util.*;
import org.xml.sax.Attributes;

/**
 * This is the base class for a source object. They are used to set the data for
 * creating an object and for parsing downloaded data.
 * 
 * @author sutter
 */
public abstract class OsmSrcData<T extends OsmObject> {
	
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
	
	/** This method adds a property to the src data. */
	public void addProperty(String key, String value) {
		properties.add(new PropertyPair(key,value));
	}
	
	/** This method returns the properties. */
	public List<PropertyPair> getProperties() {
		return properties;
	}
	
	//-----------------------
	// Parsing Methods
	//-----------------------
	
	/** This method is used in XMl parsing. */
	public void startElement(String name, Attributes attr, OsmData osmData) {
		//parse a key/value pair
		if(name.equalsIgnoreCase("tag")) {
			String key = attr.getValue("k");
			String value = attr.getValue("v");
			properties.add(new PropertyPair(key,value));
		}
	}
	
	public void endElement(OsmData osmData) {
		//create the osm object for this data
		OsmObject object = osmData.getOsmObject(id, type, true);
		if(object != null) {
			this.copyInto((T)object, osmData);
			object.objectCreated(osmData);
		}
		else {
			//this shouldn't happen - the type was unrecognized
		}
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
	void setId(long id) {
		this.id = id;
	}
	
	/** This method copies the src data to the target. */
	void copyInto(T targetData, OsmData osmData) {
		targetData.setId(id);
		for(PropertyPair pp:properties) {
			targetData.setProperty(pp.key,pp.value);
		}
	}
	
	/** This method copies the src data to this object. */
	void copyFrom(T srcData) {
		this.id = srcData.getId();
		this.type = srcData.getObjectType();
		properties.clear();
		for(String key:srcData.getPropertyKeys()) {
			properties.add(new PropertyPair(key,srcData.getProperty(key)));
		}
	}
	
	/** this method should be called by objects extending osm object so the
	 * base values can be parsed. */
	void parseElementBase(String name, Attributes attr) {
		user = attr.getValue("user");
		uid = attr.getValue("uid");
		visible = OsmParser.getBoolean(attr,"visible",true);
		version = attr.getValue("version");
		changeset = attr.getValue("changeset");
		timestamp = attr.getValue("timestamp");
	}
	
	//=========================
	// Internal Classes
	//=========================
	
	public static class PropertyPair {
		
		public PropertyPair(String key, String value) {
			this.key = key;
			this.value = value;
		}
		
		public String key;
		public String value;
	}
	
}
