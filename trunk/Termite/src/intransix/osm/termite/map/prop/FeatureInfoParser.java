package intransix.osm.termite.map.prop;

import intransix.osm.termite.map.*;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class FeatureInfoParser extends DataParser<Object,FeatureInfo> {
	
	@Override
	public FeatureInfo parseValueData(JSONObject json, KeyNode<Object,FeatureInfo> parentKey) {
		try {
			FeatureInfo parentData = null;
			PropertyNode<Object,FeatureInfo> parentProp;
			if(parentKey != null) {
				parentProp = parentKey.getParentValue();
				if(parentProp != null) {
					parentData = parentProp.getData();
				}
			}
			
			JSONObject dataJson = json.getJSONObject("data");
			return FeatureInfo.parse(dataJson, parentData);
		}
		catch(Exception ex) {
			return null;
		}
	}

	@Override
	public Object parseKeyData(JSONObject json, PropertyNode<Object,FeatureInfo> parentValue) {
		return null;
	}
}
