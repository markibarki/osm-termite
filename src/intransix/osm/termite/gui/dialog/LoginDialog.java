/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.dialog;

import intransix.osm.termite.app.LoginManager;
import intransix.osm.termite.gui.TermiteFXGui;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author sutter
 */
public class LoginDialog extends Stage {
	
	private LoginManager loginManager;
	private Runnable successCallback;
	private Runnable cancelCallback;
	
	public LoginDialog(LoginManager loginManager, Runnable successCallback, Runnable cancelCallback) {
		super(StageStyle.TRANSPARENT);
		this.initModality(Modality.WINDOW_MODAL);
		this.initOwner(TermiteFXGui.getStage());
		
		this.loginManager = loginManager;
		this.successCallback = successCallback;
		this.cancelCallback = cancelCallback;
		
		//create the layout
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		//user name field and label
		Label label = new Label("Dummy login...");
		grid.add(label, 0, 0);
		
		//add a button - Use the hbox so we can use a different alignment for button
		Button okButton = new Button("OK");
		okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
//fix this
String username = "sutter_dave";
String password = "sungun1!";
				LoginDialog.this.loginManager.setCredentials(username,password);
				LoginDialog.this.hide();
				LoginDialog.this.successCallback.run();
			}
		});
		grid.add(okButton, 0, 1);
		
		//create the scene
		Scene scene = new Scene(grid, 300, 275);
		this.setScene(scene);
	}
}
