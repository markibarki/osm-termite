
package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.data.edit.MoveEdit;
import intransix.osm.termite.map.data.edit.EditDestPoint;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author sutter
 */
public class MoveAction implements MouseEditAction {
	
	private OsmData osmData;
	private EditLayer editLayer;
	private EditDestPoint moveStartPoint;
	
	//working variabls to initialize edit display for move
	private HashMap<Object,EditObject> editMap;
	private boolean lastNodeWasNew;
	private boolean lastSegmentWasNew;
	private boolean lastSegmentWasDynamic;
	
	public void init(OsmData osmData, EditLayer editLayer) {
		this.osmData = osmData;
		this.editLayer = editLayer;
	
		this.moveStartPoint = editLayer.getSelectionPoint();
		Point2D mouseMerc = editLayer.getMapPanel().getMousePointMerc();
		
		//these are the objects that will move
		List<Object> selection = editLayer.getSelection();
		
		//these lists are to display the move preview
		editLayer.clearPending();
		List<EditObject> pendingObjects = editLayer.getPendingObjects();
		List<EditNode> movingNodes = editLayer.getMovingNodes();
		List<EditSegment> pendingSnapSegments = editLayer.getPendingSnapSegments();
		
		//add all unique nodes to the moving nodes
		editMap = new HashMap<Object,EditObject>();
		EditNode editNode;
		for(Object selectObject:selection) {
			if(selectObject instanceof OsmNode) {
				//add this edit node
				editNode = this.getEditNode((OsmNode)selectObject);
				if(lastNodeWasNew) {
					movingNodes.add(editNode);
				}
			}
			else if(selectObject instanceof OsmWay) {
				for(OsmNode node:((OsmWay)selectObject).getNodes()) {
					editNode = this.getEditNode(node);
					if(lastNodeWasNew) {
						movingNodes.add(editNode);
					}
				}
			}
		}
		//get the segments for each of the moving nodes
		EditSegment editSegment;
		for(EditNode en:movingNodes) {
			pendingObjects.add(en);
			for(OsmSegment segment:en.node.getSegments()) {
				editSegment = getEditSegment(segment);
				if(lastSegmentWasNew) {
					pendingObjects.add(editSegment);
					if(lastSegmentWasDynamic) {
						pendingSnapSegments.add(editSegment);
					}
				}
			}
		}
		
		//set this initial position
		updateMovingNodes(mouseMerc);
		
		//clean up working data
		editMap = null;
	}
	
	@Override
	public void updateMovingNodes(Point2D mouseMerc) {
		if(moveStartPoint != null) {
			double dx = mouseMerc.getX() - moveStartPoint.point.getX();
			double dy = mouseMerc.getY() - moveStartPoint.point.getY();
			
			List<EditNode> movingNodes = editLayer.getMovingNodes();
			
			for(EditNode en:movingNodes) {
				//for a move, all nodes should be real so node should exist
				if(en.node != null) {
					Point2D nodePoint = en.node.getPoint();
					en.point.setLocation(nodePoint.getX() + dx, nodePoint.getY() + dy);
				}
			}
		}
	}
	
	@Override
	public void mousePressed(EditDestPoint clickDestPoint) {
		
		List<Object> selection = editLayer.getSelection();
		
		if(!selection.isEmpty()) {
			//execute the move
			MoveEdit me = new MoveEdit(osmData);
			me.selectionMoved(selection,moveStartPoint,clickDestPoint);
		}
		//exit move mode
		editLayer.exitMove();
		
		//update select point
		editLayer.setSelectionPoint(clickDestPoint);

	}
	
	@Override
	public void featureLayerUpdated(FeatureInfo featureInfo) {
		//no action
	}

	private EditNode getEditNode(OsmNode node) {
		EditObject editObject = editMap.get(node);
		if(editObject == null) {
			lastNodeWasNew = true;
			EditNode en = new EditNode(node);
			editMap.put(node,en);
			return en;
		}
		else {
			lastNodeWasNew = false;
			return (EditNode)editObject;
		}
	}

	private EditSegment getEditSegment(OsmSegment segment) {
		EditObject editObject = editMap.get(segment);
		if(editObject == null) {
			//create a new segment
			lastSegmentWasNew = true;
			int newCount = 0;
			EditNode en1 = this.getEditNode(segment.getNode1());
			if(lastNodeWasNew) newCount++;
			EditNode en2 = this.getEditNode(segment.getNode2());
			if(lastNodeWasNew) newCount++;
			EditSegment editSegment = new EditSegment(segment,en1,en2);

			//segment is dynamic if one node is new and the other is not
			lastSegmentWasDynamic = (newCount == 1);

			return editSegment;
		}
		else {
			lastSegmentWasNew = false;
			//this variable is valid only for new segments
			lastSegmentWasDynamic = false;
			return (EditSegment)editObject;
		}
	}
	

	
}
