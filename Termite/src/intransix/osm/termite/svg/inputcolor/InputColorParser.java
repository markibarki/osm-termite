package intransix.osm.termite.svg.inputcolor;

import intransix.osm.termite.map.*;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class InputColorParser extends DataParser<Object,String> {
	@Override
	public String parseValueData(JSONObject json, KeyNode<Object,String> parentKey) {
		try {
			return json.optString("inputcolor",null);
		}
		catch(Exception ex) {
			return null;
		}
	}

	@Override
	public Object parseKeyData(JSONObject json, PropertyNode<Object,String> parentValue) {
		return null;
	}
}
