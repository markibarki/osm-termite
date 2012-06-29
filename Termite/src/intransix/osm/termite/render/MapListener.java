
package intransix.osm.termite.render;

/**
 *
 * @author sutter
 */
public interface MapListener {
	public void onZoom(MapPanel mapPanel);
	public void onPanStart(MapPanel mapPanel);
	public void onPanEnd(MapPanel mapPanel);
}
