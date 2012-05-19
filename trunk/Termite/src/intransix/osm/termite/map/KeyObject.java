package intransix.osm.termite.map;

import java.util.*;
import org.json.*;

/**
 * This encapsulates a key object, with is a type of property object which also specifies
 * allowed values for itself.
 * 
 * @author sutter
 */
public class KeyObject<T> extends PropertyObject<T> {
	
	public final static boolean DEFAULT_EXCLUSIVE = false;
	
	private ArrayList<PropertyObject> values = new ArrayList<PropertyObject>();
	private boolean isExclusive = false;
	
	/** This method returns the values for this key. */
	public ArrayList<PropertyObject> getValues() {
		return values;
	}
	
	/** This method returns true if the value must be in the set of values listed
	 * for this key or if other values are OK. */
	public boolean getIsExclusive() {
		return isExclusive;
	}
	
	/** This method loads the most specific data object for the given
	 * map object. It checks for a matching property among the child values of the 
	 * key first, then it searches among the child keys. */
	@Override
	public PropertyObject getPropertyObject(MapObject mapObject) {
		//see if the map object has this key
		String value = mapObject.getProperty(this.getName());
		if(value != null) {
			for(PropertyObject prop:values) {
				//check for a match
				if(value.equalsIgnoreCase(prop.getName())) {
					//this matches - check for a more specific value
					PropertyObject childProperty = prop.getPropertyObject(mapObject);
					//if yes there is a child object
					//if no, return this property
					if(childProperty != null) return childProperty;
					else return prop;
				}
				//else try the next key
			}
		}
		//no matching child keys, return this
		return super.getPropertyObject(mapObject);
	}
	
	/** This method returns the first key object for the specified map object and key name. */
	@Override
	public KeyObject getKeyObject(MapObject mapObject, String keyName) {
		//see if the key in in the list for this object
		super.getKeyObject(mapObject,keyName);
		
		//see if the key is associated with any property values for this key
		for(PropertyObject prop:values) {
			KeyObject childKey = prop.getKeyObject(mapObject, keyName);
			if(childKey != null) return childKey;
		}
		//none found
		return null;
	}
	
	//===============================
	// protected methods
	//===============================
	
	/** This method parses the extra data in the key object beyond the property object. */
	@Override
	protected void parseAdditionalData(JSONObject json, DataParser<T> dataParser, 
			ArrayList<T> parentData) throws Exception {
		//get keys
		ArrayList<PropertyObject> props = new ArrayList<PropertyObject>();
		PropertyObject prop;
		JSONArray valuesJson = json.optJSONArray("values");
		if(valuesJson != null) {
			JSONObject valueJson;
			int cnt = valuesJson.length();
			for(int i = 0; i < cnt; i++) {
				valueJson = valuesJson.getJSONObject(i);
				prop = new PropertyObject<T>();
				prop.parse(valueJson,dataParser,parentData);
				values.add(prop);
			}
		}
		//get is exclusie
		isExclusive = json.optBoolean("exclusive",DEFAULT_EXCLUSIVE);
	}
}
