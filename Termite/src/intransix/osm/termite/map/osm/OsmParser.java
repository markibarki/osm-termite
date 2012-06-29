package intransix.osm.termite.map.osm;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class parses an OSM request data.
 * 
 * @author sutter
 */
public class OsmParser extends DefaultHandler {
	
	//==========================
	// Properties
	//==========================
	
	OsmData osmData;
	
	OsmSrcData activeObject = null;
	String activeObjectName = null;
	
	//==========================
	// Properties
	//==========================
	
	/** This method parses an XMl data request from OSM.
	 * 
	 * @param fileName	The file to parse
	 * @return			An OsmData object holding the data in the request
	 */
	public OsmData parse(String uri) {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			osmData = new OsmData();
			
			saxParser.parse(uri, this);

			return osmData;

		} catch (Exception e) {
			e.printStackTrace();
			osmData = null;
			return null;
		}
		

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
