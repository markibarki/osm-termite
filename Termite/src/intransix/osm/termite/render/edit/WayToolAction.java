package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.data.edit.EditDestPoint;
import intransix.osm.termite.map.data.edit.WayNodeEdit;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.Point2D;
import java.util.List;

/**
 *
 * @author sutter
 */
public class WayToolAction implements MouseEditAction {
	
	private OsmData osmData;
	private EditLayer editLayer;
	private FeatureInfo featureInfo;
	private OsmRelation activeLevel;
	private OsmWay activeWay;
	private boolean addToWayStart;
	private EditNode editNode;
	
	public void init(OsmData osmData, EditLayer editLayer) {
		this.osmData = osmData;
		this.editLayer = editLayer;

		this.activeLevel = editLayer.getActiveLevel();
		this.activeWay = editLayer.getActiveWay();
		this.featureInfo = editLayer.getFeatureInfo();
		
		//clear selection only if there is not an active way
		//otherwise the selection is the active way
		if(activeWay == null) {
			editLayer.clearSelection();
		}
		else {
			List<Integer> selectedWayNodes = editLayer.getSelectedWayNodes();
			if(selectedWayNodes.size() == 1) {
				int selectedIndex = selectedWayNodes.get(0);
				addToWayStart = (selectedIndex == 0);
			}
			else {
				addToWayStart = false;
			}
		}
		
		//initialize virtual node with a dummy point - it will get updated on a mouse move
		setPendingData(new Point2D.Double());
	}
	
	@Override
	public void updateMovingNodes(Point2D mouseMerc) {
		if(editNode != null) {
			editNode.point.setLocation(mouseMerc.getX(), mouseMerc.getY());
		}
	}
	
	@Override
	public void mousePressed(EditDestPoint clickDestPoint) {
	
		//execute a way node addition
		WayNodeEdit wne = new WayNodeEdit(osmData);
		activeWay = wne.wayToolClicked(activeWay,!addToWayStart,clickDestPoint,featureInfo,activeLevel);

		if(activeWay != null) {
			//make sure this is selected
			List<Object> selection = editLayer.getSelection();
			List<Integer> selectedWayNodes = editLayer.getSelectedWayNodes();
			
			if(selection.isEmpty()) {
				selection.add(activeWay);
			}
			//update the active node
			int activeWayNodeIndex;
			if(!addToWayStart) {
				activeWayNodeIndex = activeWay.getNodes().size()-1;
				selectedWayNodes.clear();
				selectedWayNodes.add(activeWayNodeIndex);
			}
			//prepare for next
			setPendingData(clickDestPoint.point);
		}
		else {
			//something wrong happened - clean up
			editLayer.clearSelection();
		}
	}
	
	@Override
	public void featureLayerUpdated(FeatureInfo featureInfo) {
		//no action
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
		if(activeWay != null) {
			List<OsmNode> nodes = activeWay.getNodes();
			
			if(!nodes.isEmpty()) {
				int nodeIndex = this.addToWayStart ? 0 : nodes.size() - 1;
				OsmNode activeNode = nodes.get(nodeIndex);
				EditNode en2 = new EditNode(activeNode);
				EditSegment es = new EditSegment(null,editNode,en2);
				pendingObjects.add(en2);
				pendingObjects.add(es);
				pendingSnapSegments.add(es);
			}
		}
	}
	
}
