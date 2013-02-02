/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.dialog;

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
public class CommitDialog extends Stage {
	
	public CommitDialog() {
		super(StageStyle.TRANSPARENT);
		this.initModality(Modality.WINDOW_MODAL);
		this.initOwner(TermiteFXGui.getStage());
		
		//create the layout
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		//user name field and label
		Label label = new Label("Dummy commit...");
		grid.add(label, 0, 0);
		
		//add a button - Use the hbox so we can use a different alignment for button
		Button okButton = new Button("OK");
		okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				CommitDialog.this.hide();
			}
		});
		grid.add(okButton, 0, 1);
		
		//create the scene
		Scene scene = new Scene(grid, 300, 275);
		this.setScene(scene);
	}
	
	public String getMessage() {
		return "dummy message";
	}
}
