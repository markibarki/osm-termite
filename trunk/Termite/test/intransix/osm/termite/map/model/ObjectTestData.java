package intransix.osm.termite.map.model;

import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.osm.*;
import java.util.*;

/**
 * This is the base object for holding reference data for nodes, ways and relations
 * to facilitate testing.
 * 
 * @author sutter
 */
public abstract class ObjectTestData {
	
	public static TermiteData termiteData;
	public static OsmData osmData;
	
	public long id;
	public List<Long> rels = new ArrayList<Long>();
	public HashMap<String,String> props = new HashMap<String,String>();
	public String featureInfoName;
	public int minDataVersion;
	
	/** This method validates the given object, comapring the reference data
	 * to the actual data. */
	public abstract void validate();

	/** This method validates that the given object was deleted. */
	public abstract void validateDeleted();
	
	protected void baseValidate(TermiteObject tObject, OsmObject oObject) {

		//check properties - both directions to make sure they are the same
		for(String key:props.keySet()) {
			String refValue = props.get(key);
			String actValue = oObject.getProperty(key);
			assert(refValue.equals(actValue));
		}
		Collection<String> actProps = oObject.getPropertyKeys();
		for(String key:actProps) {
			String refValue = props.get(key);
			String actValue = oObject.getProperty(key);
			assert(actValue.equals(refValue));
		}

		//check relations
		List<TermiteRelation> actRels = tObject.getRelations();
		int cnt = rels.size();
System.out.println("sizes - rel:" + rels.size() + ", act:" + actRels.size());
		assert(cnt == actRels.size());
		for(TermiteRelation tr:actRels) {
			OsmRelation or = tr.getOsmObject();
			assert(rels.contains(or.getId()));	
		}
		
		//check feature info
		FeatureInfo fi = tObject.getFeatureInfo();
		if(fi == null) assert(featureInfoName == null);
		else assert(featureInfoName.equals(fi.getName()));
		
		assert(tObject.getDataVersion() >= minDataVersion);
	}
}
