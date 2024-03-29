package intransix.osm.termite.app.mapdata.publish;

import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.app.filter.FilterRule;
import intransix.osm.termite.map.workingdata.OsmRelation;
import java.util.*;
import org.json.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.app.level.LevelFilterRule;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author sutter
 */
public class ProductJson {
	
	private final static int OPENLAYERS_NAT_PRECISION = 3;
	private final static int LAT_LON_PRECISION = 7;
	private final static char KEY_VALUE_DELIMITER = ':';
	
	private final static int MERC_UNIT_FRAME = 0;
	private final static int LAT_LON_FRAME = 1;
	private final static int OPENLAYERS_NAT_FRAME = 2;
	
	
	private MapDataManager mapDataManager;
	private OsmWay structure;
	private List<OsmRelation> levels;
	private int version;
	
	private JSONObject structureJson;
	
	private JSONArray structureLevelJsons;
	private List<JSONObject> levelJsons = new ArrayList<JSONObject>();
	
	private JSONObject footprintJson;
	
	private HashSet<String> namespaces = new HashSet<String>();
	private AffineTransform mercToNat;
	private AffineTransform natToMerc;
	private double widthMeters;
	private double heightMeters;

	public ProductJson(MapDataManager mapDataManager, OsmWay structure, List<OsmRelation> levels, int version) {
		this.mapDataManager = mapDataManager;
		this.structure = structure;
		this.levels = levels;
		this.version = version;
	}
	
	public JSONObject getStructureJson() {
		return structureJson;
	}
	
	public List<JSONObject> getLevelJsons() {
		return levelJsons;
	}
	
	public JSONObject getFootprintJson() {
		return footprintJson;
	}
	
	public void createProducts() throws Exception {
		//create product jsons
		loadStructureJson();
		for(OsmRelation level:levels) {
			loadLevelJson(level);
		}
		completeJsons();
		
		//create footprint feature
		createFootprintFeature();
	}
	
	private void loadStructureJson() throws Exception {
		JSONObject json = new JSONObject();
		json.put("id",structure.getId());
		json.put("v",version);
		String name = structure.getProperty(OsmModel.KEY_NAME);
		if(name == null) {
			name = "Structure " + structure.getId();
		}
		json.put("nm",name);
		
		structureLevelJsons = new JSONArray();
		json.put("lvl",structureLevelJsons);
		
		//set the transform and height and width
		double defaultAngleDeg = structure.getDoubleProperty("default_angle",0);
		createTransform(defaultAngleDeg);
		
		AffineTransform natToLatLon = MercatorCoordinates.convertMercatorToLatLonTransform(natToMerc);
		double[] matrix = new double[6];
		natToLatLon.getMatrix(matrix);
		JSONArray natToLatLonJson = new JSONArray();
		for(int i = 0; i < 6; i++) {
			natToLatLonJson.put(matrix[i]);
		}
		
		//save transform info
		json.put("t",natToLatLonJson);
		json.put("h",heightMeters);
		json.put("w",widthMeters);
		
		structureJson = json;
		
	}
	
