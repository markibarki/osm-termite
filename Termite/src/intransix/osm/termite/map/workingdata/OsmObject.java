package intransix.osm.termite.map.workingdata;

import intransix.osm.termite.app.filter.FilterRule;
import intransix.osm.termite.app.mapdata.instruction.UnchangedException;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.util.PropertyPair;
import intransix.osm.termite.map.dataset.OsmSrcData;
import java.util.*;

/**
 * This method holds data common to OsmObjects
 * 
 * @author sutter
 */
public abstract class OsmObject<T extends OsmSrcData> {
	
	//====================
	// Constants
	//====================
	
	/** These are values of boolean strings that will be accepted as true. The
	 * first value is the desired value. */
	public final static String[] TRUE_STRINGS = {"yes","true","t"};
	
	/** These are values of boolean strings that will be accepted as false. The
	 * first value is the desired value. */
	public final static String[] FALSE_STRINGS = {"no","false","f"};
	
	//=======================
	// Properties
	//=======================
	
	//this tracks the count of pigguback users
	private final static Object piggybackLock = new Object();
	private static int piggybackDataCount = 0;
	
	private long id;
	private String type;
	
	private HashMap<String,String> tags = new HashMap<String,String>();
	private List<OsmRelation> relations = new ArrayList<OsmRelation>();
	
	private PiggybackData[] piggybackData = new PiggybackData[piggybackDataCount];
	
	private boolean isLoaded = false;
	private boolean isVirtual = false;
	private int dataVersion = OsmData.INVALID_DATA_VERSION;
	
	//=======================
	// Constructor
	//=======================
	
	/** This method allows users to register to set piggyback data on an object.
	 * This call must be made before an data is loaded. 
	 * 
	 * @return		The index to use to retrieve piggyback data from an OsmObject 
	 */
	public static int registerPiggybackUser() {
		synchronized(piggybackLock) {
			return piggybackDataCount++;
		}
	}
	
	/** This method gets the ID for the object. */
	public long getId() {
		return id;
	}
	
	/** This gets the object type string. */
	public String getObjectType() {
		return type;
	}
	
	/** This retrieves the piggyback data set for the given index. */
	public PiggybackData getPiggybackData(int index) {
		return piggybackData[index];
	}
	
	/** This sets piggyback data for the given index. */
	public void setPiggybackData(int index, PiggybackData data) {
		piggybackData[index] = data;
	}
	
	//-----------------------
	// State Management Methods
	//-----------------------
	
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
	
	public int getDataVersion() {
		return dataVersion;
	}

	//-------------------------
	// Relation methods
	//-------------------------
	
	/** This gets the list of relations of which this object is a member. */
	public List<OsmRelation> getRelations() {
		return relations;
	}
	
	//--------------------------
	// Property Methods
	//--------------------------
	
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
	
	/** This returns the number of properties. */
	public boolean  hasProperties() {
		return tags.size() > 0;
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
	
	/** The argument is the combined type + osmId string. */
	OsmObject(String type, long id) {
		this.type = type;
		this.id = id;
	}
	
	/** This method sets the id. */
	public void setId(long id) {
		this.id = id;
	}
	
	/** This method sets the isLoaded flag for the object. */
	public void setIsLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}
	
	/** This sets the data version. */
	public void setDataVersion(int version) {
		this.dataVersion = version;
	}
	
	/** This method updates the version number for all relations containing this object. */
	public void setContainingObjectDataVersion(int version) {
		for(OsmRelation relation:relations) {
			relation.setDataVersion(version);
		}
	}
	
	/** This copies the property values from one map object to another. */
	public void copyPropertiesInto(OsmObject mapObject) {
		for(String key:tags.keySet()) {
			mapObject.tags.put(key,tags.get(key));
		}
	}
	
	/** This method adds or updates a property. */
	public void setProperty(String tag, String propertyValue) {
		this.tags.put(tag,propertyValue);
	}
	
	/** This method removed a property. */
	public void removeProperty(String tag) {
		this.tags.remove(tag);
	}
	
	public void addRelation(OsmRelation relation) {
		if(!relations.contains(relation)) {
			relations.add(relation);
		}
	}
	
	public void removeRelation(OsmRelation relation) {
		relations.remove(relation);
	}
	
	/** This method verifies an object can be deleted. There can be no external
	 * objects referring to this one.
	 * 
	 * @throws UnchangedException	Thrown if this object can not be deleted 
	 */
	public void verifyDelete() throws UnchangedException {
		if(!relations.isEmpty()) {
			throw new UnchangedException("An object cannot be deleted is a relation contains it.");
		}
	}
	
	public void objectDeleted(OsmData osmData) {
		//this should be cleared before we delete
		//we must check for this earlier
		if(!relations.isEmpty()) {
			throw new RuntimeException("A relation referenced the deleted object");
		}
	}
	
	/** This method copies this object into the target. */
	public void copyInto(T targetData) {
		targetData.setId(id);
		for(String key:tags.keySet()) {
			targetData.addProperty(key,this.getProperty(key));
		}
	}
	
	/** This method copies the src data to this object. */
	public void copyFrom(T srcData, OsmData osmData) {
		this.id = srcData.getId();
		this.type = srcData.getObjectType();
		this.tags.clear();
		List<PropertyPair> properties = srcData.getProperties();
		for(PropertyPair pp:properties) {
			tags.put(pp.key,pp.value);
		}
		this.setIsLoaded(true);
	}

}
