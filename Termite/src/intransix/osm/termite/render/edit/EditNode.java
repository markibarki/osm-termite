package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.data.OsmObject;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author sutter
 */
public class EditNode extends EditObject {
	
	public Point2D point;
	public FeatureInfo featureInfo;
	public OsmNode node;
	
	/** This method returns the OsmObject for this edit object. */
	@Override
	public OsmObject getOsmObject() {
		return node;
	}
	
	/** This method loads an nodes that should move if the object is
	 * selected.
	 * 
	 * @param movingNodes	This is a list of moving nodes to be filled
	 */
	@Override
	public void loadMovingNodes(List<EditNode> movingNodes) {
		//add this node
		if(!movingNodes.contains(this)) {
			movingNodes.add(this);
		}
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
		if(!pendingObjects.contains(this)) {
			pendingObjects.add(this);
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

		renderPoint(g2,mercatorToPixels,point,styleInfo.RADIUS_PIXELS);
	}
	
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
