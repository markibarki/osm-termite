package intransix.osm.termite.app;

import intransix.osm.termite.gui.TermiteFXGui;
import intransix.osm.termite.gui.dialog.DialogCallback;
import intransix.osm.termite.gui.dialog.LoginDialog;


/**
 *
 * @author sutter
 */
public class LoginManager {
	
	private String username;
	private String password;
	
	public void setCredentials(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean loginValid() {
		return ((username != null)&&(password != null));
	}
	
	public void loadLoginInfo(final DialogCallback successCallback, final DialogCallback cancelCallback) {
		final LoginDialog loginDialog = new LoginDialog(TermiteFXGui.getStage());
		loginDialog.init(this, successCallback, cancelCallback);
		loginDialog.show();
	}
	
}
