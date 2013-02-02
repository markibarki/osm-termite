package intransix.osm.termite.render.map;

import java.awt.geom.AffineTransform;

/**
 *
 * @author sutter
 */
public interface Feature {
	void transform(AffineTransform oldLocalToNewLocal);
}
