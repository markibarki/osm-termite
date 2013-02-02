package intransix.osm.termite.app.viewregion;

import java.awt.geom.AffineTransform;

/**
 *
 * @author sutter
 */
public interface LocalCoordinateListener {
	void onLocalCoordinateChange(ViewRegionManager vrm, AffineTransform oldLocalToNewLocal);
}
