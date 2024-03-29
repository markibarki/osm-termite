package intransix.osm.termite.map.workingdata;

import intransix.osm.termite.map.feature.FeatureInfoMap;
import org.json.*;
import java.util.HashSet;

/**
 * This class holds information about the OSM data model.
 * 
 * @author sutter
 */
public class OsmModel {
	
	public static double mxOffset = 0;
	public static double myOffset = 0;
	
	public static String TYPE_LEVEL = "level";
	public static String ROLE_PARENT = "structure";
	public static String ROLE_ANCHOR = "anchor";
	public static String ROLE_NODE = "node";
	public static String KEY_ZLEVEL = "zlevel";
	public static String KEY_REF_KEY = "ref:key";
	public static String KEY_REF = "ref";
	public static String KEY_REF_SCOPE_GEOM = "ref:scope:geom";
	public static String KEY_REF_SCOPE_RELATION = "ref:scope:rel";
	public static String DEFAULT_ANGLE = "default_angle";
	
	//osm standards
	public final static String TYPE_NODE = "node";
	public final static String TYPE_WAY = "way";
	public final static String TYPE_RELATION = "relation";
	public final static String KEY_TYPE = "type";
	public final static String TYPE_MULTIPOLYGON = "multipolygon";
	public final static String KEY_AREA = "area";
	public final static String KEY_NAME = "name";
	
	public static HashSet<String> GEOMETRIC_KEYS = new HashSet<String>();
	
	//-----------------------------
	// URLS
	//-----------------------------
//	public static String OSM_SERVER = "http://api.openstreetmap.org";
public static String OSM_SERVER = "http://api06.dev.openstreetmap.org";
public static String PUBLISH_SERVICE = "http://open.micello.com/mapdata";

	//data request, arguments: minLat, minLon,maxLat,maxLon
	public static String DATA_REQUEST_PATH = "/api/0.6/map?bbox=%2$.8f,%1$.8f,%4$.8f,%3$.8f";
	
	//open change set
	public static String OPEN_CHANGESET_REQUEST_PATH = "/api/0.6/changeset/create";
	
	//commit, arugments: commit id
	public static String COMMIT_REQUEST_PATH = "/api/0.6/changeset/%1$d/upload";
	
	//close change set, arugments: commit id
	public static String CLOSE_CHANGESET_REQUEST_PATH = "/api/0.6/changeset/%1$d/close";
	
	//===============================
	// Methods
	//===============================
	
	/** This method parses a json file that holds model parameters. */
	public static void parse(JSONObject json) throws Exception {
		JSONObject nameDefs = json.optJSONObject("nameDefs");
		if(nameDefs != null) {
			TYPE_LEVEL = nameDefs.optString("levelType",TYPE_LEVEL);
			ROLE_PARENT = nameDefs.optString("parentRole",ROLE_PARENT);
			ROLE_NODE = nameDefs.optString("nodeRole",ROLE_NODE);
			ROLE_ANCHOR = nameDefs.optString("anchorRole",ROLE_ANCHOR);
			KEY_ZLEVEL = nameDefs.optString("zlevelKey",KEY_ZLEVEL);
			KEY_REF_KEY = nameDefs.optString("refKeyKey",KEY_REF_KEY);
			KEY_REF = nameDefs.optString("refKey",KEY_REF);
			KEY_REF_SCOPE_GEOM = nameDefs.optString("refScopeGeomKey",KEY_REF_SCOPE_GEOM);
			KEY_REF_SCOPE_RELATION = nameDefs.optString("refScopeRelKey",KEY_REF_SCOPE_RELATION);
			DEFAULT_ANGLE = nameDefs.optString("defaultAngle",DEFAULT_ANGLE);
		}
		
		OSM_SERVER = json.optString("osmServer",OSM_SERVER);
		PUBLISH_SERVICE = json.optString("publishService",PUBLISH_SERVICE);
		
		JSONArray geometricKeys = json.optJSONArray("geometricKeys");
		if(geometricKeys != null) {
			int cnt = geometricKeys.length();
			for(int i = 0; i < cnt; i++) {
				GEOMETRIC_KEYS.add(geometricKeys.getString(i));
			}
		}
			
	}
	
}
