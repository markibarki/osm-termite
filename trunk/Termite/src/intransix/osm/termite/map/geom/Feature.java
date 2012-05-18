package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;
import intransix.osm.termite.theme.Style;
import java.awt.Graphics2D;

/**
 * This encapsulates a general map feature within termite (not osm)
 * @author sutter
 */
public abstract class Feature extends MapObject {
	
	//====================
	// Constants
	//====================
	
	/** This gives the feature type. */
	public enum FeatureType {
		POINT, LINE, MULTILINE, AREA, MULTIAREA
	}
	
	//====================
	// Private Proeprties
	//====================

	private FeatureType featureType;
	private Style style;
	
	//====================
	// Public Methods
	//====================
	
	/** This method returns the feature type.
	 * 
	 * @return		The feature type 
	 */
	public FeatureType getFeatureType() {
		return featureType;
	}

	public void setStyle(Style style) {
		this.style = style;
	}
	
	public Style getStyle() {
		return style;
	}

	/** This clears the style value. */
	public void clearStyle() {
		this.style = null;
	}
	
	/** renders the feature. */
	public abstract void render(Graphics2D g);
	
	//====================
	// Public Methods
	//====================
	
	/** Constructor. It should only be called by members of this package. */
	Feature(FeatureType featureType) {
		this.featureType = featureType;
	}
	
	
}
