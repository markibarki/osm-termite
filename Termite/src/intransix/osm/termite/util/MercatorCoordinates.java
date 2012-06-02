package intransix.osm.termite.util;

import java.awt.geom.AffineTransform;

/**
 * These are transformations between latitude and longitude in radians and the version of
 * mercator coordinates we are using. The range is 2^25, which corresponds roughly to meters.
 * 
 * This source code was provided by Micello, Inc.
 */
public class MercatorCoordinates {

	public final static int MERCATOR_ZOOM = 25;
	
	private final static double TWO_TO_25 = 0x01 << MERCATOR_ZOOM;
	
	public final static double MIN_SIZE = 0;
	public final static double MAX_SIZE = TWO_TO_25;
	
	private final static double EARTH_CIRCUMFERENCE = 40040000.0; //somewhere between polar and equitorial value - I picked it at random though
	private final static double METERS_PER_MERC0 = EARTH_CIRCUMFERENCE / TWO_TO_25;

	/** This method returns the number of meters in 1 mercator coordinate unit, for either the x or y direction. */
	public static double metersPerMerc(double my) {
		double latRad = myToLatRad(my);
		return METERS_PER_MERC0 * Math.cos(latRad);

	}

	public static double lonRadToMx(double lonRad) {
		return TWO_TO_25 * (.5 + lonRad/(2 * Math.PI) );
	}

	public static double latRadToMy(double latRad) {
		double sinLat = Math.sin(latRad);
		return TWO_TO_25 * ( .5 - Math.log((1 + sinLat)/(1-sinLat))/(4 * Math.PI) );
	}

	public static double mxToLonRad(double mx) {
		return 2*Math.PI * (mx/TWO_TO_25 - .5);
	}

	public static double myToLatRad(double my) {
		return Math.atan( Math.sinh( 2*Math.PI * (.5 - my/TWO_TO_25) ));
	}

	private final static int MXX_INDEX = 0;
	private final static int MYX_INDEX = 1;
	private final static int MXY_INDEX = 2;
	private final static int MYY_INDEX = 3;
	private final static int MX0_INDEX = 4;
	private final static int MY0_INDEX = 5;

	private final static double MERC_TO_LON_DEG = 360.0 / TWO_TO_25;

	/** This takes as input a transformation matrix from natural coordinates to mercator
	 * coordinates and returns a tranformation from natural coordinates to lat lon. */
	public static AffineTransform convertMercatorToLatLonTransform(AffineTransform natToMxy) {
		double[] llElements = new double[6];
		natToMxy.getMatrix(llElements);

		//convert the 0 point
		double mx0 = Math.toDegrees(mxToLonRad(llElements[MX0_INDEX]));
		double my0Rad = myToLatRad(llElements[MY0_INDEX]);
		double my0 = Math.toDegrees(my0Rad);

		double cosLat = Math.cos(my0Rad);

		//rescale the transform matrix, change sign for Y
		double mxx = MERC_TO_LON_DEG * llElements[MXX_INDEX];
		double mxy = MERC_TO_LON_DEG * llElements[MXY_INDEX];
		double myx = -cosLat * MERC_TO_LON_DEG * llElements[MYX_INDEX];
		double myy = -cosLat * MERC_TO_LON_DEG * llElements[MYY_INDEX];

		return new AffineTransform(mxx,myx,mxy,myy,mx0,my0);

	}

	/** This takes as input a transformation matrix from natural coordinates to mercator
	 * coordinates and returns a tranformation from natural coordinates to lat lon. */
	public static AffineTransform convertLatLonToMercatorTransform(AffineTransform natToLatLon) {
		double[] llElements = new double[6];
		natToLatLon.getMatrix(llElements);

		//convert the 0 point
		double lon0Rad = Math.toRadians(llElements[MX0_INDEX]);
		double mx0 = MercatorCoordinates.lonRadToMx(lon0Rad);
		double lat0Rad = Math.toRadians(llElements[MY0_INDEX]);
		double my0 = MercatorCoordinates.latRadToMy(lat0Rad);

		double cosLat = Math.cos(lat0Rad);

		//rescale the transform matrix, change sign for Y
		double mxx = llElements[MXX_INDEX] / MERC_TO_LON_DEG;
		double mxy = llElements[MXY_INDEX] / MERC_TO_LON_DEG;
		double myx = llElements[MYX_INDEX] / (-cosLat * MERC_TO_LON_DEG);
		double myy = llElements[MYY_INDEX] / (-cosLat * MERC_TO_LON_DEG);

		return new AffineTransform(mxx,myx,mxy,myy,mx0,my0);

	}

}
