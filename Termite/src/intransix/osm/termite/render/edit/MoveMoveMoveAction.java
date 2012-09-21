package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmData;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import intransix.osm.termite.map.data.edit.EditDestPoint;
import java.util.List;

/**
 * This is an unfortunate name but it follows the mouse move action naming conventions
 * used here. It is applicable for a Move (first move) edit operation, it executes the update
 * on the Moving nodes (second move) and it is a MouseMoveAction (third move).
 * 
 * @author sutter
 */
public class MoveMoveMoveAction implements MouseMoveAction {
	private OsmData osmData;
	private EditLayer editLayer;
	
	public boolean init(OsmData osmData, EditLayer editLayer) {
		this.osmData = osmData;
		this.editLayer = editLayer;
		return true;
	}
	
	public void mouseMoved(Point2D mouseMerc, double mercRadSq, MouseEvent e) {
		EditDestPoint moveStartPoint = editLayer.getSelectionPoint();
		if(moveStartPoint != null) {
			double dx = mouseMerc.getX() - moveStartPoint.point.getX();
			double dy = mouseMerc.getY() - moveStartPoint.point.getY();
			
			List<EditNode> movingNodes = editLayer.getMovingNodes();
			
			for(EditNode en:movingNodes) {
				//for a move, all nodes should be real so node should exist
				if(en.node != null) {
					Point2D nodePoint = en.node.getPoint();
					en.point.setLocation(nodePoint.getX() + dx, nodePoint.getY() + dy);
				}
			}
		}
	}
}
