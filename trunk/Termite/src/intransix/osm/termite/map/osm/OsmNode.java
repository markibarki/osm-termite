package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.MapObject;
import org.xml.sax.Attributes;
import java.util.ArrayList;

/**
 *
 * @author sutter
 */
public class OsmNode extends OsmObject {
	
	public final static double INVALID_ANGLE = 720;
	
	//node
	private double lat;
	private double lon;
	
	/** The argument is the combined type + osmId string. */
	public OsmNode(long id) {
		super(TYPE_NODE,id);
	}
	
	public double getLat() {
		return lat;
	}
	
	public double getLon() {
		return lon;
	}
	
	@Override
	public void startElement(String name, Attributes attr, OsmXml root) {
		//let the parent parse
		super.startElement(name,attr,root);
		
		//parse this node
		if(name.equalsIgnoreCase("node")) {
			//parse common stuff
			parseElementBase(name, attr);
			//node specific
			lat = OsmXml.getDouble(attr,"lat",INVALID_ANGLE);
			lon = OsmXml.getDouble(attr,"lon",INVALID_ANGLE);
		}
	}
	
}
