package intransix.osm.termite.gui.task;

import intransix.osm.termite.app.mapdata.commit.*;
import intransix.osm.termite.app.LoginManager;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.dialog.BlockerDialog;
import intransix.osm.termite.gui.dialog.CommitDialog;
import intransix.osm.termite.gui.dialog.MessageDialog;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class CommitTask extends Task<Void> {
	
	//====================
	// Properties
	//====================
	
	private final static String EXCEPTION_MSG_BASE = "There was an error: ";
	
	private MapDataManager mapDataManager;
	private CommitAction commitAction;
	private LoginManager loginManager;
	private String commitMessage;
	private BlockerDialog blocker;
	
	private boolean success = false;
	private boolean canceled = false;
	private String errorMsg;
	
	
	//====================
	// Public Methods
	//====================
	
	/** Constructor. */
	public CommitTask(MapDataManager mapDataManager, LoginManager loginManager, String message) {
		this.mapDataManager = mapDataManager;
		this.loginManager = loginManager;
		this.commitMessage = message;
	}
	
	public void execute() {
		Thread th = new Thread(this);
		th.setDaemon(true);
		th.start();
		synchronized(this) {
			if(!isDone()) {
				blocker = new BlockerDialog(this,"Saving data...",false);
				blocker.show();
			}
		}
	}
	
	/** This is the main task method, called by SwingWorker. */
	@Override
	public Void call() {
		
		commitAction = new CommitAction(mapDataManager, loginManager);
		
		try {
			success = commitAction.verifyChangeSet();
			
			if(!success) {
				MessageDialog.show(commitAction.getErrorMessage());
				canceled = true;
				return null;
			}
			
//			//get login info
//			String username = loginManager.getUsername();
//			String password = loginManager.getPassword();
//			if((username == null)||(password == null)) {
//				//get the login info
//				loginManager.loadLoginInfo();
//				username = loginManager.getUsername();
//				password = loginManager.getPassword();
//				if((username == null)||(password == null)) {
//					//user canceled
//					canceled = true;
//					return null;
//				}
//			}
//			
//			//get commit message
//			CommitDialog commitDialog = new CommitDialog();
//			commitDialog.show();
//		
//			String message = commitDialog.getMessage();
//			if(message == null) {
//				//if not message the commit was canceled
//				canceled = true;
//				return null;
//			}
			
			success = commitAction.commit(commitMessage);
			
			if(!success) {
				errorMsg = commitAction.getErrorMessage();
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
	
	/** This method is called in the UI thread on completion of the task. */
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
		
		if(canceled) {
			return;
		}
		
		if(success) {
			//do the data update in the UI thread
			commitAction.postProcessInUiThread();
		}
		else {
			//show an error message
			MessageDialog.show(errorMsg);
		}	
	}
}
