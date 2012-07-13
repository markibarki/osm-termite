package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.data.OsmObject;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import intransix.osm.termite.map.data.OsmSegment;
import java.util.HashMap;

/**
 *
 * @author sutter
 */
public class SnapSegment extends SnapObject {
	
	//=======================
	// Properties
	//=======================
	
	//display line start
	public Point2D p1;
	//display line end
	public Point2D p2;
	//type
	public SnapType snapType;
	
	//=======================
	// Public Methods
	//=======================
	
	/** Constructor */
	public SnapSegment() {
		//set type as unknown - se dont' know what kind of segment
		super(SnapType.UNKNOWN);
	}
	
	/** This method tests if the mouse point hits the segment. If so, it 
	 * create a SnapSegment object. 
	 * 
	 * @param segment		The osm segment being tested
	 * @param mouseMerc		The mouse location, in mercator coordinates
	 * @param mercRadSq		The radius for a hit, in mercator coordinates.
	 * @return 
	 */
	public static SnapSegment testSegmentHit(OsmSegment segment, 
			Point2D mouseMerc, double mercRadSq) {
		
		return getSnapSegment(segment.getNode1().getPoint(),
				segment.getNode2().getPoint(),
				mouseMerc,false,mercRadSq);
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
		
		if(snapType == SnapObject.SnapType.SEGMENT_INT) {
			g2.setStroke(styleInfo.HOVER_PRESELECT_STROKE);
		}
		else {
			g2.setStroke(styleInfo.HOVER_EXTENSION_STROKE);
		}
		renderSegment(g2,mercatorToPixels,p1,p2);
	}
	
	/** This method looks up an select object for this snap object.  . There is
	 * no select object for a segment.
	 * 
	 * @param editMap	The edit map of existing edit objects
	 * @return			The edit object
	 */
	@Override
	public EditObject getSelectObject() {
		//no select object for segment
		return null;
	}
	
	//===========================
	// Private Methods
	//===========================
	
