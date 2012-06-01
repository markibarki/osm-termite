package intransix.osm.termite.map.geom;

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
public class TermiteFeature extends TermiteObject {
	
	//====================
	// Constants
	//====================
	
	public final static int DEFAULT_ZORDER = 999;
	
	//====================
	// Private Proeprties
	//====================

	private ArrayList<TermiteWay> ways = new ArrayList<TermiteWay>();
	private boolean isArea = false;
	private ArrayList<TermiteLevel> levels = new ArrayList<TermiteLevel>();
	
	private OsmRelation osmRelation;
	
	private Style style;
	private FeatureInfo featureInfo;
	private ArrayList<FeatureLevelGeom> featureGeomList = new ArrayList<FeatureLevelGeom>();
	private boolean isDirty = true;
	
	
	//====================
	// Public Methods
	//====================
	
	public boolean getIsArea() {
		return isArea;
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
	
	public void setIsArea(boolean isArea) {
		this.isArea = isArea;
	}
	
	/** This method should be called when the rendering information for the
	 * feature changes. */
	public void setDirty() {
		isDirty = true;
	}
	
	//====================
	// Package Methods
	//====================
	
	/** Constructor. It should only be called by members of this package. */
	TermiteFeature(long id) {
		super(id);
	}
	
	public boolean getIsDirty() {
		return isDirty;
	}
	
	public void setIsDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
	
	FeatureLevelGeom addLevel(TermiteLevel level) {
		this.levels.add(level);
		FeatureLevelGeom flg = new FeatureLevelGeom(this,level);
		this.featureGeomList.add(flg);
		return flg;
	}
	
	void addWay(TermiteWay way) {
		this.ways.add(way);
//we should check this feature doesn't already have a way
		way.setFeature(this);
	}
	
	void load(OsmRelation osmRelation, TermiteData data) {
		this.osmRelation = osmRelation;
		copyProperties(osmRelation);
		
		//load members
		for(OsmMember osmMember:osmRelation.getMembers()) {
			//only allow multi ways
			if(osmMember.member instanceof OsmWay) {
				TermiteWay termiteWay = data.getTermiteWay(osmMember.member.getId(), true);
				addWay(termiteWay);
			}
		}
	}
	
	ArrayList<TermiteWay> getWays() {
		return ways;
	}
	
	void classify() {
		FeatureInfo fi = TermiteData.getFeatureInfoMap().getFeatureInfo(this);
		this.setFeatureInfo(fi);

		if(fi != null) {
			//check for setting the area parameter
			if(this.getProperty("area") == null) {
				if(fi.getDefaultPath() == FeatureInfo.GEOM_TYPE_AREA) {
					this.setIsArea(true);
				}
			}
		}
	}
}
