package intransix.osm.termite.render.source.dialog;

import intransix.osm.termite.app.geocode.GeocodeManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;


public class SourceDialogContent extends VBox implements Initializable {

	@FXML //  fx:id="addButton"
    private Button addButton; // Value injected by FXMLLoader
	
    @FXML //  fx:id="removeButton"
    private Button removeButton; // Value injected by FXMLLoader

	@FXML //  fx:id="tableView"
    private SourceListTable sourceListTable; // Value injected by FXMLLoader
	
	public SourceDialogContent() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SourceDialogContent.fxml"));
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
        assert removeButton != null : "fx:id=\"removeButton\" was not injected: check your FXML file 'PropertyPane.fxml'.";
        assert addButton != null : "fx:id=\"addButton\" was not injected: check your FXML file 'PropertyPane.fxml'.";
        assert sourceListTable != null : "fx:id=\"sourceListTable\" was not injected: check your FXML file 'PropertyPane.fxml'.";

        // initialize your logic here: all @FXML variables will have been injected

    }
	
	
	@FXML
	public void onAddButton() {
		
	}
	
	@FXML
	public void onRemoveButton() {
		
	}
	
	public void setGeocodeManager(GeocodeManager geocodeManager) {
		this.sourceListTable.setData(geocodeManager.getSourceLayers());
	}
}
