package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.Point2D;

/**
 *
 * @author sutter
 */
public class EditNode {
	public Point2D point;
	public FeatureInfo featureInfo;
	public OsmNode node;
	
	/** This creates an edit node for this OsmNode. */
	public EditNode(OsmNode node) {
		this.node = node;
		this.featureInfo = node.getFeatureInfo();
		this.point = new Point2D.Double(node.getPoint().getX(),node.getPoint().getY());
	}
	
	/** This creates an edit node with a copy of the given point and feature info
	 * and a null OsmNode. */
	public EditNode(Point2D point, FeatureInfo featureInfo) {
		this.node = null;
		this.featureInfo = featureInfo;
		this.point = new Point2D.Double(point.getX(),point.getY()); 
	}
}
