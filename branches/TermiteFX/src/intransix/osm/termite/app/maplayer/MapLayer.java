package intransix.osm.termite.app.maplayer;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Pane;

/**
 *
 * @author sutter
 */
public class MapLayer extends Pane {
	
	public final static int ORDER_BASE_MAP_1 = 0;
	public final static int ORDER_BASE_MAP_2 = 1;
	public final static int ORDER_BASE_MAP_3 = 2;
	public final static int ORDER_EDIT_MAP = 3;
	public final static int ORDER_OVERLAY_1 = 4;
	public final static int ORDER_OVERLAY_2 = 5;
	public final static int ORDER_OVERLAY_3 = 6;
	public final static int ORDER_EDIT_MARKINGS = 7;
	
	public final static double INVALID_ANGLE = Double.MAX_VALUE;

	//--------------------
	// Methods from Node we will use
	//--------------------
	
	
	private StringProperty nameProperty = new SimpleStringProperty();
	private boolean active = false;
	private float order;
	private double preferredAngleRadians = INVALID_ANGLE;	
	private MapLayerManager mapLayerManager;
	
	
	public void setOrder(float order) {
		this.order = order;
	}
	
	public float getOrder() {
		return order;
	}
	
	public void setName(String name) {
		this.nameProperty.set(name);
	}
	
	public String getName() {
		return nameProperty.get();
	}
	
	public StringProperty nameProperty() {
		return nameProperty;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	public void setActiveState(boolean active) {
		this.active = active;
	}
	
	public final boolean getActiveState() {
		return active;
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
	
	public void connect(MapLayerManager mapLayerManager) {
	}
	
	public void disconnect(MapLayerManager mapLayerManager) {
	}	
	
}
