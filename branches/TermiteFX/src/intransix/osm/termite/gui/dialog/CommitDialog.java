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
import javafx.stage.WindowEvent;

/**
 *
 * @author sutter
 */
public class CommitDialog extends TermiteDialog {
	
	
	public CommitDialog(DialogCallback successCallback, DialogCallback cancelCallback) {
		super(TermiteFXGui.getStage());
		
		//construct dialog
		Label label = new Label("Dummy commit...");
		this.init(label, successCallback, cancelCallback);
	}
	
	public String getMessage() {
		return "dummy message";
	}
}
