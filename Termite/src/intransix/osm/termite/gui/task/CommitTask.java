package intransix.osm.termite.gui.task;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.app.LoginManager;
import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.gui.BlockerDialog;
import intransix.osm.termite.gui.dialog.CommitDialog;
import intransix.osm.termite.net.NetRequest;
import javax.swing.*;

/**
 *
 * @author sutter
 */
public class CommitTask extends SwingWorker<Object,Object>{
	
	private String message;
	private OsmData osmData;
	private LoginManager loginManager;
	private JDialog blocker;
	
	private boolean success = false;
	private boolean canceled = false;
	private String errorMsg;
	
	public CommitTask(OsmData osmData, LoginManager loginManager) {
		this.osmData = osmData;
		this.loginManager = loginManager;
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
			//get the change set
			OsmChangeSet changeSet = new OsmChangeSet();
			osmData.loadChangeSet(changeSet);
			
			if(changeSet.isEmpty()) {
				JOptionPane.showMessageDialog(null,"There is no data to commit.");
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
			changeSet.setMessage(message);
			
			//make network requests
			NetRequest xmlRequest;
			int responseCode;
			
			//open a change set
			OpenChangeSetRequest openChangeSetRequest = new OpenChangeSetRequest(changeSet);
			xmlRequest = new NetRequest(openChangeSetRequest);
			xmlRequest.setCredentials(username, password);
			responseCode = xmlRequest.doRequest();

			if(responseCode == 200) {
				//success
				success = true;
			}
			else if(responseCode == 401) {
				//unauthorized
				errorMsg = "There username and password are not valid.";
				loginManager.setCredentials(username,null);
				success = false;
				return null;
			}
			else {
				errorMsg = "Server error: response code " + responseCode;
				success = false;
				return null;
			}
			
			//commit the data
			CommitRequest commitRequest = new CommitRequest(changeSet,osmData);
			xmlRequest = new NetRequest(commitRequest);
			xmlRequest.setCredentials(username, password);
			responseCode = xmlRequest.doRequest();
			
			if(responseCode == 200) {
				//success
				success = true;
			}
			else if(responseCode == 401) {
				//unauthorized
				errorMsg = "There username and password are not valid.";
				loginManager.setCredentials(username,null);
				success = false;
				return null;
			}
			else {
				errorMsg = "Server error: response code " + responseCode;
				success = false;
				return null;
			}
			
			//close the change set
			CloseChangeSetRequest closeChangeSetRequest = new CloseChangeSetRequest(changeSet);
			xmlRequest = new NetRequest(closeChangeSetRequest);
			xmlRequest.setCredentials(username, password);
			responseCode = xmlRequest.doRequest();
			
			if(responseCode == 200) {
				//success
				success = true;
			}
			else if(responseCode == 401) {
				//unauthorized
				errorMsg = "There username and password are not valid.";
				loginManager.setCredentials(username,null);
				success = false;
				return null;
			}
			else {
				errorMsg = "Server error: response code " + responseCode;
				success = false;
				return null;
			}
			
			success = true;
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
		
		if(canceled) {
			return;
		}
		
		if(success) {
			//no action needed
		}
		else {
			JOptionPane.showMessageDialog(null,"There was an error: " + errorMsg);
		}	
	}
}
