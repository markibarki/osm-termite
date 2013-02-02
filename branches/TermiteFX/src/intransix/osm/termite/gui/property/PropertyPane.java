package intransix.osm.termite.gui.property;

import intransix.osm.termite.gui.TermiteFXGui;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class PropertyPane extends VBox implements Initializable {

	@FXML //  fx:id="newButton"
    private Button newButton; // Value injected by FXMLLoader
	
    @FXML //  fx:id="deleteButton"
    private Button deleteButton; // Value injected by FXMLLoader

    @FXML //  fx:id="editButton"
    private Button editButton; // Value injected by FXMLLoader

	@FXML //  fx:id="tableView"
    private PropertyTable propertyTable; // Value injected by FXMLLoader
	
	public PropertyPane() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PropertyPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}


    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        assert deleteButton != null : "fx:id=\"deleteButton\" was not injected: check your FXML file 'PropertyPane.fxml'.";
        assert editButton != null : "fx:id=\"editButton\" was not injected: check your FXML file 'PropertyPane.fxml'.";
		assert newButton != null : "fx:id=\"newButton\" was not injected: check your FXML file 'PropertyPane.fxml'.";
        assert propertyTable != null : "fx:id=\"tableView\" was not injected: check your FXML file 'PropertyPane.fxml'.";

        // initialize your logic here: all @FXML variables will have been injected

    }
	
	
	@FXML
	public void onAddButton() {
		Stage stage = loadDialog();
		stage.show();
	}
	
	private Stage loadDialog() {
		
		final Stage dialog = new Stage(StageStyle.TRANSPARENT);
		dialog.initModality(Modality.WINDOW_MODAL);

		dialog.initOwner(TermiteFXGui.getStage());
		
		//create the layout
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));
		
		//add the title
		Text scenetitle = new Text("New Property");
		scenetitle.setId("dialog-title");
		grid.add(scenetitle, 0, 0, 2, 1);

		//user name field and label
		Label label = new Label("Key:");
		grid.add(label, 0, 1);

		final TextField nameField = new TextField();
		grid.add(nameField, 1, 1);
		
		//add a button - Use the hbox so we can use a different alignment for button
		Button okButton = new Button("OK");
		okButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				String name = nameField.getText();
				propertyTable.addData(name,name);
				dialog.hide();
			}
		});
		grid.add(okButton, 1, 2);
		
		Button cancelButton = new Button("Cancel");
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				dialog.hide();
			}
		});
		grid.add(cancelButton, 2, 2);

		//create the scene
		Scene scene = new Scene(grid, 300, 275);
		dialog.setScene(scene);
		
		return dialog;
	}

}
