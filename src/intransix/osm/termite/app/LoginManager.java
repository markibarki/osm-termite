package intransix.osm.termite.app;

//import intransix.osm.termite.gui.dialog.LoginDialog;

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
	
	public void loadLoginInfo() {
throw new RuntimeException("Add login dialog box!");
//		LoginDialog loginDialog = new LoginDialog(null,this);
//		loginDialog.setVisible(true);
	}
	
}
