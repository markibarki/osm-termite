package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmSegment;
import java.awt.geom.Point2D;

/**
 *
 * @author sutter
 */
public class EditInsertNodeSegment {
	public Point2D point;
	public OsmSegment segment;
	
	/** This creates an edit node for this OsmNode. */
	public EditInsertNodeSegment(OsmSegment segment,double xCenter,double yCenter) {
		this.point = new Point2D.Double(xCenter,yCenter);
		this.segment = segment;
	}
}
