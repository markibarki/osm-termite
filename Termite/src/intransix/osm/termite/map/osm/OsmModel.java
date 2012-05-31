package intransix.osm.termite.map.osm;

import org.json.*;
import java.util.HashSet;

/**
 * This class holds information about the OSM data model.
 * 
 * @author sutter
 */
public class OsmModel {
	public static boolean doNodeLevelLabels = false;
	
	public static String TYPE_STRUCTURE = "structure";
	public static String TYPE_LEVEL = "level";
	public static String ROLE_PARENT = "parent";
	public static String ROLE_LEVEL = "level";
	public static String ROLE_SHELL = "shell";
	public static String ROLE_ANCHOR = "anchor";
	public static String ROLE_FEATURE = "feature";
	public static String KEY_ZLEVEL = "zlevel";
	public static String KEY_ZCONTEXT = "structure";
	public static String KEY_REF_KEY = "ref:key";
	public static String KEY_REF = "ref";
	public static String KEY_REF_SCOPE_GEOM = "ref:scope:geom";
	public static String KEY_REF_SCOPE_RELATION = "ref:scope:rel";
	
	public static HashSet<String> GEOMETRIC_KEYS = new HashSet<String>();
	
	public static void parse(JSONObject json) throws Exception {
		JSONObject nameDefs = json.optJSONObject("nameDefs");
		if(nameDefs != null) {
			TYPE_STRUCTURE = nameDefs.optString("structureType",TYPE_STRUCTURE);
			TYPE_LEVEL = nameDefs.optString("levelType",TYPE_LEVEL);
			ROLE_PARENT = nameDefs.optString("parentRole",ROLE_PARENT);
			ROLE_LEVEL = nameDefs.optString("levelRole",ROLE_LEVEL);
			ROLE_SHELL = nameDefs.optString("shellRole",ROLE_SHELL);
			ROLE_FEATURE = nameDefs.optString("featureRole",ROLE_FEATURE);
			ROLE_ANCHOR = nameDefs.optString("anchorRole",ROLE_ANCHOR);
			KEY_ZLEVEL = nameDefs.optString("zlevelKey",KEY_ZLEVEL);
			KEY_ZCONTEXT = nameDefs.optString("zcontextKey",KEY_ZCONTEXT);
			KEY_REF_KEY = nameDefs.optString("refKeyKey",KEY_REF_KEY);
			KEY_REF = nameDefs.optString("refKey",KEY_REF);
			KEY_REF_SCOPE_GEOM = nameDefs.optString("refScopeGeomKey",KEY_REF_SCOPE_GEOM);
			KEY_REF_SCOPE_RELATION = nameDefs.optString("refScopeRelKey",KEY_REF_SCOPE_RELATION);
		}
		
		String levelSpec = json.optString("levelSpec",null);
		doNodeLevelLabels = levelSpec.equalsIgnoreCase("nodes");
		
		JSONArray geometricKeys = json.optJSONArray("geometricKeys");
		if(geometricKeys != null) {
			int cnt = geometricKeys.length();
			for(int i = 0; i < cnt; i++) {
				GEOMETRIC_KEYS.add(geometricKeys.getString(i));
			}
		}
			
	}
	
}
