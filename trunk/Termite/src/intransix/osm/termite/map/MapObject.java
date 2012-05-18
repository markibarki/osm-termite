/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.map;

import java.util.HashMap;

/**
 * This object provides the functionality of an id and a set of tags, used
 * by OSM map objects and by the internal map objects.
 * 
 * @author sutter
 */
public class MapObject {
	
	//====================
	// Constants
	//====================
	
	/** These are values of boolean strings that will be accepted as true. The
	 * first value is the desired value. */
	public final static String[] TRUE_STRINGS = {"yes","true","t"};
	
	/** These are values of boolean strings that will be accepted as false. The
	 * first value is the desired value. */
	public final static String[] FALSE_STRINGS = {"no","false","f"};
	
	//====================
	// Private Proeprties
	//====================
	
	private long id;
	private HashMap<String,String> tags = new HashMap<String,String>();
	
	//====================
	// Public Methods
	//====================
	
	/** Constructor. It should only be called by members of this package. */
	public MapObject() {
	}
	
	/** This returns the ID for the object. The type of ID this refers to 
	 * depends on the type of feature this is. 
	 * 
	 * @return		The ID for the object 
	 */
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public void setProperty(String tag, String propertyValue) {
		this.tags.put(tag,propertyValue);
	}
	
	/** This method returns the given string property. If it is not found 
	 * null is returned. 
	 * @param property		The key for the property
	 * @return				The value for the property
	 */
	public String getProperty(String tag) {
		return tags.get(tag);
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
	

	
		
}
