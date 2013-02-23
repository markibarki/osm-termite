
package intransix.osm.termite.app.viewregion;

import java.awt.geom.AffineTransform;
import javafx.scene.transform.Affine;

/**
 *
 * @author sutter
 */
public interface MapListener {
	public void onZoom(ViewRegionManager vrm);
	public void onPanStart(ViewRegionManager vrm);
	public void onPanStep(ViewRegionManager vrm);
	public void onPanEnd(ViewRegionManager vrm);
	
	public void onLocalCoordinateSet(AffineTransform mercToLocal, Affine localToMercFX);
}
