package intransix.osm.termite.app.mapdata;

import intransix.osm.termite.map.workingdata.*;
import intransix.osm.termite.map.dataset.*;
import intransix.osm.termite.app.mapdata.instruction.EditAction;
import intransix.osm.termite.app.preferences.Preferences;
import java.util.ArrayList;
import java.util.List;
import intransix.osm.termite.render.map.RenderLayer;
import intransix.osm.termite.render.checkout.DownloadLayer;
import intransix.osm.termite.util.JsonIO;
import javax.swing.JOptionPane;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class MapDataManager {

	private OsmDataSet dataSet;
	private OsmData osmData;
	private List<MapDataListener> mapDataListeners = new ArrayList<MapDataListener>();
	
	private RenderLayer renderLayer;
	private DownloadLayer downloadLayer;
	private DownloadEditorMode downloadEditorMode;
	
	// start data source and instructions------------
	
	private final static long FIRST_ID = -1;
	public final static int INITIAL_DATA_VERSION = 0;
	
	private long nextId = FIRST_ID;
	
	private final static int FIRST_EDIT_NUMBER = 1;
	
	private int nextEditNumber = FIRST_EDIT_NUMBER;
	
	//the list of edit actions
	private List<EditAction> actions = new ArrayList<EditAction>();
	private int nextAddIndex = 0;
	
	public boolean dataPresent() {
		return (osmData != null);
	}
	
	/** This method gets the next available map object id, to be used for generating
	 * temporary IDs. */
	public synchronized long getNextId() {
		return nextId--;
	}
	
	
	/** This is a test method to load the latest edit number used. */
	public int test_getLatestEditNumber() {
		return nextEditNumber - 1;
	}
	
	//-----------------------------
	// Undo/Redo Actions
	//-----------------------------
	
	/** This method returns a description of the undo command. If there is no
	 * command to undo, null will be returned. 
	 * 
	 * @return	A description of the action to be done for undo
	 */
	public String getUndoMessage() {
		if(nextAddIndex > 0) {
			EditAction undoAction = actions.get(nextAddIndex - 1);
			String desc = undoAction.getDesc();
			if(desc == null) {
				desc = "Unspecified action";
			}
			return desc;
		}
		else {
			return null;
		}
	}
	
	public String getRedoMessage() {
		if(nextAddIndex < actions.size()) {
			EditAction redoAction = actions.get(nextAddIndex);
			String desc = redoAction.getDesc();
			if(desc == null) {
				desc = "Unspecified action";
			}
			return desc;
		}
		else {
			return null;
		}
	}
	
	public boolean undo() {
		if(nextAddIndex > 0) {
			EditAction undoAction = actions.get(nextAddIndex - 1);
			try {
				nextAddIndex--;
				undoAction.undoAction();
				return true;
			}
			catch(Exception ex) {
				ex.printStackTrace();
				reportFatalError(undoAction.getDesc(),ex.getMessage());
				return false;
			}
		}
		else {
			JOptionPane.showMessageDialog(null,"There is no action to undo.");
			return false;
		}
	}
	
	public boolean redo() {
		if(nextAddIndex < actions.size()) {
			EditAction redoAction = actions.get(nextAddIndex);
			try {
				nextAddIndex++;
				redoAction.doAction();
				return true;
			}
			catch(Exception ex) {
				ex.printStackTrace();
				reportFatalError(redoAction.getDesc(),ex.getMessage());
				return false;
			}
		}
		else {
			JOptionPane.showMessageDialog(null,"There is no action to redo.");
			return false;
		}
	}
	
	/** This method clears the command queue. */
	public void clearCommandQueue() {
		actions.clear();
		nextAddIndex = 0;
	}
	
	/** This method saves the actions to the queue. If there are any actions that
	 * can be redone, they will be removed before this is added. 
	 * 
	 * @param action	The action to add 
	 */
	public void saveAction(EditAction action) {
		//get rid of any actions after this one
		while(nextAddIndex < actions.size()) {
			actions.remove(actions.size() - 1);
		}
		actions.add(action);
		nextAddIndex = actions.size();
	}
	
	/** This method gets the next available edit number, to be used for data
	 * versioning within the editor. */
	public synchronized int getNextEditNumber() {
		return nextEditNumber++;
	}
	
	
	/** This method notifies any data changed listeners. It should be called 
	 * when the data changes.
	 * 
	 * @param editNumber	This is the data version for any data changed in this edit. 
	 */
	public void dataChanged(int editNumber) {
		//notify listeners
		synchronized(mapDataListeners) {
			for(MapDataListener listener:mapDataListeners) {
				listener.osmDataChanged(editNumber);
			}
		}
	}

	
	private void reportFatalError(String actionDesc, String exceptionMsg) {
		JOptionPane.showMessageDialog(null,"There was a fatal error on the action: " + actionDesc +
				"; " + exceptionMsg + "The application must exit");
		System.exit(-1);
	}
	
	// end data source and instructions ------------------------------
	
	public void init() throws Exception {
		
		String configFileName = Preferences.getProperty("modelFile");
		if(configFileName == null) {
			throw new Exception("OSM Model file not found.");
		}
		JSONObject modelJson = JsonIO.readJsonFile(configFileName);
		OsmModel.parse(modelJson);
		
		renderLayer = new RenderLayer(this);
		this.addMapDataListener(renderLayer);
		
		downloadLayer = new DownloadLayer();
		downloadEditorMode = new DownloadEditorMode(this,downloadLayer);
	}
	
	public DownloadLayer getDownloadLayer() {
		return downloadLayer;
	}
	
	public RenderLayer getRenderLayer() {
		return renderLayer;
	}
	
	public DownloadEditorMode getDownloadEditorMode() {
		return downloadEditorMode;
	}
	
	/** This method gets the map data. */
	public OsmData getOsmData() {
		return osmData;
	}
	
	/** This method gets the map data. */
	public OsmDataSet getDataSet() {
		return dataSet;
	}
	
	public void setData(OsmDataSet dataSet) {
		this.dataSet = dataSet;
		int initialVersionNumber = OsmData.INVALID_DATA_VERSION;
		
		if(dataSet != null) {
			initialVersionNumber = this.getNextEditNumber();
			osmData = new OsmData();
			osmData.loadFromDataSet(dataSet,initialVersionNumber);
		}
		else {
			osmData = null;
		}
		
		synchronized(mapDataListeners) {
			for(MapDataListener listener:mapDataListeners) {
				listener.onMapData(osmData != null);
			}
		}
		
		if(osmData != null) {
			dataChanged(initialVersionNumber);
		}
	}
	
	public void clearData() {
		setData(null);
	}
	
	/** This adds a map data listener. */
	public void addMapDataListener(MapDataListener listener) {
		synchronized(mapDataListeners) {
			for(MapDataListener l:mapDataListeners) {
				if(l.getMapDataListenerPriority() > listener.getMapDataListenerPriority()) {
					mapDataListeners.add(listener);
					return;
				}
			}
			mapDataListeners.add(listener);
		}
	}

	/** This removes a map data listener. */
	public void removeMapDataListener(MapDataListener listener) {
		synchronized(mapDataListeners) {
			mapDataListeners.remove(listener);
		}
	}	

}
