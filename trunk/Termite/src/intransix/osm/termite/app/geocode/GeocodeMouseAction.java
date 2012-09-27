package intransix.osm.termite.app.geocode;

import intransix.osm.termite.render.source.GeocodeLayer;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 *
 * @author sutter
 */
public interface GeocodeMouseAction {
	public void init(GeocodeLayer geocodeLayer);
	
	/** This should return false if these if no move action. */
	public boolean doMove();
	
	public void mouseMoved(Point2D mouseMerc, MouseEvent e);
	public void mousePressed(Point2D mouseMerc, MouseEvent e);
}
