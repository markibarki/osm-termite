package intransix.osm.termite.app.edit;

import intransix.osm.termite.map.data.OsmSegment;
import intransix.osm.termite.map.data.OsmNode;
import java.awt.geom.Point2D;

/**
 *
 * @author sutter
 */
public class VirtualNode {
	public OsmSegment segment;
	public Point2D point;
	
	public VirtualNode(OsmSegment segment) {
		this.segment = segment;
		double xCenter = (segment.getNode1().getPoint().getX() + segment.getNode2().getPoint().getX())/2;
		double yCenter = (segment.getNode1().getPoint().getY() + segment.getNode2().getPoint().getY())/2;
		this.point = new Point2D.Double(xCenter,yCenter);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if((obj == null) || (obj.getClass() != this.getClass())) return false;
		//this is a virtual node - compare segments
		return (this.segment == ((VirtualNode)obj).segment);
	}

	@Override
	public int hashCode() {
		return segment.hashCode();
	}
}
