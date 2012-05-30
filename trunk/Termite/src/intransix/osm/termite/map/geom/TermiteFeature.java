package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;
import intransix.osm.termite.map.prop.FeatureInfo;
import intransix.osm.termite.theme.Style;
import intransix.osm.termite.map.osm.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

/**
 * This encapsulates a general map feature within termite (not osm)
 * @author sutter
 */
public abstract class TermiteFeature extends MapObject implements Comparable<TermiteFeature> {
	
	//====================
	// Constants
	//====================
	
	public final static int DEFAULT_ZORDER = 999;
	
	//====================
	// Private Proeprties
	//====================

	private TermiteLevel level;
	private Style style;
	private FeatureInfo featureInfo;

	private Shape shape;
	
	private OsmRelation osmRelation;
	private ArrayList<TermiteWay> ways = new ArrayList<TermiteWay>();
	private boolean isArea = false;
	
	//====================
	// Public Methods
	//====================
	
	public TermiteLevel getLevel() {
		return level;
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
	public void render(Graphics2D g) {
		Style style = this.getStyle();
		if((shape != null)&&(style != null)) {
			
			//load style params
			Color fillColor = null;
			Color strokeColor = null;
			Stroke stroke = null;
			if(isArea) {
				fillColor = style.getBodyColor();
				strokeColor = style.getOutlineColor();
			}
			else {
				fillColor = null;
				strokeColor = style.getBodyColor();
			}
			stroke = style.getStroke(1);
			
			//render the object	
			if(fillColor != null) {
				g.setPaint(fillColor);
				g.fill(shape);
			}
			if((strokeColor != null)&&(stroke != null)) {
				g.setStroke(stroke);
				g.setColor(strokeColor);
				g.draw(shape);
			}
			
			
		}
	}
	
	@Override
	public int compareTo(TermiteFeature other) {
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
	// Package Methods
	//====================
	
	/** Constructor. It should only be called by members of this package. */
	TermiteFeature() {}
	
	void setLevel(TermiteLevel level) {
		this.level = level;
	}
	
	
}
