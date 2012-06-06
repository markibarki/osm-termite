package intransix.osm.termite.map.model;

import java.util.ArrayList;
import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.map.prop.FeatureInfo;

/**
 *
 * @author sutter
 */
public class TermiteWay extends TermiteObject {
	
	private OsmWay osmWay;
	
	private boolean isArea = false;
	
	private ArrayList<TermiteLevel> levels = new ArrayList<TermiteLevel>();
	
	private TermiteMultiPoly multiPoly;
	
	public ArrayList<TermiteLevel> getLevels() {
		return levels;
	}
	
	public boolean getIsArea() {
		return isArea;
	}
	
	public OsmWay getOsmWay() {
		return osmWay;
	}

	//====================
	// Package Methods
	//====================
	
	void addLevel(TermiteLevel level) {
		if(!levels.contains(level)) levels.add(level);
		level.addWay(this);
	}
	
	void setOsmWay(OsmWay osmWay) {
		this.osmWay = osmWay;
	}
	
	@Override
	OsmObject getOsmObject() {
		return osmWay;
	}
	
	/** This method overrides the classify method to add the functionality 
	 * of checking the default area parameter. */
	@Override
	void classify() {
		super.classify();
		
		//check for setting the area parameter
		FeatureInfo featureInfo = this.getFeatureInfo();
		if(featureInfo != null) {
			if(osmWay.getProperty("area") == null) {
				this.isArea = (featureInfo.getDefaultPath() == FeatureInfo.GEOM_TYPE_AREA);
			}
		}
	}
	

	
	

	
	
}
