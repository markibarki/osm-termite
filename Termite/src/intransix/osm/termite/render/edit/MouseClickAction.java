package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.edit.EditDestPoint;
import java.awt.event.MouseEvent;

/**
 *
 * @author sutter
 */
public interface MouseClickAction {
	
	public boolean init(OsmData osmData, EditLayer editLayer);
	
	public void mousePressed(EditDestPoint clickDestPoint, MouseEvent e);
}
