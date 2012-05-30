package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author sutter
 */
public class TermiteLevel extends MapObject {
	
	private TermiteStructure structure;
	private ArrayList<TermiteFeature> features = new ArrayList<TermiteFeature>();
	
	public TermiteStructure getStructure() {
		return structure;
	}
	
	public void addFeature(TermiteFeature feature) {
		this.features.add(feature);
		feature.setLevel(this);
	}
	
	public ArrayList<TermiteFeature> getFeatures() {
		return features;
	}
	
	public TermiteFeature getFeature(String idString) {
//implement this!!!
return null;
	}
	
	public void orderFeatures() {
		Collections.sort(features);
	}
	
	//====================
	// Package Methods
	//====================

	void setStructure(TermiteStructure structure) {
		this.structure = structure;
	}
}
