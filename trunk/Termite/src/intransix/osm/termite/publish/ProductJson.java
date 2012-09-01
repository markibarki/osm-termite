package intransix.osm.termite.publish;

import intransix.osm.termite.map.data.OsmRelation;
import java.util.*;
import org.json.*;
import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.gui.filter.LevelFilterRule;
import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author sutter
 */
public class ProductJson {
	
	private final static int PRECISION = 3;
	private final static char KEY_VALUE_DELIMITER = ':';
	
	private OsmData osmData;
	private OsmWay structure;
	private List<OsmRelation> levels;
	
	private JSONObject structureJson;
	
	private JSONArray structureLevelJsons;
	private List<JSONObject> levelJsons;
	
	private HashSet<String> namespaces = new HashSet<String>();
	private AffineTransform mercToNat;

	public ProductJson(OsmData osmData, OsmWay structure, List<OsmRelation> levels) {
		this.osmData = osmData;
		this.structure = structure;
		this.levels = levels;
	}
	
	public JSONObject getStructureJson() {
		return structureJson;
	}
	
	public List<JSONObject> getLevelJsons() {
		return levelJsons;
	}
	
	public void createProducts() throws Exception {
		loadStructureJson();
		for(OsmRelation level:levels) {
			loadLevelJson(level);
		}
		completeJsons();
	}
	
	private void loadStructureJson() throws Exception {
		JSONObject json = new JSONObject();
		json.put("id",structure.getId());
		String name = structure.getProperty(OsmModel.KEY_NAME);
		if(name != null) {
			name = "Structure " + structure.getId();
		}
		json.put("nm",name);
		
		structureLevelJsons = new JSONArray();
		json.put("lvl",structureLevelJsons);
		
		//set the transform and height and width
		Rectangle2D rect = getBounds();
		loadTransformInfo(json,rect);	
		
		structureJson = json;
		
	}
	
	private Rectangle2D getBounds() {
		
		//measure the structure bounds
		Rectangle2D rect = null;
		Point2D point;
		//check the levels
		for(OsmRelation level:levels) {
			FilterRule filter = new LevelFilterRule(level);
			for(OsmNode node:osmData.getOsmNodes()) {

				//this is a cludgy work aroudn to get the filter value for the level
				int originalState = node.getFilterState();
				filter.filterObject(node);
				if(node.renderEnabled()) {
					point = node.getPoint();
					if(rect == null) {
						rect = new Rectangle2D.Double(point.getX(),point.getY(),0,0);
					}
					else {
						rect.add(point);
					}
				}
				node.setFilterState(originalState);
			}
		}
		//add in the parent structure - assume rect has been set since there are levels
		for(OsmNode node:structure.getNodes()) {
			rect.add(node.getPoint());
		}
		return rect;
	}
	
	private void loadTransformInfo(JSONObject json, Rectangle2D rect) throws Exception {
		
		//generate the height, width and transform
		double metersPerMerc = MercatorCoordinates.metersPerMerc(rect.getCenterY());
		
		double minLat = MercatorCoordinates.mxToLonRad(rect.getMaxY());
		double minLon = MercatorCoordinates.mxToLonRad(rect.getMinX());
		double maxLat = MercatorCoordinates.mxToLonRad(rect.getMinY());
		double maxLon = MercatorCoordinates.mxToLonRad(rect.getMaxX());
		
		double maxPixX = (rect.getWidth()) * metersPerMerc;
		double maxPixY = (rect.getHeight()) * metersPerMerc;
		
		//get the transform from nat to latlon
		double txx = (maxLon - minLon)/maxPixX;
		double tyx = 0;
		double txy = 0;
		double tyy = (minLat - maxLat)/maxPixY;
		double tx0 = minLon;
		double ty0 = maxLat;
		JSONArray natToLatLonJson = new JSONArray();
		natToLatLonJson.put(txx);
		natToLatLonJson.put(tyx);
		natToLatLonJson.put(txy);
		natToLatLonJson.put(tyy);
		natToLatLonJson.put(tx0);
		natToLatLonJson.put(ty0);
		
		//get the nat to merc and mrec to nat tranforms
		txx = 1/metersPerMerc;
		tyx = 0;
		txy = 0;
		tyy = 1/metersPerMerc;
		tx0 = rect.getMinX();
		ty0 = rect.getMinY();
		AffineTransform natToMerc = new AffineTransform();
		natToMerc.setTransform(txx, tyx, txy, tyy, tx0, ty0);
		mercToNat = natToMerc.createInverse();
			
		json.put("t",natToLatLonJson);
		json.put("h",maxPixY);
		json.put("w",maxPixX);
		
	}
			
