package intransix.osm.termite.render;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Composite;

/**
 *
 * @author sutter
 */
public abstract class MapLayer {
	
	private String name;
	private boolean active = false;
	private boolean visible = true;
	private MapPanel mapPanel;
	private MapLayerManager mapLayerManager;
	private float alpha = 1;
	private Composite composite;	
	
	public MapLayer(MapLayerManager mapLayerManager) {
		this.mapLayerManager = mapLayerManager;
		this.mapPanel = mapLayerManager.getMapPanel();
	}
	
	public final MapPanel getMapPanel() {
		return mapPanel;
	}
	
	public void setName(String name) {
		this.name = name;
		mapLayerManager.layerStateChanged(this);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public void setActiveState(boolean active) {
		this.active = active;
		mapLayerManager.layerStateChanged(this);
	}
	
	public final boolean getActiveState() {
		return active;
	}
	
	public void setOpacity(float alpha) {
		if(alpha != 1) {
			this.alpha = alpha;
			composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		}
		else {
			this.alpha = 1;
			composite = null;
		}
	}
	
	public float getOpacity() {
		return alpha;
	}
	
	public abstract void render(Graphics2D g2);
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		mapLayerManager.layerStateChanged(this);
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	//==================
	// Protected Methods
	//==================
	
	Composite getComposite() {
		return composite;
	}
}