package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author sutter
 */
public class Level extends MapObject {
	
	private Structure structure;
	private ArrayList<Feature> features = new ArrayList<Feature>();
	
	public Structure getStructure() {
		return structure;
	}
	
	public void addFeature(Feature feature) {
		this.features.add(feature);
		feature.setLevel(this);
	}
	
	public ArrayList<Feature> getFeatures() {
		return features;
	}
	
	public Feature getFeature(String idString) {
//implement this!!!
return null;
	}
	
	public void orderFeatures() {
		Collections.sort(features);
	}
	
	//====================
	// Package Methods
	//====================

	void setStructure(Structure structure) {
		this.structure = structure;
	}
}
