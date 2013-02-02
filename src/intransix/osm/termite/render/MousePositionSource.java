package intransix.osm.termite.render;

import java.awt.geom.Point2D;

/**
 * This interface is for retrieving the location of the mouse on the screen. 
 * 
 * @author sutter
 */
public interface MousePositionSource {
	
	/** This method gets the location of the mouse in Mercator coordinates. It should
	 * be used if the mouse is needed outside of a mouse event. */
	Point2D getMousePointMerc();
	
	/** This method gets the location of the mouse in Pixel coordinates. It should
	 * be used if the mouse is needed outside of a mouse event. */
	Point2D getMousePointPix();
}
