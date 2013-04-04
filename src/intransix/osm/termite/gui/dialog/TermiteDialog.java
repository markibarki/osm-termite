/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.dialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * This is the base class for a dialog box. It supports the addition of a content node and up to two buttons.
 * There is a positive button and a negative button. Each of these is optional. If there is a negative button,
 * then closing the dialog box will trigger the negative action using the system close control. Otherwise the system
 * close control has not action. The positive button is "OK" by default and the negative button is "Cancel". These values
 * can be overwritten. The callback functions passed in should return true on handling the event if the dialog box should close. If
 * the callback returns false the dialog box will stay open. 
 * 
 * @author sutter
 */
public class TermiteDialog extends Stage {
	
	private final static String DEFAULT_POSITIVE_BUTTON_TEXT = "OK";
	private final static String DEFAULT_NEGATIVE_BUTTON_TEXT = "Cancel";
	
	private final static int MIN_WIDTH = 200;
	private final static int MIN_HEIGHT = 150;
	
	private DialogCallback positiveCallback;
	private DialogCallback negativeCallback;
	
	/** This method creates a dialog. */
	public TermiteDialog(Stage parent) {
		super(StageStyle.UTILITY);
		this.initModality(Modality.WINDOW_MODAL);
		this.initOwner(parent);
	}
	
	/** This is the same as the full init call except it uses the default button names, "OK" and "Cancel". */
	public void init(Node content, DialogCallback positiveCallback, DialogCallback negativeCallback) {
		init(content,positiveCallback,negativeCallback,DEFAULT_POSITIVE_BUTTON_TEXT,DEFAULT_NEGATIVE_BUTTON_TEXT);
	}
	
	/** This method initializes the dialog with the given positive and negative callbacks. Either or both callbacks
	 * can be null, in which case there will be no buttons for these actions. */
	public void init(Node content, DialogCallback positiveCallback, DialogCallback negativeCallback, String positiveText, String negativeText) {
		
		setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent e) {
				boolean success;
				if(TermiteDialog.this.negativeCallback != null) {
					success = TermiteDialog.this.negativeCallback.handle(TermiteDialog.this);
				}
				else {
					success = false;
				}
				
				//if not success, consume event so window does not close.
				if(!success) e.consume();
			}
		});
		
		this.positiveCallback = positiveCallback;
		this.negativeCallback = negativeCallback;
		
		//create the layout
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));
		
		//add positive button
		if(positiveCallback != null) { 
			Button positiveButton = new Button(positiveText);
			positiveButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					boolean success = TermiteDialog.this.positiveCallback.handle(TermiteDialog.this);
					if(success) {
						TermiteDialog.this.hide();
					}
				}
			});
			int xPosition = (negativeCallback != null) ? 0 : 1; 
			grid.add(positiveButton, xPosition, 1);
		}
		
		if(negativeCallback != null) {
			Button negativeButton = new Button(negativeText);
			negativeButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					boolean success = TermiteDialog.this.negativeCallback.handle(TermiteDialog.this);
					if(success) {
						TermiteDialog.this.hide();
					}
				}
			});
			int xPosition = (positiveCallback != null) ? 2 : 1; 
			grid.add(negativeButton, xPosition, 1);
		}
		
		grid.add(content, 0, 0, 3, 1);
		
		this.setMinWidth(MIN_WIDTH);
		this.setMinHeight(MIN_HEIGHT);
		
		//create the scene
		Scene scene = new Scene(grid);
		this.setScene(scene);
	}

}
