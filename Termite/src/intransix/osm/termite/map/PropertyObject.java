package intransix.osm.termite.map;

import java.util.ArrayList;

import org.json.*;

/**
 * This is a property mapping object. 
 * 
 * @author sutter
 */
public class PropertyObject<TK, TV> {
	private String name;
	private ArrayList<KeyObject<TK,TV>> keys = new ArrayList<KeyObject<TK,TV>>();
	private TV data = null;
	
	/** This method parses the Property Object. The DataParser object should
	 * be sent in to parse the data. If the data parser is missing, no data
	 * will be parsed. */
	public void parse(JSONObject json, DataParser<TK, TV> dataParser) throws Exception {
		
		//get name
		name = json.optString("name","unnamed");
		
		//get data
		if(dataParser != null) {
			data = dataParser.parseValueData(json);
			if(dataParser != null) {
				dataParser.addValueParentData(data);
			}
		}
		else {
			data = null;
		}
		
		//get keys
		KeyObject key;
		JSONArray keysJson = json.optJSONArray("keys");
		if(keysJson != null) {
			JSONObject keyJson;
			int cnt = keysJson.length();
			for(int i = 0; i < cnt; i++) {
				keyJson = keysJson.getJSONObject(i);
				key = new KeyObject<TK,TV>();
				key.parse(keyJson,dataParser);
				keys.add(key);
			}
		}
		
		//remove the local data from the parent data list
		if((dataParser != null)&&(data != null)) {
			dataParser.removeValueParentData(data);
		}
	}
	
	/** This method returns the name. */
	public String getName() {
		return name;
	}
	
	/** This method returns the keys for this object. */
	public ArrayList<KeyObject<TK,TV>> getKeys() {
		return keys;
	}
	
	/** This method returns the data for this object. */
	public TV getData() {
		return data;
	}
	
	/** This method traverses the property tree and finds the matching property
	 * node for the given map object. It returns the data for the property node.  */
	public TV getPropertyData(MapObject mapObject) {
		String value;
		for(KeyObject<TK,TV> key:keys) {
			//see if the map object has a matching key
			value = mapObject.getProperty(key.getName());
			if(value != null) {
				//see if this key has a more specific value
				TV childData = key.getPropertyData(mapObject);
				//if yes, return it
				//if no, return this
				if(childData != null) return childData;
				else return this.data;
			}
			//else try the next key
		}
		//no matching child keys, return this
		return this.data;
	}
	
	/** This method returns the key node for the given key name on the
	 * given map object. If null is returned that no key was found. */
	public KeyObject<TK,TV> getKey(MapObject mapObject, String keyName) {
		for(KeyObject key:keys) {
			//check if key is defined here
			if(keyName.equalsIgnoreCase(key.getName())) return key;
			
			//check if it is defined on a child property
			KeyObject<TK,TV> childKey = key.getKey(mapObject, keyName);
			if(childKey != null) return childKey;
		}
		//none found
		return null;
	}
	
	/** This method returns the property node for the given key and value on the
	 * given map object. If null is returned that no match was found. This will return
	 * the property even if a different value for this key already exists on the map 
	 * object. */
	public PropertyObject<TK,TV> getKeyValue(MapObject mapObject, String keyName, String keyValue) {
		KeyObject<TK,TV> key = this.getKey(mapObject, keyName);
		if(key != null) {
			//check value on key
			return key.getKeyValue(keyValue);
		}
		else {
			//key not found
			return null;
		}
	}
	
	//======================== 
	// Protected Methods
	//========================

}
