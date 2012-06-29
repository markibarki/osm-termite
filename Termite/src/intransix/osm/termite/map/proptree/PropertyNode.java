package intransix.osm.termite.map.proptree;

import intransix.osm.termite.map.data.OsmObject;
import java.util.ArrayList;

import org.json.*;

/**
 * This is the property node in a Property Tree. 
 * 
 * @author sutter
 */
public class PropertyNode<TK, TV> {
	
	private KeyNode<TK,TV> parentKey;
	private String name;
	private ArrayList<KeyNode<TK,TV>> keys = new ArrayList<KeyNode<TK,TV>>();
	private TV data = null;
	
	/** This method parses the Property Object. The DataParser object should
	 * be sent in to parse the data. If the data parser is missing, no data
	 * will be parsed. */
	public void parse(JSONObject json, KeyNode<TK,TV> parentKey, 
			DataParser<TK, TV> dataParser) throws Exception {
		
		this.parentKey = parentKey;
		
		//get name
		name = json.optString("value","unnamed");
		
		//get data
		if(dataParser != null) {
			data = dataParser.parseValueData(json,parentKey);
		}
		else {
			data = null;
		}
		
		//get keys
		KeyNode key;
		JSONArray keysJson = json.optJSONArray("keys");
		if(keysJson != null) {
			JSONObject keyJson;
			int cnt = keysJson.length();
			for(int i = 0; i < cnt; i++) {
				keyJson = keysJson.getJSONObject(i);
				key = new KeyNode<TK,TV>();
				key.parse(keyJson,this,dataParser);
				keys.add(key);
			}
		}
	}
	
	/** This returns the parent key for this property. */
	public KeyNode<TK,TV> getParentKey() {
		return parentKey;
	}
	
	/** This method returns the name. */
	public String getName() {
		return name;
	}
	
	/** This method returns the keys for this object. */
	public ArrayList<KeyNode<TK,TV>> getKeys() {
		return keys;
	}
	
	/** This method returns the data for this object. */
	public TV getData() {
		return data;
	}
	
	/** This method traverses the property tree and finds the matching property
	 * node for the given map object. */
	public PropertyNode<TK,TV> getClassifyingProperty(OsmObject mapObject) {
		String value;
		for(KeyNode<TK,TV> key:keys) {
			//see if the map object has a matching key
			value = mapObject.getProperty(key.getName());
			if(value != null) {
				//see if this key has a more specific value
				PropertyNode<TK,TV> childProp = key.getClassifyingProperty(mapObject);
				//if yes, return it
				//if no, return this
				if(childProp != null) return childProp;
				else return this;
			}
			//else try the next key
		}
		//no matching child keys, return this
		return this;
	}
	
	/** This method is the same as getClassifyingProperty except is returns the
	 * data associated with that property object. */
	public TV getPropertyData(OsmObject mapObject) {
		PropertyNode<TK,TV> prop = this.getClassifyingProperty(mapObject);
		if(prop != null) return prop.data;
		else return null;
	}
	
	/** This method returns the key node for the given key name on the
	 * given map object. If null is returned that no key was found. */
	public KeyNode<TK,TV> getKey(OsmObject mapObject, String keyName) {
		for(KeyNode key:keys) {
			//check if key is defined here
			if(keyName.equalsIgnoreCase(key.getName())) return key;
			
			//check if it is defined on a child property
			KeyNode<TK,TV> childKey = key.getKey(mapObject, keyName);
			if(childKey != null) return childKey;
		}
		//none found
		return null;
	}
	
	/** This method returns the property node for the given key and value on the
	 * given map object. If null is returned that no match was found. This will return
	 * the property even if a different value for this key already exists on the map 
	 * object. */
	public PropertyNode<TK,TV> getKeyValue(OsmObject mapObject, String keyName, String keyValue) {
		KeyNode<TK,TV> key = this.getKey(mapObject, keyName);
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
