package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.MouseMoveAction;
import intransix.osm.termite.app.edit.editobject.EditNode;
import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.map.workingdata.OsmData;
//import java.awt.event.MouseEvent;
//import java.awt.geom.Point2D;
import intransix.osm.termite.app.edit.impl.EditDestPoint;
import java.util.List;

/**
 * This is an unfortunate name but it follows the mouse move action naming conventions
 * used here. It is applicable for a Move (first move) edit operation, it executes the update
 * on the Moving nodes (second move) and it is a MouseMoveAction (third move).
 * 
 * @author sutter
 */
public class MoveMoveMoveAction implements MouseMoveAction {

	private EditManager editManager;
	
	public MoveMoveMoveAction(EditManager editManager) {
		this.editManager = editManager;
	}
	
	@Override
	public boolean init() {
		return true;
	}
	
//	@Override
//	public void mouseMoved(Point2D mouseMerc, double mercRadSq, MouseEvent e) {
//		EditDestPoint moveStartPoint = editManager.getSelectionPoint();
//		if(moveStartPoint != null) {
//			double dx = mouseMerc.getX() - moveStartPoint.point.getX();
//			double dy = mouseMerc.getY() - moveStartPoint.point.getY();
//			
//			List<EditNode> movingNodes = editManager.getMovingNodes();
//			
//			for(EditNode en:movingNodes) {
//				//for a move, all nodes should be real so node should exist
//				if(en.node != null) {
//					Point2D nodePoint = en.node.getPoint();
//					en.point.setLocation(nodePoint.getX() + dx, nodePoint.getY() + dy);
//				}
//			}
//		}
//		
//		editManager.getEditLayer().notifyContentChange();
//	}
}
