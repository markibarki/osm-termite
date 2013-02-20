/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.app.maplayer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

/**
 *
 * @author sutter
 */
public abstract class CanvasLayer extends Canvas implements MapLayer, ChangeListener<Parent> {
	
	private String name;
	private boolean active = false;
	private float order;
	private double preferredAngleRadians = INVALID_ANGLE;	
	private MapLayerManager mapLayerManager;
	
	public CanvasLayer() {
		this.parentProperty().addListener(this);
	}
	
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
	
	@Override
	public void connect(MapLayerManager mapLayerManager) {
		
		this.mapLayerManager = mapLayerManager;
	}
	
	@Override
	public void disconnect(MapLayerManager mapLayerManager) {
		
		this.mapLayerManager = null;
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
	
	/** This listener is used to bind the layer to the parent size. */
	@Override
	public void changed(ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) {
		if(oldValue != null) {
			//bind the height and width to just fit the map pane
			this.heightProperty().unbind();
			this.widthProperty().unbind();
		}
		if((newValue != null)&&(newValue instanceof Pane)) {
			//bind the height and width to just fit the map pane
			//to be more general I could use the type Region instead of Pane, since that has the properties we need
			Pane pane = (Pane)newValue;
			this.heightProperty().bind(pane.heightProperty());
			this.widthProperty().bind(pane.widthProperty());
		}
	}
	
}


