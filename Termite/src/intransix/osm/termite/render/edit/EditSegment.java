package intransix.osm.termite.render.edit;

import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
/**
 *
 * @author sutter
 */
public class EditSegment {
	public Point2D p1;
	public Point2D p2;
	public Line2D line;
	
	public EditSegment(Point2D p1, Point2D p2) {
		this.p1 = new Point2D.Double(p1.getX(),p1.getY());
		this.p2 = new Point2D.Double(p2.getX(),p2.getY());
		this.line = new Line2D.Double(p1,p2);
	}
	
	public void updateP1(Point2D p1) {
		p1.setLocation(p1);
		line.setLine(p1,p2);
	}
	
	public void updateP2(Point2D p2) {
		p2.setLocation(p2);
		line.setLine(p1,p2);
	}
	
	
		
}
