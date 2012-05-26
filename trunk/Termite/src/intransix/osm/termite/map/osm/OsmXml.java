package intransix.osm.termite.map.osm;

import java.util.HashMap;
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
	
	HashMap<String,OsmObject> objectMap = new HashMap<String,OsmObject>();
	
	//osm (root)
	String version;
	String generator;
	
	OsmObject activeObject = null;
	String activeObjectName = null;
	
	OsmObject getOsmObject(String osmId, String type) {
		String id = type + osmId;
		OsmObject obj = objectMap.get(id);
		if(obj == null) {
			//create a new object and add to map
			if(type.equalsIgnoreCase("node")) {
				obj = new OsmNode(id);
			}
			else if(type.equalsIgnoreCase("way")) {
				obj = new OsmWay(id);
			}
			else if(type.equalsIgnoreCase("relation")) {
				obj = new OsmRelation(id);
			}
			else {
				//unknown object
				return null;
			}
			
			objectMap.put(id,obj);
		}
		return obj;
	}

	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		
		if(activeObject != null) {
			activeObject.startElement(name,attributes,this);
		}
		else if(name.equalsIgnoreCase("osm")) {
			version = attributes.getValue("version");
			generator = attributes.getValue("generator");
		}
		else {
			String osmId = attributes.getValue("id");
			activeObject = getOsmObject(osmId,name);
			if(activeObject != null) {
				//we are processing a new object
				activeObjectName = name;
				activeObject.startElement(name,attributes,this);
			}
		}
	}

	public void endElement(String uri, String localName,
			String name) throws SAXException {

		if(activeObjectName != null) {
			if(activeObjectName.equalsIgnoreCase(name)) {
				activeObject = null;
				activeObjectName = null;
			}
		}
	}

	public void characters(char ch[], int start, int length) throws SAXException {
	}
}
