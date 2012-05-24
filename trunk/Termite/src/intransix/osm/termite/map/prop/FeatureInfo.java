package intransix.osm.termite.map.prop;

import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class FeatureInfo {
	
	public final static int GEOM_TYPE_NONE = -1;
	public final static int GEOM_TYPE_POINT = 0;
	public final static int GEOM_TYPE_LINE = 1;
	public final static int GEOM_TYPE_AREA = 2;
	
	public final static int ALLOWED_TYPE_POINT = 1;
	public final static int ALLOWED_TYPE_LINE = 2;
	public final static int ALLOWED_TYPE_AREA = 4;
	public final static int ALLOWED_TYPES_ALL = ALLOWED_TYPE_POINT | ALLOWED_TYPE_LINE | ALLOWED_TYPE_AREA;
	
	public final static int DEFAULT_ZORDER = 999;
	public final static int DEFAULT_ALLOWED_TYPES = ALLOWED_TYPES_ALL;
	public final static int DEFAULT_DEFAULT_PATH = GEOM_TYPE_LINE;
	
	//this is used for draw order
	private int zorder = DEFAULT_ZORDER;
	
	//this is used for import/export geometry
	private String inputColor;
	
	//this is rules for the geometry
	private int allowedTypes;
	private int defaultPathType;
	
	public int getZorder() {
		return zorder;
	}
	
	public String getInputColor() {
		return inputColor;
	}
	
	public static FeatureInfo parse(JSONObject json, FeatureInfo parent) {
		FeatureInfo fp = new FeatureInfo();
		if(json != null) {
			//read these or use parent value as default
			fp.zorder = json.optInt("zorder",(parent != null) ? parent.zorder : DEFAULT_ZORDER);
			fp.allowedTypes = json.optInt("allowedtypes",(parent != null) ? parent.allowedTypes : DEFAULT_ALLOWED_TYPES);
			fp.defaultPathType = json.optInt("defaultpath",(parent != null) ? parent.defaultPathType : DEFAULT_DEFAULT_PATH);
			
			//do not inherit here! Must be unique
			fp.inputColor = json.optString("inputcolor",null);
		}
		
		return fp;
	}
}
