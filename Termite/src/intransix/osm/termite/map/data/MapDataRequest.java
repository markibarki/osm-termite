package intransix.osm.termite.map.data;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import intransix.osm.termite.net.RequestSource;

/**
 * This class manages an OSM map data request.
 * 
 * @author sutter
 */
public class MapDataRequest extends RequestSource {
	
	//==========================
	// Properties
	//==========================
	
	private OsmData osmData;
	private String url;
	
	
	OsmSrcData activeObject = null;
	String activeObjectName = null;
	
	//==========================
	// Properties
	//==========================
	
	/** Constructor. */
	public MapDataRequest(double minLat, double minLon, double maxLat, double maxLon) {
		osmData = new OsmData();
		
		String path = String.format(OsmModel.DATA_REQUEST_PATH,minLat,minLon,maxLat,maxLon);
		setUrl(OsmModel.OSM_SERVER + path);
	}
	
	/** This method returns the loaded OsmData */
	public OsmData getOsmData() {
		return osmData;
	}
	
	/** This method should be called after parsing is complete. */
	@Override
	public void parsingCompleted() {
		osmData.dataChanged(OsmData.INITIAL_DATA_VERSION);
	}
	
	/** This is the SAX parser start element method. */
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		
		if(activeObject != null) {
			activeObject.startElement(name,attributes,osmData);
		}
		else if(name.equalsIgnoreCase("osm")) {
			String version = attributes.getValue("version");
			osmData.setVersion(version);
			String generator = attributes.getValue("generator");
			osmData.setGenerator(generator);
		}
		else {
			long osmId = getLong(attributes,"id",OsmData.INVALID_ID);
			if(osmId != OsmData.INVALID_ID) {
				//lookup or create object
				activeObject = osmData.createOsmSrcObject(osmId,name);
				if(activeObject != null) {
					//we are processing a new object
					activeObjectName = name;
					activeObject.startElement(name,attributes,osmData);
				}
			}
		}
	}

	/** This is the SAX parser end element method. */
	@Override
	public void endElement(String uri, String localName,
			String name) throws SAXException {

		if(activeObjectName != null) {
			if(activeObjectName.equalsIgnoreCase(name)) {
				if(activeObject != null) {
					activeObject.endElement(osmData);
				}
				activeObject = null;
				activeObjectName = null;
			}
		}
	}

	/** This is the SAX parser characters method. */
	public void characters(char ch[], int start, int length) throws SAXException {
	}
	
		
	//==========================
	// Protected Methods
	//==========================
	
	/** This method reads a boolean value from the attributes. */ 
	protected static boolean getBoolean(Attributes attr, String key, boolean defaultValue) {
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
	protected static long getLong(Attributes attr, String key, long defaultValue) {
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
	protected static double getDouble(Attributes attr, String key, double defaultValue) {
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
}
