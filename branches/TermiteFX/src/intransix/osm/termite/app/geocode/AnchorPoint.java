
package intransix.osm.termite.app.geocode;


import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * This is an Anchor point for controlling geocoding of an image.
 * @author sutter
 */
public class AnchorPoint {
	
	/** This gives the type of anchor point. */
	public enum PointType {
		TRANSLATE,
		ROTATE_SCALE_XY,
		ROTATE_SCALE_X,
		ROTATE_SCALE_Y,
		FREE_TRANSFORM;	
	}
	
	public final static int RADIUS_PIX = 10;
	
	private PointType pointType;
	public void setPointType(PointType pointType) {
		this.pointType = pointType;
	}
	public PointType getPointType() {
		return pointType;
	}
	
	public boolean isActive;
	public Point2D mercPoint;
	public Point2D imagePoint;
	public Object anchorPointGraphic;
	
	public boolean getIsActive() {
		return isActive;
	}
	
	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	/** This method checks if the test point hits the anchor point. */
	public boolean hitCheck(Point2D testMerc, double mercPerPixelsScale) {
		if(mercPoint != null) {
			return (mercPoint.distance(testMerc) < RADIUS_PIX * mercPerPixelsScale);
		}
		else {
			return false;
		}
	}
	
	/** This clears the anchor point, making it not set.*/
	public void reset() {
		this.pointType = null;
		this.mercPoint = null;
		this.imagePoint = null;
		this.isActive = false;
	}
	
	/** This method is used to for 3 point orthogonal gecodes to determine
	 * which point scales in the x direction and which scales in the y direction.
	 * 
	 * @param p1	the first scale anchor point
	 * @param p2	the second scale anchor point
	 */ 
	public static void setScalePointTypes(AnchorPoint p1, AnchorPoint p2) {
		Point2D ip1 = p1.imagePoint;
		Point2D ip2 = p2.imagePoint;
		
		if((ip1 != null)&&(ip2 != null)) {
			double tan1 = Math.abs(ip1.getY())/(Math.abs(ip1.getX()) + .001);
			double tan2 = Math.abs(ip2.getY())/(Math.abs(ip2.getX()) + .001);
			if(tan1 < tan2) {
				p1.pointType = AnchorPoint.PointType.ROTATE_SCALE_X;
				p2.pointType = AnchorPoint.PointType.ROTATE_SCALE_Y;
			}
			else {
				p1.pointType = AnchorPoint.PointType.ROTATE_SCALE_Y;
				p2.pointType = AnchorPoint.PointType.ROTATE_SCALE_X;
			}
		}
		else {
			p1.pointType = AnchorPoint.PointType.ROTATE_SCALE_XY;
			p2.pointType = AnchorPoint.PointType.ROTATE_SCALE_XY;
		}
	}
}
