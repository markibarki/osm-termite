package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.model.TermiteObject;
import java.util.Collection;
import java.util.HashMap;
import org.xml.sax.Attributes;


/**
 * This method holds data common to OsmObjects
 * 
 * @author sutter
 */
public abstract class OsmObject<T extends OsmObject> {
	
	//====================
	// Constants
	//====================
	
	/** These are values of boolean strings that will be accepted as true. The
	 * first value is the desired value. */
	public final static String[] TRUE_STRINGS = {"yes","true","t"};
	
	/** These are values of boolean strings that will be accepted as false. The
	 * first value is the desired value. */
	public final static String[] FALSE_STRINGS = {"no","false","f"};
	
	public final static long INVALID_ID = 0;
	public final static int INVALID_LOCAL_VERSION = -1;
	public final static int INITIAL_LOCAL_VERSION = 0;
	
	//=======================
	// Properties
	//=======================
			
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
	
	private TermiteObject<T> termiteObject;
	
	private HashMap<String,String> tags = new HashMap<String,String>();
	
	//=======================
	// Constructor
	//=======================
	
	/** The argument is the combined type + osmId string. */
	OsmObject(String type, long id) {
		this.type = type;
		this.id = id;
	}
	
	/** This method gets the ID for the object. */
	public long getId() {
		return id;
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
	
	/** This gets the object type string. */
	public String getObjectType() {
		return type;
	}
	
	/** This method sets the termite node for this OsmN0de. */
	public void setTermiteObject(TermiteObject<T> termiteObject) {
		this.termiteObject = termiteObject;
	}
	
	/** This method gets the TermiteNode for this OsmNode. */
	public TermiteObject<T> getTermiteObject() {
		return termiteObject;
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
	
	/** This method copies relevant data from the base OsmObject needed for reproducing
	 * the data set. */
	public void copyInto(T newObject) {
		newObject.setIsLoaded(this.isLoaded);
		newObject.setIsVirtual(this.isVirtual);

		this.copyPropertiesInto(newObject);
	}
	
	//--------------------------
	// Property Methods
	//--------------------------
	

	/** This copies the property values from one map object to another. */
	public void copyPropertiesInto(OsmObject mapObject) {
		for(String key:tags.keySet()) {
			mapObject.tags.put(key,tags.get(key));
		}
	}
	
	public void setProperty(String tag, String propertyValue) {
		this.tags.put(tag,propertyValue);
	}
	
	public void removeProperty(String tag) {
		this.tags.remove(tag);
	}
	
	/** This method returns the given string property. If it is not found 
	 * null is returned. 
	 * @param property		The key for the property
	 * @return				The value for the property
	 */
	public String getProperty(String tag) {
		return tags.get(tag);
	}
	
	public Collection<String> getPropertyKeys() {
		return tags.keySet();
	}
	
	/** This method returns the given property as an integer. If it is not found 
	 * the default value is returned.
	 * @param property		The key for the property
	 * @return				The value for the property
	 */
	public int getIntProperty(String tag, int defaultValue) {
		String strValue = getProperty(tag);
		if(strValue != null) {
			try {
				return Integer.parseInt(strValue);
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		return defaultValue;
	}
	
	/** This method returns the given property as a long. If it is not found 
	 * the default value is returned.
	 * @param property		The key for the property
	 * @return				The value for the property
	 */
	public long getLongProperty(String tag, long defaultValue) {
		String strValue = getProperty(tag);
		if(strValue != null) {
			try {
				return Long.parseLong(strValue);
			}
			catch(Exception ex) {
				ex.printStackTrace();
				
			}
		}
		return defaultValue;
	}
	
	
	/** This method returns the given property as a double. If it is not found 
	 * the default value is returned.
	 * @param property		The key for the property
	 * @return				The value for the property
	 */
	public double getDoubleProperty(String tag, double defaultValue) {
		String strValue = getProperty(tag);
		if(strValue != null) {
			try {
				return Double.parseDouble(strValue);
			}
			catch(Exception ex) {
				ex.printStackTrace();
				return defaultValue;
			}
		}
		return defaultValue;
	}
	
	/** This method returns the given property as a boolean. If it is not found 
	 * the default value is returned.
	 * @param property		The key for the property
	 * @return				The value for the property
	 */
	public boolean getBooleanProperty(String tag, boolean defaultValue) {
		String strValue = getProperty(tag);
		if(strValue != null) {
			for(String trueValue:TRUE_STRINGS) {
				if(strValue.equalsIgnoreCase(trueValue)) return true;
			} 
			for(String falseValue:FALSE_STRINGS) {
				if(strValue.equalsIgnoreCase(falseValue)) return false;
			}
		}
		return defaultValue;
	}
	
		
	//==========================
	// Package Methods
	//==========================
	
	void setId(long id) {
		this.id = id;
	}
	
		/** This method sets the isLoaded flag for the object. */
	void setIsLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

}
