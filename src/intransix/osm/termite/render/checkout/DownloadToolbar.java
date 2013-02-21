package intransix.osm.termite.render.checkout;

import javax.swing.*;
import intransix.osm.termite.gui.mode.download.DownloadEditorMode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 *
 * @author sutter
 */
public class DownloadToolbar extends ToolBar {
	
	private final static int SPACE_X = 50;
	private final static int SPACE_Y = 3;
	private final static int TEXT_FIELD_WIDTH = 200;
		
	private TextField searchField;
	private DownloadEditorMode downloadEditorMode;
	
	public DownloadToolbar(DownloadEditorMode downloadEditorMode) {
		this.downloadEditorMode = downloadEditorMode;
		this.initialize();
	}
		
	private void initialize() {
		
		HBox.setHgrow(this,Priority.ALWAYS);
		VBox.setVgrow(this,Priority.ALWAYS);
		
		Label label = new Label("Click map to start selection. Click again to complete. ");
		Button downloadButton = new Button("Download");
		Button clearButton = new Button("Clear [esc]");
		
//		Box.Filler space = new javax.swing.Box.Filler(new java.awt.Dimension(SPACE_X, SPACE_Y), new java.awt.Dimension(SPACE_X, SPACE_Y), new java.awt.Dimension(SPACE_X, SPACE_Y));
//		this.add(space);
		
		TextField textField = new TextField();
		textField.setPrefWidth(TEXT_FIELD_WIDTH);
		textField.setMaxWidth(USE_PREF_SIZE);
		textField.setMinWidth(USE_PREF_SIZE);

		Button searchButton = new Button("Search");
		
		this.getItems().setAll(label,downloadButton,clearButton,textField,searchButton);

		//set the button handlers
		
		downloadButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				downloadPressed();
			}
		});		
		
		clearButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				clearPressed();
			}
		});
		
		searchButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				searchPressed();
			}
		});
	}
	
	private void downloadPressed() {
		downloadEditorMode.doDownload();
	}
	
	private void clearPressed() {
		downloadEditorMode.clearSelection();
	}
	
	private void searchPressed() {
		String searchText = this.searchField.getText();
			if(searchText != null) {
				JOptionPane.showMessageDialog(null,"You must enter a search string");
				return;
			}
			downloadEditorMode.doSearch(searchText);
	}
}
