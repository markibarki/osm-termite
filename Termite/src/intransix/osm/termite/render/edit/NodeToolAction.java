/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.data.edit.EditDestPoint;
import intransix.osm.termite.map.data.edit.NodeCreateEdit;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.Point2D;
import java.util.List;
import java.awt.event.MouseEvent;

/**
 *
 * @author sutter
 */
public class NodeToolAction implements MouseEditAction {
	
	private OsmData osmData;
	private EditLayer editLayer;
	private EditNode editNode;
	
	public void init(OsmData osmData, EditLayer editLayer) {
		this.osmData = osmData;
		this.editLayer = editLayer;
		
		editLayer.clearSelection();
		
		//load the pending data - the poitn is a dummy point. 
		//It will be overwritten on the first mouse move
		setPendingData(new Point2D.Double(0,0));
	}
	
	@Override
	public void updateMovingNodes(Point2D mouseMerc) {
		if(editNode != null) {
			editNode.point.setLocation(mouseMerc.getX(), mouseMerc.getY());
		}
	}
	
	@Override
	public void mousePressed(EditDestPoint clickDestPoint, MouseEvent e) {
		
		//ignore any multi click
		if(e.getClickCount() > 1) {
			return;
		}
		
		//process normal click
		FeatureInfo featureInfo = editLayer.getFeatureInfo();
		OsmRelation activeLevel = editLayer.getActiveLevel();
	
		//execute a node addition
		NodeCreateEdit nce = new NodeCreateEdit(osmData);
		OsmNode node = nce.nodeToolClicked(clickDestPoint,featureInfo,activeLevel);
		
		//prepare for next
		setPendingData(clickDestPoint.point);
	}
	
	@Override
	public void featureLayerUpdated(FeatureInfo featureInfo) {
		if(editNode != null) {
			editNode.featureInfo = featureInfo;
		}
	}
	
	private void setPendingData(Point2D pendingPoint) {
		editLayer.clearPending();
		
		//these lists are to display the move preview
		List<EditObject> pendingObjects = editLayer.getPendingObjects();
		List<EditNode> movingNodes = editLayer.getMovingNodes();
		
		FeatureInfo featureInfo = editLayer.getFeatureInfo();
		
		editNode = new EditNode(pendingPoint,featureInfo);
		movingNodes.add(editNode);
		pendingObjects.add(editNode);
	}
	
}
