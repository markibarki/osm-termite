package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;
import intransix.osm.termite.map.prop.FeatureInfo;
import intransix.osm.termite.theme.Style;
import java.awt.Graphics2D;

/**
 * This encapsulates a general map feature within termite (not osm)
 * @author sutter
 */
public abstract class Feature extends MapObject implements Comparable<Feature> {
	
	//====================
	// Constants
	//====================
	
	/** This gives the feature type. */
	public enum FeatureType {
		POINT, LINE, MULTILINE, AREA, MULTIAREA
	}
	
	public final static int DEFAULT_ZORDER = 999;
	
	//====================
	// Private Proeprties
	//====================

	private FeatureType featureType;
	private Style style;
	private FeatureInfo featureInfo;
	
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
	
	public void setFeatureInfo(FeatureInfo featureInfo) {
		this.featureInfo = featureInfo;
	}
	
	public FeatureInfo getFeatureInfo() {
		return featureInfo;
	}

	/** This clears the style value. */
	public void clearStyle() {
		this.style = null;
	}
	
	/** renders the feature. */
	public abstract void render(Graphics2D g);
	
	@Override
	public int compareTo(Feature other) {
		int otherZorder;
		int thisZorder;
		if(other.featureInfo == null) {
			otherZorder = FeatureInfo.DEFAULT_ZORDER;
		}
		else {
			otherZorder = other.featureInfo.getZorder();
		}
		if(this.featureInfo == null) {
			thisZorder = FeatureInfo.DEFAULT_ZORDER;
		}
		else {
			thisZorder = this.featureInfo.getZorder();
		}
		return thisZorder - otherZorder;	
	}
	
	//====================
	// Public Methods
	//====================
	
	/** Constructor. It should only be called by members of this package. */
	Feature(FeatureType featureType) {
		this.featureType = featureType;
	}
	
	
}
