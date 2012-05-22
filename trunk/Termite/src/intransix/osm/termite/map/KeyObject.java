package intransix.osm.termite.map;

import java.util.*;
import org.json.*;

/**
 * This encapsulates a key object, with is a type of property object which also specifies
 * allowed values for itself.
 * 
 * @author sutter
 */
public class KeyObject<TK, TV> {
	
	public final static boolean DEFAULT_EXCLUSIVE = false;
	
	private String name;
	private ArrayList<PropertyObject<TK,TV>> values = new ArrayList<PropertyObject<TK,TV>>();
	private boolean isExclusive = false;
	private TK data = null;
	
	/** This method parses the Key Object. The DataParser object should
	 * be sent in to parse the data. If the data parser is missing, no data
	 * will be parsed. */
	public <TV> void parse(JSONObject json, DataParser<TK, TV> dataParser) throws Exception {
		
		//get name
		name = json.optString("name","unnamed");
		
		//get data
		if(dataParser != null) {
			data = dataParser.parseKeyData(json);
			if(data != null) {
				dataParser.addKeyParentData(data);
			}
		}
		else {
			data = null;
		}
		
		//get keys
		PropertyObject prop;
		JSONArray valuesJson = json.optJSONArray("values");
		if(valuesJson != null) {
			JSONObject valueJson;
			int cnt = valuesJson.length();
			for(int i = 0; i < cnt; i++) {
				valueJson = valuesJson.getJSONObject(i);
				prop = new PropertyObject<TK,TV>();
				prop.parse(valueJson,dataParser);
				values.add(prop);
			}
		}
		//get is exclusie
		isExclusive = json.optBoolean("exclusive",DEFAULT_EXCLUSIVE);
		
		//remove the local data from the parent data list
		if((dataParser != null)&&(data != null)) {
			dataParser.removeKeyParentData(data);
		}
	}
	
	/** This method returns the name. */
	public String getName() {
		return name;
	}
	
	/** This method returns the data for this object. */
	public TK getData() {
		return data;
	}
	
	/** This method returns the values for this key. */
	public ArrayList<PropertyObject<TK,TV>> getValues() {
		return values;
	}
	
	/** This method returns true if the value must be in the set of values listed
	 * for this key or if other values are OK. */
	public boolean getIsExclusive() {
		return isExclusive;
	}
	
	
	/** This method finds a matching property node for this map object, checking
	 * the child node corresponding to the matching child value for this key.  */
	public TV getPropertyData(MapObject mapObject) {
		String keyValue = mapObject.getProperty(name);
		if(keyValue != null) {
			PropertyObject<TK,TV> matchingProp = this.getKeyValue(keyValue);
			if(matchingProp != null) {
				return matchingProp.getPropertyData(mapObject);
			}
			else {
				return null;
			}
		}

		//no matching child keys, return this
		return null;
	}
	
	/** This method attempts to find a matching key object for this map object
	 * and key name. It checks if the local key is a match and, if not, it checks
	 * if the child property that matches the map object has a matching key. */
	public KeyObject<TK,TV> getKey(MapObject mapObject, String keyName) {
		//check if this key is a match
		//map object does not need to have this key already
		if(keyName.equalsIgnoreCase(name)) return this;
		
		//check for a match on child property matching this map object
		//the map object must have this key to check properties
		String keyValue = mapObject.getProperty(name);
		if(keyValue != null) {
			PropertyObject<TK,TV> matchingProp = this.getKeyValue(keyValue);
			if(matchingProp != null) return matchingProp.getKey(mapObject, keyName);
		}
		
		//none found
		return null;
	}
	
	/** This method returns the property object that contains the given value
	 * for this key. If a match is not found null is returned. */
	public PropertyObject<TK,TV> getKeyValue(String keyValue) {
		for(PropertyObject<TK,TV> prop:values) {
			//return the matching property
			if(keyValue.equalsIgnoreCase(prop.getName())) {
				return prop;
			}
		}
		return null;
	}
	
	
	//===============================
	// protected methods
	//===============================

}
