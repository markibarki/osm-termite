package intransix.osm.termite.app.mode;

import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.app.mapdata.*;
import intransix.osm.termite.app.edit.*;
import java.util.*;

/**
 *
 * @author sutter
 */
public class EditorModeManager implements MapDataListener {
	
	//========================
	// Properties
	//========================
	
	private List<EditorMode> editorModes = new ArrayList<EditorMode>();
	private EditorMode activeMode = null; //the active editor mode
	private EditorMode defaultNonDataMode;
	private EditorMode defaultDataMode;
	
	private List<EditorModeListener> modeListeners = new ArrayList<EditorModeListener>();
	
	//========================
	// Public Methods
	//========================
	
	public EditorMode getActiveMode() {
		return activeMode;
	}
	
	public void setDefaultModes(EditorMode nonDataMode, EditorMode dataMode) {
		defaultNonDataMode = nonDataMode;
		defaultDataMode = dataMode;
	}
	
	public void addMode(EditorMode mode) {
		this.editorModes.add(mode);
		mode.setEditorModeManager(this);
	}
	
	/** This method sets the editor mode. */
	public void setEditorMode(EditorMode mode) {

		//get rid of old mode
		if(activeMode != null) {
			//turn off mode
			this.activeMode.turnOff();
			this.activeMode = null;
		}

		activeMode = mode;

		//prepare the new mode
		mode.turnOn();

		//set this mode as active
		this.activeMode = mode;
		for(EditorModeListener modeListener:modeListeners) {
			modeListener.activeModeChanged(activeMode);
		}
	}
	
	/** This method will dispatch a map data event. It should be called
	 * when a map data is set to notify all interested objects. */
	@Override
	public void onMapData(boolean dataPresent) {
		boolean modeEnabled;
		for(EditorMode mode:editorModes) {
			modeEnabled = mode.getEnableStateForDataState(dataPresent);
			if(modeEnabled != mode.getModeEnabled()) {
				mode.setEnabled(modeEnabled);
				notifyEnableChange(mode);
			}
		}
		
		//change to the default mode
		if(dataPresent) {
			//default mode for map data present
			setEditorMode(defaultDataMode);
		}
		else {
			//default mode for no map data present
			setEditorMode(defaultNonDataMode);
		}
	}
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	@Override			
	public void osmDataChanged(int editNumber){
		//no action
	}
	
	/** This method retrieves the editor modes. */
	public List<EditorMode> getEditorModes() {
		return editorModes;
	}
	
	/** This method adds a mode listener. */
	public void addModeListener(EditorModeListener modeListener) {
		if(!modeListeners.contains(modeListener)) {
			modeListeners.add(modeListener);
		}
	}
	
	/** This method removes a mode listener. */
	public void removeModeListener(EditorModeListener modeListener) {
		modeListeners.remove(modeListener);
	}
	
	//=================================
	// Package Methods
	//=================================
	
	/** This method notifies any listeners the a mode enabled changed. */
	void notifyEnableChange(EditorMode mode) {
		for(EditorModeListener modeListener:modeListeners) {
			modeListener.modeEnableChanged(mode);
		}
	}
	
}
