package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.model.TermiteNode;
import intransix.osm.termite.util.MercatorCoordinates;
import org.xml.sax.Attributes;
import java.util.ArrayList;

/**
 *
 * @author sutter
 */
public class OsmNode extends OsmObject {
	
	public final static double INVALID_ANGLE = 720;
	
	//node
	private double x;
	private double y;
	private ArrayList<OsmWay> ways = new ArrayList<OsmWay>();
	
	private TermiteNode termiteNode = null;
	
	/** The argument is the combined type + osmId string. */
	public OsmNode(long id) {
		super(OsmModel.TYPE_NODE,id);
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public ArrayList<OsmWay> getWays() {
		return ways;
	}
	
	public void setTermiteNode(TermiteNode termiteNode) {
		this.termiteNode = termiteNode;
	}
	
	public TermiteNode getTermiteNode() {
		return termiteNode;
	}
	
	@Override
	public void startElement(String name, Attributes attr, OsmData osmData) {
		//let the parent parse
		super.startElement(name,attr,osmData);
		
		//parse this node
		if(name.equalsIgnoreCase("node")) {
			//parse common stuff
			parseElementBase(name, attr);
			//node specific
			double lat = OsmXml.getDouble(attr,"lat",INVALID_ANGLE);
			double lon = OsmXml.getDouble(attr,"lon",INVALID_ANGLE);
			x = MercatorCoordinates.lonRadToMx(Math.toRadians(lon)) - OsmModel.mxOffset;
			y = MercatorCoordinates.latRadToMy(Math.toRadians(lat)) - OsmModel.myOffset;
		}
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method makes a copy of this data object in the destination OsmData object. */
	@Override
	void createCopy(OsmData destOsmData) {
		OsmNode newNode = destOsmData.getOsmNode(this.getId());
		newNode.x = this.x;
		newNode.y = this.y;
		copyFromBase(newNode);
	}
	
	void addWay(OsmWay way) {
		this.ways.add(way);
	}
	
	void removeWay(OsmWay way) {
		this.ways.remove(way);
	}
	
}
