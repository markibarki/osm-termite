package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.MapObject;
import org.xml.sax.Attributes;

/**
 *
 * @author sutter
 */
public class OsmNode extends OsmObject {
	
	public final static double INVALID_ANGLE = 720;
	
	//node
	double lat;
	double lon;
	
	/** The argument is the combined type + osmId string. */
	public OsmNode(String id) {
		super(id);
	}
	
	@Override
	public void startElement(String name, Attributes attr, OsmXml root) {
		//let the parent parse
		super.startElement(name,attr,root);
		
		//parse this node
		if(name.equalsIgnoreCase("node")) {
			lat = this.getDouble(attr,"lat",INVALID_ANGLE);
			lon = this.getDouble(attr,"lat",INVALID_ANGLE);
		}
	}
	
}
