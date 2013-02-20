package intransix.osm.termite.app.maplayer;

import javafx.scene.layout.Pane;

/**
 *
 * @author sutter
 */
public interface MapLayer {
	
	public final static int ORDER_BASE_MAP_1 = 0;
	public final static int ORDER_BASE_MAP_2 = 1;
	public final static int ORDER_BASE_MAP_3 = 2;
	public final static int ORDER_EDIT_MAP = 3;
	public final static int ORDER_OVERLAY_1 = 4;
	public final static int ORDER_OVERLAY_2 = 5;
	public final static int ORDER_OVERLAY_3 = 6;
	public final static int ORDER_EDIT_MARKINGS = 7;
	
	public final static double INVALID_ANGLE = Double.MAX_VALUE;
	

	
	public float getOrder();
	
	public String getName();
	
	public void setActiveState(boolean active);
	
	public boolean getActiveState();
	
	public void setOpacity(double alpha);
	
	public double getOpacity();
	
	public boolean hasPreferredAngle();
	
	public double getPrerredAngleRadians();
	
	public void connect(MapLayerManager mapLayerManager);
	
	public void disconnect(MapLayerManager mapLayerManager);

	
}
