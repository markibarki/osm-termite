package intransix.osm.termite.util;

/**
 * This is a local Mercator coordinate system with a scale of meters. To use
 * this, set the anchor point, which transform to (lcoal x, local y)=(0,0). Once the anchor point is
 * set, it should not be altered unless all data using the former coordinates is deleted.
 * In these coordinates, a change in one unit in the x or y direction will be one meter 
 * in distance, for areas near the anchor point. Further away, the points will scale
 * like Mercator Coordinates. However, over a distance on the order of miles this is negligible.
 * 
 * Local coordinates are helpful because of numerical problems dealing with very large offsets
 * from global coordinates.
 * 
 * @author sutter
 */
public class LocalCoordinates {
	
	//===============
	// Properties
	//===============
	private static double mxOffset = 0;
	private static double myOffset = 0;
	private static double mercToMetersScale = 1;
	
	//===============
	// Public Methods
	//===============
	public static double getMetersPerMerc() {
		return mercToMetersScale;
	}
	
	public static double mercToLocalX(double mx) {
		return mercToMetersScale * (mx - mxOffset);
	}
	
	public static double mercToLocalY(double my) {
		return mercToMetersScale * (my - myOffset);
	}
	
	public static double localToMercX(double x) {
		return (x / mercToMetersScale) + mxOffset;
	}
	
	public static double localToMercY(double y) {
		return (y / mercToMetersScale) + myOffset;
	}
	
	public static void setLocalAnchor(double mx, double my) {
		mxOffset = mx; 
		myOffset = my;
		mercToMetersScale = MercatorCoordinates.metersPerMerc(my);
	}
}
