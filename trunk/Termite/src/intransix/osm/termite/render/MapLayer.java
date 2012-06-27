package intransix.osm.termite.render;

import java.awt.Graphics2D;

/**
 *
 * @author sutter
 */
public abstract class MapLayer {
	
	private boolean active;
	private boolean hidden;
	private float opacity;
	private MapPanel mapPanel;
	
	
	public void setMapPanel(MapPanel mapPanel) {
		this.mapPanel = mapPanel;
	}
	
	public final MapPanel getMapPanel() {
		return mapPanel;
	}
	
	public abstract void render(Graphics2D g2);
	
	public void setActiveState(boolean active) {
		this.active = active;
	}
	
	public final boolean getActiveState() {
		return active;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public final boolean getHidden() {
		return hidden;
	}
	
	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}
	
	public final float getOpacity() {
		return opacity;
	}
}
