package intransix.osm.termite.gui.task;

import intransix.osm.termite.app.mapdata.download.MapDataRequest;
import intransix.osm.termite.app.mapdata.download.RequestAction;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.BlockerDialog;
import intransix.osm.termite.net.NetRequest;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class MapDataRequestTask extends SwingWorker<Object,Object> {
	
	private final static String EXCEPTION_MSG_BASE = "There was an error: ";
	
	private RequestAction requestAction;
	private JDialog blocker;
	
	private boolean success = false;
	private String errorMsg;
	
	public MapDataRequestTask(MapDataManager mapDataManager, double minLat, double minLon, 
			double maxLat, double maxLon) {
		this.requestAction = new RequestAction(mapDataManager,minLat,minLon,maxLat,maxLon);
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
			success = requestAction.request();
			if(!success) {
				errorMsg = requestAction.getErrorMsg();
				return null;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			errorMsg = EXCEPTION_MSG_BASE + ex.getMessage();
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
			boolean success = requestAction.postProcessInUiThread();
			if(!success) {
				errorMsg = requestAction.getErrorMsg();
			}
		}
		
		if(!success) {
			JOptionPane.showMessageDialog(null,errorMsg);
		}	
	}
}
