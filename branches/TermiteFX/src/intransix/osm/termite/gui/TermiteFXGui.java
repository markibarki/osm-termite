package intransix.osm.termite.gui;

import intransix.osm.termite.app.TermiteFX;
import intransix.osm.termite.app.basemap.BaseMapManager;
import intransix.osm.termite.app.mapdata.MapDataManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import intransix.osm.termite.gui.feature.FeatureTree;
import intransix.osm.termite.gui.layer.LayerOpacityTable;
import intransix.osm.termite.gui.level.ContentTree;
import intransix.osm.termite.gui.menu.TermiteMenu;
import intransix.osm.termite.gui.property.PropertyPane;
import intransix.osm.termite.gui.task.ShutdownTask;
import intransix.osm.termite.gui.toolbar.TermiteToolBar;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import javax.swing.JOptionPane;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author sutter
 */
public class TermiteFXGui extends VBox implements Initializable {
	
	private static Stage stage;
	public static Stage getStage() {
		return stage;
	}
	
	private TermiteFX app;
	
	
	//top level containers
    @FXML
    private ScrollPane contentScrollPane; // Value injected by FXMLLoader
	@FXML
    private ScrollPane featureScrollPane; // Value injected by FXMLLoader
	@FXML 
    private TabPane propertyTabPane; // Value injected by FXMLLoader
	@FXML
    private StackPane mapPane; // Value injected by FXMLLoader
	@FXML
    private TabPane dataTabPane; // Value injected by FXMLLoader
	
	//controls
	@FXML
	private ContentTree contentTree;
	@FXML
	private FeatureTree featureTree;
    @FXML
	private LayerOpacityTable layerOpacityTable;
	@FXML
	private TermiteMenu termiteMenu;
	@FXML
	private TermiteToolBar termiteToolBar;
	
	public TermiteFXGui(TermiteFX app) {
		this.app = app;
		
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TermiteFXGui.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
		
		try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
	}
	
	/** This method loads the gui. */
	public void load(Stage stage) throws Exception {
		TermiteFXGui.stage = stage;
		Scene scene = new Scene(this);
		stage.setScene(scene);
		stage.show();
		
		EventHandler<WindowEvent> handler = new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				shutdown();
			}
		};
		stage.setOnHiding(handler);
	}
	

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		assert termiteMenu != null : "fx:id=\"termiteMenu\" was not injected: check your FXML file 'FXMLTest.fxml'.";
		assert termiteToolBar != null : "fx:id=\"termiteToolBar\" was not injected: check your FXML file 'FXMLTest.fxml'.";
        assert contentScrollPane != null : "fx:id=\"contentScrollPane\" was not injected: check your FXML file 'FXMLTest.fxml'.";
        assert dataTabPane != null : "fx:id=\"dataTabPane\" was not injected: check your FXML file 'FXMLTest.fxml'.";
        assert featureScrollPane != null : "fx:id=\"featureScrollPane\" was not injected: check your FXML file 'FXMLTest.fxml'.";
        assert mapPane != null : "fx:id=\"mapPane\" was not injected: check your FXML file 'FXMLTest.fxml'.";
        assert propertyTabPane != null : "fx:id=\"propertyTabPane\" was not injected: check your FXML file 'FXMLTest.fxml'.";
		assert contentTree != null : "fx:id=\"contentTree\" was not injected: check your FXML file 'FXMLTest.fxml'.";
        assert featureTree != null : "fx:id=\"featureTree\" was not injected: check your FXML file 'FXMLTest.fxml'.";
		
		termiteMenu.setApp(app);

        // initialize your logic here: all @FXML variables will have been injected
		
		//add conent pane
/*		ContentTree contentTree = new ContentTree();
//		contentTree.init();
		contentScrollPane.setContent(contentTree);
		contentScrollPane.setFitToWidth(true);
		contentScrollPane.setFitToHeight(true);
*/

/*		
		//add conent pane
		FeatureTree featureTree = new FeatureTree();
		featureTree.init();
		featureScrollPane.setContent(featureTree);
		featureScrollPane.setFitToWidth(true);
		featureScrollPane.setFitToHeight(true);
*/	
featureTree.init();

		//add a property tab
		PropertyPane propertyPane = new PropertyPane();
		Tab tab = new Tab();
		tab.setContent(propertyPane);
		tab.setText("Test Tab");
		propertyTabPane.getTabs().add(tab);
		
		//add layer tab
/*
		LayerOpacityTable layerOpacityTable = new LayerOpacityTable();
		layerOpacityTable.init();
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		scrollPane.setContent(layerOpacityTable);
		tab = new Tab();
		tab.setContent(scrollPane);
		tab.setText("Map Layers");
		dataTabPane.getTabs().add(tab);
*/
layerOpacityTable.init();
		

    }
	
	public void setMapDataManager(MapDataManager mapDataManager) {
		termiteMenu.setMapDataManager(mapDataManager);
	}
	
	public void setBaseMapManager(BaseMapManager baseMapManager) {
		termiteMenu.setBaseMapManager(baseMapManager);
	}
	
	private void shutdown() {
		try {
			ShutdownTask st = new ShutdownTask(app);
			st.execute();
		}
		catch(Exception ex) {
			JOptionPane.showMessageDialog(null,"There was an error shutting down.");
		}
	}

}