	private void createTransform(double defaultAngleDeg) {

		double defaultAngleRad = Math.toRadians(defaultAngleDeg);
		AffineTransform defaultRotationTransform = new AffineTransform();
		defaultRotationTransform.setToRotation(defaultAngleRad);
		AffineTransform invDefaultRotationTransform = new AffineTransform();
		invDefaultRotationTransform.setToRotation(-defaultAngleRad);
		
		//measure the structure bounds
		OsmData osmData = mapDataManager.getOsmData();
		Rectangle2D rect = null;
		Point2D mercPoint;
		Point2D rotPoint = new Point2D.Double();
		int filterValue;
		//check the levels
		for(OsmRelation level:levels) {
			FilterRule filter = new LevelFilterRule(level);
			for(OsmNode node:osmData.getOsmNodes()) {
				filterValue = filter.getFilterValue(node,filter.getInitialState());
				if((filterValue & FilterRule.RENDER_ENABLED) != 0) {
					mercPoint = node.getPoint();
					defaultRotationTransform.transform(mercPoint, rotPoint);
					if(rect == null) {
						rect = new Rectangle2D.Double(rotPoint.getX(),rotPoint.getY(),0,0);
					}
					else {
						rect.add(rotPoint);
					}
				}
			}
		}
		//add in the parent structure - assume rect has been set since there are levels
		for(OsmNode node:structure.getNodes()) {
			mercPoint = node.getPoint();
			defaultRotationTransform.transform(mercPoint, rotPoint);
			rect.add(rotPoint);
		}
		//get the transformation to mercator
		//get the nat to merc and mrec to openlayers nat tranforms
		double metersPerMerc = MercatorCoordinates.metersPerMerc(rect.getCenterY());
		double txx = 1/metersPerMerc;
		double tyx = 0;
		double txy = 0;
		double tyy = -1/metersPerMerc;
		double tx0 = rect.getMinX();
		double ty0 = rect.getMaxY();
		natToMerc = new AffineTransform();
		natToMerc.setTransform(txx, tyx, txy, tyy, tx0, ty0);
		natToMerc.preConcatenate(invDefaultRotationTransform);
		try {
			mercToNat = natToMerc.createInverse();
		}
		catch(Exception ex) {
			//this shouldn't happen
		}
		
		widthMeters = rect.getWidth() * metersPerMerc;
		heightMeters = rect.getHeight() * metersPerMerc;
	}
			
	private void loadLevelJson(OsmRelation level) throws Exception {
		JSONObject json = new JSONObject();
		JSONObject partialJson = new JSONObject();
		
		//get id
		json.put("id",level.getId());
		partialJson.put("id",level.getId());
		
		json.put("mid",structure.getId());
		json.put("v",version);
		
		//get zlevel
		int zlevel = level.getIntProperty(OsmModel.KEY_ZLEVEL,Integer.MAX_VALUE);
		if(zlevel == Integer.MAX_VALUE) {
			throw new Exception("Level id " + level.getId() + " have no zlevel");
		}
		json.put("z",zlevel);
		partialJson.put("z",zlevel);
		
		//get name
		String name = level.getProperty(OsmModel.KEY_NAME);
		if(name == null) {
			name = "Level " + zlevel;
		}
		json.put("nm",name);
		partialJson.put("nm",name);
		
		//add the features
		json.put("type","FeatureCollection");
		JSONArray featureArray = new JSONArray();
		json.put("features",featureArray);
		
		FilterRule filter = new LevelFilterRule(level);
		int filterValue;
		for(OsmObject feature:mapDataManager.getFeatureList()) {
			
			//this is a cludgy work aroudn to get the filter value for the level
			filterValue = filter.getFilterValue(feature,filter.getInitialState());
				if((filterValue & FilterRule.RENDER_ENABLED) != 0) {
				JSONObject featureJson = getFeatureJson(feature,OPENLAYERS_NAT_FRAME);
				if(featureJson != null) {
					featureArray.put(featureJson);
				}
			}
		}
		
		//save jsons
		levelJsons.add(json);
		structureLevelJsons.put(partialJson);
	}	
	
	private void createFootprintFeature() throws Exception {
		footprintJson = getFeatureJson(structure,LAT_LON_FRAME);
	}
	
