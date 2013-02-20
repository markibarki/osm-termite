package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.app.edit.MouseClickAction;
import intransix.osm.termite.app.edit.editobject.EditObject;
import intransix.osm.termite.app.edit.editobject.EditSegment;
import intransix.osm.termite.app.edit.editobject.EditNode;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.app.level.LevelManager;
import intransix.osm.termite.app.edit.impl.EditDestPoint;
import intransix.osm.termite.app.edit.impl.WayNodeEdit;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.List;
import java.util.ArrayList;
//import java.awt.event.MouseEvent;
//import javax.swing.JOptionPane;
//import java.awt.geom.Point2D;

/**
 *
 * @author sutter
 */
public class WayToolClickAction implements MouseClickAction {
	
	private EditManager editManager;
	private OsmWay activeWay;
	private OsmNode activeNode;
	private int insertIndex;
	
	private EditNode editNode;
	
	public WayToolClickAction(EditManager editManager) {	
		this.editManager = editManager;
	}
	
	@Override
	public boolean init() {
		
		//check for an active way
		List<Object> selection = editManager.getSelection();		
		if(selection.size() == 1) {
			Object selected = selection.get(0);
			if(selected instanceof OsmWay) {
				activeWay = (OsmWay)selected;
			}
		}

		if(activeWay != null) {		
			if(activeWay.isClosed()) {
				//don't allow adding to a closed way
				activeWay = null;
				activeNode = null;
			}
			else {
				List<OsmNode> nodes = activeWay.getNodes();
				//figure out which end to add to
				List<Integer> selectedWayNodes = editManager.getSelectedWayNodes();
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
			editManager.clearSelection();
		}
		
		//get the mouse location
//		Point2D mercPoint = editManager.getMousePointMerc();
//		setPendingData(mercPoint);
		
		editManager.getEditLayer().notifyContentChange();
		
		return true;
	}
	
//	@Override
//	public void mousePressed(Point2D mouseMerc, MouseEvent e) {
//		
//		EditDestPoint clickDestPoint = editManager.getDestinationPoint(mouseMerc);
//		
//		//on double click end the way and return
//		if(e.getClickCount() > 1) {
//			resetWayEdit();
//			return;
//		}
//	
//		//process normal click
//		FeatureTypeManager featureTypeManager = editManager.getFeatureTypeManager();
//		LevelManager levelManager = editManager.getLevelManager();
//		FeatureInfo featureInfo = featureTypeManager.getActiveFeatureType();
//		OsmRelation activeLevel = levelManager.getSelectedLevel();
//		
//		if(featureInfo == null) {
//			JOptionPane.showMessageDialog(null,"You must select a feature type before you create a way");
//			return;
//		}
//	
//		//execute a way node addition
//		WayNodeEdit wne = new WayNodeEdit(editManager.getOsmData());
//		boolean success = wne.wayToolClicked(clickDestPoint,activeWay,insertIndex,activeNode,featureInfo,activeLevel);
//		
//		if(success) {
//			//update the selection/pending state
//			activeWay = wne.getActiveWay();
//			activeNode = wne.getActiveNode();
//			int activeIndex = -1;
//			
//			if(activeWay != null) {
//				
//				if(activeWay.isClosed()) {
//					activeWay = null;
//					activeNode = null;
//					resetWayEdit();
//				}
//				else {
//					//make sure this is selected
//
//					activeIndex = activeWay.getNodes().indexOf(activeNode);
//					if(activeIndex == 0) {
//						//insert at start
//						insertIndex = 0;
//					}
//					else {
//						//insert at end
//						insertIndex = activeIndex+1;
//					}
//				}
//			}
//			
//			//update selection
//			List<Object> selection = new ArrayList<Object>();
//			List<Integer> wayNodeSelection = null;
//			if(activeWay != null) {
//				selection.add(activeWay);
//				if(activeIndex != -1) {
//					wayNodeSelection = new ArrayList<Integer>();
//					wayNodeSelection.add(activeIndex);
//				}
//			}
//			else if(activeNode != null) {
//				selection.add(activeNode);
//			}
//			editManager.setSelection(selection, wayNodeSelection);
//
//			//update the click location
//			setPendingData(clickDestPoint.point);
//			
//		}
//		else {
//			//something wrong happened - clean up
////			editLayer.clearSelection();
//		}
//		
//		editManager.getEditLayer().notifyContentChange();
//	}
//	
//	private void setPendingData(Point2D pendingPoint) {
//		editManager.clearPending();
//		
//		//these lists are to display the move preview
//		List<EditObject> pendingObjects = editManager.getPendingObjects();
//		List<EditNode> movingNodes = editManager.getMovingNodes();
//		List<EditSegment> pendingSnapSegments = editManager.getPendingSnapSegments();
//		
//		//get the node to add
//		editNode = new EditNode(pendingPoint,null);
//		movingNodes.add(editNode);
//		pendingObjects.add(editNode);
//		//get the segment from the previous node
//		if(activeNode != null) {
//
//			EditNode en2 = new EditNode(activeNode);
//
//			EditSegment es = new EditSegment(null,editNode,en2);
//			pendingObjects.add(en2);
//			pendingObjects.add(es);
//			pendingSnapSegments.add(es);
//			
//			if(activeWay != null) {
//				List<OsmNode> nodes = activeWay.getNodes();
//				//get a segment to close the way
//				if(nodes.size() > 2) {
//					int endIndex = nodes.size() - 1 - nodes.indexOf(activeNode);
//					OsmNode endNode = nodes.get(endIndex);
//					EditNode enClose = new EditNode(endNode);
//					EditSegment esClose = new EditSegment(null,editNode,enClose);
//					pendingSnapSegments.add(esClose);
//				}
//			}
//		}
//	}
	
	private void resetWayEdit() {
		editManager.getWayEditorMode().resetWayEdit();
	}
	
}
