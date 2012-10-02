package intransix.osm.termite.gui.task;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.BlockerDialog;
import intransix.osm.termite.app.mapdata.publish.PublishAction;
import javax.swing.*;

		

/**
 *
 * @author sutter
 */
public class PublishTask extends SwingWorker<Object,Object>{
	
	private final static String EXCEPTION_MSG_BASE = "There was an error: ";
	
	private PublishAction publishAction;
	
	private JDialog blocker;
	
	private boolean success = false;
	private boolean canceled = false;
	private String errorMsg;
	
	public PublishTask(MapDataManager mapDataManager, long structureId) {
		publishAction = new PublishAction(mapDataManager, structureId);
		
	}
	
	public synchronized void blockUI() {
		if(!isDone()) {
			blocker = new BlockerDialog(null,this,"Publishing map data...",false);
			blocker.setVisible(true);
		}
	}
	
	@Override
	public Object doInBackground() {
		
		try {
			boolean success;

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

			return "";	
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
		
		if(canceled) {
			return;
		}
		
		if(success) {
			//no action needed
		}
		else {
			JOptionPane.showMessageDialog(null,errorMsg);
		}	
	}
}
