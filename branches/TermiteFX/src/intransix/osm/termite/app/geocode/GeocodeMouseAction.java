package intransix.osm.termite.app.geocode;

import intransix.osm.termite.render.source.GeocodeLayer;
import java.awt.geom.Point2D;
import javafx.scene.input.MouseEvent;

/**
 * This is a base class for geocoding mouse actions.
 * @author sutter
 */
public interface GeocodeMouseAction {
	
	/** This should return false if these if no move action. */
	public boolean doMove();
	
	/** This method is called when the mouse moves, if doMove() returns true. */
	public void mouseMoved(Point2D mouseMerc, double mercPerPixelsScale, MouseEvent e);
	
	/** This method is called when the mouse is clicked. */
	public void mousePressed(Point2D mouseMerc, double mercPerPixelsScale, MouseEvent e);
}
