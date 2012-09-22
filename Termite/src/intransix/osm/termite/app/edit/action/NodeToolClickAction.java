package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.MouseClickAction;
import intransix.osm.termite.app.edit.editobject.EditObject;
import intransix.osm.termite.app.edit.editobject.EditNode;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.app.level.LevelManager;
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
public class NodeToolClickAction implements MouseClickAction {
	
	private EditManager editManager;
	private EditNode editNode;
	
	public NodeToolClickAction(EditManager editManager) {		
		this.editManager = editManager;
	}
	
	@Override
	public boolean init() {
		editManager.clearSelection();
		
		//load the pending data - the poitn is a dummy point. 
		//It will be overwritten on the first mouse move
		FeatureTypeManager featureTypeManager = editManager.getFeatureTypeManager();
		FeatureInfo featureInfo = featureTypeManager.getActiveFeatureType();
		setPendingData(new Point2D.Double(0,0),featureInfo);
		
		return true;
	}
	
	@Override
	public void mousePressed(Point2D mouseMerc, MouseEvent e) {
		
		//ignore any multi click
		if(e.getClickCount() > 1) {
			return;
		}
		
		EditDestPoint clickDestPoint = editManager.getDestinationPoint(mouseMerc);
		
		//process normal click
		OsmData osmData = editManager.getOsmData();
		FeatureTypeManager featureTypeManager = editManager.getFeatureTypeManager();
		LevelManager levelManager = editManager.getLevelManager();
		FeatureInfo featureInfo = featureTypeManager.getActiveFeatureType();
		OsmRelation activeLevel = levelManager.getSelectedLevel();
	
		//execute a node addition
		NodeCreateEdit nce = new NodeCreateEdit(osmData);
		OsmNode node = nce.nodeToolClicked(clickDestPoint,featureInfo,activeLevel);
		
		//prepare for next
		setPendingData(clickDestPoint.point,featureInfo);
	}
	
	private void setPendingData(Point2D pendingPoint, FeatureInfo featureInfo) {
		editManager.clearPending();
		
		//these lists are to display the move preview
		List<EditObject> pendingObjects = editManager.getPendingObjects();
		List<EditNode> movingNodes = editManager.getMovingNodes();
		
		editNode = new EditNode(pendingPoint,featureInfo);
		movingNodes.add(editNode);
		pendingObjects.add(editNode);
	}
	
}