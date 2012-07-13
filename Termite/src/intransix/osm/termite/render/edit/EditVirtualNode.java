package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmObject;
import intransix.osm.termite.map.data.OsmSegment;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.List;

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
	
	/** This method returns the OsmObject for this edit object. */
	@Override
	public OsmObject getOsmObject() {
		return null;
	}
	
	/** This method loads an nodes that should move if the object is
	 * selected.
	 * 
	 * @param movingNodes	This is a list of moving nodes to be filled
	 */
	@Override
	public void loadMovingNodes(List<EditNode> movingNodes) {
		
		movingNodes.add(enVirtual);
	}

	/** This method loads the pending objects and pending snap segments if this
	 * object is selected. This should be called after the moving nodes are fully set.
	 * 
	 * @param pendingObjects		This is a list of pending objects to load
	 * @param pendingSnapSegments	This is a list of snap segments to load.
	 * @param movingNodes			This is a list of moving nodes to load.
	 */
	@Override
	public void loadPendingObjects(List<EditObject> pendingObjects,
			List<EditSegment> pendingSnapSegments,
			List<EditNode> movingNodes) {

		//virtual segment to pending segments
		if(!pendingObjects.contains(es1)) {
			pendingObjects.add(es1);
		}
		//add vitual to snap if the non-vritual end is NOT moving
		if((!movingNodes.contains(en1))&&(!pendingSnapSegments.contains(es1))) {
			pendingSnapSegments.add(es1);
		}
		//virtual segment to pending segments
		if(!pendingObjects.contains(es2)) {
			pendingObjects.add(es2);
		}
		//add vitual to snap if the non-vritual end is NOT moving
		if((!movingNodes.contains(en2))&&(!pendingSnapSegments.contains(es2))) {
			pendingSnapSegments.add(es2);
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
		
		es1.render(g2, mercatorToPixels, styleInfo);
		es2.render(g2, mercatorToPixels, styleInfo);
	}
	
	//=======================
	// Package Methods
	//=======================
	
	/** This method should not be called except from within the EditObjects. 
	 * Use the static method from EditObject to get an instance of this object. */
	EditVirtualNode(OsmSegment osmSegment) {
		this.osmSegment = osmSegment;
		en1 = (EditNode)editMap.get(osmSegment.getNode1());
		en2 = (EditNode)editMap.get(osmSegment.getNode2());
		
		double xCenter = (osmSegment.getNode1().getPoint().getX() + osmSegment.getNode2().getPoint().getX())/2;
		double yCenter = (osmSegment.getNode1().getPoint().getY() + osmSegment.getNode2().getPoint().getY())/2;
		Point2D midPoint = new Point2D.Double(xCenter,yCenter);
		enVirtual = new EditNode(midPoint,null);

		es1 = new EditSegment(en1,enVirtual);
		es2 = new EditSegment(enVirtual,en2);
	}
}
