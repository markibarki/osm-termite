package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmSegment;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.app.edit.MouseClickAction;
import intransix.osm.termite.app.edit.editobject.EditObject;
import intransix.osm.termite.app.edit.editobject.EditSegment;
import intransix.osm.termite.app.edit.editobject.EditNode;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.gui.mode.edit.SelectEditorMode;
import intransix.osm.termite.app.edit.impl.MoveEdit;
import intransix.osm.termite.app.edit.impl.EditDestPoint;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author sutter
 */
public class MoveClickAction implements MouseClickAction {
	
	private EditManager editManager;
	private EditDestPoint moveStartPoint;
	
	//working variabls to initialize edit display for move
	private HashMap<Object,EditObject> editMap;
	private boolean lastNodeWasNew;
	private boolean lastSegmentWasNew;
	private boolean lastSegmentWasDynamic;
	
	public MoveClickAction(EditManager editManager) {
		this.editManager = editManager;
	}
	
	public boolean init() {

		this.moveStartPoint = editManager.getSelectionPoint();
		
		//these are the objects that will move
		List<Object> selection = editManager.getSelection();
		
		//these lists are to display the move preview
		editManager.clearPending();
		List<EditObject> pendingObjects = editManager.getPendingObjects();
		List<EditNode> movingNodes = editManager.getMovingNodes();
		List<EditSegment> pendingSnapSegments = editManager.getPendingSnapSegments();
		
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
//		initMovingNodes();
		
		//clean up working data
		editMap = null;
//		editManager.getEditLayer().notifyContentChange();
		return true;
	}
	
	@Override
	public void mousePressed(Point2D mouseMerc, MouseEvent e) {
		
		EditDestPoint clickDestPoint = editManager.getDestinationPoint(mouseMerc);
		List<Object> selection = editManager.getSelection();
		
		if(!selection.isEmpty()) {
			//execute the move
			MoveEdit me = new MoveEdit(editManager.getOsmData());
			boolean success = me.selectionMoved(selection,moveStartPoint,clickDestPoint);
			
			if(success) {
				//if the move caused a node to be deleted, clear the selection
				//so we don't have the invalid object stroed there
				boolean nodeDeleted = me.getNodeDeleted();
				if(nodeDeleted) {
					editManager.clearSelection();
				}
				
				//update select point
				editManager.setSelectionPoint(clickDestPoint);
			}
		}
		
		//clean up and exit move mode
		editManager.clearPreview();
		editManager.clearPending();
		SelectEditorMode sem = editManager.getSelectEditorMode();
		sem.setSelectState();
		
//		editManager.getEditLayer().notifyContentChange();
	}
	
	private void initMovingNodes() {
		Point2D mouseMerc = editManager.getMousePointMerc();
//handle this better!!!
if(mouseMerc == null) {
	mouseMerc = moveStartPoint.point;
}
		double dx = mouseMerc.getX() - moveStartPoint.point.getX();
		double dy = mouseMerc.getY() - moveStartPoint.point.getY();

		List<EditNode> movingNodes = editManager.getMovingNodes();

		for(EditNode en:movingNodes) {
			//for a move, all nodes should be real so node should exist
			if(en.node != null) {
				Point2D nodePoint = en.node.getPoint();
				en.point.setLocation(nodePoint.getX() + dx, nodePoint.getY() + dy);
			}
		}
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
