package intransix.osm.termite.map.osm;

import java.util.*;
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
	
	//==========================
	// Private Fields
	//==========================
	HashMap<Long,OsmNode> nodeMap = new HashMap<Long,OsmNode>();
	HashMap<Long,OsmWay> wayMap = new HashMap<Long,OsmWay>();
	HashMap<Long,OsmRelation> relationMap = new HashMap<Long,OsmRelation>();
	
	//osm (root)
	String version;
	String generator;
	
	OsmObject activeObject = null;
	String activeObjectName = null;
	
	public void parse(String fileName) {

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			saxParser.parse(fileName, this);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}	
	
	OsmObject getOsmObject(long id, String type) {
		if(type.equalsIgnoreCase(OsmObject.TYPE_NODE)) {
			return getOsmNode(id);
		}
		else if(type.equalsIgnoreCase(OsmObject.TYPE_WAY)) {
			return getOsmWay(id);
		}
		else if(type.equalsIgnoreCase(OsmObject.TYPE_RELATION)) {
			return getOsmRelation(id);
		}
		else {
			//unknown object
			return null;
		}
	}

	public OsmNode getOsmNode(long id) {
		OsmNode node = nodeMap.get(id);
		if(node == null) {
			node = new OsmNode(id);
			nodeMap.put(id,node);
		}
		return node;
	}
	
	public OsmWay getOsmWay(long id) {
		OsmWay way = wayMap.get(id);
		if(way == null) {
			way = new OsmWay(id);
			wayMap.put(id,way);
		}
		return way;
	}
	
	public OsmRelation getOsmRelation(long id) {
		OsmRelation relation = relationMap.get(id);
		if(relation == null) {
			relation = new OsmRelation(id);
			relationMap.put(id,relation);
		}
		return relation;
	}
	
	public Collection<OsmNode> getOsmNodes() {
		return nodeMap.values();
	}
	
	public Collection<OsmWay> getOsmWays() {
		return wayMap.values();
	}
	
	public Collection<OsmRelation> getOsmRelations() {
		return relationMap.values();
	}

	@Override
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
			long osmId = getLong(attributes,"id",OsmObject.INVALID_ID);
			if(osmId != OsmObject.INVALID_ID) {
				//lookup or create object
				activeObject = getOsmObject(osmId,name);
				if(activeObject != null) {
					//we are processing a new object
					activeObjectName = name;
					activeObject.startElement(name,attributes,this);
				}
			}
		}
	}

	@Override
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
