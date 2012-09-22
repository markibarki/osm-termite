package intransix.osm.termite.app.edit;

import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.map.data.OsmData;
import java.awt.geom.Point2D;
import java.awt.event.MouseEvent;

/**
 *
 * @author sutter
 */
public interface MouseClickAction {
	
	public boolean init();
	
	public void mousePressed(Point2D mouseMerc, MouseEvent e);
}
