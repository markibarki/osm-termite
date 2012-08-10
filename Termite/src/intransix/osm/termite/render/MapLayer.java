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
	private boolean hidden = false;
	private MapPanel mapPanel;
	private float alpha = 1;
	private Composite composite;	
	
	public void setMapPanel(MapPanel mapPanel) {
		this.mapPanel = mapPanel;
	}
	
	public final MapPanel getMapPanel() {
		return mapPanel;
	}
	
	public void setName(String name) {
		this.name = name;
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
		if(mapPanel != null) {
			mapPanel.layerStateChanged(this);
		}
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
	
	public boolean canDelete () {
		return false;
	}
	
	public boolean canHide() {
		return true;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
		if(mapPanel != null) {
			mapPanel.layerStateChanged(this);
		}
	}
	
	public boolean getHidden() {
		return hidden;
	}
	
	public boolean hasSettings() {
		return false;
	}
	
	public void showSettings() {
	}
		
	
	//==================
	// Protected Methods
	//==================
	
	Composite getComposite() {
		return composite;
	}
}
