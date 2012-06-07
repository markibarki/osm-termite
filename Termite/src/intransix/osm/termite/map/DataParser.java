package intransix.osm.termite.map;

import org.json.JSONObject;

/**
 * This is a parser interface for the PropertyTree json file.
 * 
 * @author sutter
 */
public abstract class DataParser<TK, TV> {
	
	public abstract TV parseValueData(JSONObject json, KeyNode<TK,TV> parentKey);
	
	public abstract TK parseKeyData(JSONObject json, PropertyNode<TK,TV> parentValue);
	
}
