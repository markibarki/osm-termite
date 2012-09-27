
package intransix.osm.termite.app.geocode;

import java.awt.Color;
import java.awt.geom.*;
import java.awt.Graphics2D;

/**
 *
 * @author sutter
 */
public class AnchorPoint {
	
	public enum PointType {
		TRANSLATE(Color.BLUE),
		ROTATE_SCALE_XY(Color.PINK),
		ROTATE_SCALE_X(Color.PINK),
		ROTATE_SCALE_Y(Color.ORANGE),
		FREE_TRANSFORM(Color.BLUE);
		
		PointType(Color color) {
			this.color = color;
		}
		
		public Color getColor() {
			return color;
		}
		
		private Color color;
		
	}
	
	public final static int RADIUS_PIX = 10;
	private final static Color SELECT_COLOR = Color.RED;
	
	public PointType pointType;
	
	public Point2D mercPoint;
	public Point2D imagePoint;
	
	private Point2D pixPoint = new Point2D.Double();
	
	public void renderPoint(Graphics2D g2,AffineTransform mercToPixels, 
			boolean isSelected, boolean inMove, AffineTransform moveImageToMerc) {
		
		if(pointType == null) return;
		
		Color color = isSelected ? SELECT_COLOR : pointType.getColor();
		g2.setColor(color);
		
		if(inMove) {
			//if in move, transform from image to merc using the move transform
			//then transform to pixels
			moveImageToMerc.transform(imagePoint,pixPoint);
			mercToPixels.transform(pixPoint,pixPoint);
		}
		else {
			//not in move, transform straight from merc to pixels
			mercToPixels.transform(mercPoint,pixPoint);
		}
		
		int x = (int)pixPoint.getX();
		int y = (int)pixPoint.getY();
		g2.drawOval(x-RADIUS_PIX, y-RADIUS_PIX, 2*RADIUS_PIX, 2*RADIUS_PIX);
		g2.drawLine(x-RADIUS_PIX,y,x+RADIUS_PIX,y);
		g2.drawLine(x,y-RADIUS_PIX,x,y+RADIUS_PIX);
	}
	
	public boolean hitCheck(Point2D testMerc, double mercPerPixelsScale) {
		if(mercPoint != null) {
			return (mercPoint.distance(testMerc) < RADIUS_PIX * mercPerPixelsScale);
		}
		else {
			return false;
		}
	}
	
	public void reset() {
		this.pointType = null;
		this.mercPoint = null;
		this.imagePoint = null;
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