	private void completeJsons() throws Exception {
		//save the name spaces
		JSONArray namespaceJson = new JSONArray();
		structureJson.put("ns",namespaceJson);
		for(String namespace:namespaces) {
			namespaceJson.put(namespace);
		}
	}
	
	
	/** This method creates a geojson feature for an OSM Object. The transform dictates
	 * the coordinates of the json. A unit transform will create merc coordinates.	 */
	private JSONObject getFeatureJson(OsmObject feature, int frame) throws Exception {
		
		//don't include nodes with no properties
		if((feature instanceof OsmNode)&&(!feature.hasProperties())) {
			return null;
		}
		
		JSONObject json = new JSONObject();
		
		json.put("id",feature.getId());
		json.put("type","Feature");
		
		//add a style property, if this is not a generic object
		//we want a single serialized name that includes all the properties
		//that identify the object. The feature info will do this.
		//But we will pull the leaeding key out as the propery key
		FeatureInfo featureInfo = MapDataManager.getObjectFeatureInfo(feature);
		String name = featureInfo.getName();
		int index = name.indexOf(KEY_VALUE_DELIMITER);
		JSONObject props = new JSONObject();
		if(index > 0) {
			String key = name.substring(0, index);
			String value = name.substring(index+1);
			props.put(key,value);
		}
		
		//add a name property, if there is one
		name = feature.getProperty("name");
		if(name != null) {
			props.put("name",name);
		}
		String ref = feature.getProperty("ref");
		if(ref != null) {
			props.put("ref",ref);
		}
		if(props.length() > 0) {
			json.put("properties",props);
		}
		
		//set the geometry
		JSONObject geomJson = new JSONObject();
		JSONArray coordinates = null;
		json.put("geometry",geomJson);
		if(feature instanceof OsmNode) {
			geomJson.put("type","Point");
			coordinates = getJsonPoint((OsmNode)feature,frame);
		}
		else if(feature instanceof OsmWay) {
			OsmWay way = (OsmWay)feature;
			if(way.getIsArea()) {
				geomJson.put("type","Polygon");
				coordinates = getJsonPointArrayList(way,frame);
			}
			else {
				geomJson.put("type","LineString");
				coordinates = getJsonPointArray(way,frame);
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
	JSONArray getJsonPoint(OsmNode node, int frame) throws Exception {
		Point2D point = new Point2D.Double();
		int precision;
		if(frame == OPENLAYERS_NAT_FRAME) {
			mercToNat.transform(node.getPoint(), point);
			precision = OPENLAYERS_NAT_PRECISION;
		}
		else if(frame == LAT_LON_FRAME) {
			double lon = Math.toDegrees(MercatorCoordinates.mxToLonRad(node.getPoint().getX()));
			double lat = Math.toDegrees(MercatorCoordinates.myToLatRad(node.getPoint().getY()));
			point.setLocation(lon,lat);
			precision = LAT_LON_PRECISION;
		}
		else {
			throw new Exception("Unsupported output format");
		}
		JSONArray pointJson = new JSONArray();
		pointJson.put(new FormattedDecimal(point.getX(),precision));
		pointJson.put(new FormattedDecimal(point.getY(),precision));
		return pointJson;
	}
	
	/** This returns an json array of points corresponding to the passed node list. */
	JSONArray getJsonPointArray(OsmWay way, int frame) throws Exception {
		
		JSONArray pointJsonArray = new JSONArray();
		for(OsmNode node:way.getNodes()) {
			JSONArray point = getJsonPoint(node,frame);
			if(point != null) {
				pointJsonArray.put(point);
			}
		}
		return pointJsonArray;
	}
	
	/** This returns a list of point lists, corresponding to the main nodes
	 * along with any inside rings. This is used for a polygon. */
	JSONArray getJsonPointArrayList(OsmWay way /*, List<OsmWay> holes */, int frame) throws Exception {		
		JSONArray pointJsonArrayList = new JSONArray();
		JSONArray pointJsonArray = getJsonPointArray(way,frame);
		pointJsonArrayList.put(pointJsonArray);
		
//this is for a polygon with holes
//		for(OsmWay hole:holes) {
//			 pointJsonArray = getJsonPointArray(hole.nodes);
//			 pointJsonArrayList.put(pointJsonArray);
//		}
		return pointJsonArrayList;
	}
	
	

}
