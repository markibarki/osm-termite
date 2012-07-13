package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sutter
 */
public class EditNode extends EditObject {
	
	public Point2D point;
	public FeatureInfo featureInfo;
	public OsmNode node;
	
//	/** This method returns the OsmObject for this edit object. */
//	@Override
//	public OsmObject getOsmObject() {
//		return node;
//	}
//	
//	/** This method loads an nodes that should move if the object is
//	 * selected.
//	 * 
//	 * @param movingNodes	This is a list of moving nodes to be filled
//	 */
//	@Override
//	public void loadMovingNodes(Set<EditNode> movingNodes) {
//		//add this node
//		movingNodes.add(this);
//	}
//
//	/** This method loads the pending objects and pending snap segments if this
//	 * object is selected. This should be called after the moving nodes are fully set.
//	 * 
//	 * @param pendingObjects		This is a list of pending objects to load
//	 * @param pendingSnapSegments	This is a list of snap segments to load.
//	 * @param movingNodes			This is a list of moving nodes to load.
//	 */
//	@Override
//	public void loadPendingObjects(Set<EditObject> pendingObjects,
//			Set<EditSegment> pendingSnapSegments,
//			Set<EditNode> movingNodes) {
//			
//		//add this node
//		pendingObjects.add(this);
//		
//		for(OsmSegment segment:node.getSegments()) {
//			//add this segment to pending
//			EditSegment es = EditObject.getEditSegment(segment);
//			pendingObjects.add(es);
//			
//			//check if this is a pending snap segment - one node is not moving
//			EditNode otherNode = es.en1;
//			if(otherNode.node == node) otherNode = es.en2;
//			if(!movingNodes.contains(otherNode)) {
//				pendingSnapSegments.add(es);
//			}
//		}
//	}
	
	/** This method renders the object.
	 * 
	 * @param g2				The graphics context
	 * @param mercatorToPixels	The transform from mercator coordinates to pixels
	 * @param styleInfo			The style info for rendering
	 */
	@Override
	public void render(Graphics2D g2, AffineTransform mercatorToPixels, 
			StyleInfo styleInfo) {

		renderPoint(g2,mercatorToPixels,point,styleInfo.RADIUS_PIXELS);
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		if(this == obj)
//			return true;
//		if((obj == null) || (obj.getClass() != this.getClass()))
//			return false;
//		// object must be Test at this point
//		EditNode test = (EditNode)obj;
//		//point should be non-null for virtual or real
//		return ((this.node == test.node)&&(this.point.equals(test.point)));
//	}
//
//	@Override
//	public int hashCode() {
//		int hash = 7;
//		hash = 31 * hash + point.hashCode();
//		hash = 31 * hash + (null == node ? 0 : node.hashCode());
//		return hash;
//	}
	
	//=======================
	// Package Methods
	//=======================
	
	/** This method should not be called except from within the EditObjects. 
	 * Use the static method from EditObject to get an instance of this object. */
	EditNode(OsmNode node) {
		this.node = node;
		this.featureInfo = node.getFeatureInfo();
		this.point = new Point2D.Double(node.getPoint().getX(),node.getPoint().getY());
	}
	
	/** This method should not be called except from within the EditObjects. 
	 * Use the static method from EditObject to get an instance of this object. */
	EditNode(Point2D point, FeatureInfo featureInfo) {
		this.node = null;
		this.featureInfo = featureInfo;
		this.point = new Point2D.Double(point.getX(),point.getY()); 
	}
}
