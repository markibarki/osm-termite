/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.dialog;

import intransix.osm.termite.gui.TermiteFXGui;
import javafx.concurrent.Task;
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
public class ConfirmDialog {
	public static void show(String msg, final Runnable okCallback, final Runnable cancelCallback) {
		final Stage dialog = new Stage(StageStyle.TRANSPARENT);
		
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(TermiteFXGui.getStage());
		
		//create the layout
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		//user name field and label
		Label label = new Label(msg);
		grid.add(label, 0, 0, 2, 1);
		
		//add a button - Use the hbox so we can use a different alignment for button
			Button okButton = new Button("OK");
			okButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					dialog.hide();
					okCallback.run();
				}
			});
			grid.add(okButton, 0, 1);
			
			Button cancelButton = new Button("Cancel");
			cancelButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					dialog.hide();
					cancelCallback.run();
				}
			});
			grid.add(cancelButton, 1, 1);

		
		//create the scene
		Scene scene = new Scene(grid, 300, 275);
		dialog.setScene(scene);
		dialog.show();
	}
}
