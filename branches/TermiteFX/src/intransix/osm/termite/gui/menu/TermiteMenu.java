/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.menu;

import intransix.osm.termite.app.TermiteFX;
import intransix.osm.termite.app.basemap.BaseMapListener;
import intransix.osm.termite.app.basemap.BaseMapManager;
import intransix.osm.termite.app.mapdata.MapDataListener;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.render.tile.TileInfo;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;

/**
 *
 * @author sutter
 */
public class TermiteMenu extends HBox implements Initializable, MapDataListener, BaseMapListener {
	
	//=========================
	// Constants
	//=========================
	private final static String UNDO_BASE_TEXT = "Undo";
	private final static String REDO_BASE_TEXT = "Redo";
	private final static String UNDO_REDO_CONNECTOR = ": ";
	//=========================
	// Properties
	//=========================
	
	private TermiteFX app; 
	private BaseMapManager baseMapManager;
	private MapDataManager mapDataManager;
	
	//-------------------
	// UI Elements
	//-------------------
	@FXML
	private Menu baseMapMenu;
	@FXML
	private MenuItem noneBaseMapMenuItem;
	@FXML
	private MenuItem undoMenuItem;
	@FXML
	private MenuItem redoMenuItem;
	
	//============================
	// Public Methods
	//============================
	
	public TermiteMenu() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("TermiteMenu.fxml"));
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
		assert baseMapMenu != null : "Base map menu not loaded";
		assert noneBaseMapMenuItem != null : "None base map menu item not loaded";
		assert undoMenuItem != null : "Undo menu item not loaded";
		assert redoMenuItem != null : "Redo menu item not loaded";
		
    }
	
	public void setApp(TermiteFX app) {
		this.app = app;
	}
	
	/** This method sets the map data manager. */
	public void setMapDataManager(MapDataManager mapDataManager) {
		this.mapDataManager = mapDataManager;
		mapDataManager.addMapDataListener(this);
	}
	
	/** This method sets the base map manager. */
	public void setBaseMapManager(BaseMapManager baseMapManager) {
		this.baseMapManager = baseMapManager;
		//initialize the base map list
		EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
			public void handle(ActionEvent t) {
				onMapBaseSelection(t);
			}
		};
		//initialize the base map item
		noneBaseMapMenuItem.setUserData(null);
		noneBaseMapMenuItem.setOnAction(handler);
		//initialize map list
		for(TileInfo tileInfo:baseMapManager.getBaseMapList()) {
			MenuItem menuItem = new MenuItem(tileInfo.getName());
			menuItem.setUserData(tileInfo);
			menuItem.setOnAction(handler);
			baseMapMenu.getItems().add(menuItem);
		}
		
		//add a listener to highlight the selected base map
		baseMapManager.addBaseMapListener(this);
	}
	
	//-----------------------
	// Listeners
	//-----------------------
	
	/** This method is called when the map data is set or cleared. It will be called 
	 * with the value true when the data is set and false when the data is cleared. The
	 * method osmDataChanged is also called when the data is set.
	 * 
	 * @param dataPresent	Set to true if data is present, false if data is cleared.
	 */
	@Override
	public void onMapData(boolean dataPresent) {
		//update the menu items
		osmDataChanged(0);
	}
	
	/** This method is called when the data has changed. It is used here to update the undo/redo status and message
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	@Override
	public void osmDataChanged(int editNumber) {
		String undoMessage = mapDataManager.getUndoMessage();
		if(undoMessage != null) {
			undoMenuItem.setDisable(false);
			undoMenuItem.setText(UNDO_BASE_TEXT + UNDO_REDO_CONNECTOR + undoMessage);
		}
		else {
			undoMenuItem.setText(UNDO_BASE_TEXT);
			undoMenuItem.setDisable(true);
		}
		String redoMessage = mapDataManager.getRedoMessage();
		if(redoMessage != null) {
			redoMenuItem.setDisable(false);
			redoMenuItem.setText(REDO_BASE_TEXT + UNDO_REDO_CONNECTOR + redoMessage);
		}
		else {
			redoMenuItem.setText(REDO_BASE_TEXT);
			redoMenuItem.setDisable(true);
		}
	}
	
	/** This method returns the priority for the listener. It is used to determine
	 * the order the listeners get called.
	 * 
	 * @return				The priority of the map listener
	 */
	@Override
	public int getMapDataListenerPriority() {
		return PRIORITY_DATA_CONSUME;
	}
	
	/** This method is called when the baseMap changes. */
	@Override
	public void baseMapChanged(TileInfo tileInfo) {
		
	}
	
	//-----------------------
	// Event handlers
	//------------------------
	@FXML
	public void onFileCommit(ActionEvent e) {
		
	}
	
	@FXML
	public void onFileClearData(ActionEvent e) {
		
	}
	
	@FXML
	public void onFileQuit(ActionEvent e) {
		app.exit();
	}
	
	@FXML
	public void onEditUndo(ActionEvent e) {
		mapDataManager.undo();
	}
	
	@FXML
	public void onEditRedo(ActionEvent e) {
		mapDataManager.redo();
	}
	
	@FXML
	public void onMapBaseSelection(ActionEvent e) {
		Object baseData = ((MenuItem)(e.getSource())).getUserData();
		if(baseData instanceof TileInfo) {
			baseMapManager.setBaseMap((TileInfo)baseData);
		}
		else {
			baseMapManager.setBaseMap(null);
		}
		
	}
	
	@FXML
	public void onMapSource(ActionEvent e) {
		
	}
	
	@FXML
	public void onDevPublish(ActionEvent e) {
		
	}
	
	@FXML
	public void onHelpAbout(ActionEvent e) {
		
	}
}
