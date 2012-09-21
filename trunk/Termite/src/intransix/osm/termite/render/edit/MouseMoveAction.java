package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmData;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 *
 * @author sutter
 */
public interface MouseMoveAction {
	
	public boolean init(OsmData osmData, EditLayer editLayer);
	
	public void mouseMoved(Point2D mouseMerc, double mercRadSq, MouseEvent e);
}