	/** This method calculates a snap point for an input point on a segment. The
	 * edit layer will snap from the input (mouse) location to a segment. The snap
	 * is also rendered in the UI. It is rendered differently for snapping to a point
	 * within a real segment as opposed to snapping to an extension of a real segment
	 * of snapping to a virtual segment. In the case of the virtual segment, the
	 * extension will be drawn from the segment point 1. In the case of an extension
	 * from a real segment, the extension will be drawn from the closest end.
	 * 
	 * @param segPt1			This is one point on the segment. If this is a virtual
	 *							segment this should be the anchor point.
	 * @param segPt2			This is the other point on the segment.
	 * @param inPoint			This is the input point (mouse location)
	 * @param segmentIsVirtual	This should be flagged as true if the segment is virtual.
	 *							A snap within a real segment is rendered differently
	 *							than a snap outside of a real segment.
	 * @param mercRadSq			This is the radius of a hit in mercator coordinates.
	 * @return					The SnapSegmnt object. Null if there is no snap.
	 */
	private static SnapSegment getSnapSegment(Point2D segPt1, Point2D segPt2, 
			Point2D inPoint, boolean segmentIsVirtual, double mercRadSq) {
		//check for a hit
		if(Line2D.ptLineDistSq(segPt1.getX(),segPt1.getY(),segPt2.getX(),segPt2.getY(),inPoint.getX(),inPoint.getY()) >= mercRadSq) {
			//no hit for this segment
			return null;
		}
			
		//calculate the hit point
		SnapSegment ss = new SnapSegment();
		double dxs = segPt2.getX() - segPt1.getX();
		double dys = segPt2.getY() - segPt1.getY();
		double dxp = inPoint.getX() - segPt1.getX();
		double dyp = inPoint.getY() - segPt1.getY();
		
		double fraction = (dxs * dxp + dys * dyp)/(dxs * dxs + dys * dys);
		ss.snapPoint = new Point2D.Double(segPt1.getX() + fraction * dxs,segPt1.getY() + fraction * dys);
		ss.err2 = ss.snapPoint.distanceSq(inPoint);
		if(segmentIsVirtual) {
			//for a virtual segment, draw from pt1 to snap point.
			//pt1 one should be the anchor point
			ss.p1 = new Point2D.Double(segPt1.getX(),segPt1.getY());
			ss.p2 = ss.snapPoint;
			//fill in proper snap type
			ss.snapType = SnapObject.SnapType.UNKNOWN;
		}
		else if(fraction < 0) {
			//snap to extension from point 1
			ss.p1 = new Point2D.Double(segPt1.getX(),segPt1.getY());
			ss.p2 = ss.snapPoint;
			ss.snapType = SnapObject.SnapType.SEGMENT_EXT;
		}
		else if(fraction > 1) {
			//snap to extension from point 2
			ss.p1 = new Point2D.Double(segPt2.getX(),segPt2.getY());
			ss.p2 = ss.snapPoint;
			ss.snapType = SnapObject.SnapType.SEGMENT_EXT;
		}
		else {
			//snap to segment
			ss.p1 = new Point2D.Double(segPt1.getX(),segPt1.getY());
			ss.p2 = new Point2D.Double(segPt2.getX(),segPt2.getY());
			ss.snapType = SnapObject.SnapType.SEGMENT_INT;
		}
		
		return ss;
	}
	
	
	/** This method creates the best horizontal or vertical snap for the given segment and mouse point.
	 * The horizontal and vertical are measured relative to the current screen coordinates and
	 * not global coordinates. 
	 * The segment should include the mouse point as one of the ends. If no perpendicular snap is
	 * found null is returned.
	 * 
	 * @param es			The segment that should be checked
	 * @param mouseMerc		the mouse position, in mercator coordinates
	 * @param mercRadSq		the error radius for a snap, in mercator coordinates
	 * @param mercPix00		the 0,0 pixel location on the screen, in mercator coordinates
	 * @param mercPix10		the 1,0 pixel location on the screen, in mercator coordinates
	 * @param mercPix01		the 0,1 pixel location on the screen, in mercator coordinates
	 * @return 
	 */
	public static SnapSegment getHorOrVertSnapSegment(EditSegment es, Point2D mouseMerc, double mercRadSq,
			Point2D mercPix00, Point2D mercPix10, Point2D mercPix01) {
		
		//use the point AWAY from the mouse as the base point
		Point2D basePoint;
		if(mouseMerc.equals(es.en1.point)) {
			basePoint = es.en2.point;
		}
		else {
			basePoint = es.en1.point;
		}
		
		//add the screen horizontal and vertical diretions to make a virtual segment 
		Point2D hvPoint = new Point2D.Double();
		//horizontal
		hvPoint.setLocation(basePoint.getX() + mercPix10.getX() - mercPix00.getX(), 
				basePoint.getY() + mercPix10.getY() - mercPix00.getY());
		SnapSegment ssh = getSnapSegment(basePoint,hvPoint,mouseMerc,true,mercRadSq);
		//vertical
		hvPoint.setLocation(basePoint.getX() + mercPix01.getX() - mercPix00.getX(), 
				basePoint.getY() + mercPix01.getY() - mercPix00.getY());
		SnapSegment ssv = getSnapSegment(basePoint,hvPoint,mouseMerc,true,mercRadSq);		
		
		if((ssh != null)&&((ssv == null)||(ssh.err2 < ssv.err2))) {
			ssh.snapType = SnapObject.SnapType.HORIZONTAL;
			return ssh;
		}
		else if(ssv != null) {
			ssv.snapType = SnapObject.SnapType.VERTICAL;
			return ssv;
		}
		else {
			return null;
		}
	}
	
	/** This method loads the best perpendicular snap segment. If none is found
	 * null is returned. This only searches for a perpendicular between the 
	 * input segment and segments connected to it at the FIXED end. */
	public static SnapSegment getPerpSegment(EditSegment editSegment, Point2D mouseMerc, double mercRadSq) {
		OsmNode pivotNode;
		Point2D pivotPoint = new Point2D.Double();
		SnapSegment ss;
		SnapSegment ss0 = null;
		OsmNode node;
		Point2D basePoint;
		
		//check for perpindiculars only to segments conected to the non-mouse node
		if(editSegment.en1.point.equals(mouseMerc)) {
			node = editSegment.en2.node;
		}
		else {
			node = editSegment.en1.node;
		}
		
		if(node != null) {
			basePoint = node.getPoint();
			for(OsmSegment segment:node.getSegments()) {
				if(segment.getNode1() == node) {
					pivotNode = segment.getNode2();
				}
				else {
					pivotNode = segment.getNode1();
				}
				double dx = pivotNode.getPoint().getX() - basePoint.getX();
				double dy = pivotNode.getPoint().getY() - basePoint.getY();
				pivotPoint.setLocation(basePoint.getX() - dy,basePoint.getY() + dx);
				//get snap to virtual segment
				ss = getSnapSegment(basePoint,pivotPoint,mouseMerc,true,mercRadSq);
				if((ss != null)&&((ss0 == null)||(ss.err2 < ss0.err2))) {
					ss0 = ss;
				}
			}
		}
		
		//set the proper snap type - not known for virtual nodes
		if(ss0 != null) {
			ss0.snapType = SnapObject.SnapType.SEGMENT_PERP;
		}
		
		return ss0;
	}
	
}
