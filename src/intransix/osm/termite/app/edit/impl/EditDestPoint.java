package intransix.osm.termite.app.edit.impl;

import intransix.osm.termite.map.workingdata.OsmNode;
import java.awt.geom.Point2D;


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
	 * to. */
	public OsmNode snapNode;
	
}
