package intransix.osm.termite.render;

import java.awt.geom.AffineTransform;

/**
 *
 * @author sutter
 */
public interface LocalCoordinateListener {
	void onLocalCoordinateChange(MapPanel mapPanel, AffineTransform oldLocalToNewLocal);
}
