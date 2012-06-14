package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.feature.FeatureInfo;

/**
 * This is a base class for the TermiteObject.
 * 
 * @author sutter
 */
public abstract class TermiteObject<T extends OsmObject> {
	
	//=========================
	// Properties
	//=========================
	
	public final static int INVALID_TERMITE_VERSION = -1;
	
	private Object renderData;
	private Object editData;
	
	FeatureInfo featureInfo = null;
	
private int termiteLocalVersion = 0;
public int getTermiteLocalVersion() {
	return termiteLocalVersion;
}
void incrementTermiteVersion() {
	termiteLocalVersion++;
}
	
	//=========================
	// Public Methods
	//=========================
	
	/** This method gets the feature info associated with this object. */
	public FeatureInfo getFeatureInfo() {
		return featureInfo;
	}

	/** This method returns the render data.  */
	public Object getRenderData() {
		return renderData;
	}
	
	/** This method sets the render data. The render data is an arbitrary object
	 * that is set by the rendering software. */
	public void setRenderData(Object renderData) {
		this.renderData = renderData;
	}
	
	/** This method returns the edit data. */
	public Object getEditData() {
		return editData;
	}
	
	/** This method sets the edit data. The edit data is an arbitrary object
	 * that is set by the edit software.  */
	public void setEditData(Object editData) {
		this.editData = editData;
	}
	
	//=======================
	// Package Methods
	//=======================
	
	/** This method should be filled in by extending objects to return the 
	 * OSM object for the given Termite Object.
	 * @return 
	 */
	public abstract T getOsmObject();
	
	/** This method determines the FeatureInfo for the object. */
	void classify() {
		featureInfo = OsmModel.featureInfoMap.getFeatureInfo(getOsmObject());
	}
	
	abstract void init(TermiteData termiteData, T osmObject);
	
	abstract void objectDeleted(TermiteData termiteData);
	
	void propertiesUpdated(TermiteData termiteData) {
		
	}
}
