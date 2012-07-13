package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.data.OsmObject;
import intransix.osm.termite.map.data.OsmWay;
import intransix.osm.termite.map.data.OsmSegmentWrapper;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.*;

/**
 *
 * @author sutter
 */
public class EditWay /*extends EditObject*/ {
//	public List<Point2D> points;
//	public FeatureInfo featureInfo;
//	public OsmWay way;
//	
////	/** This method returns the OsmObject for this edit object. */
////	@Override
////	public OsmObject getOsmObject() {
////		return way;
////	}
////	
////	/** This method loads an nodes that should move if the object is
////	 * selected.
////	 * 
////	 * @param movingNodes	This is a list of moving nodes to be filled
////	 */
////	@Override
////	public void loadMovingNodes(Set<EditNode> movingNodes) {
////		//add all nodes to moving nodes
////		//virtual nodes should also be added, but they will be added by the virtual
////		//node selection
////		for(OsmNode node:way.getNodes()) {
////			EditNode en = EditObject.getEditNode(node);
////			movingNodes.add(en);
////		}
////	}
////
////	/** This method loads the pending objects and pending snap segments if this
////	 * object is selected. This should be called after the moving nodes are fully set.
////	 * 
////	 * @param pendingObjects		This is a list of pending objects to load
////	 * @param pendingSnapSegments	This is a list of snap segments to load.
////	 * @param movingNodes			This is a list of moving nodes to load.
////	 */
////	@Override
////	public void loadPendingObjects(Set<EditObject> pendingObjects,
////			Set<EditSegment> pendingSnapSegments,
////			Set<EditNode> movingNodes) {
////		
////		for(OsmNode node:way.getNodes()) {
////			//add all nodes to pending objects - this will add all connected
////			//segments properly to pending and pendingSnap
////			EditNode en = EditObject.getEditNode(node);
////			en.loadPendingObjects(pendingObjects, pendingSnapSegments, movingNodes);
////		}	
////	}
//	
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
//		g2.setStroke(styleInfo.SELECT_STROKE);
//
//		//render
//		renderWay(g2,mercatorToPixels,way);
//	}
//	
////	@Override
////	public boolean equals(Object obj) {
////		if(this == obj)
////			return true;
////		if((obj == null) || (obj.getClass() != this.getClass()))
////			return false;
////		//object must be EditWay at this point
////		EditWay test = (EditWay)obj;
////		return (this.way == test.way);
////	}
////
////	@Override
////	public int hashCode() {
////		return way.hashCode();
////	}
//	
//	//=======================
//	// Package Methods
//	//=======================
//	
//	/** This method should not be called except from within the EditObjects. 
//	 * Use the static method from EditObject to get an instance of this object. */
//	EditWay(OsmWay way) {
//		this.way = way;
//		this.featureInfo = way.getFeatureInfo();
//		this.points = new ArrayList<Point2D>();
//		for(OsmNode node:way.getNodes()) {
//			Point2D point = new Point2D.Double(node.getPoint().getX(),node.getPoint().getY());
//			points.add(point);
//		}
//	}
}
