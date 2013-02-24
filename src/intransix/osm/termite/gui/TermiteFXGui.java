package intransix.osm.termite.gui;

import intransix.osm.termite.app.TermiteFX;
import intransix.osm.termite.app.basemap.BaseMapListener;
import intransix.osm.termite.app.basemap.BaseMapManager;
import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import intransix.osm.termite.gui.feature.FeatureTree;
import intransix.osm.termite.gui.layer.LayerOpacityTable;
import intransix.osm.termite.gui.level.ContentTree;
import intransix.osm.termite.gui.map.MapPane;
import intransix.osm.termite.gui.menu.TermiteMenu;
import intransix.osm.termite.gui.mode.EditorModeManager;
import intransix.osm.termite.gui.mode.download.DownloadEditorMode;
import intransix.osm.termite.gui.mode.edit.NodeEditorMode;
import intransix.osm.termite.gui.mode.edit.SelectEditorMode;
import intransix.osm.termite.gui.mode.edit.WayEditorMode;
import intransix.osm.termite.gui.mode.source.GeocodeEditorMode;
import intransix.osm.termite.gui.property.PropertyPane;
import intransix.osm.termite.gui.task.ShutdownTask;
import intransix.osm.termite.gui.toolbar.TermiteToolBar;
import intransix.osm.termite.map.theme.Theme;
import intransix.osm.termite.render.checkout.DownloadLayer;
import intransix.osm.termite.render.edit.EditLayer;
import intransix.osm.termite.render.map.RenderLayer;
import intransix.osm.termite.render.source.GeocodeLayer;
import intransix.osm.termite.render.tile.TileInfo;
import intransix.osm.termite.render.tile.TileLayer;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class TermiteFXGui extends VBox implements Initializable, BaseMapListener {
	
	private static Stage stage;
	public static Stage getStage() {
		return stage;
	}
	
	private TermiteFX app;
	
	//managers
	private ViewRegionManager viewRegionManager;
	private EditorModeManager editorModeManager;
	private MapLayerManager mapLayerManager;
	
	//modes
	private DownloadEditorMode downloadEditorMode;
	private SelectEditorMode selectEditorMode;
	private NodeEditorMode nodeEditorMode;
	private WayEditorMode wayEditorMode;
	private GeocodeEditorMode geocodeEditorMode;
	
	//layers
	private TileLayer baseMapLayer;
	private DownloadLayer downloadLayer;
	private RenderLayer renderLayer;
	private EditLayer editLayer;
	private GeocodeLayer geocodeLayer;
	
	
	//top level containers
    @FXML
    private ScrollPane contentScrollPane; // Value injected by FXMLLoader
	@FXML
    private ScrollPane featureScrollPane; // Value injected by FXMLLoader
	@FXML 
    private TabPane propertyTabPane; // Value injected by FXMLLoader
	@FXML
    private MapPane mapPane; // Value injected by FXMLLoader
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
	
	/** Constructor */
	public TermiteFXGui(TermiteFX app, Stage stage) {
		this.app = app;
		TermiteFXGui.stage = stage;
		
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
	public void load() throws Exception {
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
		
		//---------------
		//create managers
		//---------------
		
		//view region manager
		viewRegionManager = new ViewRegionManager(mapPane);
		viewRegionManager.setInitialView();
		//pass initial view
		mapPane.onMapViewChange(viewRegionManager, true);
		viewRegionManager.addMapListener(mapPane);
		//configure so state is saved on shutdown
		app.addShutdownListener(viewRegionManager);	
		
		//map layer manager
		mapLayerManager = new MapLayerManager();
		mapLayerManager.addLayerListener(mapPane);
		
		//editor manager
		editorModeManager = new EditorModeManager();
		termiteToolBar.setEditorModeManager(editorModeManager);
		
		//-------------
		//create modes
		//-------------
		
		downloadEditorMode = new DownloadEditorMode();
		editorModeManager.addMode(downloadEditorMode);
		
		selectEditorMode = new SelectEditorMode();
		editorModeManager.addMode(selectEditorMode);
		
		nodeEditorMode = new NodeEditorMode();
		editorModeManager.addMode(nodeEditorMode);
		
		wayEditorMode = new WayEditorMode();
		editorModeManager.addMode(wayEditorMode);
		
		geocodeEditorMode = new GeocodeEditorMode();
		editorModeManager.addMode(geocodeEditorMode);
		
		termiteToolBar.initModes();
		editorModeManager.setDefaultModes(downloadEditorMode,selectEditorMode);
		
		//--------------
		//create layers
		//--------------
		
		baseMapLayer = new TileLayer();
		baseMapLayer.setActiveState(true);
		mapLayerManager.addLayer(baseMapLayer);
		//passs initial view
		baseMapLayer.onMapViewChange(viewRegionManager, true);
		viewRegionManager.addMapListener(baseMapLayer);
		//needed to update config when tiles change
		baseMapLayer.setViewRegionManager(viewRegionManager);
		
		downloadLayer = new DownloadLayer();
		downloadEditorMode.setDownloadLayer(downloadLayer);
		mapLayerManager.addLayer(downloadLayer);
		//pass initial view
		downloadLayer.onMapViewChange(viewRegionManager, true);
		viewRegionManager.addMapListener(downloadLayer);
		
		renderLayer = new RenderLayer();
		mapLayerManager.addLayer(renderLayer);
renderLayer.onMapViewChange(viewRegionManager, true);
		viewRegionManager.addMapListener(renderLayer);
		
		editLayer = new EditLayer();
		
		geocodeLayer = new GeocodeLayer();
		
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

		//key handlers for navigation
		this.addEventFilter(KeyEvent.KEY_PRESSED,new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent e) {
				EventTarget target = e.getTarget();
				System.out.println("Target: " + e.getTarget());
				if(target instanceof Control) {
					System.out.println("Event passed through");
				}
				else {
					System.out.println(e.getCode());
					keyPressed(e);
					e.consume();
				}
			}
		});



    }
	
	public void setTheme(Theme theme) {
		renderLayer.setTheme(theme);
	}
	
	public void setMapDataManager(MapDataManager mapDataManager) {
		termiteMenu.setMapDataManager(mapDataManager);
		downloadEditorMode.setMapDataManager(mapDataManager);
		renderLayer.setMapDataManager(mapDataManager);
		
		mapDataManager.addMapDataListener(editorModeManager);
		mapDataManager.addMapDataListener(renderLayer);
		
	}
	
	public void setFeatureTypeManager(FeatureTypeManager featureTypeManager) {
		renderLayer.setFeatureTypeManager(featureTypeManager);
	}
	
	public void setBaseMapManager(BaseMapManager baseMapManager) {
		termiteMenu.setBaseMapManager(baseMapManager);
		TileInfo tileInfo = baseMapManager.getBaseMapInfo();
		this.baseMapChanged(tileInfo);
		baseMapManager.addBaseMapListener(this);
	}
	
	/** This method is called when the baseMap changes. */
	@Override
	public void baseMapChanged(TileInfo tileInfo) {
		baseMapLayer.setTileInfo(tileInfo);
		baseMapLayer.setVisible( (tileInfo != null) ? true : false);
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
	
	//key inputs
	
	private final double PAN_PIX = 30;
	private final double ZOOM_FACTOR = 1.3;
	
	private void keyPressed(KeyEvent e) {
		if(viewRegionManager == null) return;
		
		switch(e.getCode()) {
			case UP:
				viewRegionManager.translate(0,PAN_PIX);
				break;
				
			case DOWN:
				viewRegionManager.translate(0,-PAN_PIX);
				break;
				
			case RIGHT:
				viewRegionManager.translate(-PAN_PIX,0);
				break;
				
			case LEFT:
				viewRegionManager.translate(PAN_PIX,0);
				break;
				
			case PAGE_UP:
				viewRegionManager.zoom(ZOOM_FACTOR); 
				break;
			
			case PAGE_DOWN:
				viewRegionManager.zoom(1/ZOOM_FACTOR);
				break;
				
			case COMMA:
				rotAngRad += .2;
				viewRegionManager.setRotation(rotAngRad);
				break;
				
			case PERIOD:
				rotAngRad -= .2;
				viewRegionManager.setRotation(rotAngRad);
				break;
		}
	}
	
	private double rotAngRad = 0;
	
}


