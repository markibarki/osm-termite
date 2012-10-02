package intransix.osm.termite.map.model;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.feature.FeatureInfo;
import java.util.*;

/**
 * This is the base object for holding reference data for nodes, ways and relations
 * to facilitate testing.
 * 
 * @author sutter
 */
public abstract class ObjectTestData {
	
	public static OsmData osmData;
	public static MapDataManager mapDataManager;
	
	public long id;
	public List<Long> rels = new ArrayList<Long>();
	public HashMap<String,String> props = new HashMap<String,String>();
	public String featureInfoName;
	
	/** The data version is checked only if it is not set to OsmData.INVALID_DATA_VERSION */
	public int dataVersion = OsmData.INVALID_DATA_VERSION;
	
	/** This method validates the given object, comapring the reference data
	 * to the actual data. */
	public abstract void validate();

	/** This method validates that the given object was deleted. */
	public abstract void validateDeleted();
	
	protected void baseValidate(OsmObject osmObject) {

		//check properties - both directions to make sure they are the same
		for(String key:props.keySet()) {
			String refValue = props.get(key);
			String actValue = osmObject.getProperty(key);
			assert(refValue.equals(actValue));
		}
		Collection<String> actProps = osmObject.getPropertyKeys();
		for(String key:actProps) {
			String refValue = props.get(key);
			String actValue = osmObject.getProperty(key);
			assert(actValue.equals(refValue));
		}

		//check relations
		List<OsmRelation> actRels = osmObject.getRelations();
		int cnt = rels.size();
		assert(cnt == actRels.size());
		for(OsmRelation or:actRels) {
			assert(rels.contains(or.getId()));	
		}
		
		//check feature info
		FeatureInfo fi = MapDataManager.getObjectFeatureInfo(osmObject);
		if(fi == null) assert(featureInfoName == null);
		else assert(featureInfoName.equals(fi.getName()));
		
		if(dataVersion != OsmData.INVALID_DATA_VERSION) {
			assert(osmObject.getDataVersion() == dataVersion);
		}
	}
}
