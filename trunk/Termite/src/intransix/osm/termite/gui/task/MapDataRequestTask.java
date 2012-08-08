package intransix.osm.termite.gui.task;

import intransix.osm.termite.map.data.MapDataRequest;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.gui.BlockerDialog;
import intransix.osm.termite.net.XmlRequest;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class MapDataRequestTask extends SwingWorker<Object,Object> {
	
	private MapDataRequest mapDataRequest;
	private TermiteGui gui;
	private JDialog blocker;
	
	private boolean success = false;
	private String errorMsg;
	
	public MapDataRequestTask(TermiteGui gui, double minLat, double minLon, 
			double maxLat, double maxLon) {
		this.gui = gui;
		this.mapDataRequest = new MapDataRequest(minLat, minLon, maxLat, maxLon);
	}
	
	public synchronized void blockUI() {
		if(!isDone()) {
			blocker = new BlockerDialog(gui,this,"Loading map data...",false);
			blocker.setVisible(true);
		}
	}
	
	@Override
	public Object doInBackground() {
		
		try {
			XmlRequest xmlRequest = new XmlRequest(mapDataRequest);
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
			gui.setMapData(mapDataRequest.getOsmData());
		}
		else {
			JOptionPane.showMessageDialog(null,"There was an error: " + errorMsg);
		}	
	}
}
