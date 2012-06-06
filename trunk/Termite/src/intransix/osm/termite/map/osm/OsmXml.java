package intransix.osm.termite.map.osm;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author sutter
 */
public class OsmXml extends DefaultHandler {
	
	OsmData osmData;
	
	//osm (root)
	String version;
	String generator;
	
	OsmObject activeObject = null;
	String activeObjectName = null;
	
	public OsmData parse(String fileName) {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			osmData = new OsmData();
			
			saxParser.parse(fileName, this);

			return osmData;

		} catch (Exception e) {
			e.printStackTrace();
			osmData = null;
			return null;
		}
		

	}	
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		
		if(activeObject != null) {
			activeObject.startElement(name,attributes,osmData);
		}
		else if(name.equalsIgnoreCase("osm")) {
			version = attributes.getValue("version");
			generator = attributes.getValue("generator");
		}
		else {
			long osmId = getLong(attributes,"id",OsmObject.INVALID_ID);
			if(osmId != OsmObject.INVALID_ID) {
				//lookup or create object
				activeObject = osmData.getOsmObject(osmId,name);
				if(activeObject != null) {
					//we are processing a new object
					activeObjectName = name;
					activeObject.startElement(name,attributes,osmData);
				}
			}
		}
	}

	@Override
	public void endElement(String uri, String localName,
			String name) throws SAXException {

		if(activeObjectName != null) {
			if(activeObjectName.equalsIgnoreCase(name)) {
				//finish parsing
				activeObject.endElement(name, this);
				activeObject.setLocalVersion(OsmObject.INITIAL_LOCAL_VERSION);
				
				activeObject = null;
				activeObjectName = null;
			}
		}
	}

	public void characters(char ch[], int start, int length) throws SAXException {
	}
	
		//helper methods
	
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
