package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmSegmentWrapper;
import intransix.osm.termite.map.data.OsmWay;
import java.awt.Graphics2D;
import java.awt.geom.*;


/**
 *
 * @author sutter
 */
public abstract class EditDrawable {
	
	//========================
	// Properties
	//========================
	
	//These are used as working variables during a draw
	private static Point2D workingPoint1 = new Point2D.Double();
	private static Point2D workingPoint2 = new Point2D.Double();
	private static Line2D workingLine = new Line2D.Double();
	private static Rectangle2D workingRect = new Rectangle2D.Double();
	
	//=========================
	// Public Methods
	//=========================
	
	/** This method renders the object.
	 * 
	 * @param g2				The graphics context
	 * @param mercatorToPixels	The transform from mercator coordinates to pixels
	 * @param styleInfo			The style info for rendering
	 */
	public abstract void render(Graphics2D g2, AffineTransform mercatorToPixels, 
			StyleInfo styleInfo);
	
	//=========================
	// Protected Methods
	//=========================
	
	/** This method renders a point. */
	static void renderPoint(Graphics2D g2, AffineTransform mercatorToPixels,
			Point2D point, double radiusPix) {
		mercatorToPixels.transform(point,workingPoint1);
		workingRect.setRect(workingPoint1.getX()-radiusPix, workingPoint1.getY() - radiusPix,
				2*radiusPix, 2*radiusPix);
		g2.fill(workingRect);
	}
	
	/** This method renders a way. */
	static void renderWay(Graphics2D g2, AffineTransform mercatorToPixels,
			OsmWay way) {
		
		for(OsmSegmentWrapper osw:way.getSegments()) {
			if(osw.segment.editEnabled()) {
				renderSegment(g2,mercatorToPixels,
						osw.segment.getNode1().getPoint(),
						osw.segment.getNode2().getPoint());
			}
		}
	}
	
	/** This method renders a segment. */
	static void renderSegment(Graphics2D g2, AffineTransform mercatorToPixels,
			Point2D p1, Point2D p2) {
		
		mercatorToPixels.transform(p1,workingPoint1);
		mercatorToPixels.transform(p2,workingPoint2);
		workingLine.setLine(workingPoint1,workingPoint2);
		g2.draw(workingLine);
	}
	
}
