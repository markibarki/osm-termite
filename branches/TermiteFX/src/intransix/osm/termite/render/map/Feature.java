package intransix.osm.termite.render.map;

import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.theme.Theme;

/**
 *
 * @author sutter
 */
public interface Feature {
	FeatureInfo getFeatureInfo();
	
	void initStyle(Theme theme);
	
}
