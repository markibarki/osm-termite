package intransix.osm.termite.app.geocode;

import intransix.osm.termite.render.source.GeocodeLayer;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * This is a base class for geocoding mouse actions.
 * @author sutter
 */
public interface GeocodeMouseAction {
	
	/** This method is called when the mouse action is set on the layer. */
	public void init(GeocodeLayer geocodeLayer);
	
	/** This should return false if these if no move action. */
	public boolean doMove();
	
	/** This method is called when the mouse moves, if doMove() returns true. */
	public void mouseMoved(Point2D mouseMerc, MouseEvent e);
	
	/** This method is called when the mouse is clicked. */
	public void mousePressed(Point2D mouseMerc, MouseEvent e);
}
