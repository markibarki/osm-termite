package intransix.osm.termite.app.maplayer;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Composite;
import java.util.ArrayList;
import java.util.List;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.render.MapPanel;

/**
 *
 * @author sutter
 */
public abstract class MapLayer {
	
	public final static double INVALID_ANGLE = Double.MAX_VALUE;
	
	private String name;
	private boolean active = false;
	private boolean visible = true;
	private float alpha = 1;
	private float order;
	private double preferredAngleRadians = INVALID_ANGLE;
	private Composite composite;	
	
	private List<MapLayerListener> layerListeners = new ArrayList<MapLayerListener>();
	//the element that controls the display coordinates
	private ViewRegionManager viewRegionManager;
	//the ui element wyich displays the layers. Needed to connect mouse interactions.
	private MapPanel mapPanel;
	
	public void setViewRegionManager(ViewRegionManager viewRegionManager) {
		this.viewRegionManager = viewRegionManager;
	}
	
	public ViewRegionManager getViewRegionManager() {
		return viewRegionManager;
	}
	
	public void setMapPanel(MapPanel mapPanel) {
		this.mapPanel = mapPanel;
	}
	
	public MapPanel getMapPanel() {
		return mapPanel;
	}
	
	public void setOrder(float order) {
		this.order = order;
	}
	
	public float getOrder() {
		return order;
	}
	
	public void setName(String name) {
		this.name = name;
		notifyStateChange();
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
		notifyStateChange();
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
		notifyStateChange();
	}
	
	public float getOpacity() {
		return alpha;
	}
		
	public Composite getComposite() {
		return composite;
	}
	
	public boolean hasPreferredAngle() {
		return (preferredAngleRadians != INVALID_ANGLE);
	}
	
	public double getPrerredAngleRadians() {
		return preferredAngleRadians;
	}
	
	/** This method sets the preferred frame of reference angle for this layer. 
	 * By default there is no preferred angle. To explicitly set no preferred
	 * angle, use the value preferredAngleRadians = INVALID_ANGLE;
	 * 
	 * @param preferredAngleRadians 
	 */
	public void setPreferredAngleRadians(double preferredAngleRadians) {
		this.preferredAngleRadians = preferredAngleRadians;
	}
	
	public abstract void render(Graphics2D g2);
	
	public void setVisible(boolean visible) {
		this.visible = visible;
		notifyStateChange();
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	/** This method adds a mode listener. */
	public void addLayerListener(MapLayerListener layerListener) {
		if(!layerListeners.contains(layerListener)) {
			layerListeners.add(layerListener);
		}
	}
	
	/** This method removes a mode listener. */
	public void removeLayerListener(MapLayerListener layerListener) {
		layerListeners.remove(layerListener);
	}
	
	/** This method notifies any listeners the a mode enabled changed. */
	public void notifyContentChange() {
		for(int i = 0; i < layerListeners.size(); i++) {
			MapLayerListener layerListener = layerListeners.get(i);
			layerListener.layerContentChanged(this);
		}
	}
	
	//=================================
	// Private Methods
	//=================================
	
	private void notifyStateChange() {
		for(int i = 0; i < layerListeners.size(); i++) {
			MapLayerListener layerListener = layerListeners.get(i);
			layerListener.layerStateChanged(this);
		}
	}
	
}
