package intransix.osm.termite.gui.task;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.dialog.BlockerDialog;
import intransix.osm.termite.app.mapdata.publish.PublishAction;
import intransix.osm.termite.gui.dialog.MessageDialog;
import javafx.concurrent.Task;

		

/**
 *
 * @author sutter
 */
public class PublishTask extends Task<Void> {
	
	private final static String EXCEPTION_MSG_BASE = "There was an error: ";
	
	private PublishAction publishAction;
	
	private BlockerDialog blocker;
	
	private boolean success = false;
	private boolean canceled = false;
	private String errorMsg;
	
	public PublishTask(MapDataManager mapDataManager, long structureId) {
		publishAction = new PublishAction(mapDataManager, structureId);
		
	}
	
	public synchronized void blockUI() {
		if(!isDone()) {
			blocker = new BlockerDialog(this,"Publishing map data...",false);
			blocker.show();
		}
	}
	
	@Override
	public Void call() {
		
		try {
			success = publishAction.verifyPublish();

			if(!success) {
				this.errorMsg = publishAction.getErrorMsg();
				return null;
			}

			success = publishAction.publish();
			if(!success) {
				this.errorMsg = publishAction.getErrorMsg();
				return null;
			}
			
			return null;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			errorMsg = EXCEPTION_MSG_BASE + ex.getMessage();
			success = false;
			
			return null;
		}
		
	}
	
	@Override
	public synchronized void done() {
		
		if(blocker != null) {
			blocker.hide();
		}
		
		if(canceled) {
			return;
		}
		
		if(success) {
			//no action needed
		}
		else {
			MessageDialog.show(errorMsg);
		}	
	}
}
