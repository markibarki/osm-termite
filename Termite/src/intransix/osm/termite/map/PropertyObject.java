package intransix.osm.termite.map;

import java.util.ArrayList;

import org.json.*;

/**
 * This is a property mapping object. 
 * 
 * @author sutter
 */
public class PropertyObject<T> {
	private String name;
	private ArrayList<KeyObject> keys = new ArrayList<KeyObject>();
	private T data = null;
	
	/** This method parses the Property Object. The DataParser object should
	 * be sent in to parse the data. If the data parser is missing, no data
	 * will be parsed. */
	public void parse(JSONObject json, DataParser<T> dataParser, 
			ArrayList<T> parentData) throws Exception {
		
		//get name
		name = json.optString("name","unnamed");
		
		//get data
		if(dataParser != null) {
			data = dataParser.parseData(json, parentData);
		}
		else {
			data = null;
		}
		
		//add parent data to parse keys and, if needed, values
		if(data != null) {
			parentData.add(data);
		}
		
		//get keys
		KeyObject key;
		JSONArray keysJson = json.optJSONArray("keys");
		if(keysJson != null) {
			JSONObject keyJson;
			int cnt = keysJson.length();
			for(int i = 0; i < cnt; i++) {
				keyJson = keysJson.getJSONObject(i);
				key = new KeyObject<T>();
				key.parse(keyJson,dataParser,parentData);
				keys.add(key);
			}
		}
		
		//get other stuff - used by extending classes
		parseAdditionalData(json, dataParser, parentData);
		
		//remove the local data from the parent data list
		if(data != null) {
			parentData.remove(data);
		}
	}
	
	/** This method returns the name. */
	public String getName() {
		return name;
	}
	
	/** This method returns the keys for this object. */
	public ArrayList<KeyObject> getKeys() {
		return keys;
	}
	
	/** This method returns the data for this object. */
	public T getData() {
		return data;
	}
	
	/** This method loads the property object that best matches this map object.
	 * For a non-key property object, it searches the keys in order and sees if
	 * there is a matching child property in the key. If not, the local object
	 * is returned. */
	public PropertyObject getPropertyObject(MapObject mapObject) {
		String value;
		for(KeyObject key:keys) {
			//see if the map object has a matching key
			value = mapObject.getProperty(key.getName());
			if(value != null) {
				//see if this key has a more specific value
				PropertyObject childProperty = key.getPropertyObject(mapObject);
				//if yse, return it
				//if no, return this
				if(childProperty != null) return childProperty;
				else return this;
			}
			//else try the next key
		}
		//no matching child keys, return this
		return this;
	}
	
	/** This method returns the data object associated with the best match
	 * property object for this map object. */
	public PropertyObject getDataObject(MapObject mapObject) {
		String value;
		for(KeyObject key:keys) {
			//see if the map object has a matching key
			value = mapObject.getProperty(key.getName());
			if(value != null) {
				//see if this key has a more specific value
				PropertyObject childProperty = key.getPropertyObject(mapObject);
				//if yse, return it
				//if no, return this
				if(childProperty != null) return childProperty.getDataObject(mapObject);
				else return this.getDataObject(mapObject);
			}
			//else try the next key
		}
		//no matching child keys, return this
		return this.getDataObject(mapObject);
	}
	
	/** This method returns the first key object it finds with the matching
	 * key name. For a non-ley property object it searches the included keys.*/
	public KeyObject getKeyObject(MapObject mapObject, String keyName) {
		for(KeyObject key:keys) {
			//key name is in this list
			if(keyName.equalsIgnoreCase(key.getName())) return key;
			
			//see if the key name is in a child list
			KeyObject childKey = key.getKeyObject(mapObject, keyName);
			if(childKey != null) return childKey;
		}
		//none found
		return null;
	}
	
	//======================== 
	// Protected Methods
	//========================
	
	/** This method is provided so extending classes can parse additional 
	 * information, mainly the key object, while the parent data is set. */
	protected void parseAdditionalData(JSONObject json, DataParser<T> dataParser, 
			ArrayList<T> parentData) throws Exception {
		//no added data to parse
	}
}
