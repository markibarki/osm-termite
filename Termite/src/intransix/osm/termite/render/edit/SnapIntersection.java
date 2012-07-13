package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmObject;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author sutter
 */
public class SnapIntersection extends SnapObject {
	
	//this is the limit for ignoring pairs of lines for intersecting
	private final static double ALMOST_PARALLEL_SIN_THETA = .1; //5.7 degrees

	public SnapSegment s1;
	public SnapSegment s2;
	
	/** Constructor */
	public SnapIntersection() {
		super(SnapType.INTERSECTION);
	}
	
	/** This method calculates the intersection of the two segments and checks if
	 * the intersection in in range. 
	 * 
	 * @param ss1			one segment
	 * @param ss2			the other segment
	 * @param mouseMerc		the mouse location,in mercator coordinates 
	 * @param mercRadSq		the error radius for a snap, in mercator coordinates
	 * @return 
	 */
	public static void loadIntersectiona(List<SnapSegment> snapSegments, 
			Point2D mouseMer, double mercRadSq, List<SnapObject> snapObjects) {
		
		int cnt = snapSegments.size();
		for(int i = 0; i < cnt; i++) {
			final SnapSegment ss1 = snapSegments.get(i);
			for(int j = i+1; j < cnt; j++) {
				final SnapSegment ss2 = snapSegments.get(j);
				
				double xs1 = ss1.p1.getX();
				double ys1 = ss1.p1.getY();
				double dx1 = ss1.p2.getX() - xs1;
				double dy1 = ss1.p2.getY() - ys1;
				double xs2 = ss2.p1.getX();
				double ys2 = ss2.p1.getY();
				double dx2 = ss2.p2.getX() - xs2;
				double dy2 = ss2.p2.getY() - ys2;

				double den = dx1*dy2 - dy1*dx2;
				double len1 = Math.sqrt(dx1*dx1 + dy1*dy1);
				double len2 = Math.sqrt(dx2*dx2 + dy2*dy2);

				//make sure lines are not cloe to being parallel
				if( Math.abs(den / (len1 * len2)) < ALMOST_PARALLEL_SIN_THETA) continue;

				//find intersection
				double num = -(xs1*dy2 - ys1*dx2) + (xs2*dy2 - ys2*dx2);
				double alpha = num / den;

				double xSnap = xs1 + alpha * dx1;
				double ySnap = ys1 + alpha * dy1;

				double err2 = mouseMer.distanceSq(xSnap,ySnap);

				if(err2 < mercRadSq) {
					//not in range
					SnapIntersection si = new SnapIntersection();
					si.snapPoint = new Point2D.Double(xSnap,ySnap);
					si.s1 = ss1;
					si.s2 = ss2;
					si.err2 = err2;
					
					snapObjects.add(si);
				}		
			}
		}
	}
	
	/** This method renders the object.
	 * 
	 * @param g2				The graphics context
	 * @param mercatorToPixels	The transform from mercator coordinates to pixels
	 * @param styleInfo			The style info for rendering
	 */
	@Override
	public void render(Graphics2D g2, AffineTransform mercatorToPixels, 
			StyleInfo styleInfo) {
		
		g2.setColor(styleInfo.HOVER_PRESELECT_COLOR);

		if(s1.snapType == SnapObject.SnapType.SEGMENT_INT) {
			g2.setStroke(styleInfo.HOVER_PRESELECT_STROKE);
		}
		else {
			g2.setStroke(styleInfo.HOVER_EXTENSION_STROKE);
		}
		renderSegment(g2,mercatorToPixels,s1.p1,s1.p2);

		if(s2.snapType == SnapObject.SnapType.SEGMENT_INT) {
			g2.setStroke(styleInfo.HOVER_PRESELECT_STROKE);
		}
		else {
			g2.setStroke(styleInfo.HOVER_EXTENSION_STROKE);
		}
		renderSegment(g2,mercatorToPixels,s2.p1,s2.p2);
	}
	
	/** This method looks up an edit object for this snap object. There is no
	 * select object for an intersection.
	 * 
	 * @return			The edit object
	 */
	@Override
	public EditObject getSelectEditObject() {
		return null;
	}
}
