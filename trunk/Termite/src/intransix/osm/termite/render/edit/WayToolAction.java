package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.data.edit.EditDestPoint;
import intransix.osm.termite.map.data.edit.WayNodeEdit;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.MouseEvent;

/**
 *
 * @author sutter
 */
public class WayToolAction implements MouseEditAction {
	
	private OsmData osmData;
	private EditLayer editLayer;
	private OsmWay activeWay;
	private OsmNode activeNode;
	private int insertIndex;
	
	private EditNode editNode;
	
	@Override
	public void init(OsmData osmData, EditLayer editLayer) {
		this.osmData = osmData;
		this.editLayer = editLayer;
		this.activeWay = editLayer.getWaySelection();
		if(activeWay != null) {		
			if(activeWay.isClosed()) {
				//don't allow adding to a closed way
				activeWay = null;
				activeNode = null;
			}
			else {
				List<OsmNode> nodes = activeWay.getNodes();
				//figure out which end to add to
				List<Integer> selectedWayNodes = editLayer.getSelectedWayNodes();
				int activeIndex = -1;
				if(selectedWayNodes.size() == 1) {
					activeIndex = selectedWayNodes.get(0);
					
				}
				//if the start is not selected, use the end
				if(activeIndex == 0) {
					activeNode = nodes.get(0);
					insertIndex = 0;
				}
				else {
					activeNode = nodes.get(nodes.size()-1);
					insertIndex = nodes.size();
				}
			}
		}
		else {
			editLayer.clearSelection();
		}
		
		//get the mouse location
		Point2D mercPoint = editLayer.getMapPanel().getMousePointMerc();
		setPendingData(mercPoint);
	}
	
	@Override
	public void updateMovingNodes(Point2D mouseMerc) {
		if(editNode != null) {
			editNode.point.setLocation(mouseMerc.getX(), mouseMerc.getY());
		}
	}
	
	@Override
	public void mousePressed(EditDestPoint clickDestPoint, MouseEvent e) {
		
		//on double click end the way and return
		if(e.getClickCount() > 1) {
			editLayer.resetWayEdit();
			return;
		}
	
		//process normal click
		FeatureInfo featureInfo = editLayer.getFeatureInfo();
		OsmRelation activeLevel = editLayer.getActiveLevel();
	
		//execute a way node addition
		WayNodeEdit wne = new WayNodeEdit(osmData);
		boolean success = wne.wayToolClicked(clickDestPoint,activeWay,insertIndex,activeNode,featureInfo,activeLevel);
		
		if(success) {
			//update the selection/pending state
			activeWay = wne.getActiveWay();
			activeNode = wne.getActiveNode();
			int activeIndex = -1;
			
			if(activeWay != null) {
				
				if(activeWay.isClosed()) {
					activeWay = null;
					activeNode = null;
					editLayer.resetWayEdit();
				}
				else {
					//make sure this is selected

					activeIndex = activeWay.getNodes().indexOf(activeNode);
					if(activeIndex == 0) {
						//insert at start
						insertIndex = 0;
					}
					else {
						//insert at end
						insertIndex = activeIndex+1;
					}
				}
			}
			
			//update selection
			List<Object> selection = new ArrayList<Object>();
			List<Integer> wayNodeSelection = null;
			if(activeWay != null) {
				selection.add(activeWay);
				if(activeIndex != -1) {
					wayNodeSelection = new ArrayList<Integer>();
					wayNodeSelection.add(activeIndex);
				}
			}
			else if(activeNode != null) {
				selection.add(activeNode);
			}
			editLayer.setSelection(selection, wayNodeSelection);

			//update the click location
			setPendingData(clickDestPoint.point);
			
		}
		else {
			//something wrong happened - clean up
//			editLayer.clearSelection();
		}
	}
	
	private void setPendingData(Point2D pendingPoint) {
		editLayer.clearPending();
		
		//these lists are to display the move preview
		List<EditObject> pendingObjects = editLayer.getPendingObjects();
		List<EditNode> movingNodes = editLayer.getMovingNodes();
		List<EditSegment> pendingSnapSegments = editLayer.getPendingSnapSegments();
		
		//get the node to add
		editNode = new EditNode(pendingPoint,null);
		movingNodes.add(editNode);
		pendingObjects.add(editNode);
		//get the segment from the previous node
		if(activeNode != null) {

			EditNode en2 = new EditNode(activeNode);

			EditSegment es = new EditSegment(null,editNode,en2);
			pendingObjects.add(en2);
			pendingObjects.add(es);
			pendingSnapSegments.add(es);
			
			if(activeWay != null) {
				List<OsmNode> nodes = activeWay.getNodes();
				//get a segment to close the way
				if(nodes.size() > 2) {
					int endIndex = nodes.size() - 1 - nodes.indexOf(activeNode);
					OsmNode endNode = nodes.get(endIndex);
					EditNode enClose = new EditNode(endNode);
					EditSegment esClose = new EditSegment(null,editNode,enClose);
					pendingSnapSegments.add(esClose);
				}
			}
		}
	}
	
}
