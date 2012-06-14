package intransix.osm.termite.map.osm;

import intransix.osm.termite.util.MercatorCoordinates;
import intransix.osm.termite.util.LocalCoordinates;
import org.xml.sax.Attributes;

/**
 * This class holds data in an OSM node. 
 * 
 * @author sutter
 */
public class OsmNode extends OsmObject<OsmNode> {
	
	//======================
	// Properties
	//======================
	
	public final static double INVALID_ANGLE = 720;
	
	//node
	private double x;
	private double y;
	
	//======================
	// Public Methods
	//======================
	
	/** Constructor. */
	public OsmNode(long id) {
		super(OsmModel.TYPE_NODE,id);
	}
	
	public OsmNode() {
		super(OsmModel.TYPE_NODE,OsmObject.INVALID_ID);
	}
	
	/** This method gets the X coordinate of the node, in local mercator units. */
	public double getX() {
		return x;
	}
	
	/** This method gets the Y coordinate of the node, in local mercator units. */
	public double getY() {
		return y;
	}
	
	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
		incrementLocalVersion();
	}
	
	/** This method is used to parse the OsmNode. */
	@Override
	public void startElement(String name, Attributes attr, OsmData osmData) {
		//let the parent parse
		super.startElement(name,attr,osmData);
		
		//parse this node
		if(name.equalsIgnoreCase("node")) {
			//parse common stuff
			parseElementBase(name, attr);
			//get local coordinates in meters
			double lat = OsmParser.getDouble(attr,"lat",INVALID_ANGLE);
			double lon = OsmParser.getDouble(attr,"lon",INVALID_ANGLE);
			double mx = MercatorCoordinates.lonRadToMx(Math.toRadians(lon));
			double my = MercatorCoordinates.latRadToMy(Math.toRadians(lat));
			x = LocalCoordinates.mercToLocalX(mx);
			y = LocalCoordinates.mercToLocalY(my);
		}
	}
	
	/** This method makes a copy of this data object in the destination OsmData object. */
	@Override
	public void copyInto(OsmNode newNode) {
		newNode.x = this.x;
		newNode.y = this.y;
		super.copyInto(newNode);
	}
}
