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
public class EditSegment extends EditObject {
	public EditNode en1;
	public EditNode en2;
	public OsmSegment osmSegment;
	
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
//		//no movin nodes for a segment
//		//there should be no segments in a selection
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
//		//no pending objects. There should be no segments in a selection.
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
		
		g2.setStroke(styleInfo.SELECT_STROKE);
		renderSegment(g2,mercatorToPixels,en1.point,en2.point);
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
	
	/** This method should not be called except from within the EditObjects. 
	 * Use the static method from EditObject to get an instance of this object. */
	EditSegment(OsmSegment osmSegment, EditNode en1, EditNode en2) {
		this.en1 = en1;
		this.en2 = en2;
		this.osmSegment = osmSegment;
	}
}
