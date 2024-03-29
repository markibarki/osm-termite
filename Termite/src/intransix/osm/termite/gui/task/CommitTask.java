package intransix.osm.termite.gui.task;

import intransix.osm.termite.app.mapdata.commit.*;
import intransix.osm.termite.app.LoginManager;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.BlockerDialog;
import intransix.osm.termite.gui.dialog.CommitDialog;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class CommitTask extends SwingWorker<Object,Object>{
	
	//====================
	// Properties
	//====================
	
	private final static String EXCEPTION_MSG_BASE = "There was an error: ";
	
	private MapDataManager mapDataManager;
	private CommitAction commitAction;
	private LoginManager loginManager;
	private JDialog blocker;
	
	private boolean success = false;
	private boolean canceled = false;
	private String errorMsg;
	
	
	//====================
	// Public Methods
	//====================
	
	/** Constructor. */
	public CommitTask(MapDataManager mapDataManager, LoginManager loginManager) {
		this.mapDataManager = mapDataManager;
		this.loginManager = loginManager;
	}
	
	/** Calling this method will block the UI thread until the task completes. 
	 * It should preferably be called from the UI thread. */
	public synchronized void blockUI() {
		if(!isDone()) {
			blocker = new BlockerDialog(null,this,"Loading map data...",false);
			blocker.setVisible(true);
		}
	}
	
	/** This is the main task method, called by SwingWorker. */
	@Override
	public Object doInBackground() {
		
		commitAction = new CommitAction(mapDataManager, loginManager);
		
		try {
			success = commitAction.verifyChangeSet();
			
			if(!success) {
				JOptionPane.showMessageDialog(null,commitAction.getErrorMessage());
				canceled = true;
				return null;
			}
			
			//get login info
			String username = loginManager.getUsername();
			String password = loginManager.getPassword();
			if((username == null)||(password == null)) {
				//get the login info
				loginManager.loadLoginInfo();
				username = loginManager.getUsername();
				password = loginManager.getPassword();
				if((username == null)||(password == null)) {
					//user canceled
					canceled = true;
					return null;
				}
			}
			
			//get commit message
			CommitDialog commitDialog = new CommitDialog(null);
			commitDialog.setVisible(true);
		
			String message = commitDialog.getMessage();
			if(message == null) {
				//if not message the commit was canceled
				canceled = true;
				return null;
			}
			
			success = commitAction.commit(message);
			
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
		
		return "";
	}
	
	/** This method is called in the UI thread on completion of the task. */
	@Override
	public synchronized void done() {
		
		if(blocker != null) {
			blocker.setVisible(false);
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
			JOptionPane.showMessageDialog(null,errorMsg);
		}	
	}
}
