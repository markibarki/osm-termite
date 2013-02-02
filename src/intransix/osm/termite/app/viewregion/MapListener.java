
package intransix.osm.termite.app.viewregion;

import intransix.osm.termite.render.MapPanel;

/**
 *
 * @author sutter
 */
public interface MapListener {
	public void onZoom(ViewRegionManager vrm);
	public void onPanStart(ViewRegionManager vrm);
	public void onPanEnd(ViewRegionManager vrm);
}
