package intransix.osm.termite.map;

import java.util.ArrayList;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public abstract class DataParser<TK, TV> {
	
	public abstract TV parseValueData(JSONObject json, KeyNode<TK,TV> parentKey);
	
	public abstract TK parseKeyData(JSONObject json, PropertyNode<TK,TV> parentValue);
	
}
