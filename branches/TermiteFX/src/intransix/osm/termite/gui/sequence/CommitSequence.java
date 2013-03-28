package intransix.osm.termite.gui.sequence;

import intransix.osm.termite.app.LoginManager;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.dialog.CommitDialog;
import intransix.osm.termite.gui.dialog.MessageDialog;
import intransix.osm.termite.gui.task.CommitTask;

/**
 *
 * @author sutter
 */
public class CommitSequence {
	
	private MapDataManager mapDataManager;
	private LoginManager loginManager;
	private CommitDialog commitDialog;
	private Runnable cancelCallback;
			
	public void commitData(MapDataManager mapDataManager, LoginManager loginManager) {
		this.mapDataManager = mapDataManager;
		this.loginManager = loginManager;
		
		//get login credentials
		if((mapDataManager == null)&&(loginManager == null)) {
			MessageDialog.show("Uer Interface not properly initialized: MapDataManger or LoginManger missing.");
			return;
		}
		
		cancelCallback = new Runnable() {
			public void run() {
				commitCanceled();
			}
		};
		
		Runnable loginSuccessCallback = new Runnable() {
			public void run() {
				commitStep2();
			}
		};
		
		//get login info
		String username = loginManager.getUsername();
		String password = loginManager.getPassword();
		if((username == null)||(password == null)) {
			//get the login info
			loginManager.loadLoginInfo(loginSuccessCallback,cancelCallback);
		}
	}
	
	public void commitStep2() {
		Runnable messageSuccessCallback = new Runnable() {
			public void run() {
				commitStep3();
			}
		};
		
		//get commit message
		commitDialog = new CommitDialog(messageSuccessCallback,cancelCallback);
		commitDialog.show();
		
	}
	
	public void commitStep3() {
		String message = commitDialog.getMessage();
		
		CommitTask commitTask = new CommitTask(mapDataManager,loginManager,message);
		commitTask.execute();
	}
	
	public void commitCanceled() {
		//nothing to do
		return;
	}
}
