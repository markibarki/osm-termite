/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmObject;
import intransix.osm.termite.map.osm.OsmData;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author sutter
 */
public class ObjectTestData {
	
	public static TermiteData termiteData;
	public static OsmData osmData;
	
	public long id;
	public HashMap<String,String> props = new HashMap<String,String>();
	public String featureInfoName;
	public int minOsmVersion;
	public int minTermiteVersion;
	
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
