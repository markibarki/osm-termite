package intransix.osm.termite.render.source.dialog;

import intransix.osm.termite.app.geocode.GeocodeManager;
import intransix.osm.termite.app.maplayer.MapLayer;
import intransix.osm.termite.app.maplayer.MapLayerListener;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.gui.dialog.MessageDialog;
import intransix.osm.termite.gui.dialog.TermiteDialog;
import intransix.osm.termite.render.source.SourceLayer;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;


/** This class provides the UI content for the source dialog box. */
public class SourceDialogContent extends VBox implements Initializable {
	
	private TermiteDialog dialog;
	
	private ViewRegionManager viewRegionManager;
	private MapLayerManager mapLayerManager;
	private GeocodeManager geocodeManager;

	@FXML //  fx:id="addButton"
    private Button addButton; // Value injected by FXMLLoader
	
    @FXML //  fx:id="removeButton"
    private Button removeButton; // Value injected by FXMLLoader

	@FXML //  fx:id="tableView"
    private SourceListTable sourceListTable; // Value injected by FXMLLoader
	
	/** Constructor */
	public SourceDialogContent(TermiteDialog dialog, MapLayerManager mapLayerManager, ViewRegionManager viewRegionManager, GeocodeManager geocodeManager) {
		this.dialog = dialog;
		this.viewRegionManager = viewRegionManager;
		this.mapLayerManager = mapLayerManager;
		this.geocodeManager = geocodeManager;
		
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
		sourceListTable.init(this);
		sourceListTable.updateLayerList(geocodeManager.getSourceLayers());

    }
	
	@FXML
	public void onAddButton() {
		FileChooser fileChooser = new FileChooser();
		File file = fileChooser.showOpenDialog(dialog);
		if(file != null) {
			createSourceLayer(file);
		}
	}
	
	@FXML
	public void onRemoveButton() {
		SourceLayer layer = sourceListTable.getSelectedLayer();
		if(layer != null) {
			deleteSourceLayer(layer);
		}
		else {
			MessageDialog.show(this.dialog,"No source image is selected.");
		}
	}
	
	/** This method creates a source layer from a file. */
	public void createSourceLayer(File file) {
		SourceLayer layer = new SourceLayer();

		//we need to set the initial transform in case we need a default transform set for the loaded image
		layer.onMapViewChange(viewRegionManager, true);
		layer.loadImage(file);

		geocodeManager.addSourceLayer(layer);
		setLayerVisible(layer,true);
		
		sourceListTable.updateLayerList(geocodeManager.getSourceLayers());
	}
	
	/** This method hides or shows a source layer. */
	public void setLayerVisible(SourceLayer layer, boolean visible) {
		if(visible) {
			mapLayerManager.addLayer(layer);
			layer.onMapViewChange(viewRegionManager, true);
			viewRegionManager.addMapListener(layer);
			layer.setIsActive(true);
		}
		else {
			mapLayerManager.removeLayer(layer);
			viewRegionManager.removeMapListener(layer);
			layer.setIsActive(false);
		}
	}
	
	/** This method deletes a source layer. */
	public void deleteSourceLayer(SourceLayer layer) {
		mapLayerManager.removeLayer(layer);
		geocodeManager.removeSourceLayer(layer);
		viewRegionManager.removeMapListener(layer);
		
		sourceListTable.updateLayerList(geocodeManager.getSourceLayers());
	}
	
	

}
