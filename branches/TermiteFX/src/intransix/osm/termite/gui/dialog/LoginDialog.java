package intransix.osm.termite.gui.dialog;

import intransix.osm.termite.app.LoginManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 *
 * @author sutter
 */
public class LoginDialog extends TermiteDialog {
	
	private TextField nameField;
	private PasswordField pwdField;
	
	public LoginDialog(Stage parent) {
		super(parent);
	}
	
	/** This method sets the login manager and the callbacks. */ 
	public void init(final LoginManager loginManager, final DialogCallback successCallback, final DialogCallback cancelCallback) {
		
		//thei verifies the input
		DialogCallback okCallback = new DialogCallback() {
			@Override
			public boolean handle(TermiteDialog dialog) {
				String name = nameField.getText().trim();
				String pwd = pwdField.getText().trim();
				if((name.length() == 0)||(pwd.length() == 0)) {
					MessageDialog.show(LoginDialog.this,"Username of password not valid");
					return false;
				}
				
				loginManager.setCredentials(name, pwd);
				successCallback.handle(dialog);
				return true;
			}
		};
		
		//create the layout
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		//user name field and label
		Label nameLabel = new Label("Username: ");
		grid.add(nameLabel, 0, 0);
		nameField = new TextField();
		grid.add(nameField, 1, 0);
		Label pwdLabel = new Label("Password: ");
		grid.add(pwdLabel, 0, 1);
		pwdField = new PasswordField();
		grid.add(pwdField, 1, 1);
		
		this.init(grid, okCallback, cancelCallback);
	}
}
