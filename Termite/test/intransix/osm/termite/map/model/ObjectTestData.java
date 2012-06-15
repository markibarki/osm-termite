package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmObject;
import intransix.osm.termite.map.osm.OsmData;
import java.util.Collection;
import java.util.HashMap;

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
	public HashMap<String,String> props = new HashMap<String,String>();
	public String featureInfoName;
	public int minOsmVersion;
	public int minTermiteVersion;
	
	/** This method validates the given object, comapring the reference data
	 * to the actual data. */
	public abstract void validate();

	/** This method validates that the given object was deleted. */
	public abstract void validateDeleted();
	
	protected void checkProperties(OsmObject o, HashMap<String,String> p) {
		//check properties - both directions to make sure they are the same
		for(String key:p.keySet()) {
			String refValue = p.get(key);
			String actValue = o.getProperty(key);
			assert(refValue.equals(actValue));
		}
		Collection<String> actProps = o.getPropertyKeys();
		for(String key:actProps) {
			String refValue = p.get(key);
			String actValue = o.getProperty(key);
			assert(actValue.equals(refValue));
		}
	}
}
