
package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.data.VirtualNode;
import intransix.osm.termite.app.edit.MouseClickAction;
import intransix.osm.termite.app.edit.editobject.EditVirtualNode;
import intransix.osm.termite.app.edit.editobject.EditObject;
import intransix.osm.termite.app.edit.editobject.EditSegment;
import intransix.osm.termite.app.edit.editobject.EditNode;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.edit.SelectEditorMode;
import intransix.osm.termite.app.level.LevelManager;
import intransix.osm.termite.app.edit.impl.EditDestPoint;
import intransix.osm.termite.app.edit.impl.WayNodeEdit;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.geom.Point2D;
import java.util.List;
import java.awt.event.MouseEvent;

/**
 *
 * @author sutter
 */
public class VirtualNodeClickAction implements MouseClickAction {

	private EditManager editManager;
	private LevelManager levelManager;
	private VirtualNode virtualNode;
	private EditVirtualNode editVirtualNode;
	
	public VirtualNodeClickAction(EditManager editManager) {
		this.editManager = editManager;
		this.levelManager = editManager.getLevelManager();
	}
	
	@Override
	public boolean init() {
		
		editManager.clearPending();
		
		//selection
		List<Object> selection = editManager.getSelection();
		
		//these lists are to display the move preview
		List<EditObject> pendingObjects = editManager.getPendingObjects();
		List<EditNode> movingNodes = editManager.getMovingNodes();
		List<EditSegment> pendingSnapSegments = editManager.getPendingSnapSegments();
		
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
			Point2D mouseMerc = editManager.getMousePointMerc();
			editVirtualNode.enVirtual.point.setLocation(mouseMerc.getX(), mouseMerc.getY());
			
			editManager.getEditLayer().notifyContentChange();
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public void mousePressed(Point2D mouseMerc, MouseEvent e) {
		if(virtualNode == null) return;
		
		EditDestPoint clickDestPoint = editManager.getDestinationPoint(mouseMerc);
		
		WayNodeEdit wne = new WayNodeEdit(editManager.getOsmData());
		wne.nodeInserted(virtualNode.segment,clickDestPoint,levelManager.getSelectedLevel());
		
		//clean up and exit move mode
		editManager.clearPreview();
		editManager.clearPending();
		SelectEditorMode sem = editManager.getSelectEditorMode();
		sem.setSelectState();
		
		editManager.getEditLayer().notifyContentChange();
	}
		
}
