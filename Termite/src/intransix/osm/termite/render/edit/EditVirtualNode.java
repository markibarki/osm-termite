package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmObject;
import intransix.osm.termite.map.data.OsmSegment;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author sutter
 */
public class EditVirtualNode extends EditObject {
	public EditNode en1;
	public EditNode en2;
	public OsmSegment osmSegment;
	
	//this is a virtual point
	public EditNode enVirtual;
	public EditSegment es1;
	public EditSegment es2;
	
//	/** This method returns the OsmObject for this edit object. */
//	@Override
//	public OsmObject getOsmObject() {
//		return null;
//	}
//	
//	/** This method loads an nodes that should move if the object is
//	 * selected.
//	 * 
//	 * @param movingNodes	This is a list of moving nodes to be filled
//	 */
//	@Override
//	public void loadMovingNodes(Set<EditNode> movingNodes) {
//		
//		movingNodes.add(enVirtual);
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
//		//virtual segment to pending segments
//		pendingObjects.add(en1);
//		pendingObjects.add(es1);
//		pendingObjects.add(es2);
//		
//		//only virtual node should be moving, so these segments should be used
//		//in snap check
//		pendingSnapSegments.add(es1);
//		pendingSnapSegments.add(es2);
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
		
		enVirtual.render(g2, mercatorToPixels, styleInfo);
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		if(this == obj)
//			return true;
//		if((obj == null) || (obj.getClass() != this.getClass()))
//			return false;
//		// object must be Test at this point
//		EditSegment test = (EditSegment)obj;
//		//en1 and en2 should be non-null for virtual or real
//		return ((this.osmSegment == test.osmSegment)&&
//				(this.en1.equals(test.en1))&&
//				(this.en2.equals(test.en2)));
//	}
//
//	@Override
//	public int hashCode() {
//		int hash = 7;
//		hash = 31 * hash + en1.hashCode();
//		hash = 31 * hash + en2.hashCode();
//		hash = 31 * hash + (null == osmSegment ? 0 : osmSegment.hashCode());
//		return hash;
//	}
	
	//=======================
	// Package Methods
	//=======================
	
	/** Constructor */
	EditVirtualNode(VirtualNode virtualNode) {
		this.osmSegment = virtualNode.segment;
		en1 = new EditNode(osmSegment.getNode1());
		en2 = new EditNode(osmSegment.getNode2());
		
		double xCenter = (osmSegment.getNode1().getPoint().getX() + osmSegment.getNode2().getPoint().getX())/2;
		double yCenter = (osmSegment.getNode1().getPoint().getY() + osmSegment.getNode2().getPoint().getY())/2;
		Point2D midPoint = new Point2D.Double(xCenter,yCenter);
		enVirtual = new EditNode(midPoint,null);

		es1 = new EditSegment(null,en1,enVirtual);
		es2 = new EditSegment(null,enVirtual,en2);
	}
}
