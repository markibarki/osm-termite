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
	
	public final static long INVALID_ID = 0;
	
	public final static String TYPE_NODE = "node";
	public final static String TYPE_WAY = "way";
	public final static String TYPE_RELATION = "relation";
			
	//common
	private long id;
	private String type;
	private boolean isLoaded = false;
	
	private String user;
	private String uid;
	private boolean visible;
	private String version;
	private String changeset;
	private String timestamp;
	
	/** The argument is the combined type + osmId string. */
	OsmObject(String type, long id) {
		this.type = type;
		this.id = id;
	}
	
	public boolean getIsLoaded() {
		return isLoaded;
	}
	
	public void setIsLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}
	
	public long getId() {
		return id;
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
		user = attr.getValue("user");
		uid = attr.getValue("uid");
		visible = OsmXml.getBoolean(attr,"visible",true);
		version = attr.getValue("version");
		changeset = attr.getValue("changeset");
		timestamp = attr.getValue("timestamp");
	}

}
