package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmData;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;

/**
 *
 * @author sutter
 */
public class CreateMoveMoveAction implements MouseMoveAction {
	
	private OsmData osmData;
	private EditLayer editLayer;
	private EditNode editNode;
	
	public boolean init(OsmData osmData, EditLayer editLayer) {
		this.osmData = osmData;
		this.editLayer = editLayer;
		List<EditNode> movingNodes = editLayer.getMovingNodes();
		if(movingNodes.size() == 1) {
			editNode = movingNodes.get(0);
			return true;
		}
		else {
			return false;
		}
	}
	
	public void mouseMoved(Point2D mouseMerc, double mercRadSq, MouseEvent e) {
		editNode.point.setLocation(mouseMerc.getX(), mouseMerc.getY());
	}
}
