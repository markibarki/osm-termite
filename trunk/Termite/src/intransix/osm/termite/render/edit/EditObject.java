package intransix.osm.termite.render.edit;

import java.util.*;
import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.Point2D;

/**
 *
 * @author sutter
 */
public abstract class EditObject extends EditDrawable {
//	
//	/** This method returns the OsmObject for this edit object. */
//	public abstract OsmObject getOsmObject();
	
//	/** This method loads an nodes that should move if the object is
//	 * selected.
//	 * 
//	 * @param editMap		This is the map of the active edit objects
//	 * @param movingNodes	This is a list of moving nodes to be filled
//	 */
//	public abstract void loadMovingNodes(Set<EditNode> movingNodes);
//
//	/** This method loads the pending objects and pending snap segments if this
//	 * object is selected. This should be called after the moving nodes are fully set.
//	 * 
//	 * @param editMap				This is the map of the active edit objects
//	 * @param pendingObjects		This is a list of pending objects to load
//	 * @param pendingSnapSegments	This is a list of snap segments to load.
//	 * @param movingNodes			This is a list of moving nodes to load.
//	 */
//	public abstract void loadPendingObjects(Set<EditObject> pendingObjects,
//			Set<EditSegment> pendingSnapSegments,
//			Set<EditNode> movingNodes);
	
//	/** This gets the EditNode associated with the given osm node. */
//	public static EditNode getEditNode(OsmNode node) {
//		EditNode editNode = (EditNode)node.getEditData();
//		if(editNode == null) {
//			editNode = new EditNode(node);
//			node.setEditData(editNode);
//		}
//		return editNode;
//	}
//	
//	public static EditNode getEditNode(Point2D point, FeatureInfo featureInfo) {
//		return new EditNode(point,featureInfo);
//	}
//	
//	/** This gets the edit way for the given osm way. */
//	public static EditWay getEditWay(OsmWay way) {
//		EditWay editWay = (EditWay)way.getEditData();
//		if(editWay == null) {
//			editWay = new EditWay(way);
//			way.setEditData(editWay);
//		}
//		return editWay;
//	}
//	
//	public static EditSegment getEditSegment(OsmSegment segment) {
//		
//		EditSegment editSegment = (EditSegment)segment.getEditData();
//		if(editSegment == null) {
//			editSegment = new EditSegment(segment);
//			segment.setEditData(editSegment);
//		}
//		return editSegment;
//	}
//	
//	public static EditSegment getEditSegment(EditNode en1, EditNode en2) {
//		return new EditSegment(en1,en2);
//	}
//	
//	public static EditVirtualNode getEditVirtualNode(OsmSegment osmSegment) {
//		return new EditVirtualNode(osmSegment);
//	}
//	
}
