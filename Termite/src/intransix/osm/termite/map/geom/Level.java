package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;
import java.util.ArrayList;

/**
 *
 * @author sutter
 */
public class Level extends MapObject {
	
	private ArrayList<Feature> features = new ArrayList<Feature>();
	
	public void addFeature(Feature feature) {
		this.features.add(feature);
	}
	
	public ArrayList<Feature> getFeatures() {
		return features;
	}
	
}