	private void loadLevelJson(OsmRelation level) throws Exception {
		JSONObject json = new JSONObject();
		JSONObject partialJson = new JSONObject();
		
		//get id
		json.put("id",level.getId());
		partialJson.put("id",level.getId());
		
		json.put("mid",structure.getId());
		
		//get zlevel
		int zlevel = level.getIntProperty(OsmModel.KEY_ZLEVEL,Integer.MAX_VALUE);
		if(zlevel == Integer.MAX_VALUE) {
			throw new Exception("Level id " + level.getId() + " have no zlevel");
		}
		json.put("z",zlevel);
		partialJson.put("z",zlevel);
		
		//get name
		String name = structure.getProperty(OsmModel.KEY_NAME);
		if(name != null) {
			name = "Level " + zlevel;
		}
		json.put("nm",name);
		partialJson.put("nm",name);
		
		//add the features
		json.put("type","FeatureCollection");
		JSONArray featureArray = new JSONArray();
		json.put("features",featureArray);
		
		FilterRule filter = new LevelFilterRule(level);
		for(OsmObject feature:osmData.getFeatureList()) {
			
			//this is a cludgy work aroudn to get the filter value for the level
			int originalState = feature.getFilterState();
			filter.filterObject(feature);
			if(feature.renderEnabled()) {
				JSONObject featureJson = getFeatureJson(feature);
				if(featureJson != null) {
					featureArray.put(featureJson);
				}
			}
			feature.setFilterState(originalState);
		}
		
		//save jsons
		levelJsons.add(json);
		structureLevelJsons.put(partialJson);
	}	
	
	private void completeJsons() throws Exception {
		//save the name spaces
		JSONArray namespaceJson = new JSONArray();
		structureJson.put("ns",namespaceJson);
		for(String namespace:namespaces) {
			namespaceJson.put(namespace);
		}
	}
	
	
	private JSONObject getFeatureJson(OsmObject feature) throws Exception {
		
		//don't include nodes with no properties
		if((feature instanceof OsmNode)&&(feature.hasProperties())) {
			return null;
		}
		
		JSONObject json = new JSONObject();
		
		json.put("id",feature.getId());
		json.put("type","Feature");
		
		//add a style property, if this is not a generic object
		//we want a single serialized name that includes all the properties
		//that identify the object. The feature info will do this.
		//But we will pull the leaeding key out as the propery key
		FeatureInfo featureInfo = feature.getFeatureInfo();
		String name = featureInfo.getName();
		int index = name.indexOf(KEY_VALUE_DELIMITER);
		if(index > 0) {
			String key = name.substring(0, index);
			String value = name.substring(index+1);
			JSONObject props = new JSONObject();
			props.put(key,value);
			json.put("properties",props);
		}
		
		//set the geometry
		JSONObject geomJson = new JSONObject();
		JSONArray coordinates = null;
		json.put("geometry",geomJson);
		if(feature instanceof OsmNode) {
			geomJson.put("type","Point");
			coordinates = getJsonPoint((OsmNode)feature);
		}
		else if(feature instanceof OsmWay) {
			OsmWay way = (OsmWay)feature;
			if(way.getIsArea()) {
				geomJson.put("type","Polygon");
				coordinates = getJsonPointArrayList(way);
			}
			else {
				geomJson.put("type","LineString");
				coordinates = getJsonPointArray(way);
			}
		}
		
		if(coordinates != null) {
			geomJson.put("coordinates",coordinates);
		}
		else {
			//return no object if there are no coordinates
			return null;
		}
		
		return json;		
	}
	
	/** This method creates a json point in the format [x,y]. */
	JSONArray getJsonPoint(OsmNode node) throws Exception {
		Point2D point = new Point2D.Double();
		mercToNat.transform(node.getPoint(), point);
		JSONArray pointJson = new JSONArray();
		pointJson.put(new FormattedDecimal(point.getX(),PRECISION));
		pointJson.put(new FormattedDecimal(point.getY(),PRECISION));
		return pointJson;
	}
	
	/** This returns an json array of points corresponding to the passed node list. */
	JSONArray getJsonPointArray(OsmWay way) throws Exception {
		
		JSONArray pointJsonArray = new JSONArray();
		for(OsmNode node:way.getNodes()) {
			JSONArray point = getJsonPoint(node);
			if(point != null) {
				pointJsonArray.put(point);
			}
		}
		return pointJsonArray;
	}
	
	/** This returns a list of point lists, corresponding to the main nodes
	 * along with any inside rings. This is used for a polygon. */
	JSONArray getJsonPointArrayList(OsmWay way /*, List<OsmWay> holes */) throws Exception {		
		JSONArray pointJsonArrayList = new JSONArray();
		JSONArray pointJsonArray = getJsonPointArray(way);
		pointJsonArrayList.put(pointJsonArray);
		
//this is for a polygon with holes
//		for(OsmWay hole:holes) {
//			 pointJsonArray = getJsonPointArray(hole.nodes);
//			 pointJsonArrayList.put(pointJsonArray);
//		}
		return pointJsonArrayList;
	}
	
	

}
