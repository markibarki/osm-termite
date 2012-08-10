
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
public class VirtualNodeAction implements MouseEditAction {

	private OsmData osmData;
	private EditLayer editLayer;
	private OsmRelation activeLevel;
	private VirtualNode virtualNode;
	private EditVirtualNode editVirtualNode;
	
	@Override
	public void init(OsmData osmData, EditLayer editLayer) {
		this.osmData = osmData;
		this.editLayer = editLayer;
		
		this.activeLevel = editLayer.getActiveLevel();
		Point2D mouseMerc = editLayer.getMapPanel().getMousePointMerc();
		
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
		
		//set this initial position
		updateMovingNodes(mouseMerc);
	}
	
	@Override
	public void updateMovingNodes(Point2D mouseMerc) {
		if(editVirtualNode != null) {
			editVirtualNode.enVirtual.point.setLocation(mouseMerc.getX(), mouseMerc.getY());
		}
	}
	
	@Override
	public void mousePressed(EditDestPoint clickDestPoint) {
		if(virtualNode == null) return;
		
		WayNodeEdit wne = new WayNodeEdit(osmData);
		wne.nodeInserted(virtualNode.segment,clickDestPoint,activeLevel);

		//clear selection. user has to reselect to move again
		//but move mode is still active because it is controlled elsewhere
		editLayer.clearPending();
		editLayer.clearSelection();
	}
		
}
