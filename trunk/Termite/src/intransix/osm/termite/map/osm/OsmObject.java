package intransix.osm.termite.map.osm;

import java.util.HashMap;
import intransix.osm.termite.map.MapObject;
import java.util.ArrayList;

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
public class OsmObject extends MapObject {
			
	//common
	private String id;
	
	private String osmId;
	private String user;
	private String uid;
	private boolean visible;
	private String version;
	private String changeset;
	private String timestamp;
	
	private ArrayList<OsmRelation> parentRelation = new ArrayList<OsmRelation>();
	
	/** The argument is the combined type + osmId string. */
	OsmObject(String id) {
		this.id = id;
	}
	
	public void startElement(String name, Attributes attr, OsmXml root) {
		//parse a key/value pair
		if(name.equalsIgnoreCase("tag")) {
			String key = attr.getValue("k");
			String value = attr.getValue("v");
			this.setProperty(key, value);
		}
	}
	
	/** this method should be called by objects extending osm object so the
	 * base values can be parsed. */
	public void parseElementBase(String name, Attributes attr) {
		osmId = attr.getValue("id");
		user = attr.getValue("user");
		uid = attr.getValue("uid");
		visible = getBoolean(attr,"visible",true);
		version = attr.getValue("version");
		changeset = attr.getValue("changeset");
		timestamp = attr.getValue("timestamp");
	}
	
	//helper methods
	
	protected boolean getBoolean(Attributes attr, String key, boolean defaultValue) {
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
	
	protected double getDouble(Attributes attr, String key, double defaultValue) {
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
	
	
	void addParentRelation(OsmRelation relation) {
		this.parentRelation.add(relation);
	}

}
