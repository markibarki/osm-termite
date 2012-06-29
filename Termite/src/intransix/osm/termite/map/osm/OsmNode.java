package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.model.TermiteData;
import intransix.osm.termite.util.MercatorCoordinates;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import java.awt.geom.Point2D;

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
	Point2D mxy = new Point2D.Double();
	
	private List<OsmWay> ways = new ArrayList<OsmWay>();
	
	//======================
	// Public Methods
	//======================
	
	/** Constructor. */
	public OsmNode(long id) {
		super(OsmModel.TYPE_NODE,id);
	}
	
	public OsmNode() {
		super(OsmModel.TYPE_NODE,OsmData.INVALID_ID);
	}
	
	/** This method gets the X coordinate of the node, in local mercator units. */
	public Point2D getPoint() {
		return mxy;
	}
	
	public void setPosition(double x, double y) {
		mxy.setLocation(x, y);
	}
	
	public List<OsmWay> getWays() {
		return ways;
	}
	
	//====================
	// Package Methods
	//====================
	
	void addWay(OsmWay way) {
		if(!ways.contains(way)) {
			ways.add(way);
		}
	}
	
	void removeWay(OsmWay way) {
		ways.remove(way);
	}
	
	/** This method verifies an object can be deleted. There can be no external
	 * objects referring to this one.
	 * 
	 * @throws UnchangedException	Thrown if this object can not be deleted 
	 */
	@Override
	void verifyDelete() throws UnchangedException {
		super.verifyDelete();
		if(!ways.isEmpty()) {
			throw new UnchangedException("A node cannot be deleted is a way contains it.");
		}
	}
	
	@Override
	void objectDeleted(OsmData osmData) {
		super.objectDeleted(osmData);
		
		//ways should be empty
		//we must check for this earlier so this exception is not thrown
		if(!ways.isEmpty()) throw new RuntimeException("A way referenced the deleted node");
		
	}
	
	/** This method updates the version number for all relations containing this object. */
	@Override
	void setContainingObjectDataVersion(int version) {
		super.setContainingObjectDataVersion(version);
		for(OsmWay way:ways) {
			way.setDataVersion(version);
		}
	}
}
