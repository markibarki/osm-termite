package intransix.osm.termite.render;

import java.awt.Graphics2D;

/**
 *
 * @author sutter
 */
public interface MapLayer {
	
	public void setMapPanel(MapPanel mapPanel);
	
	public void render(Graphics2D g2);
}
