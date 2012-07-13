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
public class EditWay extends EditObject {
	public List<Point2D> points;
	public FeatureInfo featureInfo;
	public OsmWay way;
	
	/** This method returns the OsmObject for this edit object. */
	@Override
	public OsmObject getOsmObject() {
		return way;
	}
	
	/** This method loads an nodes that should move if the object is
	 * selected.
	 * 
	 * @param movingNodes	This is a list of moving nodes to be filled
	 */
	@Override
	public void loadMovingNodes(List<EditNode> movingNodes) {
		//add all nodes to moving nodes
		//virtual nodes should also be added, but they will be added by the virtual
		//node selection
		for(OsmNode node:way.getNodes()) {
			EditNode en = (EditNode)editMap.get(node);
			if(!movingNodes.contains(en)) {
				movingNodes.add(en);
			}
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
		
		//add all segments to pending segments
		for(OsmSegmentWrapper osw:way.getSegments()) {
			EditObject es = editMap.get(osw.segment);
			if((es != null)&&(!pendingObjects.contains(es))) {
				pendingObjects.add(es);
			}
		}
		//no snap segments associated with a adding a way
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

		g2.setStroke(styleInfo.SELECT_STROKE);

		//render
		renderWay(g2,mercatorToPixels,way);
	}
	
	//=======================
	// Package Methods
	//=======================
	
	/** This method should not be called except from within the EditObjects. 
	 * Use the static method from EditObject to get an instance of this object. */
	EditWay(OsmWay way) {
		this.way = way;
		this.featureInfo = way.getFeatureInfo();
		this.points = new ArrayList<Point2D>();
		for(OsmNode node:way.getNodes()) {
			Point2D point = new Point2D.Double(node.getPoint().getX(),node.getPoint().getY());
			points.add(point);
		}
	}
}
