package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.MouseMoveAction;
import intransix.osm.termite.app.edit.editobject.EditSegment;
import intransix.osm.termite.app.edit.snapobject.SnapSegment;
import intransix.osm.termite.app.edit.snapobject.SnapObject;
import intransix.osm.termite.app.edit.snapobject.SnapIntersection;
import intransix.osm.termite.app.edit.snapobject.SnapNode;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.app.filter.FilterManager;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmSegment;
//import java.awt.event.MouseEvent;
//import java.awt.geom.Point2D;
//import java.awt.geom.AffineTransform;
//import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author sutter
 */
public class CreateSnapMoveAction implements MouseMoveAction {
	
	//this is the limit for ignoring pairs of lines for intersecting
	private final static double ALMOST_PARALLEL_SIN_THETA = .1; //5.7 degrees
	
	private EditManager editManager;
	private List<SnapSegment> workingSnapSegments = new ArrayList<SnapSegment>();
	
	public CreateSnapMoveAction(EditManager editManager) {
		this.editManager = editManager;
	}
	
	@Override
	public boolean init() {
		
		return true;
	}
	
//	@Override
//	public void mouseMoved(Point2D mouseMerc, double mercRadSq, MouseEvent e) {
//		MapDataManager mapDataManager = editManager.getOsmData();
//		if(mapDataManager == null) return;
//		
//		//get snapObjects
//		List<SnapObject> snapObjects = editManager.getSnapObjects();
//		boolean wasActive = (snapObjects.size() > 0);
//		snapObjects.clear();
//		
//		//check for hovering over these objects
//		SnapObject snapObject;
//		
//		//check nodes and segments from them
//		for(OsmNode node:mapDataManager.getOsmData().getOsmNodes()) {		
//	
//			//make sure edit is enabled for this object
//			if(!FilterManager.getObjectEditEnabled(node)) continue;
//
//			//check for a node hit
//			snapObject = SnapNode.testNode(node, mouseMerc, mercRadSq);
//			if(snapObject != null) {
//				snapObjects.add(snapObject);
//			}
//
//			//check for a segment hit
//			for(OsmSegment segment:(node).getSegments()) {
//				if(!FilterManager.getSegmentEditEnabled(segment)) continue;
//
//				//only do the segments that start with this node, to avoid doing them twice
//				if(segment.getNode1() == node) {
//					//snap preview - when we are in an edit
//					//check for segment and extension hit
//					//do this when a mouse edit action is active
//
//					SnapSegment snapSegment = testSegmentHit(segment,
//							mouseMerc,mercRadSq);
//					if(snapSegment != null) {
//						snapObjects.add(snapSegment);
//						workingSnapSegments.add(snapSegment);
//					}
//				}
//			}
//		}
//		
//		//check for snapping in the pending snap segments
//		SnapSegment ss;
//		List<EditSegment> pendingSnapSegments = editManager.getPendingSnapSegments();
//		if(pendingSnapSegments != null) {
////			AffineTransform pixelsToMercator= viewRegionManager.getPixelsToMercator();
////			Point2D mercPix00 = new Point2D.Double(0,0);
////			Point2D mercPix10 = new Point2D.Double(1,0);
////			Point2D mercPix01 = new Point2D.Double(0,1);
////			pixelsToMercator.transform(mercPix00, mercPix00);
////			pixelsToMercator.transform(mercPix10, mercPix10);
////			pixelsToMercator.transform(mercPix01, mercPix01);
//		
//			for(EditSegment es:pendingSnapSegments) {
//				//check for horizontal snap
//ss = null;
////				ss = getHorOrVertSnapSegment(es,mouseMerc,mercRadSq,mercPix00,mercPix10,mercPix01);
//				if(ss != null) {
//					snapObjects.add(ss);
//					workingSnapSegments.add(ss);
//				}
//				else {
//					//only check perpicular if it is not already a horizontal or vertical snap
//					//check for perps from both ends
//					ss = getPerpSegment(es,mouseMerc,mercRadSq);
//					if(ss != null) {
//						snapObjects.add(ss);
//						workingSnapSegments.add(ss);
//					}
//				}
//			}
//		}
//		
//		//check for intersections
//		loadIntersections(workingSnapSegments, mouseMerc, mercRadSq, snapObjects);
//		workingSnapSegments.clear();
//		
//		//order the snap objects and select the active one
//		if(snapObjects.size() > 1) {
//			Collections.sort(snapObjects);
//		}
//		int activeSnapObject = snapObjects.isEmpty() ? -1 : 0;
//		editManager.setActiveSnapObject(activeSnapObject);
//		
//		boolean isActive = (snapObjects.size() > 0);
//		
//		if(wasActive||isActive) {
//			editManager.getEditLayer().notifyContentChange();
//		}
//	}
	
