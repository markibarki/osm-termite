package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.edit.EditDestPoint;
import java.awt.geom.Point2D;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.awt.event.MouseEvent;

/**
 *
 * @author sutter
 */
public interface MouseEditAction {
	
	public void init(OsmData osmData, EditLayer editLayer);
	
	public void updateMovingNodes(Point2D mouseMerc);
	
	public void mousePressed(EditDestPoint clickDestPoint, MouseEvent e);
}
