package intransix.osm.termite.map.geom;

import java.awt.Graphics2D;
import java.awt.geom.*;

/**
 * This represents a point feature.
 * 
 * @author sutter
 */
public class PointFeature extends Feature {
	
	private Point2D point;
	
////used for rendering for now
//private Rectangle2D rect = new Rectangle2D.Double();
	
	public PointFeature() {
		super(FeatureType.POINT);
	}
	
	/** This gets the point object for this feature. */
	public Point2D getPoint(Point2D point) {
		return point;
	}
	
	/** This sets the value of the point. */
	public void setPoint(Point2D point) {
		this.point = point;
	}
	
	public void render(Graphics2D g) {
		//implement this!!!
	}
}
