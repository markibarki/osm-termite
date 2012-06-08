package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.model.TermiteNode;
import intransix.osm.termite.util.MercatorCoordinates;
import intransix.osm.termite.util.LocalCoordinates;
import org.xml.sax.Attributes;
import java.util.ArrayList;

/**
 * This class holds data in an OSM node. 
 * 
 * @author sutter
 */
public class OsmNode extends OsmObject {
	
	//======================
	// Properties
	//======================
	
	public final static double INVALID_ANGLE = 720;
	
	//node
	private double x;
	private double y;
	private ArrayList<OsmWay> ways = new ArrayList<OsmWay>();
	
	private TermiteNode termiteNode = null;
	
	//======================
	// Public Methods
	//======================
	
	/** Constructor. */
	public OsmNode(long id) {
		super(OsmModel.TYPE_NODE,id);
	}
	
	/** This method gets the X coordinate of the node, in local mercator units. */
	public double getX() {
		return x;
	}
	
	/** This method gets the Y coordinate of the node, in local mercator units. */
	public double getY() {
		return y;
	}
	
	/** This method gets the ways which include this node. */
	public ArrayList<OsmWay> getWays() {
		return ways;
	}
	
	/** This method sets the termite node for this OsmN0de. */
	public void setTermiteNode(TermiteNode termiteNode) {
		this.termiteNode = termiteNode;
	}
	
	/** This method gets the TermiteNode for this OsmNode. */
	public TermiteNode getTermiteNode() {
		return termiteNode;
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
	
	//========================
	// Package Methods
	//========================
	
	/** This method makes a copy of this data object in the destination OsmData object. */
	@Override
	void createCopy(OsmData destOsmData) {
		OsmNode newNode = destOsmData.createOsmNode(this.getId());
		newNode.x = this.x;
		newNode.y = this.y;
		copyFromBase(newNode);
	}
	
	/** This method adds a way to the node. */
	void addWay(OsmWay way) {
		this.ways.add(way);
	}
	
	/** This method removes a way from the node. */
	void removeWay(OsmWay way) {
		this.ways.remove(way);
	}
	
	void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
}
