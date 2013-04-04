package intransix.osm.termite.gui.sequence;

import intransix.osm.termite.app.LoginManager;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.gui.TermiteFXGui;
import intransix.osm.termite.gui.dialog.CommitDialog;
import intransix.osm.termite.gui.dialog.DialogCallback;
import intransix.osm.termite.gui.dialog.MessageDialog;
import intransix.osm.termite.gui.dialog.TermiteDialog;
import intransix.osm.termite.gui.task.CommitTask;

/**
 *
 * @author sutter
 */
public class CommitSequence {
	
	private MapDataManager mapDataManager;
	private LoginManager loginManager;
	private CommitDialog commitDialog;
	private DialogCallback cancelCallback;
			
	public void commitData(MapDataManager mapDataManager, LoginManager loginManager) {
		this.mapDataManager = mapDataManager;
		this.loginManager = loginManager;
		
		//get login credentials
		if((mapDataManager == null)&&(loginManager == null)) {
			MessageDialog.show(TermiteFXGui.getStage(),"Uer Interface not properly initialized: MapDataManger or LoginManger missing.");
			return;
		}
		
		cancelCallback = new DialogCallback() {
			@Override
			public boolean handle(TermiteDialog dialog) {
				commitCanceled();
				return true;
			}
		};
		
		DialogCallback loginSuccessCallback = new DialogCallback() {
			@Override
			public boolean handle(TermiteDialog dialog) {
				commitStep2();
				return true;
			}
		};
		
		//get login info
		if(!loginManager.loginValid()) {
			//get the login info
			loginManager.loadLoginInfo(loginSuccessCallback, cancelCallback);
		}
		else {
			commitStep2();
		}
	}
	
	public void commitStep2() {
		DialogCallback messageSuccessCallback = new DialogCallback() {
			@Override
			public boolean handle(TermiteDialog dialog) {
				commitStep3();
				return true;
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
