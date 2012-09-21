package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.OsmNode;
import intransix.osm.termite.map.data.OsmWay;
import intransix.osm.termite.map.data.edit.EditDestPoint;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sutter
 */
public class SelectClickAction implements MouseClickAction {
	
	private EditLayer editLayer;
	
	public boolean init(OsmData osmData, EditLayer editLayer) {
		this.editLayer = editLayer;
		return true;
	}
	
	public void mousePressed(EditDestPoint clickDestPoint, MouseEvent e) {
		//do a selection with the press
		
		//get the preview objects, so it can be added to the selection
		List<SnapObject> snapObjects = editLayer.getSnapObjects();
		int activeSnapObject = editLayer.getActiveSnapObject();
				
		//store the latest point used for selection, for the move anchor
		EditDestPoint selectionPoint = clickDestPoint;

		Object selectObject = null;

		//do a selection
		if((activeSnapObject > -1)&&(activeSnapObject < snapObjects.size())) {	

			//a little preprocessing
			SnapObject snapObject = snapObjects.get(activeSnapObject);
			selectionPoint.point = snapObject.snapPoint;
			if(snapObject instanceof SnapNode) {
				//use the node point as the start point
				selectionPoint.snapNode = ((SnapNode)snapObject).node;
			}

			//get the edit object for this snap object
			selectObject = snapObject.getSelectObject();
		}
		editLayer.setSelectionPoint(selectionPoint);

		boolean wasVirtualNode = editLayer.getVirtualNodeSelected();
		boolean isVirtualNode = selectObject instanceof VirtualNode;

		//handle selection

		//check normal select or select node in a way
		List<Object> selection = editLayer.getSelection();
		List<Integer> selectedWayNodes = editLayer.getSelectedWayNodes();
		
		
		
		OsmWay selectWay = null;

//clean up this logic
		if(selection.size() == 1) {
			Object selected = selection.get(0);
			if(selected instanceof OsmWay) selectWay = (OsmWay)selected;
		}

		if((selectWay != null)&&(selectObject instanceof OsmNode)&&
				(selectWay.getNodes().contains((OsmNode)selectObject))) {

			//select a node within a way
			int selectedIndex = selectWay.getNodes().indexOf((OsmNode)selectObject);
			if(e.isShiftDown()) {
				if(!selectedWayNodes.contains(selectedIndex)) {
					selectedWayNodes.add(selectedIndex);
				}
				else {
					selectedWayNodes.remove(selectedIndex);
				}
			}
			else {
				selectedWayNodes.clear();
				selectedWayNodes.add(selectedIndex);
			}
		}
		else {
			//normal select action

			//if shift is down do add/remove rather than replace selection
			//except do not allow virtual nodes to be selected with anything else
			boolean doAddRemove = ((e.isShiftDown())&&
					(!isVirtualNode)&&(!wasVirtualNode));

			if(doAddRemove) {
				if(selectObject != null) {
					if(selection.contains(selectObject)) {
						selection.remove(selectObject);
					}
					else {
						selection.add(selectObject);
					}
				}
			}
			else {
				selection.clear();
				if((selectObject != null)&&(!selection.contains(selectObject))) {
					selection.add(selectObject);
				}
			}

			//make sure selected nodes cleared
			selectedWayNodes.clear();
		}

		editLayer.setVirtualNodeSelected(isVirtualNode);

		//report selection
		editLayer.setSelection(selection, selectedWayNodes);
		editLayer.notifyContentChange();
	}
}
