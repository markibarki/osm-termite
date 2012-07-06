package intransix.osm.termite.map.data.edit;

import java.awt.geom.Point2D;
import intransix.osm.termite.map.data.*;


/**
 * This class encapsulates the destination location for an edit event, including
 * a node creation, node addition to a way and a drag operation. 
 * 
 * @author sutter
 */
public class EditDestPoint {
	
	/** This is the location for the mouse event. */
	public Point2D point;
	
	/** In the case of snapping to a node, this is the node that should be snapped
	 * to. In this case, snapNode2 should be null. In the case of snapping to a 
	 * line segment, this should be one of the two nodes on the segment. */
	public OsmNode snapNode;
	
	/** This value is used of a point is snapped to a line segment. In this case, 
	 * snapNode2 should be the opposite end node from snapNode. If this is not the
	 * case of snapping to a line segment, this should be null.
	 */
	public OsmNode snapNode2;
}
