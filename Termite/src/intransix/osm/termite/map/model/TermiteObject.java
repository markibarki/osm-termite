package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.prop.FeatureInfo;

/**
 *
 * @author sutter
 */
public abstract class TermiteObject {
	
	private Object renderData;
	private Object editData;
	
	FeatureInfo featureInfo = null;
	
	
	public FeatureInfo getFeatureInfo() {
		return featureInfo;
	}

	public Object getRenderData() {
		return renderData;
	}
	
	public void setRenderData(Object renderData) {
		this.renderData = renderData;
	}
	
	public Object getEditData() {
		return editData;
	}
	
	public void setEditData(Object editData) {
		this.editData = editData;
	}
	
	//=======================
	// Package Methods
	//=======================
	
	abstract OsmObject getOsmObject();
	
	void classify() {
		featureInfo = OsmModel.featureInfoMap.getFeatureInfo(getOsmObject());
	}
}
