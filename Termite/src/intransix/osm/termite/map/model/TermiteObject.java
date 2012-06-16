package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.*;

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
	
	private List<TermiteRelation> relations = new ArrayList<TermiteRelation>();
	
	FeatureInfo featureInfo = null;
	
private int dataVersion = 0;
public int getDataVersion() {
	return dataVersion;
}
void incrementDataVersion() {
	dataVersion++;
}

void setLocalDataVersion(int version) {
	this.dataVersion = version;
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
	
	/** This gets the list of relations of which this object is a member. */
	public List<TermiteRelation> getRelations() {
		return relations;
	}
	
	//=======================
	// Package Methods
	//=======================
	
	void addRelation(TermiteRelation relation) {
		if(!relations.contains(relation)) {
			relations.add(relation);
			incrementDataVersion();
		}
	}
	
	void removeRelation(TermiteRelation relation) {
		relations.remove(relation);
		incrementDataVersion();
	}
	
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
	
	void objectDeleted(TermiteData termiteData) {
		
		//this should be cleared before we delete
		//we must check for this earlier
		if(!relations.isEmpty()) {
			throw new RuntimeException("A relation referenced the deleted object");
		}
	}
	
	void propertiesUpdated(TermiteData termiteData) {
		
	}
	
	static <T> void removeAllCopies(List<T> list, T obj) {
		boolean objectRemoved;
		do {
			objectRemoved = list.remove(obj);
		} while(objectRemoved);
	}
}
