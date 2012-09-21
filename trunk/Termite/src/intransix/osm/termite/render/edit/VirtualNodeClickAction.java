
package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.data.edit.EditDestPoint;
import intransix.osm.termite.map.data.edit.WayNodeEdit;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.Point2D;
import java.util.List;
import java.awt.event.MouseEvent;

/**
 *
 * @author sutter
 */
public class VirtualNodeClickAction implements MouseClickAction {

	private OsmData osmData;
	private EditLayer editLayer;
	private OsmRelation activeLevel;
	private VirtualNode virtualNode;
	private EditVirtualNode editVirtualNode;
	
	@Override
	public boolean init(OsmData osmData, EditLayer editLayer) {
		this.osmData = osmData;
		this.editLayer = editLayer;
		
		this.activeLevel = editLayer.getActiveLevel();
		
		editLayer.clearPending();
		
		//selection
		List<Object> selection = editLayer.getSelection();
		
		//these lists are to display the move preview
		List<EditObject> pendingObjects = editLayer.getPendingObjects();
		List<EditNode> movingNodes = editLayer.getMovingNodes();
		List<EditSegment> pendingSnapSegments = editLayer.getPendingSnapSegments();
		
		//there should be one and it should be a virtual node
		for(Object selectObject:selection) {
			if(selectObject instanceof VirtualNode) {
				editVirtualNode = new EditVirtualNode((VirtualNode)selectObject);
				movingNodes.add(editVirtualNode.enVirtual);
				pendingObjects.add(editVirtualNode.enVirtual);
				pendingObjects.add(editVirtualNode.es1);
				pendingObjects.add(editVirtualNode.es2);
				pendingSnapSegments.add(editVirtualNode.es1);
				pendingSnapSegments.add(editVirtualNode.es2);
				
				this.virtualNode = (VirtualNode)selectObject;
			}
		}
		
		if(virtualNode != null) {
			//set initial position
			Point2D mouseMerc = editLayer.getMapPanel().getMousePointMerc();
			editVirtualNode.enVirtual.point.setLocation(mouseMerc.getX(), mouseMerc.getY());
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public void mousePressed(EditDestPoint clickDestPoint, MouseEvent e) {
		if(virtualNode == null) return;
		
		WayNodeEdit wne = new WayNodeEdit(osmData);
		wne.nodeInserted(virtualNode.segment,clickDestPoint,activeLevel);

		//clear the move edit
		editLayer.clearMoveEdit();
	}
		
}
