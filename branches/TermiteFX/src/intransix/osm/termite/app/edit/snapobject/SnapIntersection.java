package intransix.osm.termite.app.edit.snapobject;

import intransix.osm.termite.render.edit.Style;
import intransix.osm.termite.render.edit.StyleInfo;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

/**
 *
 * @author sutter
 */
public class SnapIntersection extends SnapObject {
	
//debug code
public String toString() {
	return "intersection: " + s1.snapType.name() + " + " + s2.snapType.name();
}	

	public SnapSegment s1;
	public SnapSegment s2;
	
	/** Constructor */
	public SnapIntersection() {
		super(SnapType.INTERSECTION);
	}
	
//	/** This method renders the object.
//	 * 
//	 * @param g2				The graphics context
//	 * @param mercatorToPixels	The transform from mercator coordinates to pixels
//	 * @param styleInfo			The style info for rendering
//	 */
//	@Override
//	public void render(Graphics2D g2, AffineTransform mercatorToPixels, 
//			StyleInfo styleInfo) {
//		
//		Style style;
//		
//		style = s1.getHoverStyle(styleInfo);
//		renderSegment(g2,mercatorToPixels,s1.p1,s1.p2,style);
//
//		style = s2.getHoverStyle(styleInfo);
//		renderSegment(g2,mercatorToPixels,s2.p1,s2.p2,style);
//	}
	
	/** This method looks up an select object for this snap object.  . There is no
	 * select object for an intersection.
	 * 
	 * @return			The edit object
	 */
	@Override
	public Object getSelectObject() {
		//no select object for intersection
		return null;
	}
	
	/** This value is used by compareTo to differentiate matching object types. */
	@Override
	protected double compareTiebreaker() {
		//for intersections, find the smaller sum or segment types
		return s1.snapType.getOrder() + s2.snapType.getOrder();
	}
}
