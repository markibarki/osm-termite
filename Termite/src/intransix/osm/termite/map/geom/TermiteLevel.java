package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.osm.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author sutter
 */
public class TermiteLevel extends TermiteObject {
	
	public final static int INVALID_ZLEVEL = Integer.MIN_VALUE;
	
	private TermiteStructure structure;
	private TermiteFeature shell;
	private ArrayList<TermiteFeature> features = new ArrayList<TermiteFeature>();
	private ArrayList<FeatureLevelGeom> geomList = new ArrayList<FeatureLevelGeom>();
	
	//valid only for method 1
	private OsmRelation osmRelation;
	
	public TermiteStructure getStructure() {
		return structure;
	}
	
	public int getZlevel() {
		return this.getIntProperty(OsmModel.KEY_ZLEVEL,INVALID_ZLEVEL);
	}
	
	public long getStructureId() {
		return this.getLongProperty(OsmModel.KEY_ZCONTEXT,INVALID_ID);
	}
	
	public void addFeature(TermiteFeature feature) {
		if(features.contains(feature)) return;
		features.add(feature);
		FeatureLevelGeom flg = feature.addLevel(this);
		geomList.add(flg);
	}
	
	public ArrayList<TermiteFeature> getFeatures() {
		return features;
	}
	
	public ArrayList<FeatureLevelGeom> getLevelGeom() {
		return geomList;
	}
	
	public void orderFeatures() {
		Collections.sort(geomList);
	}
	
	//====================
	// Package Methods
	//====================
	
	/** Constructor. It should only be called by members of this package. */
	TermiteLevel(long id) {
		super(id);
	}

	void setStructure(TermiteStructure structure) {
		this.structure = structure;
	}
	
	void load(OsmRelation osmRelation, TermiteData data) {
		this.osmRelation = osmRelation;
		copyProperties(osmRelation);
		
		//load members
		for(OsmMember osmMember:osmRelation.getMembers()) {
			long memberId = osmMember.member.getId();
			OsmObject member = osmMember.member;
			//only allow multi ways
			if(osmMember.role.equalsIgnoreCase(OsmModel.ROLE_FEATURE)) {
				TermiteFeature feature;
				if(member instanceof OsmNode) {
					//create a virtual way and feature for this node
					TermiteNode termiteNode = data.getTermiteNode(memberId, true);
					feature = data.createVirtualFeatureForNode(termiteNode);
				}
				else if(member instanceof OsmWay) {
					//create a virutal feature for this way
					TermiteWay termiteWay = data.getTermiteWay(memberId, true);
					feature = data.createVirtualFeatureForWay(termiteWay);
				}
				else {
					//just look up feature directly
					feature = data.getTermiteFeature(memberId, true);
				}
				
				//add feature to level
				if(feature != null) {
					this.addFeature(feature);
				}
			}
			else if(osmMember.role.equalsIgnoreCase(OsmModel.ROLE_SHELL)) {
				//get level shell
				TermiteWay way = data.getTermiteWay(memberId, true);
				shell = data.createVirtualFeatureForWay(way);
				this.addFeature(shell);
			}
		}
	}
	
	void loadLevelFromShell(OsmWay osmShell, TermiteFeature termiteShell) {
		this.osmRelation = null;
		this.shell = termiteShell;
		copyProperties(osmShell);
	}
}
