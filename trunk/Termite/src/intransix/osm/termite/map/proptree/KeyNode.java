package intransix.osm.termite.map.proptree;

import intransix.osm.termite.map.osm.OsmObject;
import java.util.*;
import org.json.*;

/**
 * This is a key node in a property tree. It encapsulates the  keys for 
 * a given property, and in turn includes property values for this key.
 * 
 * @author sutter
 */
public class KeyNode<TK, TV> {
	
	//===============
	// Properties
	//===============
	
	public final static boolean DEFAULT_EXCLUSIVE = false;
	
	private PropertyNode parentValue;
	private String name;
	private ArrayList<PropertyNode<TK,TV>> values = new ArrayList<PropertyNode<TK,TV>>();
	private boolean isExclusive = false;
	private TK data = null;
	
	//===============
	// Public Methods
	//===============
	
	/** This method parses the Key Object. The DataParser object should
	 * be sent in to parse the data. If the data parser is missing, no data
	 * will be parsed. */
	public <TV> void parse(JSONObject json, PropertyNode<TK,TV> parentValue,
			DataParser<TK, TV> dataParser) throws Exception {
		
		this.parentValue = parentValue;
		
		//get name
		name = json.optString("key","unnamed");
		
		//get data
		if(dataParser != null) {
			data = dataParser.parseKeyData(json,parentValue);
		}
		else {
			data = null;
		}
		
		//get keys
		PropertyNode prop;
		JSONArray valuesJson = json.optJSONArray("values");
		if(valuesJson != null) {
			JSONObject valueJson;
			int cnt = valuesJson.length();
			for(int i = 0; i < cnt; i++) {
				valueJson = valuesJson.getJSONObject(i);
				prop = new PropertyNode<TK,TV>();
				prop.parse(valueJson,this,dataParser);
				values.add(prop);
			}
		}
		//get is exclusie
		isExclusive = json.optBoolean("exclusive",DEFAULT_EXCLUSIVE);
	}
	
	/** This returns the parent value for this key. */
	public PropertyNode<TK,TV> getParentValue() {
		return parentValue;
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
	public ArrayList<PropertyNode<TK,TV>> getValues() {
		return values;
	}
	
	/** This method returns true if the value must be in the set of values listed
	 * for this key or if other values are OK. */
	public boolean getIsExclusive() {
		return isExclusive;
	}
	
	
	/** This method finds a matching property node for this map object, checking
	 * the child node corresponding to the matching child value for this key.  */
	public PropertyNode<TK,TV> getClassifyingProperty(OsmObject mapObject) {
		String keyValue = mapObject.getProperty(name);
		if(keyValue != null) {
			PropertyNode<TK,TV> matchingProp = this.getKeyValue(keyValue);
			if(matchingProp != null) {
				return matchingProp.getClassifyingProperty(mapObject);
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
	public KeyNode<TK,TV> getKey(OsmObject mapObject, String keyName) {
		//check if this key is a match
		//map object does not need to have this key already
		if(keyName.equalsIgnoreCase(name)) return this;
		
		//check for a match on child property matching this map object
		//the map object must have this key to check properties
		String keyValue = mapObject.getProperty(name);
		if(keyValue != null) {
			PropertyNode<TK,TV> matchingProp = this.getKeyValue(keyValue);
			if(matchingProp != null) return matchingProp.getKey(mapObject, keyName);
		}
		
		//none found
		return null;
	}
	
	/** This method returns the property object that contains the given value
	 * for this key. If a match is not found null is returned. */
	public PropertyNode<TK,TV> getKeyValue(String keyValue) {
		for(PropertyNode<TK,TV> prop:values) {
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
