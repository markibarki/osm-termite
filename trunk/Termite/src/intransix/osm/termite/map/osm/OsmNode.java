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
	private ArrayList<OsmWay> parentWays = new ArrayList<OsmWay>();
	private ArrayList<OsmRelation> parentRelation = new ArrayList<OsmRelation>();
	
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
			lon = this.getDouble(attr,"lon",INVALID_ANGLE);
		}
	}
	
	
	
	void addParentWay(OsmWay way) {
		this.parentWays.add(way);
	}
	
}
