package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.MapObject;
import org.xml.sax.Attributes;


/**
 * This method holds data common to OsmObjects
 * 
 * @author sutter
 */
public abstract class OsmObject extends MapObject {
	
	//=======================
	// Properties
	//=======================
	
	public final static long INVALID_ID = 0;
	public final static int INVALID_LOCAL_VERSION = -1;
	public final static int INITIAL_LOCAL_VERSION = 1;
			
	//common
	private long id;
	private String type;
	private boolean isLoaded = false;
	private boolean isVirtual = false;
	
	private String user;
	private String uid;
	private boolean visible;
	private String version;
	private String changeset;
	private String timestamp;
	
	private int localVersion = 0;
	
	//=======================
	// Constructor
	//=======================
	
	/** The argument is the combined type + osmId string. */
	OsmObject(String type, long id) {
		this.type = type;
		this.id = id;
	}
	
	/** This method sets the isLoaded flag for the object. */
	void setIsLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}
	
	/** This method gets the isLoaded flag for the object. */
	public boolean getIsLoaded() {
		return isLoaded;
	}
	
	/** This method gets the virtual flag for the object. */
	public boolean getIsVirtual() {
		return isVirtual;
	}
	
	/** This method set the virtual flag for the object. */
	public void setIsVirtual(boolean isVirtual) {
		this.isVirtual = isVirtual;
	}
	
	/** This method gets the ID for the object. */
	public long getId() {
		return id;
	}
	
	/** This method sets the local version for the object. */
	public void setLocalVersion(int localVersion) {
		this.localVersion = localVersion;
	}
	
	/** This method gets the local version for the object. */
	public int getLocalVersion() {
		return localVersion;
	}
	
	/** This method is used in XMl parsing. */
	public void startElement(String name, Attributes attr, OsmData osmData) {
		//parse a key/value pair
		if(name.equalsIgnoreCase("tag")) {
			String key = attr.getValue("k");
			String value = attr.getValue("v");
			this.setProperty(key, value);
		}
	}
	
	/** this method should be called by objects extending osm object so the
	 * base values can be parsed. */
	public void parseElementBase(String name, Attributes attr) {
		user = attr.getValue("user");
		uid = attr.getValue("uid");
		visible = OsmParser.getBoolean(attr,"visible",true);
		version = attr.getValue("version");
		changeset = attr.getValue("changeset");
		timestamp = attr.getValue("timestamp");
	}
	
	/** This is called when the object is finished being parsed. */
	public void endElement(String name, OsmParser root) {
		this.setIsLoaded(true);
	}
	
	//==========================
	// Package Methods
	//==========================
	
	/** This method makes a copy of this data object in the destination OsmData object.
	 * This method should be overridden by the */
	abstract void createCopy(OsmData destOsmData);
	
	/** This method copies relevent data from the base OsmObject needed for reproducing
	 * the data set. */
	void copyFromBase(OsmObject newObject) {
		newObject.isLoaded = this.isLoaded;
		newObject.isVirtual = this.isVirtual;
		newObject.localVersion = this.localVersion;

		newObject.copyProperties(this);
	}

}