package intransix.osm.termite.map.osm;

import java.util.*;
import org.xml.sax.Attributes;

/**
 *
 * @author sutter
 */
public abstract class OsmSrcData<T extends OsmObject> {
	
	private long id;
	private String type;
	
	private String user;
	private String uid;
	private boolean visible;
	private String version;
	private String changeset;
	private String timestamp;
	
	private List<PropertyPair> properties = new ArrayList<PropertyPair>();
	
	public long getId() {
		return id;
	}
	
	public String getObjectType() {
		return type;
	}
	
	/** This method copies the src data to the target. */
	public void copyInto(T targetData, OsmData osmData) {
		targetData.setId(id);
		for(PropertyPair pp:properties) {
			targetData.setProperty(pp.key,pp.value);
		}
	}
	
	/** This method copies the src data to this object. */
	public void copyFrom(T srcData) {
		this.id = srcData.getId();
		this.type = srcData.getObjectType();
		properties.clear();
		for(String key:srcData.getPropertyKeys()) {
			properties.add(new PropertyPair(key,srcData.getProperty(key)));
		}
	}
	
	//-----------------------
	// Parsing Methods
	//-----------------------
	
	OsmSrcData(String type, long id) {
		this.type = type;
		this.id = id;
	}
	
	void setId(long id) {
		this.id = id;
	}
	
	/** This method is used in XMl parsing. */
	void startElement(String name, Attributes attr, OsmData osmData) {
		//parse a key/value pair
		if(name.equalsIgnoreCase("tag")) {
			String key = attr.getValue("k");
			String value = attr.getValue("v");
			properties.add(new PropertyPair(key,value));
		}
	}
	
	void endElement(OsmData osmData) {
		//create the osm object for this data
		OsmObject object = osmData.getOsmObject(id, type, true);
		this.copyInto((T)object, osmData);
		object.objectCreated(osmData);
	}
	
	/** this method should be called by objects extending osm object so the
	 * base values can be parsed. */
	void parseElementBase(String name, Attributes attr) {
		user = attr.getValue("user");
		uid = attr.getValue("uid");
		visible = OsmParser.getBoolean(attr,"visible",true);
		version = attr.getValue("version");
		changeset = attr.getValue("changeset");
		timestamp = attr.getValue("timestamp");
	}
	
	public class PropertyPair {
		
		public PropertyPair(String key, String value) {
			this.key = key;
			this.value = value;
		}
		
		public String key;
		public String value;
	}
	
}
