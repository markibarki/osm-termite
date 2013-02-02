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
public class BlockerDialog extends Stage {
	
	private Task task;
	
	public BlockerDialog(Task task, String msg, boolean cancelable) {
		super(StageStyle.TRANSPARENT);
		this.task = task;
		this.initModality(Modality.WINDOW_MODAL);
		this.initOwner(TermiteFXGui.getStage());
		
		//create the layout
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		//user name field and label
		Label label = new Label(msg);
		grid.add(label, 0, 0);
		
		//add a button - Use the hbox so we can use a different alignment for button
		if(cancelable) {
			Button cancelButton = new Button("Cancel");
			cancelButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					BlockerDialog.this.task.cancel();
					BlockerDialog.this.hide();
				}
			});
			grid.add(cancelButton, 0, 1);
		}
		
		//create the scene
		Scene scene = new Scene(grid, 300, 275);
		this.setScene(scene);
	}
}
