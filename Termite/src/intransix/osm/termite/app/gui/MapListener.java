
package intransix.osm.termite.app.gui;

/**
 *
 * @author sutter
 */
public interface MapListener {
	public void onZoom(double zoomScale);
	public void onPanStart();
	public void onPanEnd();
}
