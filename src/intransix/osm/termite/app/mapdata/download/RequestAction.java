package intransix.osm.termite.app.mapdata.download;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.net.NetRequest;
import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author sutter
 */
public class RequestAction {
	
	private MapDataManager mapDataManager;
	private MapDataRequest mapDataRequest;
	private String errorMsg;
	private Rectangle2D downloadBounds;
	
	public RequestAction(MapDataManager mapDataManager, Rectangle2D downloadBounds) {
		this.mapDataManager = mapDataManager;
		this.downloadBounds = downloadBounds;
		
		double minLat = Math.toDegrees(MercatorCoordinates.myToLatRad(downloadBounds.getMaxY()));
		double minLon = Math.toDegrees(MercatorCoordinates.mxToLonRad(downloadBounds.getMinX()));
		double maxLat = Math.toDegrees(MercatorCoordinates.myToLatRad(downloadBounds.getMinY()));
		double maxLon = Math.toDegrees(MercatorCoordinates.mxToLonRad(downloadBounds.getMaxX()));
		this.mapDataRequest = new MapDataRequest(minLat, minLon, maxLat, maxLon);
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}
	
	public boolean request() throws Exception {
		NetRequest xmlRequest = new NetRequest(mapDataRequest);
		int responseCode = xmlRequest.doRequest();

		if(responseCode == 200) {
			return true;
		}
		else {
			errorMsg = "Server error: response code " + responseCode;
			return false;
		}
	}
	
	public boolean postProcessInUiThread() {
		//load the working data
		mapDataManager.setData(mapDataRequest.getDataSet(),downloadBounds);
		return true;
	}
}
