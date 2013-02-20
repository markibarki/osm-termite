package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.MouseMoveAction;
import intransix.osm.termite.app.edit.editobject.EditNode;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.map.workingdata.OsmData;
//import java.awt.event.MouseEvent;
//import java.awt.geom.Point2D;
import java.util.List;

/**
 *
 * @author sutter
 */
public class CreateMoveMoveAction implements MouseMoveAction {

	private EditManager editManager;
	private EditNode editNode;
	
	public CreateMoveMoveAction(EditManager editManager) {
		this.editManager = editManager;
	}
	
	@Override
	public boolean init() {

		List<EditNode> movingNodes = editManager.getMovingNodes();
		if(movingNodes.size() == 1) {
			editNode = movingNodes.get(0);
			editManager.getEditLayer().notifyContentChange();
			return true;
		}
		else {
			return false;
		}
	}
	
//	@Override
//	public void mouseMoved(Point2D mouseMerc, double mercRadSq, MouseEvent e) {
//		List<EditNode> movingNodes = editManager.getMovingNodes();
//		if(movingNodes.size() == 1) {
//			editNode = movingNodes.get(0);
//			editNode.point.setLocation(mouseMerc.getX(), mouseMerc.getY());
//			editManager.getEditLayer().notifyContentChange();
//		}
//	}
}
