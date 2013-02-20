/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.app.maplayer;

import javafx.scene.layout.Pane;

/**
 *
 * @author sutter
 */
public abstract class PaneLayer extends Pane implements MapLayer {
	
	private String name;
	private boolean active = false;
	private float order;
	private double preferredAngleRadians = INVALID_ANGLE;	
	private MapLayerManager mapLayerManager;
	
	
	public void setOrder(float order) {
		this.order = order;
	}
	
	@Override
	public float getOrder() {
		return order;
	}
	
	public void setName(String name) {
		this.name = name;
		if(mapLayerManager != null) mapLayerManager.notifyStateChange(this);
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public void setActiveState(boolean active) {
		this.active = active;
		if(mapLayerManager != null) mapLayerManager.notifyStateChange(this);
	}
	
	@Override
	public final boolean getActiveState() {
		return active;
	}
	
	@Override
	public boolean hasPreferredAngle() {
		return (preferredAngleRadians != INVALID_ANGLE);
	}
	
	@Override
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
	
}

