package intransix.osm.termite.gui.task;

import intransix.osm.termite.map.data.MapDataRequest;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.BlockerDialog;
import intransix.osm.termite.net.NetRequest;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class MapDataRequestTask extends SwingWorker<Object,Object> {
	
	private MapDataRequest mapDataRequest;
	private MapDataManager mapDataManager;
	private JDialog blocker;
	
	private boolean success = false;
	private String errorMsg;
	
	public MapDataRequestTask(MapDataManager mapDataManager, double minLat, double minLon, 
			double maxLat, double maxLon) {
		this.mapDataManager = mapDataManager;
		this.mapDataRequest = new MapDataRequest(minLat, minLon, maxLat, maxLon);
	}
	
	public synchronized void blockUI() {
		if(!isDone()) {
			blocker = new BlockerDialog(null,this,"Loading map data...",false);
			blocker.setVisible(true);
		}
	}
	
	@Override
	public Object doInBackground() {
		
		try {
			NetRequest xmlRequest = new NetRequest(mapDataRequest);
			int responseCode = xmlRequest.doRequest();
			if(responseCode == 200) {
				success = true;
			}
			else {
				errorMsg = "Server error: response code " + responseCode;
				success = false;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			errorMsg = ex.getMessage();
			success = false;
		}
		
		return "";
	}
	
	@Override
	public synchronized void done() {
		
		if(blocker != null) {
			blocker.setVisible(false);
		}
		
		if(success) {
			mapDataManager.setOsmData(mapDataRequest.getOsmData());
		}
		else {
			JOptionPane.showMessageDialog(null,"There was an error: " + errorMsg);
		}	
	}
}
