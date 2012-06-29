package intransix.osm.termite.map.data;

import intransix.osm.termite.util.MercatorCoordinates;
import org.xml.sax.Attributes;
import java.awt.geom.Point2D;

/**
 * This class holds data in an OSM node. 
 * 
 * @author sutter
 */
public class OsmNodeSrc extends OsmSrcData<OsmNode> {
	
	//======================
	// Properties
	//======================
	
	//node
	Point2D mxy = new Point2D.Double();
	
	//======================
	// Public Methods
	//======================
	
	public OsmNodeSrc() {
		super(OsmModel.TYPE_NODE,OsmData.INVALID_ID);
	}
	
	/** This method gets the X coordinate of the node, in local mercator units. */
	public Point2D getPoint() {
		return mxy;
	}
	
	public void setPosition(double x, double y) {
		mxy.setLocation(x, y);
	}
	
	//------------------------------
	// Parse Methods
	//-----------------------------
	
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
			double lat = OsmParser.getDouble(attr,"lat",OsmNode.INVALID_ANGLE);
			double lon = OsmParser.getDouble(attr,"lon",OsmNode.INVALID_ANGLE);
			double mx = MercatorCoordinates.lonRadToMx(Math.toRadians(lon));
			double my = MercatorCoordinates.latRadToMy(Math.toRadians(lat));
			mxy.setLocation(mx, my);
		}
	}
	
	//===================
	// Package Methods
	//===================
	
	/** Constructor. */
	OsmNodeSrc(long id) {
		super(OsmModel.TYPE_NODE,id);
	}
	
	/** Copies the source data to this instance. */
	@Override
	void copyInto(OsmNode target, OsmData osmData) {
		super.copyInto(target,osmData);
		
		target.setPosition(mxy.getX(), mxy.getY());
	}
	
	/** This method copies the src data to this object. */
	@Override
	void copyFrom(OsmNode src) {
		super.copyFrom(src);
		
		Point2D point = src.getPoint();
		this.mxy = new Point2D.Double(point.getX(),point.getY());
	}
	
}
