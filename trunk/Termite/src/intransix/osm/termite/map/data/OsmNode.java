package intransix.osm.termite.map.data;

import java.util.ArrayList;
import java.util.List;
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
	private Point2D mxy = new Point2D.Double();
	
	private List<OsmWay> ways = new ArrayList<OsmWay>();
	
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
	
	//====================
	// Package Methods
	//====================
	
	/** Constructor. */
	OsmNode(long id) {
		super(OsmModel.TYPE_NODE,id);
	}
	
	/** Constructor. */
	OsmNode() {
		super(OsmModel.TYPE_NODE,OsmData.INVALID_ID);
	}
	
	/** This method sets the position. 
	 * 
	 * @param x		The x value in mercator coordinates
	 * @param y		The y value in mercator coordinates
	 */
	void setPosition(double x, double y) {
		mxy.setLocation(x, y);
	}
	
	/** This method adds a way to the node if the way is not already there. */
	void addWay(OsmWay way) {
		if(!ways.contains(way)) {
			ways.add(way);
		}
	}
	
	/** This method removes the way from the node. */
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
	void objectCreated(OsmData osmData) {
		featureCreatedProcessing(osmData);
	}
	
	@Override
	void propertiesUpdated(OsmData osmData) {
		featurePropertiesUpdatedProcessing(osmData);
	}
	
	@Override
	void objectUpdated(OsmData osmData) {
		featureUpdated(osmData);
	}
	
	/** This method should be called when the object is deleted. */
	@Override
	void objectDeleted(OsmData osmData) {
		super.objectDeleted(osmData);
		
		//ways should be empty
		//we must check for this earlier so this exception is not thrown
		if(!ways.isEmpty()) throw new RuntimeException("Unkown program error. A way "
				+ "referenced the deleted node. This"
				+ "should be checked elsewhere before we get here.");
		
		featureDeletedProcessing(osmData);
	}
	
	/** This method updates the version number for all relations containing this object. */
	@Override
	void setContainingObjectDataVersion(OsmData osmData, int version) {
		super.setContainingObjectDataVersion(osmData,version);
		for(OsmWay way:ways) {
			way.setDataVersion(osmData,version);
		}
	}
}
