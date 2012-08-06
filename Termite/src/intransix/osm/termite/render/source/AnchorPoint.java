
package intransix.osm.termite.render.source;

import java.awt.Color;
import java.awt.geom.*;
import java.awt.Graphics2D;

/**
 *
 * @author sutter
 */
class AnchorPoint {
	
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
}
