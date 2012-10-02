package intransix.osm.termite.app.mapdata;

import intransix.osm.termite.map.workingdata.*;
import intransix.osm.termite.map.dataset.*;
import intransix.osm.termite.app.mapdata.instruction.EditAction;
import intransix.osm.termite.app.preferences.Preferences;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.ArrayList;
import java.util.List;
import intransix.osm.termite.render.map.RenderLayer;
import intransix.osm.termite.render.checkout.DownloadLayer;
import intransix.osm.termite.util.JsonIO;
import java.util.*;
import javax.swing.JOptionPane;
import org.json.JSONObject;
import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.app.filter.FilterManager;
import intransix.osm.termite.app.filter.FilterListener;

/**
 *
 * @author sutter
 */
public class MapDataManager implements FilterListener {
	
	//download
	//commit
	//publish?
	
	private static int piggybackIndex;
	static {
		piggybackIndex = OsmObject.registerPiggybackUser();
	}

	private OsmDataSet dataSet;
	private OsmData osmData;
	private List<MapDataListener> mapDataListeners = new ArrayList<MapDataListener>();
	
	private RenderLayer renderLayer;
	private DownloadLayer downloadLayer;
	private DownloadEditorMode downloadEditorMode;
	
	private FilterManager filterManager;
	private FeatureTypeManager featureTypeManager;
	
	// start data source and instructions------------
	
	private final static long FIRST_ID = -1;
	public final static int INITIAL_DATA_VERSION = 0;
	
	private long nextId = FIRST_ID;
	
	private final static int FIRST_EDIT_NUMBER = 1;
	
	private int nextEditNumber = FIRST_EDIT_NUMBER;
	
	//the list of edit actions
	private List<EditAction> actions = new ArrayList<EditAction>();
	private int nextAddIndex = 0;
	
	//This list holds the object according to there presentation order as determined
	//by the feature info map
	private List<OsmObject> orderedFeatures = new ArrayList<OsmObject>();
	private FeatureLayerComparator flc = new FeatureLayerComparator();
	
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
	
	/** This method returns a ordered list of osm objects, ordered according to
	 * the feature info for the given objects. 
	 * 
	 * @return		An ordered list of objects 
	 */
	public List<OsmObject> getFeatureList() {
		return orderedFeatures;
	}
	
	/** This method is called when the filter changes. */
	@Override
	public void onFilterChanged() {
		if(osmData != null) {
			for(OsmNode node:osmData.getOsmNodes()) {
				updateFilter(node);
			}
			for(OsmWay way:osmData.getOsmWays()) {
				updateFilter(way);
			}
		}
	}
	
	public void setFeatureTypeManager(FeatureTypeManager featureTypeManager) {
		this.featureTypeManager = featureTypeManager;
	}
	
	public FeatureTypeManager getFeatureTypeManager() {
		return featureTypeManager;
	}
	
	public void setFilterManager(FilterManager filterManager) {
		this.filterManager = filterManager;
	}
	
	/** Convenience methods for accessing feature data. */
	public static FeatureInfo getObjectFeatureInfo(OsmObject osmObject) {
		FeatureData fd = (FeatureData)osmObject.getPiggybackData(piggybackIndex);
		if(fd != null) {
			return fd.getFeatureInfo();
		}
		else {
			return null;
		}
	}

	public static boolean getObjectEditEnabled(OsmObject osmObject) {
		FeatureData fd = (FeatureData)osmObject.getPiggybackData(piggybackIndex);
		if(fd != null) {
			return fd.editEnabled();
		}
		else {
			return true;
		}
	}
	
	public static boolean getObjectRenderEnabled(OsmObject osmObject) {
		FeatureData fd = (FeatureData)osmObject.getPiggybackData(piggybackIndex);
		if(fd != null) {
			return fd.renderEnabled();
		}
		else {
			return true;
		}
	}
	
	public static boolean getSegmentEditEnabled(OsmSegment segment) {
		return (getObjectEditEnabled(segment.getNode1()) && 
				getObjectEditEnabled(segment.getNode2()) );
	}
	
	public static boolean getSegmentRenderEnabled(OsmSegment segment) {
		return (getObjectRenderEnabled(segment.getNode1()) && 
				getObjectRenderEnabled(segment.getNode2()) );
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

		//update feature info
		orderedFeatures.clear();
		for(OsmNode node:osmData.getOsmNodes()) {
			processFeature(node);
			orderedFeatures.add(node);
		}
		for(OsmWay way:osmData.getOsmWays()) {
			processFeature(way);
			orderedFeatures.add(way);
		}
		Collections.sort(orderedFeatures,flc);

		//notify
		for(MapDataListener listener:mapDataListeners) {
			listener.osmDataChanged(editNumber);
		}
	}
	
	/** This method updates the feature info for the given object. */
	private void processFeature(OsmObject osmObject) {

		FeatureData featureData = (FeatureData)osmObject.getPiggybackData(piggybackIndex);
		if(featureData == null) {
			featureData = new FeatureData();
			osmObject.setPiggybackData(piggybackIndex, featureData);
		}
		
		if(!featureData.isUpToDate(osmObject)) {
			//filter state
			int filterState = filterManager.getFilterValue(osmObject);
			featureData.setFilterState(filterState);

			//feature info
			FeatureInfo featureInfo = featureTypeManager.getFeatureInfo(osmObject);
			featureData.setFeatureInfo(featureInfo);
			
			featureData.markAsUpToDate(osmObject);
		}
	}
	
	/** This method updates the feature info for the given object. */
	private void updateFilter(OsmObject osmObject) {
		FeatureData featureData = (FeatureData)osmObject.getPiggybackData(piggybackIndex);
		if(featureData == null) {
			//the object shoudl have a fully processed feature data entry
			//do not do a plain filter update
			processFeature(osmObject);
			return;
		}
		
		int filterState = filterManager.getFilterValue(osmObject);
		featureData.setFilterState(filterState);
		
		//manually update the render layer with the new filter
		this.renderLayer.notifyContentChange();
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
		
		for(MapDataListener listener:mapDataListeners) {
			listener.onMapData(osmData != null);
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
		mapDataListeners.add(listener);
	}

	/** This removes a map data listener. */
	public void removeMapDataListener(MapDataListener listener) {
		mapDataListeners.remove(listener);
	}	
	
	
	//========================
	// Classes
	//========================
	
	private class FeatureLayerComparator implements Comparator<OsmObject> {
		public int compare(OsmObject o1, OsmObject o2) {
			//remove sign bit, making negative nubmers larger than positives (with small enough negatives)
			FeatureData fd;
			FeatureInfo fi;
			
			fd = (FeatureData)o1.getPiggybackData(piggybackIndex);
			fi = fd.getFeatureInfo();
			int ord1 = (fi != null) ? fi.getZorder() : FeatureInfo.DEFAULT_ZORDER;
			
			fd = (FeatureData)o2.getPiggybackData(piggybackIndex);
			fi = fd.getFeatureInfo();
			int ord2 = (fi != null) ? fi.getZorder() : FeatureInfo.DEFAULT_ZORDER;
			return ord1 - ord2;
		}
	}
}
