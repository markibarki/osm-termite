package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.data.OsmWay;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.Point2D;
import java.util.*;

/**
 *
 * @author sutter
 */
public class EditWay {
	public List<Point2D> points;
	public FeatureInfo featureInfo;
	public OsmWay way;
	
	/** This creates an edit way for this OsmWay. */
	public EditWay(OsmWay way) {
		this.way = way;
		this.featureInfo = way.getFeatureInfo();
		this.points = new ArrayList<Point2D>();
		for(OsmNode node:way.getNodes()) {
			Point2D point = new Point2D.Double(node.getPoint().getX(),node.getPoint().getY());
			points.add(point);
		}
	}
	
	/** This creates an edit way with a copy of the given point and feature info
	 * and a null OsmWay. */
	public EditWay(Point2D point, FeatureInfo featureInfo) {
		this.way = null;
		this.featureInfo = featureInfo;
		this.points = new ArrayList<Point2D>();
		if(point != null) {
			points.add(new Point2D.Double(point.getX(),point.getY())); 
		}
	}
}
