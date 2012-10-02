/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.app.mapdata.download;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.net.NetRequest;

/**
 *
 * @author sutter
 */
public class RequestAction {
	
	private MapDataManager mapDataManager;
	private MapDataRequest mapDataRequest;
	private String errorMsg;
	
	public RequestAction(MapDataManager mapDataManager, double minLat, double minLon, 
			double maxLat, double maxLon) {
		this.mapDataManager = mapDataManager;
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
		mapDataManager.setData(mapDataRequest.getDataSet());
		return true;
	}
}
