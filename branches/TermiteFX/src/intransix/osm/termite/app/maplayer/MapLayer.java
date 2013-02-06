package intransix.osm.termite.app.maplayer;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Composite;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.render.MapPanel;
import javafx.scene.layout.Pane;

/**
 *
 * @author sutter
 */
public abstract class MapLayer extends Pane {
	
	public final static int ORDER_BASE_MAP_1 = 0;
	public final static int ORDER_BASE_MAP_2 = 1;
	public final static int ORDER_BASE_MAP_3 = 2;
	public final static int ORDER_EDIT_MAP = 3;
	public final static int ORDER_OVERLAY_1 = 4;
	public final static int ORDER_OVERLAY_2 = 5;
	public final static int ORDER_OVERLAY_3 = 6;
	public final static int ORDER_EDIT_MARKINGS = 7;
	
	public final static double INVALID_ANGLE = Double.MAX_VALUE;
	
	private String name;
	private boolean active = false;
	private boolean visible = true;
	private float alpha = 1;
	private float order;
	private double preferredAngleRadians = INVALID_ANGLE;
	private Composite composite;	
	
	//to distribute change events
	MapLayerManager mapLayerManager;
	//the element that controls the display coordinates
	private ViewRegionManager viewRegionManager;
	//the ui element wyich displays the layers. Needed to connect mouse interactions.
	private MapPanel mapPanel;
	
	public ViewRegionManager getViewRegionManager() {
		return viewRegionManager;
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
		if(mapLayerManager != null) mapLayerManager.notifyStateChange(this);
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
		if(mapLayerManager != null) mapLayerManager.notifyStateChange(this);
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
		if(mapLayerManager != null) mapLayerManager.notifyStateChange(this);
	}
	
//	public float getOpacity() {
//		return alpha;
//	}
		
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
	
//	public void setVisible(boolean visible) {
//		this.visible = visible;
//		if(mapLayerManager != null) mapLayerManager.notifyStateChange(this);
//	}
//	
//	public boolean isVisible() {
//		return visible;
//	}
	
	/** This method notifies any listeners the a mode enabled changed. */
	public void notifyContentChange() {
		if(mapLayerManager != null) mapLayerManager.notifyContentChange(this);
	}
	
	//=================================
	// Package Methods
	//=================================
	
	/** This method connects the layer to the map panel. */
	void connect(MapLayerManager mapLayerManager,
			ViewRegionManager viewRegionManager,
			MapPanel mapPanel) {
		this.mapLayerManager = mapLayerManager;
		this.viewRegionManager = viewRegionManager;
		this.mapPanel = mapPanel;
	}
	
	//=================================
	// Private Methods
	//=================================
	
	
	
}
