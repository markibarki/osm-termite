
package intransix.osm.termite.app.viewregion;

/**
 *
 * @author sutter
 */
public interface MapListener {
	public void onZoom(ViewRegionManager vrm);
	public void onPanStart(ViewRegionManager vrm);
	public void onPanStep(ViewRegionManager vrm);
	public void onPanEnd(ViewRegionManager vrm);
}
