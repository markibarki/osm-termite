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
	
	protected static HashMap<Object,EditObject> editMap = new HashMap<Object,EditObject>();
	
	/** This method clears any EditObjects being tracked. It should only be called if there 
	 * are no instances of EditObjects being used in the edit layer. */
	public static void clearEditMap() {
		editMap.clear();
	}
	
	/** This method returns the OsmObject for this edit object. */
	public abstract OsmObject getOsmObject();
	
	/** This method loads an nodes that should move if the object is
	 * selected.
	 * 
	 * @param editMap		This is the map of the active edit objects
	 * @param movingNodes	This is a list of moving nodes to be filled
	 */
	public abstract void loadMovingNodes(List<EditNode> movingNodes);

	/** This method loads the pending objects and pending snap segments if this
	 * object is selected. This should be called after the moving nodes are fully set.
	 * 
	 * @param editMap				This is the map of the active edit objects
	 * @param pendingObjects		This is a list of pending objects to load
	 * @param pendingSnapSegments	This is a list of snap segments to load.
	 * @param movingNodes			This is a list of moving nodes to load.
	 */
	public abstract void loadPendingObjects(List<EditObject> pendingObjects,
			List<EditSegment> pendingSnapSegments,
			List<EditNode> movingNodes);
	
	/** This gets the EditNode associated with the given osm node. */
	public static EditNode getEditNode(OsmNode node) {
		EditNode editNode = (EditNode)editMap.get(node);
		if(editNode == null) {
			editNode = new EditNode(node);
			editMap.put(node,editNode);
		}
		return editNode;
	}
	
	public static EditNode getEditNode(Point2D point, FeatureInfo featureInfo) {
		return new EditNode(point,featureInfo);
	}
	
	/** This gets the edit way for the given osm way. */
	public static EditWay getEditWay(OsmWay way) {
		EditWay editWay = (EditWay)editMap.get(way);
		if(editWay == null) {
			editWay = new EditWay(way);
			editMap.put(way,editWay);
		}
		return editWay;
	}
	
	public static EditSegment getEditSegment(OsmSegment segment) {
		
		EditSegment editSegment = (EditSegment)editMap.get(segment);
		if(editSegment == null) {
			editSegment = new EditSegment(segment);
			editMap.put(segment,editSegment);
		}
		return editSegment;
	}
	
	public static EditSegment getEditSegment(EditNode en1, EditNode en2) {
		return new EditSegment(en1,en2);
	}
	
	public static EditVirtualNode getEditVirtualNode(OsmSegment osmSegment) {
		return new EditVirtualNode(osmSegment);
	}
	
}
