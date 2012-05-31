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
	
	public final static String ROLE_FEATURE = "feature";
	public final static String ROLE_SHELL = "shell";
	
	public final static String TAG_ZLEVEL = "zlevel";
	public final static String TAG_STRUCTURE = "structure";
	
	private TermiteStructure structure;
	private TermiteWay shell;
	private ArrayList<TermiteFeature> features = new ArrayList<TermiteFeature>();
	private ArrayList<FeatureLevelGeom> geomList = new ArrayList<FeatureLevelGeom>();
	
	//valid only for method 1
	private OsmRelation osmRelation;
	
	public TermiteStructure getStructure() {
		return structure;
	}
	
	public int getZlevel() {
		return this.getIntProperty(TAG_ZLEVEL,INVALID_ZLEVEL);
	}
	
	public long getStructureId() {
		return this.getLongProperty(TAG_STRUCTURE,INVALID_ID);
	}
	
	public void addFeature(TermiteFeature feature) {
		this.features.add(feature);
		feature.addLevel(this);
	}
	
	public ArrayList<TermiteFeature> getFeatures() {
		return features;
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
			if(osmMember.role.equalsIgnoreCase(ROLE_FEATURE)) {
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
					//jsut look up feature directly
					feature = data.getTermiteFeature(memberId, true);
				}
				
				//add feature to level
				if(feature != null) {
					this.features.add(feature);
					feature.addLevel(this);
				}
			}
			else if(osmMember.role.equalsIgnoreCase(ROLE_SHELL)) {
				//get level shell
				shell = data.getTermiteWay(memberId, true);
			}
		}
	}
	
	void loadLevelFromShell(OsmWay osmShell, TermiteWay termiteShell) {
		this.osmRelation = null;
		this.shell = termiteShell;
		copyProperties(osmShell);
	}
}
