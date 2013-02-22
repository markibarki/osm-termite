package intransix.osm.termite.gui.task;

import intransix.osm.termite.app.mapdata.download.RequestAction;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.dialog.BlockerDialog;
import intransix.osm.termite.gui.dialog.MessageDialog;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 *
 * @author sutter
 */
public class MapDataRequestTask extends Task<Void> {
	
	private final static String EXCEPTION_MSG_BASE = "There was an error: ";
	
	private RequestAction requestAction;
	private BlockerDialog blocker;
	
	private boolean success = false;
	private String errorMsg;
	
	public MapDataRequestTask(MapDataManager mapDataManager, double minLat, double minLon, 
			double maxLat, double maxLon) {
		this.requestAction = new RequestAction(mapDataManager,minLat,minLon,maxLat,maxLon);
	}
	
	public void execute() {
		Thread th = new Thread(this);
		th.setDaemon(true);
		th.start();
		synchronized(this) {
			if(!isDone()) {
				blocker = new BlockerDialog(this,"Loading map data...",false);
				blocker.show();
			}
		}
	}
	
	@Override
	public Void call() {
		
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
		
		return null;
	}
	
	
	@Override
	public synchronized void done() {
		Platform.runLater(new Runnable() {
			@Override public void run() {
				actionInUIThread();
			}
		});
	}
	
	public void actionInUIThread() {
		if(blocker != null) {
			blocker.hide();
		}
		
		if(success) {
			boolean success = requestAction.postProcessInUiThread();
			if(!success) {
				errorMsg = requestAction.getErrorMsg();
			}
		}
		
		if(!success) {
			MessageDialog.show(errorMsg);
		}
	}
}
