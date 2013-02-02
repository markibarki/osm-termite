package intransix.osm.termite.map.workingdata;

import intransix.osm.termite.app.mapdata.instruction.UnchangedException;
import java.util.ArrayList;
import java.util.List;
import intransix.osm.termite.map.dataset.OsmNodeSrc;
import java.awt.geom.Point2D;

/**
 * This class holds data in an OSM node. 
 * 
 * @author sutter
 */
public class OsmNode extends OsmObject<OsmNodeSrc> {
	
	//======================
	// Properties
	//======================
	
	public final static double INVALID_ANGLE = 720;
	
	//node
	private Point2D mxy = new Point2D.Double();
	
	private List<OsmWay> ways = new ArrayList<OsmWay>();
	
	private List<OsmSegment> segments = new ArrayList<OsmSegment>();
	
	//======================
	// Public Methods
	//======================
	
	/** This method gets the X coordinate of the node, in local mercator units. */
	public Point2D getPoint() {
		return mxy;
	}
	
	/** This returns the list of ways that include this node. The list returned
	 * should NOT be edited. 
	 * 
	 * @return 
	 */
	public List<OsmWay> getWays() {
		return ways;
	}
	
	public List<OsmSegment> getSegments() {
		return segments;
	}
	
	/** This method checks if the node is a feature. It is a feature if it has
	 * any non-geometry properties, as defined by the OsmModel object. */
	public boolean isFeature() {
		for(String key:getPropertyKeys()) {
			if(!OsmModel.GEOMETRIC_KEYS.contains(key)) return true;
		}
		return false;
	}
	
	//====================
	// Package Methods
	//====================
	
	/** Constructor. */
	public OsmNode(long id) {
		super(OsmModel.TYPE_NODE,id);
	}
	
	/** Constructor. */
	public OsmNode() {
		super(OsmModel.TYPE_NODE,OsmData.INVALID_ID);
	}
	
	/** This method sets the position. 
	 * 
	 * @param x		The x value in mercator coordinates
	 * @param y		The y value in mercator coordinates
	 */
	public void setPosition(double x, double y) {
		mxy.setLocation(x, y);
	}
	
	/** This method adds a way to the node if the way is not already there. */
	public void addWay(OsmWay way) {
		if(!ways.contains(way)) {
			ways.add(way);
		}
	}
	
	/** This method removes the way from the node. */
	public void removeWay(OsmWay way) {
		ways.remove(way);
	}
	
	public void addSegment(OsmSegment segment) {
		if(!segments.contains(segment)) {
			segments.add(segment);
		}
	}
	
	public void removeSegment(OsmSegment segment) {
		segments.remove(segment);
	}
	
	/** This method verifies an object can be deleted. There can be no external
	 * objects referring to this one.
	 * 
	 * @throws UnchangedException	Thrown if this object can not be deleted 
	 */
	@Override
	public void verifyDelete() throws UnchangedException {
		super.verifyDelete();
		if(!ways.isEmpty()) {
			throw new UnchangedException("A node cannot be deleted is a way contains it.");
		}
	}
	
	/** This method should be called when the object is deleted. */
	@Override
	public void objectDeleted(OsmData osmData) {
		super.objectDeleted(osmData);
		
		//ways should be empty
		//we must check for this earlier so this exception is not thrown
		if(!ways.isEmpty()) throw new RuntimeException("Unkown program error. A way "
				+ "referenced the deleted node. This"
				+ "should be checked elsewhere before we get here.");
	}
	
	/** This method updates the version number for all relations containing this object. */
	@Override
	public void setContainingObjectDataVersion(int version) {
		super.setContainingObjectDataVersion(version);
		for(OsmWay way:ways) {
			way.setDataVersion(version);
		}
	}
	
	/** Copies the source data to this instance. */
	@Override
	public void copyInto(OsmNodeSrc target) {
		super.copyInto(target);
		target.setPosition(mxy.getX(), mxy.getY());
	}
	
	/** This method copies the src data to this object. */
	@Override
	public void copyFrom(OsmNodeSrc src, OsmData osmData) {
		super.copyFrom(src, osmData);
		
		Point2D point = src.getPoint();
		this.mxy = new Point2D.Double(point.getX(),point.getY());
	}
}
