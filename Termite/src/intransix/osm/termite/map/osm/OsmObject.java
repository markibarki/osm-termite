package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.*;

/**
 * This method holds data common to OsmObjects
 * 
 * @author sutter
 */
public abstract class OsmObject/*<T extends OsmObject>*/ {
	
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
			
	private long id;
	private String type;
	
	private HashMap<String,String> tags = new HashMap<String,String>();
	private List<OsmRelation> relations = new ArrayList<OsmRelation>();
	
	private Object renderData;
	private Object editData;
	private FeatureInfo featureInfo = null;
	
	private boolean isLoaded = false;
	private boolean isVirtual = false;
	private int dataVersion = 0;
	
	//=======================
	// Constructor
	//=======================
	
	/** This method gets the ID for the object. */
	public long getId() {
		return id;
	}
	
	/** This gets the object type string. */
	public String getObjectType() {
		return type;
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
	
	//---------------------------
	// Piggyback and model data methods
	//---------------------------
	
	/** This method gets the feature info associated with this object. */
	public FeatureInfo getFeatureInfo() {
		return featureInfo;
	}

	/** This method returns the render data.  */
	public Object getRenderData() {
		return renderData;
	}
	
	/** This method sets the render data. The render data is an arbitrary object
	 * that is set by the rendering software. */
	public void setRenderData(Object renderData) {
		this.renderData = renderData;
	}
	
	/** This method returns the edit data. */
	public Object getEditData() {
		return editData;
	}
	
	/** This method sets the edit data. The edit data is an arbitrary object
	 * that is set by the edit software.  */
	public void setEditData(Object editData) {
		this.editData = editData;
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
	void setId(long id) {
		this.id = id;
	}
	
	/** This method sets the isLoaded flag for the object. */
	void setIsLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}
	
	/** This sets the data version. */
	void setDataVersion(int version) {
		this.dataVersion = version;
	}
	
	/** This method updates the version number for all relations containing this object. */
	void setContainingObjectDataVersion(int version) {
		for(OsmRelation relation:relations) {
			relation.setDataVersion(version);
		}
	}
	
	/** This copies the property values from one map object to another. */
	void copyPropertiesInto(OsmObject mapObject) {
		for(String key:tags.keySet()) {
			mapObject.tags.put(key,tags.get(key));
		}
	}
	
	/** This method adds or updates a property. */
	void setProperty(String tag, String propertyValue) {
		this.tags.put(tag,propertyValue);
	}
	
	/** This method removed a property. */
	void removeProperty(String tag) {
		this.tags.remove(tag);
	}
	
	void addRelation(OsmRelation relation) {
		if(!relations.contains(relation)) {
			relations.add(relation);
		}
	}
	
	void removeRelation(OsmRelation relation) {
		relations.remove(relation);
	}
	
	/** This method verifies an object can be deleted. There can be no external
	 * objects referring to this one.
	 * 
	 * @throws UnchangedException	Thrown if this object can not be deleted 
	 */
	void verifyDelete() throws UnchangedException {
		if(!relations.isEmpty()) {
			throw new UnchangedException("An object cannot be deleted is a relation contains it.");
		}
	}
	
	void objectCreated(OsmData osmData) {
		//classify objects and add to feature list for all nodes and ways. Not for relations
		if((this instanceof OsmNode)||(this instanceof OsmWay)) {
			featureInfo = OsmModel.featureInfoMap.getFeatureInfo(this);
			int zorder = featureInfo.getZorder();

			//update the graduated list
			GraduatedList<OsmObject> orderedList = osmData.getOrderedList();
			orderedList.add(this, zorder);
		}
	}
	
	void propertiesUpdated(OsmData osmData) {
		//classify objects and add to feature list for all nodes and ways. Not for relations
		if((this instanceof OsmNode)||(this instanceof OsmWay)) {
			int initialZorder = featureInfo.getZorder();
			featureInfo = OsmModel.featureInfoMap.getFeatureInfo(this);
			int newZorder = featureInfo.getZorder();

			//update the graduated list
			GraduatedList<OsmObject> orderedList = osmData.getOrderedList();
			orderedList.move(this,newZorder,initialZorder);
		}
	}
	
	void objectDeleted(OsmData osmData) {
		//this should be cleared before we delete
		//we must check for this earlier
		if(!relations.isEmpty()) {
			throw new RuntimeException("A relation referenced the deleted object");
		}
		
		//remoge from the graducated list
		featureInfo = OsmModel.featureInfoMap.getFeatureInfo(this);
		int zorder = featureInfo.getZorder();
		
		//update the graduated list
		GraduatedList<OsmObject> orderedList = osmData.getOrderedList();
		orderedList.remove(this, zorder);
	}


}
