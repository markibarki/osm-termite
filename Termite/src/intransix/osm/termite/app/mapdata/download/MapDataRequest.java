package intransix.osm.termite.app.mapdata.download;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.map.dataset.*;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.net.NetRequest;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import intransix.osm.termite.net.RequestSource;
import intransix.osm.termite.util.MercatorCoordinates;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;
import java.util.Locale;

/**
 * This class manages an OSM map data request.
 * 
 * @author sutter
 */
public class MapDataRequest extends DefaultHandler implements RequestSource {
	
	//==========================
	// Properties
	//==========================
	
	private OsmDataSet dataSet;
	private String url;
	
	
	OsmSrcData activeObject = null;
	
	//==========================
	// Properties
	//==========================
	
	/** Constructor. */
	public MapDataRequest(double minLat, double minLon, double maxLat, double maxLon) {
		dataSet = new OsmDataSet();
		
		String path = String.format(Locale.US, OsmModel.DATA_REQUEST_PATH,minLat,minLon,maxLat,maxLon);
		url = OsmModel.OSM_SERVER + path;
	}
	
	public OsmDataSet getDataSet() {
		return dataSet;
	}
	
	/** This method should return the url. */
	@Override
	public String getUrl() {
		return url;
	}
	
	/** This method returns the HTTP request method. */
	@Override
	public String getMethod() {
		return "GET";
	}
	
	/** This method should return true if there is a payload. */
	@Override
	public boolean getHasPayload() {
		return false;
	}
	
	/** This method should be implemented to write the XMl body, if there is a payload. */
	@Override
	public void writeRequestBody(OutputStream os) throws Exception {}
	
	/** This method will be called to red the response body. */
	@Override
	public void readResponseBody(int responseCode, InputStream is) throws Exception {
		if(responseCode == 200) {
			//parse xml
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();	
			saxParser.parse(is,this);
		}
		else {
			String bodyText = NetRequest.readText(is);
			System.out.println(bodyText);
		}
	}
	
	/** This is the SAX parser start element method. */
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		
		if(name.equalsIgnoreCase("osm")) {
			//load global data
			String version = attributes.getValue("version");
			dataSet.setVersion(version);
			String generator = attributes.getValue("generator");
			dataSet.setGenerator(generator);
		}
		else if(name.equalsIgnoreCase("node")) {
			//create a node
			long osmId = getLong(attributes,"id",OsmData.INVALID_ID);
			if(osmId != OsmData.INVALID_ID) { 
				OsmNodeSrc node = new OsmNodeSrc(osmId);
				dataSet.putNodeSrc(node);
				commonElementParse(node,attributes);
				//get local coordinates in meters
				double lat = MapDataRequest.getDouble(attributes,"lat",OsmNode.INVALID_ANGLE);
				double lon = MapDataRequest.getDouble(attributes,"lon",OsmNode.INVALID_ANGLE);
				double mx = MercatorCoordinates.lonRadToMx(Math.toRadians(lon));
				double my = MercatorCoordinates.latRadToMy(Math.toRadians(lat));
				node.setPosition(mx, my);
				activeObject = node;
			}
		}
		if(name.equalsIgnoreCase("way")) {
			//create a way
			long osmId = getLong(attributes,"id",OsmData.INVALID_ID);
			if(osmId != OsmData.INVALID_ID) { 
				OsmWaySrc way = new OsmWaySrc(osmId);
				dataSet.putWaySrc(way);
				commonElementParse(way,attributes);
				activeObject = way;
			}
		}
		else if(name.equalsIgnoreCase("relation")) {
			//create a relation
			long osmId = getLong(attributes,"id",OsmData.INVALID_ID);
			if(osmId != OsmData.INVALID_ID) { 
				OsmRelationSrc relation = new OsmRelationSrc(osmId);
				dataSet.putRelationSrc(relation);
				commonElementParse(relation,attributes);
				activeObject = relation;
			}
		}
		else if(name.equalsIgnoreCase("tag")) {
			//load a property
			if(activeObject != null) {
				String key = attributes.getValue("k");
				String value = attributes.getValue("v");
				activeObject.addProperty(key, value);
			}
		}
		else if(name.equalsIgnoreCase("member")) {
			//load a member
			if(activeObject instanceof OsmRelationSrc) {
				String type = attributes.getValue("type");
				long ref = MapDataRequest.getLong(attributes,"ref",OsmData.INVALID_ID);
				String role = attributes.getValue("role");
				((OsmRelationSrc)activeObject).addMember(ref, type, role);
			}
		}
		else if(name.equalsIgnoreCase("nd")) {
			//load a node
			if(activeObject instanceof OsmWaySrc) {
				long ref = MapDataRequest.getLong(attributes,"ref",OsmData.INVALID_ID);
				((OsmWaySrc)activeObject).addNodeId(ref);
			}
		}

	}

	/** This is the SAX parser end element method. */
	@Override
	public void endElement(String uri, String localName,
			String name) throws SAXException {

		if(activeObject != null) {
			if(name.equalsIgnoreCase(activeObject.getObjectType())) {
				activeObject = null;
			}
		}
	}

	/** This is the SAX parser characters method. */
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
	}
	
	/** This method reads a boolean value from the attributes. */ 
	public static boolean getBoolean(Attributes attr, String key, boolean defaultValue) {
		String value = attr.getValue(key);
		if(value != null) {
			//try to parse the string, on failure return default
			try {
				return Boolean.parseBoolean(value);
			}
			catch(Exception ex) {
				//no action
			}
		}
		return defaultValue;
	}
	
	/** This method reads a long value from the attributes. */
	public static long getLong(Attributes attr, String key, long defaultValue) {
		String value = attr.getValue(key);
		if(value != null) {
			//try to parse the string, on failure return default
			try {
				return Long.decode(value);
			}
			catch(Exception ex) {
				//no action
			}
		}
		return defaultValue;
	}
	
	/** This method reads a double value from the attributes. */
	public static double getDouble(Attributes attr, String key, double defaultValue) {
		String value = attr.getValue(key);
		if(value != null) {
			//try to parse the string, on failure return default
			try {
				return Double.parseDouble(value);
			}
			catch(Exception ex) {
				//no action
			}
		}
		return defaultValue;
	}
	
	
	//======================
	// Private Methods
	//======================
	
	/** This does parses some attributes common to all objects. */
	private void commonElementParse(OsmSrcData osmObjectSrc, Attributes attr) {
		osmObjectSrc.setUser(attr.getValue("user"));
		osmObjectSrc.setUid(attr.getValue("uid"));
		osmObjectSrc.setVisible(MapDataRequest.getBoolean(attr,"visible",true));
		osmObjectSrc.setVersion(attr.getValue("version"));
		osmObjectSrc.setChangeset(attr.getValue("changeset"));
		osmObjectSrc.setTimestamp(attr.getValue("timestamp"));
	}
	

}