	//===========================
	// Private Methods
	//===========================
	
//		/** This method tests if the mouse point hits the segment. If so, it 
//	 * create a SnapSegment object. 
//	 * 
//	 * @param segment		The osm segment being tested
//	 * @param mouseMerc		The mouse location, in mercator coordinates
//	 * @param mercRadSq		The radius for a hit, in mercator coordinates.
//	 * @return 
//	 */
//	private SnapSegment testSegmentHit(OsmSegment segment, 
//			Point2D mouseMerc, double mercRadSq) {
//		
//		return getSnapSegment(segment.getNode1().getPoint(),
//				segment.getNode2().getPoint(),
//				mouseMerc,false,mercRadSq);
//	}
//	
//		
//	/** This method calculates a snap point for an input point on a segment. The
//	 * edit layer will snap from the input (mouse) location to a segment. The snap
//	 * is also rendered in the UI. It is rendered differently for snapping to a point
//	 * within a real segment as opposed to snapping to an extension of a real segment
//	 * of snapping to a virtual segment. In the case of the virtual segment, the
//	 * extension will be drawn from the segment point 1. In the case of an extension
//	 * from a real segment, the extension will be drawn from the closest end.
//	 * 
//	 * @param segPt1			This is one point on the segment. If this is a virtual
//	 *							segment this should be the anchor point.
//	 * @param segPt2			This is the other point on the segment.
//	 * @param inPoint			This is the input point (mouse location)
//	 * @param segmentIsVirtual	This should be flagged as true if the segment is virtual.
//	 *							A snap within a real segment is rendered differently
//	 *							than a snap outside of a real segment.
//	 * @param mercRadSq			This is the radius of a hit in mercator coordinates.
//	 * @return					The SnapSegmnt object. Null if there is no snap.
//	 */
//	private SnapSegment getSnapSegment(Point2D segPt1, Point2D segPt2, 
//			Point2D inPoint, boolean segmentIsVirtual, double mercRadSq) {
//		
////quick check for 0 length segments
//if(segPt1.distanceSq(segPt2) == 0) return null;		
//		
//		//check for a hit
//		if(Line2D.ptLineDistSq(segPt1.getX(),segPt1.getY(),segPt2.getX(),segPt2.getY(),inPoint.getX(),inPoint.getY()) >= mercRadSq) {
//			//no hit for this segment
//			return null;
//		}
//			
//		//calculate the hit point
//		SnapSegment ss = new SnapSegment();
//		double dxs = segPt2.getX() - segPt1.getX();
//		double dys = segPt2.getY() - segPt1.getY();
//		double dxp = inPoint.getX() - segPt1.getX();
//		double dyp = inPoint.getY() - segPt1.getY();
//		
//		double fraction = (dxs * dxp + dys * dyp)/(dxs * dxs + dys * dys);
//		ss.snapPoint = new Point2D.Double(segPt1.getX() + fraction * dxs,segPt1.getY() + fraction * dys);
//		ss.err2 = ss.snapPoint.distanceSq(inPoint);
//		if(segmentIsVirtual) {
//			//for a virtual segment, draw from pt1 to snap point.
//			//pt1 one should be the anchor point
//			ss.p1 = new Point2D.Double(segPt1.getX(),segPt1.getY());
//			ss.p2 = ss.snapPoint;
//			//fill in proper snap type
//			ss.snapType = SnapObject.SnapType.SEGMENT_EXT;
//		}
//		else if(fraction < 0) {
//			//snap to extension from point 1
//			ss.p1 = new Point2D.Double(segPt1.getX(),segPt1.getY());
//			ss.p2 = ss.snapPoint;
//			ss.snapType = SnapObject.SnapType.SEGMENT_EXT;
//		}
//		else if(fraction > 1) {
//			//snap to extension from point 2
//			ss.p1 = new Point2D.Double(segPt2.getX(),segPt2.getY());
//			ss.p2 = ss.snapPoint;
//			ss.snapType = SnapObject.SnapType.SEGMENT_EXT;
//		}
//		else {
//			//snap to segment
//			ss.p1 = new Point2D.Double(segPt1.getX(),segPt1.getY());
//			ss.p2 = new Point2D.Double(segPt2.getX(),segPt2.getY());
//			ss.snapType = SnapObject.SnapType.SEGMENT_INT;
//		}
//		
//		return ss;
//	}
//	
//	
//	/** This method creates the best horizontal or vertical snap for the given segment and mouse point.
//	 * The horizontal and vertical are measured relative to the current screen coordinates and
//	 * not global coordinates. 
//	 * The segment should include the mouse point as one of the ends. If no perpendicular snap is
//	 * found null is returned.
//	 * 
//	 * @param es			The segment that should be checked
//	 * @param mouseMerc		the mouse position, in mercator coordinates
//	 * @param mercRadSq		the error radius for a snap, in mercator coordinates
//	 * @param mercPix00		the 0,0 pixel location on the screen, in mercator coordinates
//	 * @param mercPix10		the 1,0 pixel location on the screen, in mercator coordinates
//	 * @param mercPix01		the 0,1 pixel location on the screen, in mercator coordinates
//	 * @return 
//	 */
//	private SnapSegment getHorOrVertSnapSegment(EditSegment es, Point2D mouseMerc, double mercRadSq,
//			Point2D mercPix00, Point2D mercPix10, Point2D mercPix01) {
//		
//		//use the point AWAY from the mouse as the base point
//		Point2D basePoint;
//		if(mouseMerc.equals(es.en1.point)) {
//			basePoint = es.en2.point;
//		}
//		else {
//			basePoint = es.en1.point;
//		}
//		
//		//add the screen horizontal and vertical diretions to make a virtual segment 
//		Point2D hvPoint = new Point2D.Double();
//		//horizontal
//		hvPoint.setLocation(basePoint.getX() + mercPix10.getX() - mercPix00.getX(), 
//				basePoint.getY() + mercPix10.getY() - mercPix00.getY());
//		SnapSegment ssh = getSnapSegment(basePoint,hvPoint,mouseMerc,true,mercRadSq);
//		//vertical
//		hvPoint.setLocation(basePoint.getX() + mercPix01.getX() - mercPix00.getX(), 
//				basePoint.getY() + mercPix01.getY() - mercPix00.getY());
//		SnapSegment ssv = getSnapSegment(basePoint,hvPoint,mouseMerc,true,mercRadSq);		
//		
//		if((ssh != null)&&((ssv == null)||(ssh.err2 < ssv.err2))) {
//			ssh.snapType = SnapObject.SnapType.HORIZONTAL;
//			return ssh;
//		}
//		else if(ssv != null) {
//			ssv.snapType = SnapObject.SnapType.VERTICAL;
//			return ssv;
//		}
//		else {
//			return null;
//		}
//	}
//	
//	/** This method loads the best perpendicular snap segment. If none is found
//	 * null is returned. This only searches for a perpendicular between the 
//	 * input segment and segments connected to it at the FIXED end. */
//	private SnapSegment getPerpSegment(EditSegment editSegment, Point2D mouseMerc, double mercRadSq) {
//		OsmNode pivotNode;
//		Point2D pivotPoint = new Point2D.Double();
//		SnapSegment ss;
//		SnapSegment ss0 = null;
//		OsmNode node;
//		Point2D basePoint;
//		
//		//check for perpindiculars only to segments conected to the non-mouse node
//		if(editSegment.en1.point.equals(mouseMerc)) {
//			node = editSegment.en2.node;
//		}
//		else {
//			node = editSegment.en1.node;
//		}
//		
//		if(node != null) {
//			basePoint = node.getPoint();
//			for(OsmSegment segment:node.getSegments()) {
//				if(segment.getNode1() == node) {
//					pivotNode = segment.getNode2();
//				}
//				else {
//					pivotNode = segment.getNode1();
//				}
//				double dx = pivotNode.getPoint().getX() - basePoint.getX();
//				double dy = pivotNode.getPoint().getY() - basePoint.getY();
//				pivotPoint.setLocation(basePoint.getX() - dy,basePoint.getY() + dx);
//				//get snap to virtual segment
//				ss = getSnapSegment(basePoint,pivotPoint,mouseMerc,true,mercRadSq);
//				if((ss != null)&&((ss0 == null)||(ss.err2 < ss0.err2))) {
//					ss0 = ss;
//				}
//			}
//		}
//		
//		//set the proper snap type - not known for virtual nodes
//		if(ss0 != null) {
//			ss0.snapType = SnapObject.SnapType.SEGMENT_PERP;
//		}
//		
//		return ss0;
//	}
//	
//	
//	/** This method calculates the intersection of the two segments and checks if
//	 * the intersection in in range. 
//	 * 
//	 * @param ss1			one segment
//	 * @param ss2			the other segment
//	 * @param mouseMerc		the mouse location,in mercator coordinates 
//	 * @param mercRadSq		the error radius for a snap, in mercator coordinates
//	 * @return 
//	 */
//	private void loadIntersections(List<SnapSegment> snapSegments, 
//			Point2D mouseMer, double mercRadSq, List<SnapObject> snapObjects) {
//		
//		int cnt = snapSegments.size();
//		for(int i = 0; i < cnt; i++) {
//			final SnapSegment ss1 = snapSegments.get(i);
//			for(int j = i+1; j < cnt; j++) {
//				final SnapSegment ss2 = snapSegments.get(j);
//				
//				double xs1 = ss1.p1.getX();
//				double ys1 = ss1.p1.getY();
//				double dx1 = ss1.p2.getX() - xs1;
//				double dy1 = ss1.p2.getY() - ys1;
//				double xs2 = ss2.p1.getX();
//				double ys2 = ss2.p1.getY();
//				double dx2 = ss2.p2.getX() - xs2;
//				double dy2 = ss2.p2.getY() - ys2;
//
//				double den = dx1*dy2 - dy1*dx2;
//				double len1 = Math.sqrt(dx1*dx1 + dy1*dy1);
//				double len2 = Math.sqrt(dx2*dx2 + dy2*dy2);
//
//				//make sure lines are not cloe to being parallel
//				if( Math.abs(den / (len1 * len2)) < ALMOST_PARALLEL_SIN_THETA) continue;
//
//				//find intersection
//				double num = -(xs1*dy2 - ys1*dx2) + (xs2*dy2 - ys2*dx2);
//				double alpha = num / den;
//
//				double xSnap = xs1 + alpha * dx1;
//				double ySnap = ys1 + alpha * dy1;
//
//				double err2 = mouseMer.distanceSq(xSnap,ySnap);
//
//				if(err2 < mercRadSq) {
//					//not in range
//					SnapIntersection si = new SnapIntersection();
//					si.snapPoint = new Point2D.Double(xSnap,ySnap);
//					si.s1 = ss1;
//					si.s2 = ss2;
//					si.err2 = err2;
//					
//					snapObjects.add(si);
//				}		
//			}
//		}
//	}
//	
	
}
