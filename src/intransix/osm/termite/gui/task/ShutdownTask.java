package intransix.osm.termite.gui.task;

import intransix.osm.termite.gui.dialog.BlockerDialog;
import intransix.osm.termite.app.TermiteFX;
import intransix.osm.termite.gui.dialog.MessageDialog;
import javafx.application.Platform;
import javafx.concurrent.Task;

/**
 *
 * @author sutter
 */
public class ShutdownTask extends Task<Void> {
	
	private TermiteFX app;
	private BlockerDialog blocker;
	
	private boolean success = false;
	private String errorMsg;
	
	public ShutdownTask(TermiteFX app) {
		this.app = app;
	}
	
	public void execute() {
		Thread th = new Thread(this);
		th.setDaemon(true);
		th.start();
		synchronized(this) {
			if(!isDone()) {
				blocker = new BlockerDialog(this,"Shutting down...",false);
				blocker.show();
			}
		}
	}
	
	@Override
	public Void call() {
		try {
			app.exit();
			success = true;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			errorMsg = ex.getMessage();
			success = false;
		}
		
		//do the ui action
		Platform.runLater(new Runnable() {
			@Override public void run() {
				if(blocker != null) {
					blocker.hide();
				}
			}
		});
		
		if(!success) {
			MessageDialog.show("There was an error shutting down: " + errorMsg);
		}	
		
		return null;
	}

}
